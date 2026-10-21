package com.supermartijn642.waystonesreimagined.mixin;

import com.supermartijn642.waystonesreimagined.WaystonesReimagined;
import com.supermartijn642.waystonesreimagined.api.targets.personal.PersonalWaystoneManager;
import com.supermartijn642.waystonesreimagined.content.packets.PersonalTargetsPacket;
import com.supermartijn642.waystonesreimagined.content.targets.personal.PersonalWaystoneManagerImpl;
import net.minecraft.network.Connection;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.players.PlayerList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerList.class)
public class PlayerListMixin {

    @Inject(
        method = "placeNewPlayer",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/server/players/PlayerList;sendLevelInfo(Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/server/level/ServerLevel;)V",
            shift = At.Shift.AFTER
        )
    )
    private void placeNewPlayer(Connection connection, ServerPlayer player, CommonListenerCookie cookie, CallbackInfo ci) {
        PersonalWaystoneManager manager = PersonalWaystoneManagerImpl.get(player, false);
        if(manager != null)
            WaystonesReimagined.CHANNEL.sendToClient(connection, new PersonalTargetsPacket(manager));
    }
}
