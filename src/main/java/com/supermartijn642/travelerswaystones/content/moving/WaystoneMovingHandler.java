package com.supermartijn642.travelerswaystones.content.moving;

import com.supermartijn642.core.TextComponents;
import com.supermartijn642.travelerswaystones.TravelersWaystones;
import com.supermartijn642.travelerswaystones.api.WaystoneEvents;
import com.supermartijn642.travelerswaystones.api.WaystoneType;
import com.supermartijn642.travelerswaystones.api.targets.WaystoneTarget;
import com.supermartijn642.travelerswaystones.content.targets.GlobalWaystoneManagerImpl;
import com.supermartijn642.travelerswaystones.content.targets.WaystoneTargetImpl;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import java.util.Map;

public class WaystoneMovingHandler {

    public static final int MAX_DISTANCE = 10;

    public static void move(Level level, Player player, WaystoneTarget target, BlockHitResult to){
        if(level.isClientSide())
            throw new IllegalStateException("Trying to move waystone client-side!");

        // Check event
        WaystoneEvents.AllowMove.Result allowMove = WaystoneEvents.AllowMove.invoke(target, level, player, to);
        if(allowMove instanceof WaystoneEvents.AllowMove.Result.Deny deny){
            Component reason = deny.reason() == null ?
                TextComponents.translation("travelerswaystones.moving.denied").color(ChatFormatting.RED).get() :
                TextComponents.fromTextComponent(deny.reason()).color(ChatFormatting.RED).get();
            player.sendOverlayMessage(TextComponents.fromTextComponent(reason).color(ChatFormatting.RED).get());
            return;
        }

        BlockPos oldPos = target.pos();

        // Mark target to prevent it from being removed while moving the waystone
        ((WaystoneTargetImpl)target).setPreventRemoveWhileMoving(true);
        BlockPos newPos;
        BlockState newBlock;
        try{
            // Move waystone blocks
            WaystoneType.MoveResult result;
            try{
                result = target.waystoneType().moveWaystone(target.block(), level, player, target, level, to);
            }catch(Exception e){
                TravelersWaystones.LOGGER.error("Encountered an exception moving waystone for block '{}'!", target.block(), e);
                player.sendSystemMessage(TextComponents.translation("travelerswaystones.moving.failed", target.block()).color(ChatFormatting.RED).get());
                return;
            }
            if(result instanceof WaystoneType.MoveResult.Failure failure){
                if(failure.reason() == null)
                    player.sendSystemMessage(TextComponents.translation("travelerswaystones.moving.failed", target.block()).color(ChatFormatting.RED).get());
                else
                    player.sendSystemMessage(TextComponents.translation("travelerswaystones.moving.failed", target.block(), failure.reason()).color(ChatFormatting.RED).get());
                return;
            }
            newPos = ((WaystoneType.MoveResult.Success)result).newPosition();
            newBlock = ((WaystoneType.MoveResult.Success)result).newBlock();
            if(WaystoneType.get(newBlock.getBlock()) == null)
                throw new IllegalStateException("Result for moving waystone '" + target.block() + "' has new block that is not a waystone '" + newBlock + "'!");
        }finally{
            ((WaystoneTargetImpl)target).setPreventRemoveWhileMoving(false);
        }

        // Move target
        Identifier biome = level.getBiome(newPos).unwrapKey().orElseThrow().identifier();
        GlobalWaystoneManagerImpl.move(target, player, level, newPos, newBlock, biome, level);

        // Add effects
        WaystoneType.Bounds dimensions = target.waystoneType().getBounds(target.block());
        ((ServerLevel)level).sendParticles(
            TravelersWaystones.discoverParticle.create(dimensions.width() + 0.05, -dimensions.height()),
            oldPos.getX() + 0.5, oldPos.getY() + dimensions.height(), oldPos.getZ() + 0.5,
            1,
            0, 0, 0,
            0
        );
        BlockPos pos = target.pos();
        ((ServerLevel)level).sendParticles(
            TravelersWaystones.discoverParticle.create(dimensions.width() + 0.05, dimensions.height()),
            pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5,
            1,
            0, 0, 0,
            0
        );
        player.sendOverlayMessage(TextComponents.translation(
            "travelerswaystones.moving.success",
            TextComponents.string(target.name()).color(ChatFormatting.WHITE).get(),
            TextComponents.number(target.pos().getX()).color(ChatFormatting.GOLD).get(),
            TextComponents.number(target.pos().getY()).color(ChatFormatting.GOLD).get(),
            TextComponents.number(target.pos().getZ()).color(ChatFormatting.GOLD).get()
        ).color(ChatFormatting.GRAY).get());
    }

    static boolean isWithinMovingDistance(WaystoneTarget target, Map<BlockPos,BlockState> placement){
        if(placement.isEmpty())
            return false;
        for(BlockPos pos : placement.keySet()){
            if(Math.abs(pos.getX() - target.pos().getX()) > MAX_DISTANCE
                || Math.abs(pos.getY() - target.pos().getY()) > MAX_DISTANCE
                || Math.abs(pos.getZ() - target.pos().getZ()) > MAX_DISTANCE)
                return false;
        }
        return true;
    }
}
