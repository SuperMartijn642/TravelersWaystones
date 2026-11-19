package com.supermartijn642.travelerswaystones.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.supermartijn642.travelerswaystones.content.moving.WaystoneMovingHandlerClient;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelRenderer.class)
public class LevelRendererMixin {

    @Inject(
        method = "renderBlockEntities",
        at = @At("HEAD")
    )
    private void renderBlockEntities(PoseStack poseStack, MultiBufferSource.BufferSource bufferSource, MultiBufferSource.BufferSource bufferSource2, Camera camera, float partialTicks, CallbackInfo ci){
        WaystoneMovingHandlerClient.extractPreview();
        WaystoneMovingHandlerClient.renderPreview(poseStack, bufferSource, camera.getPosition());
    }
}
