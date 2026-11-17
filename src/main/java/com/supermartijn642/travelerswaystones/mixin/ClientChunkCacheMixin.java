package com.supermartijn642.travelerswaystones.mixin;

import com.supermartijn642.travelerswaystones.content.targets.ClientTargetPositions;
import net.minecraft.client.multiplayer.ClientChunkCache;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundLevelChunkPacketData;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.Heightmap;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;
import java.util.function.Consumer;

@Mixin(ClientChunkCache.class)
public class ClientChunkCacheMixin {

    @Shadow
    @Final
    private ClientLevel level;

    @Inject(
        method = "replaceWithPacketData",
        at = @At("HEAD")
    )
    private void replaceWithPacketData(int chunkX, int chunkZ, FriendlyByteBuf readBuffer, Map<Heightmap.Types,long[]> heightmaps, Consumer<ClientboundLevelChunkPacketData.BlockEntityTagOutput> blockEntities, CallbackInfoReturnable<LevelChunk> ci){
        ClientTargetPositions.clearChunk(this.level, new ChunkPos(chunkX, chunkZ));
    }

    @Inject(
        method = "drop",
        at = @At("HEAD")
    )
    private void drop(ChunkPos pos, CallbackInfo ci){
        ClientTargetPositions.clearChunk(this.level, pos);
    }
}
