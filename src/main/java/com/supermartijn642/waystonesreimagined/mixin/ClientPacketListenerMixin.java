package com.supermartijn642.waystonesreimagined.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.supermartijn642.waystonesreimagined.api.targets.personal.PersonalWaystoneManager;
import com.supermartijn642.waystonesreimagined.content.targets.personal.PersonalWaystoneManagerImpl;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.game.ClientboundRespawnPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Created 25/08/2026 by SuperMartijn642
 */
@Mixin(ClientPacketListener.class)
public class ClientPacketListenerMixin {

    @Inject(
        method = "handleRespawn",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/multiplayer/ClientPacketListener;setClientLoaded(Z)V"
        )
    )
    private void handleRespawn(ClientboundRespawnPacket packet, CallbackInfo ci, @Local(ordinal = 0) LocalPlayer oldPlayer, @Local(ordinal = 1) LocalPlayer newPlayer){
        PersonalWaystoneManager manager = PersonalWaystoneManagerImpl.get(oldPlayer, false);
        if(manager != null)
            ((PersonalWaystoneManagerImpl)PersonalWaystoneManager.get(newPlayer)).copyFrom((PersonalWaystoneManagerImpl)manager);
    }
}
