package com.supermartijn642.waystonesreimagined.content.moving;

import com.mojang.blaze3d.vertex.PoseStack;
import com.supermartijn642.core.ClientUtils;
import com.supermartijn642.core.TextComponents;
import com.supermartijn642.core.gui.GuiGraphicsHelper;
import com.supermartijn642.waystonesreimagined.WaystonesReimagined;
import com.supermartijn642.waystonesreimagined.api.targets.WaystoneTarget;
import com.supermartijn642.waystonesreimagined.content.packets.MoveWaystonePacket;
import com.supermartijn642.waystonesreimagined.extension.WaystonesReimaginedRenderType;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Options;
import net.minecraft.client.color.block.BlockTintSource;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockModelRenderState;
import net.minecraft.client.renderer.block.BlockStateModelSet;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.rendertype.OutputTarget;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.model.data.ModelData;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector4f;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class WaystoneMovingHandlerClient {

    private static final Matrix4fc IDENTITY_MATRIX = new Matrix4f().identity();
    /**
     * Copy of {@link Sheets#translucentBlockItemSheet()}
     */
    private static final RenderType VALID_RENDER_TYPE = RenderType.create(
        "waystonesreimagined_valid_placement",
        RenderSetup.builder(RenderPipelines.ITEM_TRANSLUCENT)
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
        "waystonesreimagined_invalid_placement",
        RenderSetup.builder(RenderPipelines.ITEM_TRANSLUCENT)
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
        ((WaystonesReimaginedRenderType)VALID_RENDER_TYPE).waystonesreimagined_setColorModulator(new Vector4f(0.5f, 0.5f, 0.8f, 0.6f));
        ((WaystonesReimaginedRenderType)INVALID_RENDER_TYPE).waystonesreimagined_setColorModulator(new Vector4f(0.9f, 0.5f, 0.5f, 0.8f));
    }

    private static final RenderState RENDER_STATE = new RenderState();
    private static final RandomSource RANDOM_SOURCE = RandomSource.createThreadLocalInstance();
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
            ClientUtils.getPlayer().sendOverlayMessage(message);
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
            || !player.level().dimension().identifier().equals(movingTarget.dimension())
            || ClientUtils.getMinecraft().gui.screen() != null){
            stopMoving(TextComponents.translation("waystonesreimagined.moving.cancelled").color(ChatFormatting.WHITE).get());
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
            stopMoving(TextComponents.translation("waystonesreimagined.moving.cancelled").color(ChatFormatting.WHITE).get());
        }else if(options.keyUse.consumeClick()){
            ClientUtils.getMinecraft().rightClickDelay = 10;
            if(!(ClientUtils.getMinecraft().hitResult instanceof BlockHitResult hitResult) || hitResult.getType() == HitResult.Type.MISS || hitResult.isWorldBorderHit())
                return;
            Map<BlockPos,BlockState> placement = movingTarget.waystoneType().getPlacementPreview(movingTarget.block(), player.level(), player, hitResult);
            if(placement.isEmpty()){
                player.sendOverlayMessage(TextComponents.translation("waystonesreimagined.moving.no_placement", WaystoneMovingHandler.MAX_DISTANCE).color(ChatFormatting.RED).get());
                return;
            }
            if(WaystoneMovingHandler.isWithinMovingDistance(movingTarget, placement)){
                WaystonesReimagined.CHANNEL.sendToServer(new MoveWaystonePacket(movingTarget.pos(), hitResult));
                stopMoving(null);
            }else
                player.sendOverlayMessage(TextComponents.translation("waystonesreimagined.moving.too_far", WaystoneMovingHandler.MAX_DISTANCE).color(ChatFormatting.RED).get());
        }
    }

    public static boolean consumeEscape(){
        if(!isMoving())
            return false;
        stopMoving(TextComponents.translation("waystonesreimagined.moving.cancelled").color(ChatFormatting.WHITE).get());
        return true;
    }

    public static void extractOverlay(GuiGraphicsExtractor graphicsExtractor){
        if(!isMoving())
            return;
        GuiGraphicsHelper graphics = GuiGraphicsHelper.of(graphicsExtractor);
        graphics.nextStratum();
        Options options = ClientUtils.getMinecraft().options;
        graphics.submitText(
            TextComponents.translation(
                "waystonesreimagined.moving.hint",
                TextComponents.fromTextComponent(options.keyUse.getTranslatedKeyMessage()).color(ChatFormatting.GOLD).get(),
                TextComponents.fromTextComponent(options.keyAttack.getTranslatedKeyMessage()).color(ChatFormatting.GOLD).get()
            ).color(ChatFormatting.WHITE).get(),
            graphicsExtractor.guiWidth() / 2, graphicsExtractor.guiHeight() - 78,
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
        boolean isValid = WaystoneMovingHandler.isWithinMovingDistance(movingTarget, placement);
        ClientLevel level = ClientUtils.getWorld();
        BlockStateModelSet blockStateModels = ClientUtils.getMinecraft().getModelManager().getBlockStateModelSet();
        int index = 0;
        for(Map.Entry<BlockPos,BlockState> entry : placement.entrySet()){
            if(RENDER_STATE.positions.size() == index){
                RENDER_STATE.positions.add(new BlockPos.MutableBlockPos());
                RENDER_STATE.blockRenderStates.add(new BlockModelRenderState());
            }
            BlockPos pos = entry.getKey();
            RENDER_STATE.positions.get(index).set(pos);
            BlockState state = entry.getValue();
            BlockStateModel model = blockStateModels.get(state);
            BlockModelRenderState blockRenderState = RENDER_STATE.blockRenderStates.get(index);
            blockRenderState.clear();
            List<BlockStateModelPart> parts = blockRenderState.setupModel(IDENTITY_MATRIX, true);
            ModelData modelData = model.getModelData(level, pos, state, level.getModelDataManager().getAtOrEmpty(pos));
            long seed = state.getSeed(pos);
            RANDOM_SOURCE.setSeed(seed);
            model.collectParts(RANDOM_SOURCE, parts, modelData);
            IntList tintLayers = blockRenderState.tintLayers();
            for(BlockTintSource tintSource : ClientUtils.getMinecraft().getBlockColors().getTintSources(state))
                tintLayers.add(tintSource.colorInWorld(state, level, pos));
            blockRenderState.renderType = isValid ? VALID_RENDER_TYPE : INVALID_RENDER_TYPE;
            index++;
        }
    }

    public static void submitPreview(SubmitNodeCollector output, Vec3 cameraPos){
        for(int i = 0; i < RENDER_STATE.blockCount; i++){
            BlockPos.MutableBlockPos pos = RENDER_STATE.positions.get(i);
            BlockModelRenderState blockRenderState = RENDER_STATE.blockRenderStates.get(i);
            POSE_STACK.pushPose();
            POSE_STACK.translate(pos.getX() - cameraPos.x, pos.getY() - cameraPos.y, pos.getZ() - cameraPos.z);
            blockRenderState.submit(POSE_STACK, output, LightCoordsUtil.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, 0);
            POSE_STACK.popPose();
        }
    }

    private static class RenderState {
        private int blockCount;
        private final List<BlockPos.MutableBlockPos> positions = new ArrayList<>(2);
        private final List<BlockModelRenderState> blockRenderStates = new ArrayList<>(2);
    }
}
