package com.supermartijn642.travelerswaystones.mixin;

import com.supermartijn642.travelerswaystones.TravelersWaystones;
import com.supermartijn642.travelerswaystones.content.targets.GlobalWaystoneManagerImpl;
import com.supermartijn642.travelerswaystones.content.targets.WaystoneManagerSavedData;
import net.minecraft.FileUtil;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedDataType;
import net.minecraft.world.level.storage.LevelStorageSource;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.IOException;

@Mixin(MinecraftServer.class)
public class MinecraftServerMixin {

    @Shadow
    @Final
    protected LevelStorageSource.LevelStorageAccess storageSource;

    @Inject(
        method = "createLevels",
        at = @At("TAIL")
    )
    private void loadWaystoneData(CallbackInfo ci){
        //noinspection DataFlowIssue
        MinecraftServer server = (MinecraftServer)(Object)this;
        // Use the same file as Minecraft 26.1+ so waystones are kept when updating a world
        try{
            FileUtil.createDirectoriesSafe(this.storageSource.getDimensionPath(Level.OVERWORLD).resolve("data").resolve(TravelersWaystones.MODID));
        }catch(IOException e){
            TravelersWaystones.LOGGER.error("Failed to create waystones data directory!", e);
        }
        server.overworld().getDataStorage().computeIfAbsent(
            new SavedDataType<>(
                TravelersWaystones.MODID + "/waystones",
                () -> WaystoneManagerSavedData.INSTANCE,
                WaystoneManagerSavedData.codec(server),
                null
            )
        );
    }

    @Inject(
        method = "stopServer",
        at = @At("TAIL")
    )
    private void clearWaystoneData(CallbackInfo ci) {
        GlobalWaystoneManagerImpl.clear();
    }
}
