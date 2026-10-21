package com.supermartijn642.waystonesreimagined.content;

import com.supermartijn642.waystonesreimagined.api.WaystoneType;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;

import java.util.Map;

/**
 * Created 17/09/2026 by SuperMartijn642
 */
public class WaystoneTypeRegistry {

    private static final Map<Block,WaystoneType> WAYSTONES = new Reference2ReferenceOpenHashMap<>();
    private static boolean finalized = false;

    public static WaystoneType get(Block block){
        if(!finalized)
            throw new IllegalStateException("Waystones cannot be queried during initialization!");
        return WAYSTONES.getOrDefault(block, WaystoneType.DEFAULT);
    }

    public static void register(WaystoneType type, Block... blocks){
        if(finalized)
            throw new IllegalStateException("Waystones cannot be registered after initialization!");
        synchronized(WaystoneTypeRegistry.class){
            if(finalized)
                throw new IllegalStateException("Waystones cannot be registered after initialization!");
            for(Block block : blocks){
                if(WAYSTONES.put(block, type) != null)
                    throw new IllegalStateException("Duplicate waystone registration for block '" + BuiltInRegistries.BLOCK.getKey(block) + "'!");
            }
        }
    }

    public static void freeze(){
        if(finalized)
            throw new IllegalStateException("Waystone registry has already been finalized!");
        synchronized(WaystoneTypeRegistry.class){
            if(finalized)
                throw new IllegalStateException("Waystone registry has already been finalized!");
            finalized = true;
        }
    }
}
