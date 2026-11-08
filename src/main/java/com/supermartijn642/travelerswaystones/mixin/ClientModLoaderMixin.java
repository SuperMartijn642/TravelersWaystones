package com.supermartijn642.travelerswaystones.mixin;

import com.supermartijn642.travelerswaystones.content.WaystoneEventsImpl;
import com.supermartijn642.travelerswaystones.content.WaystoneTypeRegistry;
import net.minecraftforge.client.loading.ClientModLoader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientModLoader.class)
public class ClientModLoaderMixin {

    @Inject(
        method = "begin",
        at = @At("TAIL")
    )
    private static void freezeWaystoneRegistry(CallbackInfo ci){
        // Forge's Mixin version still can't target things in constructors, so
        // Instead of targeting ClientModLoader.begin from Minecraft's constructor, we have this
        WaystoneTypeRegistry.freeze();
        WaystoneEventsImpl.freeze();
    }
}
