package com.supermartijn642.travelerswaystones.content.block;

import com.supermartijn642.core.TextComponents;
import com.supermartijn642.travelerswaystones.api.WaystoneType;
import com.supermartijn642.travelerswaystones.api.targets.GlobalWaystoneManager;
import com.supermartijn642.travelerswaystones.content.targets.ClientTargetPositions;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.DismountHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3d;
import org.joml.Vector3dc;
import org.joml.Vector3i;
import org.joml.Vector3ic;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class WaystoneTypeImpl {

    private static final List<Vector3ic> TELEPORT_POSITION_CHECK_ORDER;

    static{
        List<Vector3i> xzOrder = List.of(
            new Vector3i(1, 0, 0),
            new Vector3i(0, 0, 1),
            new Vector3i(0, 0, -1),
            new Vector3i(-1, 0, 0),
            new Vector3i(1, 0, 1),
            new Vector3i(1, 0, -1),
            new Vector3i(-1, 0, 1),
            new Vector3i(-1, 0, -1)
        );
        int[] yOrder = {0, -1, 1, -2, 2};
        List<Vector3ic> positions = new ArrayList<>(xzOrder.size() * yOrder.length);
        for(int y : yOrder){
            for(Vector3i xz : xzOrder){
                positions.add(xz.add(0, y, 0, new Vector3i()));
            }
        }
        TELEPORT_POSITION_CHECK_ORDER = List.copyOf(positions);
    }

    public static boolean isBreakProtected(Player player, Level level, BlockPos pos, BlockState state){
        if(player.isSpectator() || player.isShiftKeyDown())
            return false;
        WaystoneType type = WaystoneType.get(state.getBlock());
        if(type == null || !type.hasBreakProtection(state))
            return false;
        // Waystones without a target can be broken normally
        BlockPos targetPos = type.getTargetPos(state, pos);
        return level.isClientSide() ? ClientTargetPositions.hasTarget(level, targetPos) : GlobalWaystoneManager.getTargetAt(level, targetPos) != null;
    }

    public static WaystoneType.TeleportPositionResult getTeleportPosition(BlockState waystone, Level level, BlockPos pos, Player player){
        Vec3 teleportPos = null;
        BlockPos.MutableBlockPos dummyPos = new BlockPos.MutableBlockPos();
        Consumer<Vector3ic> applyOffset = offset -> dummyPos.setWithOffset(pos, offset.x(), offset.y(), offset.z());
        if(waystone.hasProperty(BlockStateProperties.FACING)){
            applyOffset = switch(waystone.getValue(BlockStateProperties.FACING)){
                case EAST -> offset -> dummyPos.setWithOffset(pos, offset.x(), offset.y(), offset.z());
                case SOUTH -> offset -> dummyPos.setWithOffset(pos, -offset.z(), offset.y(), offset.x());
                case WEST -> offset -> dummyPos.setWithOffset(pos, -offset.x(), offset.y(), -offset.z());
                case NORTH -> offset -> dummyPos.setWithOffset(pos, offset.z(), offset.y(), -offset.x());
                default -> throw new AssertionError();
            };
        }
        for(Vector3ic offset : TELEPORT_POSITION_CHECK_ORDER){
            applyOffset.accept(offset);
            teleportPos = DismountHelper.findSafeDismountLocation(player.getType(), level, dummyPos, true);
            if(teleportPos != null)
                break;
        }
        if(teleportPos != null)
            return WaystoneType.TeleportPositionResult.success(new Vector3d(teleportPos.x, teleportPos.y, teleportPos.z));
        for(Vector3ic offset : TELEPORT_POSITION_CHECK_ORDER){
            applyOffset.accept(offset);
            teleportPos = DismountHelper.findSafeDismountLocation(player.getType(), level, dummyPos, false);
            if(teleportPos != null)
                break;
        }
        if(teleportPos != null)
            return WaystoneType.TeleportPositionResult.dangerous(new Vector3d(teleportPos.x, teleportPos.y, teleportPos.z), null);
        return WaystoneType.TeleportPositionResult.failure(null);
    }

    public static class TeleportPositionResultImpl {
        public static WaystoneType.TeleportPositionResult.Success success(Vector3dc position){
            return () -> position;
        }

        public static WaystoneType.TeleportPositionResult.Failure failure(@Nullable Component reason){
            return new Failure(reason);
        }

        public static WaystoneType.TeleportPositionResult.Dangerous dangerous(Vector3dc position, @Nullable Component warning){
            return new Dangerous(position, warning);
        }

        private record Failure(@Nullable Component reason) implements WaystoneType.TeleportPositionResult.Failure {
            @Override
            public Component reasonOrDefault(){
                return this.reason == null ? TextComponents.translation("travelerswaystones.teleport.no_space").color(ChatFormatting.RED).get() : this.reason;
            }
        }

        private record Dangerous(Vector3dc teleportPosition, @Nullable Component warning) implements WaystoneType.TeleportPositionResult.Dangerous {
            @Override
            public Component warningOrDefault(){
                return this.warning == null ? TextComponents.translation("travelerswaystones.teleport.dangerous").get() : this.warning;
            }
        }
    }

    public static class MoveResultImpl {
        public static WaystoneType.MoveResult.Success success(BlockPos newTargetPosition, BlockState newTargetBlock){
            return new WaystoneType.MoveResult.Success() {
                @Override
                public BlockPos newPosition(){
                    return newTargetPosition;
                }

                @Override
                public BlockState newBlock(){
                    return newTargetBlock;
                }
            };
        }

        public static WaystoneType.MoveResult.Failure failure(@Nullable Component reason){
            return () -> reason;
        }
    }

    public record BoundsImpl(double width, double height) implements WaystoneType.Bounds {
        public static WaystoneType.Bounds of(double width, double height){
            return new BoundsImpl(width, height);
        }
    }
}
