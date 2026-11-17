package com.supermartijn642.travelerswaystones.content.targets;

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Set;

/**
 * Keeps track target positions in the chunks the client has loaded, including targets the player has not discovered.
 * The server sends the positions in a chunk together with the chunk.
 */
public class ClientTargetPositions {

    private static final Long2ObjectOpenHashMap<Set<BlockPos>> BY_CHUNK = new Long2ObjectOpenHashMap<>();
    private static @Nullable Level level;

    public static void setChunk(Level level, ChunkPos chunk, Collection<BlockPos> positions){
        updateLevel(level);
        if(positions.isEmpty())
            BY_CHUNK.remove(chunk.pack());
        else
            BY_CHUNK.put(chunk.pack(), new ObjectOpenHashSet<>(positions));
    }

    /**
     * Forgets the targets in a chunk when the client loads or unloads it. If the chunk has targets, the server sends them after the chunk.
     */
    public static void clearChunk(Level level, ChunkPos chunk){
        updateLevel(level);
        BY_CHUNK.remove(chunk.pack());
    }

    public static boolean hasTarget(Level level, BlockPos pos){
        if(level != ClientTargetPositions.level)
            return false;
        Set<BlockPos> positions = BY_CHUNK.get(ChunkPos.pack(pos));
        return positions != null && positions.contains(pos);
    }

    private static void updateLevel(Level level){
        if(level != ClientTargetPositions.level){
            BY_CHUNK.clear();
            ClientTargetPositions.level = level;
        }
    }
}
