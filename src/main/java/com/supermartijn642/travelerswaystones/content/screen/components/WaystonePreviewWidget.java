package com.supermartijn642.travelerswaystones.content.screen.components;

import com.mojang.blaze3d.platform.cursor.CursorType;
import com.mojang.math.Axis;
import com.supermartijn642.core.ClientUtils;
import com.supermartijn642.core.TextComponents;
import com.supermartijn642.core.gui.CursorTypes;
import com.supermartijn642.core.gui.GuiGraphicsHelper;
import com.supermartijn642.core.gui.widget.BaseWidget;
import com.supermartijn642.core.gui.widget.Widget;
import com.supermartijn642.core.gui.widget.WidgetRenderContext;
import com.supermartijn642.travelerswaystones.api.targets.personal.DiscoveredWaystoneTarget;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.client.color.block.BlockTintSource;
import net.minecraft.client.input.MouseButtonInfo;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.BlockModelRenderState;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class WaystonePreviewWidget extends BaseWidget {

    private static final Matrix4fc IDENTITY = new Matrix4f().identity();
    private static final RandomSource RANDOM_SOURCE = RandomSource.create();

    private final Supplier<DiscoveredWaystoneTarget> target;
    private final List<BlockPos.MutableBlockPos> blockPositions = new ArrayList<>();
    private final List<BlockModelRenderState> blockRenderStates = new ArrayList<>();
    private final float pitch = (float)Math.toRadians(30);
    private float yaw = (float)Math.toRadians(45);
    private boolean dragging = false;
    private int mouseStartX;

    public WaystonePreviewWidget(int x, int y, int width, int height, Supplier<DiscoveredWaystoneTarget> target){
        super(x, y, width, height);
        this.target = target;
    }

    @Override
    protected <T extends Widget> T addWidget(T widget){
        return super.addWidget(widget);
    }

    @Override
    public Component getNarrationMessage(){
        return TextComponents.translation("travelerswaystones.screen.teleport.preview.narrate").get();
    }

    @Override
    public CursorType curser(int mouseX, int mouseY){
        return this.focusedWidget == null ? CursorTypes.resizeHorizontal() : super.curser(mouseX, mouseY);
    }

    @Override
    public void render(WidgetRenderContext context, GuiGraphicsHelper graphics, int mouseX, int mouseY){
        // Update rotation
        if(this.dragging){
            this.yaw += (mouseX - this.mouseStartX) / 20f;
            this.mouseStartX = mouseX;
        }

        // Update render state
        BlockState blockState = this.target.get().block();
        Map<BlockPos,BlockState> displayBlocks = this.target.get().waystoneType().getPreview(blockState);
        int blocks = displayBlocks.size();
        AABB bounds = null;
        int index = 0;
        for(Map.Entry<BlockPos,BlockState> entry : displayBlocks.entrySet()){
            if(index >= this.blockPositions.size()){
                this.blockPositions.add(new BlockPos.MutableBlockPos());
                this.blockRenderStates.add(new BlockModelRenderState());
            }
            BlockPos pos = entry.getKey();
            BlockState state = entry.getValue();
            this.blockPositions.get(index).set(pos);
            updateBlockRenderState(this.blockRenderStates.get(index), state, pos);
            VoxelShape stateBounds = state.getShape(EmptyBlockGetter.INSTANCE, pos);
            if(!stateBounds.isEmpty()){
                if(bounds == null)
                    bounds = stateBounds.bounds().move(pos);
                else
                    bounds.minmax(stateBounds.bounds().move(pos));
            }
            index++;
        }
        AABB finalBounds = bounds == null ? new AABB(BlockPos.ZERO) : bounds;

        // Submit model
        float horizontalSpan = (float)Math.sqrt(finalBounds.getXsize() * finalBounds.getXsize() + finalBounds.getZsize() * finalBounds.getZsize());
        float verticalSpan = (float)Math.sqrt(finalBounds.getXsize() * finalBounds.getXsize() + finalBounds.getYsize() * finalBounds.getYsize() + finalBounds.getZsize() * finalBounds.getZsize());
        float scale = Math.min(this.width / horizontalSpan, this.height / verticalSpan) * 0.9f;
        graphics.submitFeatures(this.x, this.y, this.width, this.height, (poseStack, output) -> {
            poseStack.pushPose();
            poseStack.translate(this.width / 2f, this.height / 2f, 0);
            poseStack.scale(scale, -scale, -scale);
            poseStack.rotate(Axis.XP, this.pitch);
            poseStack.rotate(Axis.YP, this.yaw);
            Vec3 center = finalBounds.getCenter();
            poseStack.translate(-center.x, -center.y, -center.z);
            for(int i = 0; i < blocks; i++){
                BlockPos.MutableBlockPos pos = this.blockPositions.get(i);
                poseStack.pushPose();
                poseStack.translate(pos.getX(), pos.getY(), pos.getZ());
                this.blockRenderStates.get(i).submit(poseStack, output, LightCoordsUtil.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, 0);
                poseStack.popPose();
            }
            poseStack.popPose();
        });
        super.render(context, graphics, mouseX, mouseY);
    }

    @Override
    public boolean mousePressed(int mouseX, int mouseY, MouseButtonInfo info, boolean isDoubleClick, boolean hasBeenHandled){
        hasBeenHandled |= super.mousePressed(mouseX, mouseY, info, isDoubleClick, hasBeenHandled);
        if(!hasBeenHandled && mouseX >= this.x && mouseX < this.x + this.width && mouseY >= this.y && mouseY < this.y + this.height){
            this.dragging = true;
            this.mouseStartX = mouseX;
            return true;
        }
        return hasBeenHandled;
    }

    @Override
    public boolean mouseReleased(int mouseX, int mouseY, MouseButtonInfo info, boolean hasBeenHandled){
        this.dragging = false;
        return super.mouseReleased(mouseX, mouseY, info, hasBeenHandled);
    }

    public static void updateBlockRenderState(BlockModelRenderState renderState, BlockState blockState, BlockPos pos){
        long seed = blockState.getSeed(pos);
        BlockStateModel model = ClientUtils.getMinecraft().getModelManager().getBlockStateModelSet().get(blockState);
        List<BlockStateModelPart> parts = renderState.setupModel(IDENTITY, model.hasMaterialFlag(BlockAndTintGetter.EMPTY, pos, blockState, BakedQuad.FLAG_TRANSLUCENT));
        RANDOM_SOURCE.setSeed(seed);
        model.collectParts(BlockAndTintGetter.EMPTY, pos, blockState, RANDOM_SOURCE, parts);
        IntList tints = renderState.tintLayers();
        for(BlockTintSource tintSource : ClientUtils.getMinecraft().getBlockColors().getTintSources(blockState))
            tints.add(tintSource.color(blockState));
    }
}
