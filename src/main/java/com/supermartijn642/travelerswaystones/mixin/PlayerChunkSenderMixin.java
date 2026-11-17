package com.supermartijn642.travelerswaystones.mixin;

import com.supermartijn642.travelerswaystones.content.targets.GlobalWaystoneManagerImpl;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.network.PlayerChunkSender;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerChunkSender.class)
public class PlayerChunkSenderMixin {

    @Inject(
        method = "sendChunk",
        at = @At("TAIL")
    )
    private static void sendChunkTargets(ServerGamePacketListenerImpl connection, ServerLevel level, LevelChunk chunk, CallbackInfo ci){
        GlobalWaystoneManagerImpl.onChunkSent(connection.player, level, chunk.getPos());
    }
}
