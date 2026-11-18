package com.supermartijn642.travelerswaystones.api;

import com.supermartijn642.travelerswaystones.api.targets.GlobalWaystoneManager;
import com.supermartijn642.travelerswaystones.api.targets.WaystoneTarget;
import com.supermartijn642.travelerswaystones.api.targets.personal.DiscoveredWaystoneTarget;
import com.supermartijn642.travelerswaystones.api.targets.personal.PersonalWaystoneManager;
import com.supermartijn642.travelerswaystones.content.WaystoneEventsImpl;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3dc;

import java.util.UUID;
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
        public static void addListener(Identifier identifier, Function<Context,Result> listener){
            WaystoneEventsImpl.AllowClaim.addListener(identifier, listener);
        }

        @ApiStatus.NonExtendable
        public interface Context {
            /**
             * Player to claim the waystone
             */
            ServerPlayer player();

            /**
             * Level that the target is in.
             */
            ServerLevel level();

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
     * Invoked when a new target is added, either because a player claimed a waystone or through {@link GlobalWaystoneManager#add}.
     * <p>
     * This event is only fired on the server.
     */
    public static final class Claim {
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
            ServerLevel level();

            /**
             * Player that claimed the target, or null if the target was added without a player through {@link GlobalWaystoneManager#add(UUID, ServerLevel, BlockPos, String)}.
             */
            @Nullable ServerPlayer player();
        }
    }

    /**
     * Invoked when a target is removed.
     * <p>
     * This event is only fired on the server.
     */
    public static final class Remove {
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
            ServerLevel level();

            /**
             * Player moving the target.
             */
            ServerPlayer player();

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
     * Invoked when a target is moved by a player.
     * <p>
     * For position changes of any kind, use the {@link UpdateLocation} event.
     * This event is only fired on the server.
     */
    public static final class Move {
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
            ServerLevel level();

            /**
             * Player that moved the target.
             */
            ServerPlayer player();

            /**
             * Level the target was located in before it was moved.
             */
            ServerLevel oldLevel();

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
            ResourceKey<Biome> oldBiome();
        }
    }

    /**
     * Invoked when a target's location is changed.
     * <p>
     * This event is fired on the server and on the clients of players that have discovered the target.
     */
    public static final class UpdateLocation {
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
            ResourceKey<Level> oldDimension();

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
            ResourceKey<Biome> oldBiome();

            /**
             * Whether this event is fired on the client.
             */
            boolean isClient();
        }
    }

    /**
     * Invoked when a player tries to discover a target by using its waystone to determine whether the player is allowed to.
     * If any listener returns {@link Result.Deny}, then that is the result. Otherwise, the result is {@link Result.Allow}.
     * <p>
     * Not fired for targets discovered through {@link PersonalWaystoneManager#discover(WaystoneTarget)}.
     * This event is only fired on the server.
     */
    public static final class AllowDiscover {
        public static void addListener(Identifier identifier, Function<Context,Result> listener){
            WaystoneEventsImpl.AllowDiscover.addListener(identifier, listener);
        }

        @ApiStatus.NonExtendable
        public interface Context {
            /**
             * Player to discover the target.
             */
            ServerPlayer player();

            /**
             * Target to be discovered.
             */
            WaystoneTarget target();
        }

        @ApiStatus.NonExtendable
        public sealed interface Result {
            static Result.Allow allow(){
                return WaystoneEventsImpl.AllowDiscover.allow();
            }

            static Result.Deny deny(@Nullable Component reason){
                return WaystoneEventsImpl.AllowDiscover.deny(reason);
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
     * Invoked when a player discovers a target.
     * <p>
     * This event is fired on the server and on the client for the player that discovered the target.
     */
    public static final class Discover {
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
     * Every teleport fires this event, also teleports started by other mods through {@link DiscoveredWaystoneTarget#teleport(ServerPlayer, DiscoveredWaystoneTarget, Identifier, boolean)}.
     * <p>
     * This event is only fired on the server.
     */
    public static final class AllowTeleport {
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
            ServerLevel level();

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

            /**
             * What started the teleport, {@link DiscoveredWaystoneTarget#SOURCE_WAYSTONE} or {@link DiscoveredWaystoneTarget#SOURCE_PENDANT} for teleports from Traveler's Waystones itself.
             * Other mods use their own identifiers.
             */
            Identifier source();
        }

        @ApiStatus.NonExtendable
        public sealed interface Result {
            static Result.Allow allow(){
                return WaystoneEventsImpl.AllowTeleport.allow();
            }

            static Result.Deny deny(@Nullable Component reason){
                return WaystoneEventsImpl.AllowTeleport.deny(reason);
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
     * Invoked when a player teleports to a target.
     * <p>
     * This event is only fired on the server.
     */
    public static final class Teleport {
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
            ServerPlayer player();

            /**
             * Level the target is located in.
             */
            ServerLevel level();

            /**
             * Target that the player teleported from.
             */
            @Nullable DiscoveredWaystoneTarget from();

            /**
             * Level that the player was in before being teleported.
             */
            ServerLevel oldLevel();

            /**
             * Position that the player was at before being teleported.
             */
            Vector3dc oldPosition();

            /**
             * What started the teleport.
             * @see AllowTeleport.Context#source()
             */
            Identifier source();
        }
    }

    /**
     * Invoked when a target is renamed.
     * <p>
     * This event is fired on the server and on the clients of players that have discovered the target.
     */
    public static final class Rename {
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
