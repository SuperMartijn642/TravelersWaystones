package com.supermartijn642.travelerswaystones.mixin;

import com.supermartijn642.travelerswaystones.api.WaystoneType;
import com.supermartijn642.travelerswaystones.content.block.WaystoneTypeImpl;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayerGameMode.class)
public class ServerPlayerGameModeMixin {

    @Shadow
    protected ServerLevel level;
    @Shadow
    @Final
    protected ServerPlayer player;

    @Inject(
        method = "destroyBlock",
        at = @At("HEAD"),
        cancellable = true
    )
    private void destroyBlock(BlockPos pos, CallbackInfoReturnable<Boolean> ci){
        BlockState state = this.level.getBlockState(pos);
        if(!WaystoneTypeImpl.isBreakProtected(this.player, this.level, pos, state))
            return;
        // Make sure the client still has the waystone's blocks
        BlockPos targetPos = WaystoneType.get(state.getBlock()).getTargetPos(state, pos);
        this.level.sendBlockUpdated(pos, state, state, Block.UPDATE_ALL);
        if(!targetPos.equals(pos))
            this.level.sendBlockUpdated(targetPos, this.level.getBlockState(targetPos), this.level.getBlockState(targetPos), Block.UPDATE_ALL);
        ci.setReturnValue(false);
    }
}
