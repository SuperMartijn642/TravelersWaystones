package com.supermartijn642.waystonesreimagined.api;

import com.supermartijn642.waystonesreimagined.api.targets.WaystoneTarget;
import com.supermartijn642.waystonesreimagined.content.WaystoneTypeRegistry;
import com.supermartijn642.waystonesreimagined.content.block.WaystoneTypeImpl;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3dc;

import java.util.Map;

/**
 * Provides waystone functionality for blocks.
 * <p>
 * Created 30/08/2026 by SuperMartijn642
 */
public interface WaystoneType {

    /**
     * Instance for the default implementation of {@link WaystoneType}.
     */
    WaystoneType DEFAULT = new WaystoneType() {};

    /**
     * Gets the {@link WaystoneType} registered for the given block.
     */
    @Nullable
    static WaystoneType get(Block block){
        return WaystoneTypeRegistry.get(block);
    }

    /**
     * Registers the given type for the given blocks.
     * @throws IllegalStateException when one of the given blocks is already registered
     * @see DEFAULT
     */
    static void register(WaystoneType waystoneType, Block... blocks){
        WaystoneTypeRegistry.register(waystoneType, blocks);
    }

    /**
     * Gets the size of the waystone. Used for particle effects that highlight the waystone.
     * @param waystone the waystone block
     */
    default Bounds getBounds(BlockState waystone){
        AABB bounds = waystone.getShape(EmptyBlockGetter.INSTANCE, BlockPos.ZERO).bounds();
        return Bounds.of(
            Math.max(bounds.getXsize(), bounds.getZsize()),
            bounds.getYsize()
        );
    }

    /**
     * Gets the position to teleport the player to.
     * @param waystone the waystone block
     * @param level    level that the waystone is in
     * @param pos      position of the waystone
     * @param player   player to be teleported
     * @see TeleportPositionResult#success(Vector3dc)
     * @see TeleportPositionResult#failure(Component)
     * @see TeleportPositionResult#dangerous(Vector3dc, Component)
     */
    default TeleportPositionResult getTeleportPosition(BlockState waystone, Level level, BlockPos pos, Player player){
        return WaystoneTypeImpl.getTeleportPosition(waystone, level, pos, player);
    }

    /**
     * Gets the blocks used for the waystone preview in the teleport screen.
     * @param waystone the waystone block
     */
    default Map<BlockPos,BlockState> getPreview(BlockState waystone){
        if(waystone.hasProperty(BlockStateProperties.WATERLOGGED))
            waystone = waystone.setValue(BlockStateProperties.WATERLOGGED, false);
        return Map.of(BlockPos.ZERO, waystone);
    }

    /**
     * Gets the blocks that would be placed if the waystone is moved to where the player is looking.
     * When the waystone cannot be placed or placement is obstructed, returns an empty map.
     * @param waystone  the waystone block
     * @param level     level to get the preview for
     * @param player    player to get placement for
     * @param hitResult block that the player is looking at
     * @return blocks that would be placed or an empty map if no placement applicable
     */
    default Map<BlockPos,BlockState> getPlacementPreview(BlockState waystone, Level level, Player player, BlockHitResult hitResult){
        BlockPlaceContext context = new BlockPlaceContext(level, player, InteractionHand.MAIN_HAND, ItemStack.EMPTY, hitResult);
        if(!context.canPlace())
            return Map.of();
        BlockState state = waystone.getBlock().getStateForPlacement(context);
        if(state == null)
            return Map.of();
        if(state.hasProperty(BlockStateProperties.WATERLOGGED))
            state = state.setValue(BlockStateProperties.WATERLOGGED, false);
        return Map.of(context.getClickedPos(), state);
    }

    /**
     * Whether the waystone can be moved.
     * @param waystone the waystone block
     */
    default boolean isMoveable(BlockState waystone){
        return true;
    }

    /**
     * Moves the waystone from its current position to a new position.
     * Specifically, this method places the waystone blocks at the new position and removes the current blocks.<br>
     * The waystone target will be moved separately based on the result of this method.
     * <p>
     * This should first place the waystone at the new position before removing the existing blocks such that the existing blocks can remain in case some error occurs.
     * @param waystone   the waystone block
     * @param level      level that the waystone is currently located in
     * @param player     player to get placement for
     * @param current    current waystone target before it is moved
     * @param toLevel    level to move the waystone to
     * @param toPosition what the player is looking at and where the waystone should be moved to
     * @throws IllegalStateException if {@link #isMoveable(BlockState)} returns {@code false}
     * @see MoveResult#success(BlockPos, BlockState)
     * @see MoveResult#failure(Component)
     */
    default MoveResult moveWaystone(BlockState waystone, Level level, Player player, WaystoneTarget current, Level toLevel, BlockHitResult toPosition){
        if(!this.isMoveable(waystone))
            throw new IllegalStateException("Waystone '" + waystone + "' cannot be moved!");

        // Place new block
        BlockPlaceContext context = new BlockPlaceContext(toLevel, player, InteractionHand.MAIN_HAND, ItemStack.EMPTY, toPosition);
        if(!context.canPlace())
            return MoveResult.failure(null);
        BlockState state = waystone.getBlock().getStateForPlacement(context);
        if(state == null)
            return MoveResult.failure(null);
        if(!toLevel.setBlock(context.getClickedPos(), state, Block.UPDATE_ALL))
            return MoveResult.failure(null);

        // Remove old block
        if(!level.removeBlock(current.pos(), false))
            return MoveResult.failure(null);
        return MoveResult.success(
            context.getClickedPos(),
            state
        );
    }

    /// Result for {@link #getTeleportPosition(BlockState, Level, BlockPos, Player)}.
    /// Will be one of the following:
    ///  - {@link Success} if there is a safe teleport position
    ///  - {@link Dangerous} if the teleport position is dangerous
    ///  - {@link Failure} if no teleport position is available
    /// @see #success(Vector3dc)
    /// @see #dangerous(Vector3dc, Component)
    /// @see #failure(Component)
    @ApiStatus.NonExtendable
    sealed interface TeleportPositionResult {
        /**
         * Indicates the destination is dangerous and the player should confirm teleporting.
         * @param position position that the player will be teleported to
         */
        static Success success(Vector3dc position){
            return WaystoneTypeImpl.TeleportPositionResultImpl.success(position);
        }

        /**
         * Indicates the player cannot teleport to the target.
         * @param reason message shown to the player, if null, a default message will be shown
         */
        static Failure failure(@Nullable Component reason){
            return WaystoneTypeImpl.TeleportPositionResultImpl.failure(reason);
        }

        /**
         * Indicates the destination is dangerous and the player should confirm teleporting.
         * @param position position that the player will be teleported to
         * @param warning  warning shown to the player, if null, a default message will be shown
         */
        static Dangerous dangerous(Vector3dc position, @Nullable Component warning){
            return WaystoneTypeImpl.TeleportPositionResultImpl.dangerous(position, warning);
        }

        @ApiStatus.NonExtendable
        non-sealed interface Success extends TeleportPositionResult {
            Vector3dc teleportPosition();
        }

        @ApiStatus.NonExtendable
        non-sealed interface Failure extends TeleportPositionResult {
            /**
             * Context message that can be shown to the player.
             */
            @Nullable Component reason();
        }

        @ApiStatus.NonExtendable
        non-sealed interface Dangerous extends TeleportPositionResult {
            Vector3dc teleportPosition();

            /**
             * Context message that can be shown to the player.
             */
            @Nullable Component warning();
        }
    }

    /// Result for {@link #moveWaystone(BlockState, Level, Player, WaystoneTarget, Level, BlockHitResult)}.
    /// Will be one of the following:
    ///  - {@link Success} if the waystone was moved successfully
    ///  - {@link Failure} if the waystone could not be moved
    /// @see #success(BlockPos, BlockState)
    /// @see #failure(Component)
    @ApiStatus.NonExtendable
    sealed interface MoveResult {
        /**
         * Indicates that the waystone is moved successfully and the associated target can be updated as well.
         * @param newTargetPosition the new position of the waystone target
         * @param newTargetBlock    the new block that is used as waystone
         */
        static Success success(BlockPos newTargetPosition, BlockState newTargetBlock){
            return WaystoneTypeImpl.MoveResultImpl.success(newTargetPosition, newTargetBlock);
        }

        /**
         * Indicates that the waystone could not be moved and the associated target should remain at its old position.
         * @param reason message to display to the player, if null, a default message will be shown
         */
        static Failure failure(@Nullable Component reason){
            return WaystoneTypeImpl.MoveResultImpl.failure(reason);
        }

        @ApiStatus.NonExtendable
        non-sealed interface Success extends MoveResult {
            BlockPos newPosition();

            BlockState newBlock();
        }

        @ApiStatus.NonExtendable
        non-sealed interface Failure extends MoveResult {
            /**
             * Context message that can be shown to the player.
             */
            @Nullable Component reason();
        }
    }

    @ApiStatus.NonExtendable
    interface Bounds {
        static Bounds of(double width, double height){
            return WaystoneTypeImpl.BoundsImpl.of(width, height);
        }

        double width();

        double height();
    }
}
