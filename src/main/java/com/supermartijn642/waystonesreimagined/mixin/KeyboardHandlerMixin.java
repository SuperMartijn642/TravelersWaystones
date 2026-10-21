package com.supermartijn642.waystonesreimagined.mixin;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.supermartijn642.waystonesreimagined.content.moving.WaystoneMovingHandlerClient;
import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(KeyboardHandler.class)
public class KeyboardHandlerMixin {

    @WrapWithCondition(
        method = "keyPress",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/Minecraft;pauseGame(Z)V"
        )
    )
    private boolean consumeEscapeIfMovingWaystone(Minecraft minecraft, boolean b){
        return !WaystoneMovingHandlerClient.consumeEscape();
    }
}
