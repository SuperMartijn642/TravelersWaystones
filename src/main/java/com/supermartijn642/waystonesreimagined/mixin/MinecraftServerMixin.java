package com.supermartijn642.waystonesreimagined.mixin;

import com.supermartijn642.waystonesreimagined.WaystonesReimagined;
import com.supermartijn642.waystonesreimagined.content.targets.GlobalWaystoneManagerImpl;
import com.supermartijn642.waystonesreimagined.content.targets.WaystoneManagerSavedData;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedDataType;
import net.minecraft.world.level.storage.SavedDataStorage;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Created 22/08/2026 by SuperMartijn642
 */
@Mixin(MinecraftServer.class)
public class MinecraftServerMixin {

    @Shadow
    @Final
    private SavedDataStorage savedDataStorage;

    @Inject(
        method = "<init>",
        at = @At("TAIL")
    )
    private void loadWaystoneData(CallbackInfo ci){
        //noinspection DataFlowIssue
        this.savedDataStorage.computeIfAbsent(
            new SavedDataType<>(
                WaystonesReimagined.identifier("waystones"),
                () -> WaystoneManagerSavedData.INSTANCE,
                WaystoneManagerSavedData.codec((MinecraftServer)(Object)this),
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
