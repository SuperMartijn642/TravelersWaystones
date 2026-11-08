package com.supermartijn642.travelerswaystones.api.targets;

import com.supermartijn642.travelerswaystones.api.WaystoneEvents;
import com.supermartijn642.travelerswaystones.api.WaystoneType;
import com.supermartijn642.travelerswaystones.api.targets.personal.PersonalWaystoneManager;
import com.supermartijn642.travelerswaystones.content.targets.GlobalWaystoneManagerImpl;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Manages all waystone data. Importantly, this class may only be interacted with from the server.<br>
 * For data for a specific player, see {@link PersonalWaystoneManager}.
 */
public final class GlobalWaystoneManager {

    /**
     * Shows a screen to the given to create a target at the given location. The block at the given location must have a registered {@link WaystoneType}.
     * @param player         player to show the screen to
     * @param level          level that the to be created target is in
     * @param pos            position that the to be created target is at
     * @param isNewlyPlaced  whether the target is newly placed or was naturally generated
     * @param nameSuggestion suggested name for the target, may be null
     * @return result containing whether the screen could be shown
     * @throws IllegalStateException if there is already a target at the given location
     * @throws IllegalStateException if the block at the given location has no {@link WaystoneType} registered
     */
    public static ClaimScreenResult displayClaimScreen(Player player, Level level, BlockPos pos, boolean isNewlyPlaced, @Nullable Component nameSuggestion){
        return GlobalWaystoneManagerImpl.displayClaimScreen(player, level, pos, isNewlyPlaced, nameSuggestion);
    }

    /**
     * Creates a target at the given location.
     * @param player player that is creating the target
     * @param level  level that the to be created target is in
     * @param pos    position that the to be created target is at
     * @param name   name of the to be created target
     * @return result including whether the target could be claimed
     * @throws IllegalStateException if there is already a target at the given location
     * @throws IllegalStateException if the block at the given location has no {@link WaystoneType} registered
     * @see ClaimResult
     * @see #add(Player, Level, BlockPos, String)
     */
    public static ClaimResult claim(Player player, Level level, BlockPos pos, String name){
        return GlobalWaystoneManagerImpl.claim(player, level, pos, name);
    }

    /**
     * Creates a target at the given location. This bypasses normal checks like the {@link WaystoneEvents.AllowClaim} event.
     * If this is a player action, you should use {@link #claim(Player, Level, BlockPos, String)} instead.
     * @param owner owner of the target
     * @param level level that the target is in
     * @param pos   position of the target
     * @param name  name of the target
     * @throws IllegalStateException if there is already a target at the given location
     * @throws IllegalStateException if the block at the given location has no {@link WaystoneType} registered
     */
    public static WaystoneTarget add(Player owner, Level level, BlockPos pos, String name){
        return GlobalWaystoneManagerImpl.add(owner, level, pos, name);
    }

    /**
     * Removes the given target. This means the target will be unrecoverable and all players lose access to it.
     */
    public static void remove(WaystoneTarget target){
        GlobalWaystoneManagerImpl.remove(target);
    }

    /**
     * Gets the target for the given id.
     */
    @Nullable
    public static WaystoneTarget getTargetForId(int id){
        return GlobalWaystoneManagerImpl.getTargetForId(id);
    }

    /**
     * Gets the target at the given location.
     */
    @Nullable
    public static WaystoneTarget getTargetAt(Identifier dimension, BlockPos pos){
        return forDimension(dimension).getTargetAt(pos);
    }

    /**
     * Gets the target at the given location.
     */
    @Nullable
    public static WaystoneTarget getTargetAt(Level level, BlockPos pos){
        return getTargetAt(level.dimension().identifier(), pos);
    }

    /**
     * All targets.
     */
    public static Set<WaystoneTarget> getAll(){
        return GlobalWaystoneManagerImpl.getAll();
    }

    /**
     * All targets owned by the given player.
     */
    public static Set<WaystoneTarget> getAllForOwner(UUID owner){
        return GlobalWaystoneManagerImpl.getAllForOwner(owner);
    }

    /**
     * Helper for targets in the given dimension.
     * The instance will stay relevant for as long as the server is.
     * @see Dimensional
     */
    public static Dimensional forDimension(Identifier dimension){
        return GlobalWaystoneManagerImpl.forDimension(dimension);
    }

    /**
     * Helper for targets in the given dimension.
     * The instance will stay relevant for as long as the server is.
     * @see Dimensional
     */
    public static Dimensional forDimension(Level level){
        return forDimension(level.dimension().identifier());
    }

    /**
     * All targets grouped by their owner.
     */
    public static Map<UUID,Set<WaystoneTarget>> byOwner(){
        return GlobalWaystoneManagerImpl.byOwner();
    }

    /**
     * All dimension helpers.
     * @see PersonalWaystoneManager.Dimensional
     */
    public static Map<Identifier,Dimensional> byDimension(){
        return GlobalWaystoneManagerImpl.byDimension();
    }

    /**
     * Gets all targets discovered by the player with the given uuid.
     * <p>
     * The returned instance may or may not reflect changes made after this method is called.
     * As such, the instance should <b>not</b> be held on to, but rather re-requested if needed at a later point.
     */
    public static Set<WaystoneTarget> getAllDiscoveredBy(UUID player){
        return GlobalWaystoneManagerImpl.getAllDiscoveredBy(player);
    }

    /**
     * Provides access to all targets located in a dimension.
     */
    @ApiStatus.NonExtendable
    public interface Dimensional {
        /**
         * Gets the target at the given location.
         */
        @Nullable
        WaystoneTarget getTargetAt(BlockPos pos);

        /**
         * All targets.
         */
        Set<WaystoneTarget> all();

        /**
         * All targets that are owned by the given player.
         */
        Set<WaystoneTarget> allForOwner(UUID owner);

        /**
         * All targets that are located in the given chunk.
         */
        Set<WaystoneTarget> allForChunk(ChunkPos chunk);

        /**
         * All targets grouped by their owner.
         */
        Map<UUID,Set<WaystoneTarget>> byOwner();

        /**
         * All targets grouped by the chunk they are located in.
         */
        Map<ChunkPos,Set<WaystoneTarget>> byChunk();
    }

    /// Result for {@link #displayClaimScreen(Player, Level, BlockPos, boolean, Component)}.
    /// Will be one of the following:
    ///  - {@link Success} if the screen is shown to the player
    ///  - {@link Denied} if claiming a target is denied by the {@link WaystoneEvents.AllowClaim} event
    /// @see Success
    /// @see Denied
    @ApiStatus.NonExtendable
    public sealed interface ClaimScreenResult {
        /**
         * Screen was successfully shown.
         */
        @ApiStatus.NonExtendable
        non-sealed interface Success extends ClaimScreenResult {
        }

        /**
         * Screen was not shown because claiming a target was denied by the {@link WaystoneEvents.AllowClaim} event.
         */
        @ApiStatus.NonExtendable
        non-sealed interface Denied extends ClaimScreenResult {
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
    }

    /// Result for {@link #claim(Player, Level, BlockPos, String)}.
    /// Will be one of the following:
    ///  - {@link Success} if the target is claimed
    ///  - {@link Denied} if claiming a target is denied by the {@link WaystoneEvents.AllowClaim} event
    /// @see Success
    /// @see Denied
    @ApiStatus.NonExtendable
    public sealed interface ClaimResult {
        /**
         * Target was successfully claimed.
         */
        @ApiStatus.NonExtendable
        non-sealed interface Success extends ClaimResult {
            /**
             * Target that was created.
             */
            WaystoneTarget target();
        }

        /**
         * Target was not claimed because it was denied by the {@link WaystoneEvents.AllowClaim} event.
         */
        @ApiStatus.NonExtendable
        non-sealed interface Denied extends ClaimResult {
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
    }
}
