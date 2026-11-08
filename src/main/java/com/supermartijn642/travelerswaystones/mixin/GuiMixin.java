package com.supermartijn642.travelerswaystones.mixin;

import com.supermartijn642.travelerswaystones.content.moving.WaystoneMovingHandlerClient;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public class GuiMixin {

    @Inject(
        method = "extractOverlayMessage",
        at = @At("HEAD")
    )
    private void extractOverlayMessage(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker, CallbackInfo ci){
        WaystoneMovingHandlerClient.extractOverlay(graphics);
    }
}
