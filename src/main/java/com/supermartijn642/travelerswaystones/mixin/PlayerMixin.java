package com.supermartijn642.travelerswaystones.mixin;

import com.supermartijn642.travelerswaystones.TravelersWaystones;
import com.supermartijn642.travelerswaystones.api.targets.personal.PersonalWaystoneManager;
import com.supermartijn642.travelerswaystones.content.targets.personal.PersonalWaystoneManagerImpl;
import com.supermartijn642.travelerswaystones.extension.TravelersWaystonesPlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Player.class)
public class PlayerMixin implements TravelersWaystonesPlayer {

    @Unique
    private PersonalWaystoneManagerImpl waystoneManager;

    @Override
    public PersonalWaystoneManager travelerswaystones_getWaystoneManager(boolean createIfAbsent){
        if(createIfAbsent && this.waystoneManager == null){
            //noinspection DataFlowIssue
            this.waystoneManager = new PersonalWaystoneManagerImpl((Player)(Object)this);
            //noinspection DataFlowIssue
            if(!((Player)(Object)this).level().isClientSide())
                this.waystoneManager.populateFromGlobalManager();
        }
        return this.waystoneManager;
    }

    @Inject(
        at = @At("TAIL"),
        method = "addAdditionalSaveData"
    )
    private void writeWaystones(CompoundTag compound, CallbackInfo ci){
        if(this.waystoneManager != null && !this.waystoneManager.all().isEmpty()){
            CompoundTag child = new CompoundTag();
            this.waystoneManager.write(child);
            compound.put(TravelersWaystones.MODID, child);
        }
    }

    @Inject(
        at = @At("TAIL"),
        method = "readAdditionalSaveData"
    )
    private void readWaystones(CompoundTag compound, CallbackInfo ci){
        try{
            if(compound.contains(TravelersWaystones.MODID, Tag.TAG_COMPOUND)){
                this.travelerswaystones_getWaystoneManager(true);
                this.waystoneManager.read(compound.getCompound(TravelersWaystones.MODID));
            }
        }catch(Exception e){
            //noinspection DataFlowIssue
            TravelersWaystones.LOGGER.error("Error reading waystones data for player {}!", ((Player)(Object)this).getGameProfile().getName(), e);
            this.waystoneManager = null;
        }
    }
}
