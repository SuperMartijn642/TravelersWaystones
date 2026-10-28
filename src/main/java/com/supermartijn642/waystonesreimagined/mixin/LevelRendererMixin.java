package com.supermartijn642.waystonesreimagined.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.supermartijn642.waystonesreimagined.content.moving.WaystoneMovingHandlerClient;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.SubmitNodeStorage;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelRenderer.class)
public class LevelRendererMixin {

    @Inject(
        method = "extractLevel",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/LevelRenderer;extractVisibleEntities(Lnet/minecraft/client/Camera;Lnet/minecraft/client/renderer/culling/Frustum;Lnet/minecraft/client/DeltaTracker;Lnet/minecraft/client/renderer/state/level/LevelRenderState;)V"
        )
    )
    private void extract(DeltaTracker deltaTracker, Camera camera, float deltaPartialTick, CallbackInfo ci){
        WaystoneMovingHandlerClient.extractPreview();
    }

    @Inject(
        method = "submitBlockEntities",
        at = @At("HEAD")
    )
    private void submitBlockEntities(PoseStack poseStack, LevelRenderState levelRenderState, SubmitNodeStorage submitNodeCollector, CallbackInfo ci) {
        WaystoneMovingHandlerClient.submitPreview(submitNodeCollector, levelRenderState.cameraRenderState.pos);
    }
}
