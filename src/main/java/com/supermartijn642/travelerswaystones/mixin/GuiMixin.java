package com.supermartijn642.travelerswaystones.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.supermartijn642.travelerswaystones.content.moving.WaystoneMovingHandlerClient;
import com.supermartijn642.travelerswaystones.content.targets.personal.ClientDiscoveryEffects;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public class GuiMixin {

    @Shadow
    @Nullable
    private Component subtitle;

    @Inject(
        method = "renderOverlayMessage",
        at = @At("HEAD")
    )
    private void renderOverlayMessage(GuiGraphics guiGraphics, DeltaTracker deltaTracker, CallbackInfo ci){
        WaystoneMovingHandlerClient.renderOverlay(guiGraphics);
    }

    @Inject(
        method = "renderTitle",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/gui/GuiGraphics;drawStringWithBackdrop(Lnet/minecraft/client/gui/Font;Lnet/minecraft/network/chat/Component;IIII)V",
            ordinal = 1,
            shift = At.Shift.AFTER
        )
    )
    private void renderSubtitle(GuiGraphics guiGraphics, DeltaTracker deltaTracker, CallbackInfo ci, @Local(ordinal = 2) int color){
        ClientDiscoveryEffects.renderOwnerHead(guiGraphics, this.subtitle, color);
    }
}
