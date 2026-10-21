package com.supermartijn642.waystonesreimagined.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.supermartijn642.waystonesreimagined.content.block.TallWaystoneBlock;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ClientLevel.class)
public class ClientLevelMixin {

    @WrapOperation(
        method = "addDestroyBlockEffect",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/level/block/state/BlockState;getShape(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/phys/shapes/VoxelShape;"
        )
    )
    private VoxelShape useSimplerShapeForTallWaystones(BlockState state, BlockGetter level, BlockPos pos, Operation<VoxelShape> operation) {
        if(state.getBlock() instanceof TallWaystoneBlock)
            return state.getCollisionShape(level, pos);
        return operation.call(state, level, pos);
    }
}
