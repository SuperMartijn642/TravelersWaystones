package com.supermartijn642.travelerswaystones.mixin;

import com.llamalad7.mixinextras.sugar.Local;
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
        method = "renderLevel",
        at = @At(
            value = "INVOKE_STRING",
            target = "Lnet/minecraft/util/profiling/ProfilerFiller;popPush(Ljava/lang/String;)V",
            args = "ldc=blockentities"
        )
    )
    private void renderBlockEntities(CallbackInfo ci, @Local(argsOnly = true) Camera camera, @Local PoseStack poseStack, @Local MultiBufferSource.BufferSource bufferSource){
        WaystoneMovingHandlerClient.extractPreview();
        WaystoneMovingHandlerClient.renderPreview(poseStack, bufferSource, camera.getPosition());
    }
}
