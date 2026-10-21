package com.supermartijn642.waystonesreimagined.mixin;

import com.supermartijn642.waystonesreimagined.content.moving.WaystoneMovingHandlerClient;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Created 14/09/2026 by SuperMartijn642
 */
@Mixin(LevelRenderer.class)
public class LevelRendererMixin {

    @Inject(
        method = "submitFeatures",
        at = @At("HEAD")
    )
    private void submitFeatures(LevelRenderState levelRenderState, SubmitNodeCollector submitNodeCollector, boolean renderOutline, CallbackInfo ci) {
        WaystoneMovingHandlerClient.submitPreview(submitNodeCollector, levelRenderState.cameraRenderState.pos);
    }
}
