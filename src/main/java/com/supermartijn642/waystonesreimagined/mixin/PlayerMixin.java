package com.supermartijn642.waystonesreimagined.mixin;

import com.supermartijn642.waystonesreimagined.WaystonesReimagined;
import com.supermartijn642.waystonesreimagined.api.targets.personal.PersonalWaystoneManager;
import com.supermartijn642.waystonesreimagined.content.targets.personal.PersonalWaystoneManagerImpl;
import com.supermartijn642.waystonesreimagined.extension.WaystonesReimaginedPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;

/**
 * Created 21/08/2026 by SuperMartijn642
 */
@Mixin(Player.class)
public class PlayerMixin implements WaystonesReimaginedPlayer {

    @Unique
    private PersonalWaystoneManagerImpl waystoneManager;

    @Override
    public PersonalWaystoneManager waystonesreimagined_getWaystoneManager(boolean createIfAbsent){
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
            this.waystoneManager.write(output.child(WaystonesReimagined.MODID));
    }

    @Inject(
        at = @At("TAIL"),
        method = "readAdditionalSaveData"
    )
    private void readWaystones(ValueInput input, CallbackInfo ci){
        try{
            Optional<ValueInput> child = input.child(WaystonesReimagined.MODID);
            if(child.isPresent()){
                this.waystonesreimagined_getWaystoneManager(true);
                this.waystoneManager.read(child.get());
            }
        }catch(Exception e){
            //noinspection DataFlowIssue
            WaystonesReimagined.LOGGER.error("Error reading waystones data for player {}!", ((Player)(Object)this).nameAndId().name(), e);
            this.waystoneManager = null;
        }
    }
}
