package com.supermartijn642.travelerswaystones.mixin;

import com.supermartijn642.travelerswaystones.api.targets.personal.PersonalWaystoneManager;
import com.supermartijn642.travelerswaystones.content.targets.personal.PersonalWaystoneManagerImpl;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayer.class)
public class ServerPlayerMixin {

    @Inject(
        method = "restoreFrom",
        at = @At("TAIL")
    )
    private void copyWaypointData(ServerPlayer oldPlayer, boolean restoreAll, CallbackInfo ci) {
        PersonalWaystoneManager manager = PersonalWaystoneManagerImpl.get(oldPlayer, false);
        if(manager != null)
            //noinspection DataFlowIssue
            ((PersonalWaystoneManagerImpl)PersonalWaystoneManager.get((ServerPlayer)(Object)this)).copyFrom((PersonalWaystoneManagerImpl)manager);
    }
}
