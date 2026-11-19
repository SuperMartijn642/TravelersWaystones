package com.supermartijn642.travelerswaystones.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.supermartijn642.travelerswaystones.content.moving.WaystoneMovingHandlerClient;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.SubmitNodeStorage;
import net.minecraft.client.renderer.state.LevelRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelRenderer.class)
public class LevelRendererMixin {

    @Inject(
        method = "renderLevel",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/LevelRenderer;extractVisibleBlockEntities(Lnet/minecraft/client/Camera;FLnet/minecraft/client/renderer/state/LevelRenderState;)V"
        )
    )
    private void extract(CallbackInfo ci){
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
