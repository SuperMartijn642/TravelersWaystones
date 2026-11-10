package com.supermartijn642.travelerswaystones.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.supermartijn642.travelerswaystones.content.targets.GlobalWaystoneManagerImpl;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LevelChunk.class)
public class LevelChunkMixin {

    @Shadow
    @Final
    private Level level;

    @WrapOperation(
        method = "setBlockState",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/level/chunk/LevelChunkSection;setBlockState(IIILnet/minecraft/world/level/block/state/BlockState;)Lnet/minecraft/world/level/block/state/BlockState;"
        )
    )
    private BlockState removeTargetsOfReplacedWaystones(LevelChunkSection section, int x, int y, int z, BlockState state, Operation<BlockState> operation, @Local(argsOnly = true) BlockPos pos){
        BlockState oldState = operation.call(section, x, y, z, state);
        if(!this.level.isClientSide())
            GlobalWaystoneManagerImpl.onBlockChanged(this.level, pos, oldState, state);
        return oldState;
    }
}
