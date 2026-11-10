package com.supermartijn642.travelerswaystones.api.targets.personal;

import com.supermartijn642.travelerswaystones.api.targets.WaystoneTarget;
import com.supermartijn642.travelerswaystones.content.targets.personal.PersonalWaystoneManagerImpl;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
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
    DiscoveredWaystoneTarget discover(WaystoneTarget target);

    /**
     * Opens the teleport screen for the player.
     * May be called on the server and on the client. On the server, the screen is opened through a packet.
     * <p>
     * Teleporting is only allowed when the player is close to {@code from}, or when {@code from} is null and the player carries a warp pendant.
     * @param from target that the player teleports from, or null when not teleporting from a waystone
     * @throws IllegalArgumentException if {@code from} was not discovered by the player
     */
    void openTeleportScreen(@Nullable DiscoveredWaystoneTarget from);

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
    default boolean has(Identifier dimension, BlockPos pos){
        return this.getTargetAt(dimension, pos) != null;
    }

    /**
     * Whether the player has discovered a target at the given location.
     */
    default boolean has(Level level, BlockPos pos){
        return this.has(level.dimension().identifier(), pos);
    }

    /**
     * Gets the target at the given location if the player has discovered it.
     */
    @Nullable
    default DiscoveredWaystoneTarget getTargetAt(Identifier dimension, BlockPos pos){
        return this.forDimension(dimension).getTargetAt(pos);
    }

    /**
     * Gets the target at the given location if the player has discovered it.
     */
    @Nullable
    default DiscoveredWaystoneTarget getTargetAt(Level level, BlockPos pos){
        return this.getTargetAt(level.dimension().identifier(), pos);
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
    Dimensional forDimension(Identifier dimension);

    /**
     * Helper for discovered targets in the given dimension.
     * The instance will stay relevant for as long as this manager is.
     * @see Dimensional
     */
    default Dimensional forDimension(Level level){
        return this.forDimension(level.dimension().identifier());
    }

    /**
     * All discovered targets grouped by their owner.
     */
    Map<UUID,Set<DiscoveredWaystoneTarget>> byOwner();

    /**
     * All dimension helpers.
     * @see Dimensional
     */
    Map<Identifier,Dimensional> byDimension();

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
