package com.supermartijn642.travelerswaystones.mixin;

import com.supermartijn642.travelerswaystones.content.WaystoneEventsImpl;
import com.supermartijn642.travelerswaystones.content.WaystoneTypeRegistry;
import net.minecraft.server.Main;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Main.class)
public class ServerMainMixin {

    @Inject(
        method = "main",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/server/notifications/NotificationManager;<init>()V"
        )
    )
    private static void freezeWaystoneRegistry(CallbackInfo ci){
        WaystoneTypeRegistry.freeze();
        WaystoneEventsImpl.freeze();
    }
}
