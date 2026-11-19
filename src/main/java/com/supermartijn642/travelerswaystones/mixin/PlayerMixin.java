package com.supermartijn642.travelerswaystones.mixin;

import com.supermartijn642.travelerswaystones.TravelersWaystones;
import com.supermartijn642.travelerswaystones.api.targets.personal.PersonalWaystoneManager;
import com.supermartijn642.travelerswaystones.content.targets.personal.PersonalWaystoneManagerImpl;
import com.supermartijn642.travelerswaystones.extension.TravelersWaystonesPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;

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
    private void writeWaystones(ValueOutput output, CallbackInfo ci){
        if(this.waystoneManager != null && !this.waystoneManager.all().isEmpty())
            this.waystoneManager.write(output.child(TravelersWaystones.MODID));
    }

    @Inject(
        at = @At("TAIL"),
        method = "readAdditionalSaveData"
    )
    private void readWaystones(ValueInput input, CallbackInfo ci){
        try{
            Optional<ValueInput> child = input.child(TravelersWaystones.MODID);
            if(child.isPresent()){
                this.travelerswaystones_getWaystoneManager(true);
                this.waystoneManager.read(child.get());
            }
        }catch(Exception e){
            //noinspection DataFlowIssue
            TravelersWaystones.LOGGER.error("Error reading waystones data for player {}!", ((Player)(Object)this).getGameProfile().getName(), e);
            this.waystoneManager = null;
        }
    }
}
