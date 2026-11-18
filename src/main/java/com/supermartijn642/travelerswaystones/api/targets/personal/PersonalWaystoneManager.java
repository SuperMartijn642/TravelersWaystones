package com.supermartijn642.travelerswaystones.api.targets.personal;

import com.supermartijn642.travelerswaystones.api.WaystoneEvents;
import com.supermartijn642.travelerswaystones.api.targets.WaystoneTarget;
import com.supermartijn642.travelerswaystones.content.targets.personal.PersonalWaystoneManagerImpl;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Manages waystone data for a specific player.
 * <p>
 * Available on the server and on the client. On the client, only the manager of the local player is available.
 */
@ApiStatus.NonExtendable
public interface PersonalWaystoneManager {

    /**
     * Gets the waystone manager for the given player.
     * The instance will stay relevant for as long as the player remains online.
     */
    static PersonalWaystoneManager get(Player player){
        return PersonalWaystoneManagerImpl.get(player, true);
    }

    /**
     * Whether the player has discovered the given target.
     */
    boolean has(WaystoneTarget target);

    /**
     * Gets the target for the given id.
     */
    @Nullable
    DiscoveredWaystoneTarget getTargetForId(int id);

    /**
     * Gets the {@link DiscoveredWaystoneTarget} for the given target if the player has discovered it.
     */
    @Nullable
    DiscoveredWaystoneTarget get(WaystoneTarget target);

    /**
     * Discovers the given target.
     * @throws IllegalStateException if the player has already discovered the target
     */
    default DiscoveredWaystoneTarget discover(WaystoneTarget target){
        return this.discover(target, true);
    }

    /**
     * Discovers the given target.
     * @param showEffects whether to show the discovery title and particles
     * @throws IllegalStateException if the player has already discovered the target
     */
    DiscoveredWaystoneTarget discover(WaystoneTarget target, boolean showEffects);

    /**
     * Opens the teleport screen for the player.
     * May be called on the server and on the client. On the server, the screen is opened through a packet.
     * <p>
     * Teleporting is only allowed when the player is close to {@code from}, or when {@code from} is null and the player carries a warp pendant.
     * When the player has not discovered any targets, a message is shown instead of the screen.
     * @param from target that the player teleports from, or null when not teleporting from a waystone
     * @throws IllegalArgumentException if {@code from} was not discovered by the player
     * @see #openTrustedTeleportScreen(Identifier)
     */
    default void openTeleportScreen(@Nullable DiscoveredWaystoneTarget from){
        this.openTeleportScreen(from, null);
    }

    /**
     * Opens the teleport screen for the player with the given target selected.
     * @param from     target that the player teleports from, or null when not teleporting from a waystone
     * @param selected target to select, or null to select the first entry
     * @throws IllegalArgumentException if {@code from} or {@code selected} was not discovered by the player
     * @see #openTeleportScreen(DiscoveredWaystoneTarget)
     */
    void openTeleportScreen(@Nullable DiscoveredWaystoneTarget from, @Nullable DiscoveredWaystoneTarget selected);

    /**
     * Opens the teleport screen for the player and allows one teleport from it without the player being at a waystone or carrying a warp pendant.
     * May only be called on the server.
     * <p>
     * The teleport fires the {@link WaystoneEvents.AllowTeleport} and {@link WaystoneEvents.Teleport} events with the given source.
     * The permission ends after one successful teleport or when the player logs out. Opening another trusted screen replaces it.
     * When the player has not discovered any targets, a message is shown instead of the screen.
     * @param source what opened the screen, used as the source of the teleport
     * @throws IllegalStateException if called on the client
     */
    default void openTrustedTeleportScreen(Identifier source){
        this.openTrustedTeleportScreen(source, null);
    }

    /**
     * Opens a trusted teleport screen for the player with the given target selected.
     * @param source   what opened the screen, used as the source of the teleport
     * @param selected target to select, or null to select the first entry
     * @throws IllegalArgumentException if {@code selected} was not discovered by the player
     * @throws IllegalStateException    if called on the client
     * @see #openTrustedTeleportScreen(Identifier)
     */
    void openTrustedTeleportScreen(Identifier source, @Nullable DiscoveredWaystoneTarget selected);

    /**
     * All targets discovered by the player.
     */
    Set<DiscoveredWaystoneTarget> all();

    /**
     * Targets marked as favorites.
     */
    Set<DiscoveredWaystoneTarget> favorites();

    /**
     * Targets that the player recently teleported to.
     */
    List<DiscoveredWaystoneTarget> recents();

    /**
     * Whether the player has discovered a target at the given location.
     */
    default boolean has(ResourceKey<Level> dimension, BlockPos pos){
        return this.getTargetAt(dimension, pos) != null;
    }

    /**
     * Whether the player has discovered a target at the given location.
     */
    default boolean has(Level level, BlockPos pos){
        return this.has(level.dimension(), pos);
    }

    /**
     * Gets the target at the given location if the player has discovered it.
     */
    @Nullable
    default DiscoveredWaystoneTarget getTargetAt(ResourceKey<Level> dimension, BlockPos pos){
        return this.forDimension(dimension).getTargetAt(pos);
    }

    /**
     * Gets the target at the given location if the player has discovered it.
     */
    @Nullable
    default DiscoveredWaystoneTarget getTargetAt(Level level, BlockPos pos){
        return this.getTargetAt(level.dimension(), pos);
    }

    /**
     * All discovered targets that are owned by the given player.
     */
    Set<DiscoveredWaystoneTarget> allForOwner(UUID owner);

    /**
     * Helper for discovered targets in the given dimension.
     * The instance will stay relevant for as long as this manager is.
     * @see Dimensional
     */
    Dimensional forDimension(ResourceKey<Level> dimension);

    /**
     * Helper for discovered targets in the given dimension.
     * The instance will stay relevant for as long as this manager is.
     * @see Dimensional
     */
    default Dimensional forDimension(Level level){
        return this.forDimension(level.dimension());
    }

    /**
     * All discovered targets grouped by their owner.
     */
    Map<UUID,Set<DiscoveredWaystoneTarget>> byOwner();

    /**
     * All dimension helpers.
     * @see Dimensional
     */
    Map<ResourceKey<Level>,Dimensional> byDimension();

    /**
     * Provides access to all discovered targets located in a dimension.
     */
    @ApiStatus.NonExtendable
    interface Dimensional {
        /**
         * Gets the target at the given location if the player has discovered it.
         */
        @Nullable
        DiscoveredWaystoneTarget getTargetAt(BlockPos pos);

        /**
         * All targets discovered by the player.
         */
        Set<DiscoveredWaystoneTarget> all();

        /**
         * All discovered targets that are owned by the given player.
         */
        Set<DiscoveredWaystoneTarget> allForOwner(UUID owner);

        /**
         * All discovered targets that are located in the given chunk.
         */
        Set<DiscoveredWaystoneTarget> allForChunk(ChunkPos chunk);

        /**
         * All discovered targets grouped by their owner.
         */
        Map<UUID,Set<DiscoveredWaystoneTarget>> byOwner();

        /**
         * All discovered targets grouped by the chunk they are located in.
         */
        Map<ChunkPos,Set<DiscoveredWaystoneTarget>> byChunk();
    }
}
