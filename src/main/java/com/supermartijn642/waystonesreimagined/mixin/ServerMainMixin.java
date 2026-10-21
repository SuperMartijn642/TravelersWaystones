package com.supermartijn642.waystonesreimagined.mixin;

import com.supermartijn642.waystonesreimagined.content.WaystoneEventsImpl;
import com.supermartijn642.waystonesreimagined.content.WaystoneTypeRegistry;
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
            target = ""
        )
    )
    private void freezeWaystoneRegistry(CallbackInfo ci){
        WaystoneTypeRegistry.freeze();
        WaystoneEventsImpl.freeze();
    }
}
