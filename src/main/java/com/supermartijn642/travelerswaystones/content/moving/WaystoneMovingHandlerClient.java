package com.supermartijn642.travelerswaystones.content.moving;

import com.mojang.blaze3d.vertex.PoseStack;
import com.supermartijn642.core.ClientUtils;
import com.supermartijn642.core.TextComponents;
import com.supermartijn642.core.gui.GuiGraphicsHelper;
import com.supermartijn642.travelerswaystones.TravelersWaystones;
import com.supermartijn642.travelerswaystones.api.targets.WaystoneTarget;
import com.supermartijn642.travelerswaystones.content.packets.MoveWaystonePacket;
import com.supermartijn642.travelerswaystones.extension.TravelersWaystonesRenderType;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Options;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockModelShaper;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.client.renderer.rendertype.OutputTarget;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector4f;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class WaystoneMovingHandlerClient {

    /**
     * Copy of {@link Sheets#translucentBlockItemSheet()}
     */
    private static final RenderType VALID_RENDER_TYPE = RenderType.create(
        "travelerswaystones_valid_placement",
        RenderSetup.builder(RenderPipelines.ITEM_ENTITY_TRANSLUCENT_CULL)
            .withTexture("Sampler0", TextureAtlas.LOCATION_BLOCKS)
            .setOutputTarget(OutputTarget.ITEM_ENTITY_TARGET)
            .useLightmap()
            .useOverlay()
            .affectsCrumbling()
            .sortOnUpload()
            .setOutline(RenderSetup.OutlineProperty.AFFECTS_OUTLINE)
            .createRenderSetup()
    );
    /**
     * Copy of {@link Sheets#translucentBlockItemSheet()}
     */
    private static final RenderType INVALID_RENDER_TYPE = RenderType.create(
        "travelerswaystones_invalid_placement",
        RenderSetup.builder(RenderPipelines.ITEM_ENTITY_TRANSLUCENT_CULL)
            .withTexture("Sampler0", TextureAtlas.LOCATION_BLOCKS)
            .setOutputTarget(OutputTarget.ITEM_ENTITY_TARGET)
            .useLightmap()
            .useOverlay()
            .affectsCrumbling()
            .sortOnUpload()
            .setOutline(RenderSetup.OutlineProperty.AFFECTS_OUTLINE)
            .createRenderSetup()
    );

    static{
        ((TravelersWaystonesRenderType)VALID_RENDER_TYPE).travelerswaystones_setColorModulator(new Vector4f(0.5f, 0.5f, 0.8f, 0.6f));
        ((TravelersWaystonesRenderType)INVALID_RENDER_TYPE).travelerswaystones_setColorModulator(new Vector4f(0.9f, 0.5f, 0.5f, 0.8f));
    }

    private static final RenderState RENDER_STATE = new RenderState();
    private static final PoseStack POSE_STACK = new PoseStack();
    private static WaystoneTarget movingTarget;

    public static boolean isMoving(){
        return movingTarget != null;
    }

    public static void startMoving(WaystoneTarget target){
        movingTarget = target;
    }

    private static void stopMoving(@Nullable Component message){
        movingTarget = null;
        if(message != null)
            ClientUtils.getPlayer().displayClientMessage(message, true);
    }

    public static void update(){
        if(!isMoving())
            return;
        Player player = ClientUtils.getPlayer();
        if(player == null){
            stopMoving(null);
            return;
        }
        if(!player.isAlive()
            || !player.level().dimension().equals(movingTarget.dimension())
            || ClientUtils.getMinecraft().screen != null){
            stopMoving(TextComponents.translation("travelerswaystones.moving.cancelled").color(ChatFormatting.WHITE).get());
            return;
        }
    }

    public static void handleInputs(){
        if(!isMoving())
            return;
        Player player = ClientUtils.getPlayer();
        Options options = ClientUtils.getMinecraft().options;
        if(options.keyAttack.consumeClick()){
            ClientUtils.getMinecraft().missTime = 10;
            stopMoving(TextComponents.translation("travelerswaystones.moving.cancelled").color(ChatFormatting.WHITE).get());
        }else if(options.keyUse.consumeClick()){
            ClientUtils.getMinecraft().rightClickDelay = 10;
            if(!(ClientUtils.getMinecraft().hitResult instanceof BlockHitResult hitResult) || hitResult.getType() == HitResult.Type.MISS || hitResult.isWorldBorderHit())
                return;
            Map<BlockPos,BlockState> placement = movingTarget.waystoneType().getPlacementPreview(movingTarget.block(), player.level(), player, hitResult);
            if(placement.isEmpty()){
                player.displayClientMessage(TextComponents.translation("travelerswaystones.moving.no_placement", WaystoneMovingHandler.MAX_DISTANCE).color(ChatFormatting.RED).get(), true);
                return;
            }
            if(WaystoneMovingHandler.isWithinMovingDistance(movingTarget, placement)){
                TravelersWaystones.CHANNEL.sendToServer(new MoveWaystonePacket(movingTarget.pos(), hitResult));
                stopMoving(null);
            }else
                player.displayClientMessage(TextComponents.translation("travelerswaystones.moving.too_far", WaystoneMovingHandler.MAX_DISTANCE).color(ChatFormatting.RED).get(), true);
        }
    }

    public static boolean consumeEscape(){
        if(!isMoving())
            return false;
        stopMoving(TextComponents.translation("travelerswaystones.moving.cancelled").color(ChatFormatting.WHITE).get());
        return true;
    }

    public static void renderOverlay(GuiGraphics guiGraphics){
        if(!isMoving())
            return;
        GuiGraphicsHelper graphics = GuiGraphicsHelper.of(guiGraphics);
        graphics.nextStratum();
        Options options = ClientUtils.getMinecraft().options;
        graphics.submitText(
            TextComponents.translation(
                "travelerswaystones.moving.hint",
                TextComponents.fromTextComponent(options.keyUse.getTranslatedKeyMessage()).color(ChatFormatting.GOLD).get(),
                TextComponents.fromTextComponent(options.keyAttack.getTranslatedKeyMessage()).color(ChatFormatting.GOLD).get()
            ).color(ChatFormatting.WHITE).get(),
            guiGraphics.guiWidth() / 2, guiGraphics.guiHeight() - 78,
            p -> p.center().shadow()
        );
    }

    public static void extractPreview(){
        RENDER_STATE.blockCount = 0;
        if(!isMoving())
            return;
        Player player = ClientUtils.getPlayer();
        if(!(ClientUtils.getMinecraft().hitResult instanceof BlockHitResult hitResult) || hitResult.getType() == HitResult.Type.MISS || hitResult.isWorldBorderHit())
            return;
        Map<BlockPos,BlockState> placement = movingTarget.waystoneType().getPlacementPreview(movingTarget.block(), player.level(), player, hitResult);
        if(placement.isEmpty())
            return;
        RENDER_STATE.blockCount = placement.size();
        RENDER_STATE.isValid = WaystoneMovingHandler.isWithinMovingDistance(movingTarget, placement);
        int index = 0;
        for(Map.Entry<BlockPos,BlockState> entry : placement.entrySet()){
            if(RENDER_STATE.positions.size() == index){
                RENDER_STATE.positions.add(new BlockPos.MutableBlockPos());
                RENDER_STATE.states.add(null);
            }
            RENDER_STATE.positions.get(index).set(entry.getKey());
            RENDER_STATE.states.set(index, entry.getValue());
            index++;
        }
    }

    public static void submitPreview(SubmitNodeCollector output, Vec3 cameraPos){
        ClientLevel level = ClientUtils.getMinecraft().level;
        BlockModelShaper blockModels = ClientUtils.getMinecraft().getModelManager().getBlockModelShaper();
        RenderType renderType = RENDER_STATE.isValid ? VALID_RENDER_TYPE : INVALID_RENDER_TYPE;
        for(int i = 0; i < RENDER_STATE.blockCount; i++){
            BlockPos pos = RENDER_STATE.positions.get(i);
            BlockState state = RENDER_STATE.states.get(i);
            int color = ClientUtils.getMinecraft().getBlockColors().getColor(state, level, pos, 0);
            float red = (color >> 16 & 255) / 255f, green = (color >> 8 & 255) / 255f, blue = (color & 255) / 255f;
            POSE_STACK.pushPose();
            POSE_STACK.translate(pos.getX() - cameraPos.x, pos.getY() - cameraPos.y, pos.getZ() - cameraPos.z);
            BlockStateModel model = blockModels.getBlockModel(state);
            output.submitCustomGeometry(POSE_STACK, renderType, (pose, buffer) -> ModelBlockRenderer.renderModel(pose, type -> buffer, model, red, green, blue, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, level, pos, state));
            POSE_STACK.popPose();
        }
    }

    private static class RenderState {
        private int blockCount;
        private boolean isValid;
        private final List<BlockPos.MutableBlockPos> positions = new ArrayList<>(2);
        private final List<BlockState> states = new ArrayList<>(2);
    }
}
