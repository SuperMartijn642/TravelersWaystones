package com.supermartijn642.travelerswaystones.content.targets;

import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import com.supermartijn642.travelerswaystones.TravelersWaystones;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Stores the targets as a list in 'data' like Minecraft 26.1+ does, so the file stays the same when updating a world.
 */
public class WaystoneManagerSavedData extends SavedData {

    public static WaystoneManagerSavedData load(MinecraftServer server, Path file){
        RegistryAccess registryAccess = server.registryAccess();
        if(Files.exists(file)){
            try{
                Tag data = NbtIo.readCompressed(file, NbtAccounter.unlimitedHeap()).get("data");
                if(data != null)
                    GlobalWaystoneManagerImpl.deserialize(NbtOps.INSTANCE.convertTo(JsonOps.INSTANCE, data), registryAccess);
            }catch(Exception e){
                TravelersWaystones.LOGGER.error("Encountered an exception whilst loading waystones from '{}'!", file, e);
            }
        }
        return new WaystoneManagerSavedData(registryAccess);
    }

    private final RegistryAccess registryAccess;

    private WaystoneManagerSavedData(RegistryAccess registryAccess){
        this.registryAccess = registryAccess;
    }

    @Override
    public CompoundTag save(HolderLookup.Provider provider){
        JsonElement json = GlobalWaystoneManagerImpl.serialize(this.registryAccess);
        CompoundTag tag = new CompoundTag();
        tag.put("data", JsonOps.INSTANCE.convertTo(NbtOps.INSTANCE, json));
        NbtUtils.addCurrentDataVersion(tag);
        this.setDirty(false);
        return tag;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider provider){
        // Not used, see #save(HolderLookup.Provider)
        return tag;
    }

    @Override
    public void setDirty(boolean dirty){
        GlobalWaystoneManagerImpl.setDirty(dirty);
    }

    @Override
    public boolean isDirty(){
        return GlobalWaystoneManagerImpl.isDirty();
    }
}
