package com.supermartijn642.waystonesreimagined.content.targets;

import com.mojang.serialization.Codec;
import com.supermartijn642.core.codec.CodecHelper;
import net.minecraft.core.RegistryAccess;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;

/**
 * Created 22/08/2026 by SuperMartijn642
 */
public class WaystoneManagerSavedData extends SavedData {

    public static final WaystoneManagerSavedData INSTANCE = new WaystoneManagerSavedData();

    public static Codec<WaystoneManagerSavedData> codec(MinecraftServer server){
        RegistryAccess registryAccess = server.registryAccess();
        return CodecHelper.jsonSerializerToCodec(
            _ -> GlobalWaystoneManagerImpl.serialize(registryAccess),
            json -> {
                GlobalWaystoneManagerImpl.deserialize(json, registryAccess);
                return new WaystoneManagerSavedData();
            }
        );
    }

    private WaystoneManagerSavedData(){
    }

    @Override
    public boolean isDirty(){
        return true;
    }
}
