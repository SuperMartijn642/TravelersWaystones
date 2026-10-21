package com.supermartijn642.waystonesreimagined.api.targets.personal;

import com.supermartijn642.waystonesreimagined.api.WaystoneEvents;
import com.supermartijn642.waystonesreimagined.api.WaystoneType;
import com.supermartijn642.waystonesreimagined.api.targets.WaystoneTarget;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3dc;

import java.util.UUID;

/**
 * Represents a {@link WaystoneTarget} discovered by a player.
 */
@ApiStatus.NonExtendable
public interface DiscoveredWaystoneTarget {

    /**
     * Global target that is discovered.
     */
    WaystoneTarget target();

    /**
     * Whether this target is marked as favorite.
     */
    boolean isFavorite();

    /**
     * Sets whether this target is marked as favorite.
     */
    void setFavorite(boolean favorite);

    /**
     * Whether dangerous warnings for teleporting to this target should be suppressed.
     */
    boolean suppressDangerousWarning();

    /**
     * Sets whether dangerous warnings for teleporting to this target should be suppressed.
     */
    void setSuppressDangerousWarning(boolean suppress);

    /**
     * Undiscovers this target for the associated player.
     */
    void undiscover();

    /**
     * Gets the id for {@link #target()}.
     * @see WaystoneTarget#id()
     */
    int id();

    /**
     * Gets the name for {@link #target()}.
     * @see WaystoneTarget#name()
     */
    String name();

    /**
     * Gets the owner for {@link #target()}.
     * @see WaystoneTarget#owner()
     */
    UUID owner();

    /**
     * Gets the dimension for {@link #target()}.
     * @see WaystoneTarget#dimension()
     */
    Identifier dimension();

    /**
     * Gets the biome for {@link #target()}.
     * @see WaystoneTarget#biome()
     */
    Identifier biome();

    /**
     * Gets the position for {@link #target()}.
     * @see WaystoneTarget#pos()
     */
    BlockPos pos();

    /**
     * Gets the block for {@link #target()}.
     * @see WaystoneTarget#block()
     */
    BlockState block();

    /**
     * Gets the {@link WaystoneType} instance associated with {@link #target()}'s block.
     * @see WaystoneTarget#waystoneType()
     */
    WaystoneType waystoneType();

    /**
     * Teleports the given player to this target, including associated affects like particles.
     * @param player player to teleport
     * @param from target that the player is teleporting from
     * @param allowDangerous whether to teleport if the destination is dangerous
     * @return result containing whether the teleport was successful
     * @see TeleportResult
     */
    TeleportResult teleport(ServerPlayer player, @Nullable DiscoveredWaystoneTarget from, boolean allowDangerous);

    /// Result for {@link #teleport(ServerPlayer, DiscoveredWaystoneTarget, boolean)}.
    /// Will be one of the following:
    ///  - {@link Success} if the player is teleported successfully
    ///  - {@link Dangerous} if the destination is dangerous and the {@code allowDangerous} argument was {@code false}
    ///  - {@link NoTeleportPosition} if no teleport position was available
    ///  - {@link Denied} if teleportation was denied by the {@link WaystoneEvents.AllowTeleport} event
    ///  - {@link Error} if there was some unexpected error like the destination level not being present
    /// @see Success
    /// @see Dangerous
    /// @see NoTeleportPosition
    /// @see Denied
    /// @see Error
    @ApiStatus.NonExtendable
    sealed interface TeleportResult {
        /**
         * Teleportation was successful.
         */
        @ApiStatus.NonExtendable
        non-sealed interface Success extends TeleportResult {
            /**
             * Player instance after being teleported.
             */
            Player player();

            /**
             * Level that the player was teleported to.
             */
            Level level();

            /**
             * Position that the player was teleported to.
             */
            Vector3dc position();
        }

        /**
         * Teleporting was not executed because the destination is dangerous.
         */
        @ApiStatus.NonExtendable
        non-sealed interface Dangerous extends TeleportResult {
            /**
             * Context message that can be shown to the player.
             */
            @Nullable Component warning();

            /**
             * Context message that can be shown to the player.
             * Uses a default message if none was provided.
             */
            Component warningOrDefault();
        }

        /**
         * Teleporting was not executed because no teleport position was available.
         * Usually, this is the direct result of {@link WaystoneType#getTeleportPosition(BlockState, Level, BlockPos, Player)} failing.
         */
        @ApiStatus.NonExtendable
        non-sealed interface NoTeleportPosition extends TeleportResult {
            /**
             * Context message that can be shown to the player.
             */
            @Nullable Component reason();

            /**
             * Context message that can be shown to the player.
             * Uses a default message if none was provided.
             */
            Component reasonOrDefault();
        }

        /**
         * Teleporting was not executed because it was denied by the {@link WaystoneEvents.AllowTeleport} event.
         */
        @ApiStatus.NonExtendable
        non-sealed interface Denied extends TeleportResult {
            /**
             * Context message that can be shown to the player.
             */
            @Nullable Component reason();

            /**
             * Context message that can be shown to the player.
             * Uses a default message if none was provided.
             */
            Component reasonOrDefault();
        }

        /**
         * Teleporting was not executed because some unexpected error occurred.
         * Will be the result if, for example, the destination level is not present or the player entity could not be teleported.
         */
        @ApiStatus.NonExtendable
        non-sealed interface Error extends TeleportResult {
            /**
             * Context message that can be shown to the player.
             */
            Component reason();
        }
    }
}
