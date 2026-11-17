package com.supermartijn642.travelerswaystones.api.targets;

import com.supermartijn642.travelerswaystones.api.WaystoneEvents;
import com.supermartijn642.travelerswaystones.api.WaystoneType;
import com.supermartijn642.travelerswaystones.api.targets.personal.DiscoveredWaystoneTarget;
import com.supermartijn642.travelerswaystones.api.targets.personal.PersonalWaystoneManager;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.ApiStatus;

import java.util.Set;
import java.util.UUID;

/**
 * Represents a waystone destination owned by a player.
 */
@ApiStatus.NonExtendable
public interface WaystoneTarget {

    int MIN_NAME_LENGTH = 3, MAX_NAME_LENGTH = 20;

    /**
     * Unique id for this target. The id does not change for as long as the target exists, also not when the server restarts.
     */
    int id();

    /**
     * Name of this target.
     * <p>
     * Targets can be renamed, see {@link WaystoneEvents.Rename}.
     */
    String name();

    /**
     * Owner of this target.
     */
    UUID owner();

    /**
     * Dimension that this target is in.
     * <p>
     * May change when a target is moved, see {@link WaystoneEvents.Move}.
     */
    Identifier dimension();

    /**
     * Biome that this target is in. This may not always be up to date.
     * <p>
     * May change when a target is moved, see {@link WaystoneEvents.Move}.
     */
    Identifier biome();

    /**
     * Position that this target is at.
     * <p>
     * May change when a target is moved, see {@link WaystoneEvents.Move}.
     */
    BlockPos pos();

    /**
     * Block used for this target. Block will always have a registered {@link WaystoneType}.
     * <p>
     * May change when a target is moved, see {@link WaystoneEvents.Move}.
     */
    BlockState block();

    /**
     * Gets the {@link WaystoneType} associated with this target's block.
     */
    default WaystoneType waystoneType(){
        //noinspection DataFlowIssue
        return WaystoneType.get(this.block().getBlock());
    }

    /**
     * Players that have discovered this target. Not available on the client.
     * @throws IllegalStateException when called on the client
     */
    Set<UUID> discoverers();

    /**
     * Discovers this target for the given player, including associated effects like overlay message and particles.
     * @param player player to discover the target
     * @return the discovered target for the given player
     * @throws IllegalStateException if the player has already discovered the target
     */
    default DiscoveredWaystoneTarget discover(Player player){
        return PersonalWaystoneManager.get(player).discover(this);
    }
}
