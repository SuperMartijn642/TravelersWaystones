package com.supermartijn642.travelerswaystones.api;

import com.supermartijn642.travelerswaystones.api.targets.WaystoneTarget;
import com.supermartijn642.travelerswaystones.api.targets.personal.DiscoveredWaystoneTarget;
import com.supermartijn642.travelerswaystones.content.WaystoneEventsImpl;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3dc;

import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Holds events related to waystones.
 * <p>
 * Listeners must be added during initialization and are called in the order of their identifiers.
 * For events starting with {@code Allow}, the first listener that returns {@code deny} decides the result.
 */
public final class WaystoneEvents {

    /**
     * Invoked when a player tries to claim a waystone to determine whether the player is allowed to.
     * If any listener returns {@link Result.Deny}, then that is the result. Otherwise, the result is {@link Result.Allow}.
     * <p>
     * This event is only fired on the server.
     */
    public static final class AllowClaim {
        /**
         * @param player   player to claim the waystone
         * @param level    level that the target is in
         * @param pos      position that the target is at
         * @param waystone block used as waystone
         * @throws IllegalStateException if the given block has no {@link WaystoneType} registered
         */
        public static Result invoke(Player player, Level level, BlockPos pos, BlockState waystone){
            return WaystoneEventsImpl.AllowClaim.invoke(player, level, pos, waystone);
        }

        public static void addListener(Identifier identifier, Function<Context,Result> listener){
            WaystoneEventsImpl.AllowClaim.addListener(identifier, listener);
        }

        @ApiStatus.NonExtendable
        public interface Context {
            /**
             * Player to claim the waystone
             */
            Player player();

            /**
             * Level that the target is in.
             */
            Level level();

            /**
             * Position that the target is at.
             */
            BlockPos pos();

            /**
             * Block used as waystone. Block always has a registered {@link WaystoneType}.
             */
            BlockState waystone();
        }

        @ApiStatus.NonExtendable
        public sealed interface Result {
            static Allow allow(){
                return WaystoneEventsImpl.AllowClaim.allow();
            }

            static Deny deny(@Nullable Component reason){
                return WaystoneEventsImpl.AllowClaim.deny(reason);
            }

            @ApiStatus.NonExtendable
            non-sealed interface Allow extends Result {
            }

            @ApiStatus.NonExtendable
            non-sealed interface Deny extends Result {
                @Nullable Component reason();
            }
        }
    }

    /**
     * Invoked when a player claims a waystone and a new target is added.
     * <p>
     * This event is only fired on the server.
     */
    public static final class Claim {
        /**
         * @param target target that was created
         * @param level  level that the target is in
         * @param player player that claimed the target
         */
        public static void invoke(WaystoneTarget target, Level level, Player player){
            WaystoneEventsImpl.Claim.invoke(target, level, player);
        }

        public static void addListener(Identifier identifier, Consumer<Context> listener){
            WaystoneEventsImpl.Claim.addListener(identifier, listener);
        }

        @ApiStatus.NonExtendable
        public interface Context {
            /**
             * Target that was created.
             */
            WaystoneTarget target();

            /**
             * Level that the target is in.
             */
            Level level();

            /**
             * Player that claimed the target.
             */
            Player player();
        }
    }

    /**
     * Invoked when a target is removed.
     * <p>
     * This event is only fired on the server.
     */
    public static final class Remove {
        /**
         * @param target target that was removed
         */
        public static void invoke(WaystoneTarget target){
            WaystoneEventsImpl.Remove.invoke(target);
        }

        public static void addListener(Identifier identifier, Consumer<Context> listener){
            WaystoneEventsImpl.Remove.addListener(identifier, listener);
        }

        @ApiStatus.NonExtendable
        public interface Context {
            /**
             * Target that was removed.
             */
            WaystoneTarget target();
        }
    }

    /**
     * Invoked when a player tries to move a waystone to determine whether the player is allowed to.
     * If any listener returns {@link Result.Deny}, then that is the result. Otherwise, the result is {@link Result.Allow}.
     * <p>
     * This event is only fired on the server.
     */
    public static final class AllowMove {
        /**
         * @param target target to be moved
         * @param level  level to move the target to
         * @param player player moving the target
         * @param to     position to move the target to
         */
        public static Result invoke(WaystoneTarget target, Level level, Player player, BlockHitResult to){
            return WaystoneEventsImpl.AllowMove.invoke(target, level, player, to);
        }

        public static void addListener(Identifier identifier, Function<Context,Result> listener){
            WaystoneEventsImpl.AllowMove.addListener(identifier, listener);
        }

        @ApiStatus.NonExtendable
        public interface Context {
            /**
             * Target to be moved.
             */
            WaystoneTarget target();

            /**
             * Level to move the target to.
             */
            Level level();

            /**
             * Player moving the target.
             */
            Player player();

            /**
             * Position to move the target to.
             */
            BlockHitResult to();
        }

        @ApiStatus.NonExtendable
        public sealed interface Result {
            static Result.Allow allow(){
                return WaystoneEventsImpl.AllowMove.allow();
            }

            static Result.Deny deny(@Nullable Component reason){
                return WaystoneEventsImpl.AllowMove.deny(reason);
            }

            non-sealed interface Allow extends Result {
            }

            non-sealed interface Deny extends Result {
                @Nullable Component reason();
            }
        }
    }

    /**
     * Invoked when a target is moved by a player.
     * <p>
     * For position changes of any kind, use the {@link UpdateLocation} event.
     * This event is only fired on the server.
     */
    public static final class Move {
        /**
         * @param target   target that was moved
         * @param level    level the target was moved to
         * @param player   player that moved the target
         * @param oldLevel level the target was located in before it was moved
         * @param oldPos   position the target was located at before it was moved
         * @param oldBlock block used as waystone for the target before it was moved
         * @param oldBiome biome the target was located in before it was moved
         * @throws IllegalStateException if the given old block has no {@link WaystoneType} registered
         */
        public static void invoke(WaystoneTarget target, Level level, Player player, Level oldLevel, BlockPos oldPos, BlockState oldBlock, Identifier oldBiome){
            WaystoneEventsImpl.Move.invoke(target, level, player, oldLevel, oldPos, oldBlock, oldBiome);
        }

        public static void addListener(Identifier identifier, Consumer<Context> listener){
            WaystoneEventsImpl.Move.addListener(identifier, listener);
        }

        @ApiStatus.NonExtendable
        public interface Context {
            /**
             * Target that was moved.
             */
            WaystoneTarget target();

            /**
             * Level the target was moved to.
             */
            Level level();

            /**
             * Player that moved the target.
             */
            Player player();

            /**
             * Level the target was located in before it was moved.
             */
            Level oldLevel();

            /**
             * Position the target was located at before it was moved.
             */
            BlockPos oldPos();

            /**
             * Block used as waystone for the target before it was moved.
             */
            BlockState oldBlock();

            /**
             * Biome the target was located in before it was moved.
             */
            Identifier oldBiome();
        }
    }

    /**
     * Invoked when a target's location is changed.
     * <p>
     * This event is fired on the server and on the clients of players that have discovered the target.
     */
    public static final class UpdateLocation {
        /**
         * @param target       target whose location was changed
         * @param oldDimension dimension the target was located in before it was updated
         * @param oldPos       position the target was located at before it was updated
         * @param oldBlock     block used as waystone for the target before it was updated
         * @param oldBiome     biome the target was located in before it was updated
         * @param isClient     whether this event fired on the client
         * @throws IllegalStateException if the given old block has no {@link WaystoneType} registered
         */
        public static void invoke(WaystoneTarget target, Identifier oldDimension, BlockPos oldPos, BlockState oldBlock, Identifier oldBiome, boolean isClient){
            WaystoneEventsImpl.UpdateLocation.invoke(target, oldDimension, oldPos, oldBlock, oldBiome, isClient);
        }

        public static void addListener(Identifier identifier, Consumer<Context> listener){
            WaystoneEventsImpl.UpdateLocation.addListener(identifier, listener);
        }

        @ApiStatus.NonExtendable
        public interface Context {
            /**
             * Target whose location was changed.
             */
            WaystoneTarget target();

            /**
             * Dimension the target was located in before it was updated.
             */
            Identifier oldDimension();

            /**
             * Position the target was located at before it was updated.
             */
            BlockPos oldPos();

            /**
             * Block used as waystone for the target before it was updated.
             */
            BlockState oldBlock();

            /**
             * Biome the target was located in before it was updated.
             */
            Identifier oldBiome();

            /**
             * Whether this event is fired on the client.
             */
            boolean isClient();
        }
    }

    /**
     * Invoked when a player discovers a target.
     * <p>
     * This event is fired on the server and on the client for the player that discovered the target.
     */
    public static final class Discover {
        /**
         * @param target target that was discovered
         * @param level  level the target is located in
         * @param player player that discovered the target
         */
        public static void invoke(DiscoveredWaystoneTarget target, Level level, Player player){
            WaystoneEventsImpl.Discover.invoke(target, level, player);
        }

        public static void addListener(Identifier identifier, Consumer<Context> listener){
            WaystoneEventsImpl.Discover.addListener(identifier, listener);
        }

        @ApiStatus.NonExtendable
        public interface Context {
            /**
             * Target that was discovered.
             */
            DiscoveredWaystoneTarget target();

            /**
             * Level the target is located in.
             */
            Level level();

            /**
             * Player that discovered the target.
             */
            Player player();

            /**
             * Whether this event is fired on the client.
             */
            boolean isClient();
        }
    }

    /**
     * Invoked when a target is removed from a player's discovered ones.
     * <p>
     * This event is fired on the server and on the client for the player that undiscovered the target.
     */
    public static final class Undiscover {
        /**
         * @param target target that was un-discovered
         * @param player player that the discovered target belonged to
         */
        public static void invoke(DiscoveredWaystoneTarget target, Player player){
            WaystoneEventsImpl.Undiscover.invoke(target, player);
        }

        public static void addListener(Identifier identifier, Consumer<Context> listener){
            WaystoneEventsImpl.Undiscover.addListener(identifier, listener);
        }

        @ApiStatus.NonExtendable
        public interface Context {
            /**
             * Target that was un-discovered.
             */
            DiscoveredWaystoneTarget target();

            /**
             * Player that the discovered target belonged to.
             */
            Player player();

            /**
             * Whether this event is fired on the client.
             */
            boolean isClient();
        }
    }

    /**
     * Invoked when a player tries to teleport to a target to determine whether the player is allowed to.
     * If any listener returns {@link Result.Deny}, then that is the result. Otherwise, the result is {@link Result.Allow}.
     * <p>
     * Every teleport fires this event, also teleports started by other mods through {@link DiscoveredWaystoneTarget#teleport(ServerPlayer, DiscoveredWaystoneTarget, boolean)}.
     * <p>
     * This event is only fired on the server.
     */
    public static final class AllowTeleport {
        /**
         * @param target   target to teleport to
         * @param player   player to be teleported
         * @param level    level that the target is located in
         * @param position position that the target is located at
         * @param yaw      yaw for the player after teleportation
         * @param from     the target that the player teleported from, may be null
         */
        public static Result invoke(DiscoveredWaystoneTarget target, ServerPlayer player, Level level, Vector3dc position, float yaw, @Nullable DiscoveredWaystoneTarget from){
            return WaystoneEventsImpl.AllowTeleport.invoke(target, player, level, position, yaw, from);
        }

        public static void addListener(Identifier identifier, Function<Context,Result> listener){
            WaystoneEventsImpl.AllowTeleport.addListener(identifier, listener);
        }

        @ApiStatus.NonExtendable
        public interface Context {
            /**
             * Target to teleport to.
             */
            DiscoveredWaystoneTarget target();

            /**
             * Player to be teleported.
             */
            ServerPlayer player();

            /**
             * Level that the target is located in.
             */
            Level level();

            /**
             * Position that the target is located at.
             */
            Vector3dc position();

            /**
             * Yaw for the player after teleportation.
             */
            float yaw();

            /**
             * The target that the player teleported from.
             */
            @Nullable DiscoveredWaystoneTarget from();
        }

        @ApiStatus.NonExtendable
        public sealed interface Result {
            static Result.Allow allow(){
                return WaystoneEventsImpl.AllowTeleport.allow();
            }

            static Result.Deny deny(@Nullable Component reason){
                return WaystoneEventsImpl.AllowTeleport.deny(reason);
            }

            non-sealed interface Allow extends Result {
            }

            non-sealed interface Deny extends Result {
                @Nullable Component reason();
            }
        }
    }

    /**
     * Invoked when a player teleports to a target.
     * <p>
     * This event is only fired on the server.
     */
    public static final class Teleport {
        /**
         * @param target      target that the player was teleported to
         * @param player      player that was teleported
         * @param level       level the target is located in
         * @param from        target that the player teleported from
         * @param oldLevel    level that the player was in before being teleported
         * @param oldPosition position that the player was at before being teleported
         */
        public static void invoke(DiscoveredWaystoneTarget target, Player player, Level level, @Nullable DiscoveredWaystoneTarget from, Level oldLevel, Vector3dc oldPosition){
            WaystoneEventsImpl.Teleport.invoke(target, player, level, from, oldLevel, oldPosition);
        }

        public static void addListener(Identifier identifier, Consumer<Context> listener){
            WaystoneEventsImpl.Teleport.addListener(identifier, listener);
        }

        @ApiStatus.NonExtendable
        public interface Context {
            /**
             * Target that the player was teleported to.
             */
            DiscoveredWaystoneTarget target();

            /**
             * Player that was teleported.
             */
            Player player();

            /**
             * Level the target is located in.
             */
            Level level();

            /**
             * Target that the player teleported from.
             */
            @Nullable DiscoveredWaystoneTarget from();

            /**
             * Level that the player was in before being teleported.
             */
            Level oldLevel();

            /**
             * Position that the player was at before being teleported.
             */
            Vector3dc oldPosition();
        }
    }

    /**
     * Invoked when a target is renamed.
     * <p>
     * This event is fired on the server and on the clients of players that have discovered the target.
     */
    public static final class Rename {
        /**
         * @param target   target that was renamed
         * @param player   player that renamed the target, may be null
         * @param oldName  name of the target before it was renamed
         * @param isClient whether this event fired on the client
         */
        public static void invoke(WaystoneTarget target, @Nullable Player player, String oldName, boolean isClient){
            WaystoneEventsImpl.Rename.invoke(target, player, oldName, isClient);
        }

        public static void addListener(Identifier identifier, Consumer<Context> listener){
            WaystoneEventsImpl.Rename.addListener(identifier, listener);
        }

        @ApiStatus.NonExtendable
        public interface Context {
            /**
             * Target that was renamed.
             */
            WaystoneTarget target();

            /**
             * Player that renamed the target, may be null.
             */
            @Nullable Player player();

            /**
             * Name of the target before it was renamed.
             */
            String oldName();

            /**
             * Whether this event is fired on the client.
             */
            boolean isClient();
        }
    }
}
