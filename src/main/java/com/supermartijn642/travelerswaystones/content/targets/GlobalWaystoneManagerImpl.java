package com.supermartijn642.travelerswaystones.content.targets;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.supermartijn642.core.CommonUtils;
import com.supermartijn642.core.TextComponents;
import com.supermartijn642.travelerswaystones.TravelersWaystones;
import com.supermartijn642.travelerswaystones.api.WaystoneEvents;
import com.supermartijn642.travelerswaystones.api.WaystoneType;
import com.supermartijn642.travelerswaystones.api.targets.GlobalWaystoneManager;
import com.supermartijn642.travelerswaystones.api.targets.WaystoneTarget;
import com.supermartijn642.travelerswaystones.api.targets.personal.DiscoveredWaystoneTarget;
import com.supermartijn642.travelerswaystones.api.targets.personal.PersonalWaystoneManager;
import com.supermartijn642.travelerswaystones.content.WaystoneEventsImpl;
import com.supermartijn642.travelerswaystones.content.packets.ChunkTargetsPacket;
import com.supermartijn642.travelerswaystones.content.packets.OpenClaimScreenPacket;
import com.supermartijn642.travelerswaystones.content.packets.RenameTargetPacket;
import com.supermartijn642.travelerswaystones.content.packets.UpdateTargetPositionPacket;
import com.supermartijn642.travelerswaystones.content.targets.personal.PersonalWaystoneManagerImpl;
import com.supermartijn642.travelerswaystones.util.IndexedSet;
import com.supermartijn642.travelerswaystones.util.PartitionedSet;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ReferenceOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;

import static com.supermartijn642.travelerswaystones.api.targets.GlobalWaystoneManager.Dimensional;

public class GlobalWaystoneManagerImpl {

    private static final Set<WaystoneTarget> ALL = new ObjectOpenHashSet<>(), ALL_VIEW = Collections.unmodifiableSet(ALL);
    private static final Int2ObjectOpenHashMap<WaystoneTarget> BY_ID = new Int2ObjectOpenHashMap<>();  // This is essentially the same as 'ALL', but I need both a lookup by id and a set of all targets
    private static final PartitionedSet<UUID,WaystoneTarget> BY_OWNER = new PartitionedSet<>(WaystoneTarget::owner);
    private static final Map<ResourceKey<Level>,DimensionalImpl> BY_DIMENSION = new Object2ReferenceOpenHashMap<>();
    private static final Map<ResourceKey<Level>,Dimensional> BY_DIMENSION_VIEW = Collections.unmodifiableMap(BY_DIMENSION);
    private static final Map<UUID,Set<WaystoneTarget>> BY_DISCOVERER = new Object2ObjectOpenHashMap<>();
    /**
     * Serialized targets that were added previously, but whose block no longer exists or no longer has a waystone type registered.
     * These are not used, just kept as they were saved, so the targets come back when their block or waystone type does.
     */
    private static final List<JsonObject> UNLOADED_TARGETS = new ArrayList<>();
    private static int nextTargetIndex = 0;

    public static GlobalWaystoneManager.ClaimScreenResult displayClaimScreen(ServerPlayer player, ServerLevel level, BlockPos pos, boolean isNewlyPlaced, @Nullable Component nameSuggestion){
        if(GlobalWaystoneManager.getTargetAt(level, pos) != null)
            throw new IllegalStateException("A target already exists at (" + pos.toShortString() + ") in '" + level.dimension().identifier() + "'!");
        BlockState block = level.getBlockState(pos);
        if(WaystoneType.get(block.getBlock()) == null)
            throw new IllegalStateException("No waystone type registered for block '" + block.getBlock() + "'!");
        WaystoneEvents.AllowClaim.Result permission = WaystoneEventsImpl.AllowClaim.invoke(player, level, pos, block);
        if(permission instanceof WaystoneEvents.AllowClaim.Result.Deny deny)
            return new ClaimScreenResultImpl.Denied(deny.reason());
        TravelersWaystones.CHANNEL.sendToPlayer(player, new OpenClaimScreenPacket(pos, isNewlyPlaced, nameSuggestion));
        return ClaimScreenResultImpl.SUCCESS;
    }

    public static GlobalWaystoneManager.ClaimResult claim(ServerPlayer player, ServerLevel level, BlockPos pos, String name){
        name = name.trim();
        if(name.length() < WaystoneTarget.MIN_NAME_LENGTH || name.length() > WaystoneTarget.MAX_NAME_LENGTH)
            throw new IllegalArgumentException("Name must be between " + WaystoneTarget.MIN_NAME_LENGTH + " and " + WaystoneTarget.MAX_NAME_LENGTH + " characters, not " + name.length() + "!");
        ResourceKey<Level> dimension = level.dimension();
        if(forDimension(dimension).getTargetAt(pos) != null)
            throw new IllegalStateException("A target already exists at (" + pos.getX() + "," + pos.getY() + "," + pos.getZ() + ") in " + dimension.identifier() + "!");
        BlockState block = level.getBlockState(pos);
        if(WaystoneType.get(block.getBlock()) == null)
            throw new IllegalStateException("No waystone type registered for block '" + block.getBlock() + "'!");
        WaystoneEvents.AllowClaim.Result permission = WaystoneEventsImpl.AllowClaim.invoke(player, level, pos, block);
        if(permission instanceof WaystoneEvents.AllowClaim.Result.Deny deny)
            return new ClaimResultImpl.Denied(deny.reason());
        WaystoneTarget target = add(player.getUUID(), player, level, pos, name);
        return new ClaimResultImpl.Success(target);
    }

    public static WaystoneTarget add(UUID owner, @Nullable ServerPlayer player, ServerLevel level, BlockPos pos, String name){
        name = name.trim();
        if(name.length() < WaystoneTarget.MIN_NAME_LENGTH || name.length() > WaystoneTarget.MAX_NAME_LENGTH)
            throw new IllegalArgumentException("Name must be between " + WaystoneTarget.MIN_NAME_LENGTH + " and " + WaystoneTarget.MAX_NAME_LENGTH + " characters, not " + name.length() + "!");
        ResourceKey<Level> dimension = level.dimension();
        if(forDimension(dimension).getTargetAt(pos) != null)
            throw new IllegalStateException("A target already exists at (" + pos.getX() + "," + pos.getY() + "," + pos.getZ() + ") in " + dimension.identifier() + "!");
        BlockState block = level.getBlockState(pos);
        if(WaystoneType.get(block.getBlock()) == null)
            throw new IllegalStateException("No waystone type registered for block '" + block.getBlock() + "'!");
        int id = nextTargetIndex++;
        ResourceKey<Biome> biome = level.getBiome(pos).unwrapKey().orElseThrow();
        WaystoneTargetImpl target = new WaystoneTargetImpl(false, id, name, owner, dimension, biome, pos, block);
        ALL.add(target);
        BY_ID.put(id, target);
        BY_OWNER.add(target);
        forDimensionImpl(dimension).add(target);
        sendChunkTargets(dimension, target.chunkPos());
        WaystoneEventsImpl.Claim.invoke(target, level, player);
        return target;
    }

    public static void remove(WaystoneTarget target){
        if(!((WaystoneTargetImpl)target).isValid())
            throw new IllegalArgumentException("Target has already been removed!");
        if(((WaystoneTargetImpl)target).isPreventRemoveWhileMoving())
            return;
        ((WaystoneTargetImpl)target).invalidate();
        ALL.remove(target);
        BY_ID.remove(target.id());
        BY_OWNER.remove(target);
        DimensionalImpl dimensional = forDimensionImpl(target.dimension());
        if(dimensional != null)
            dimensional.remove(target);
        sendChunkTargets(target.dimension(), ((WaystoneTargetImpl)target).chunkPos());
        for(UUID discoverer : target.discoverers()){
            Set<WaystoneTarget> targets = BY_DISCOVERER.get(discoverer);
            if(targets != null){
                targets.remove(target);
                if(targets.isEmpty())
                    BY_DISCOVERER.remove(discoverer);
            }
        }
        // Copy the discoverers as undiscovering removes them from the target
        forAllOnlinePlayers(new ObjectOpenHashSet<>(target.discoverers()), player -> {
            PersonalWaystoneManager waystoneManager = PersonalWaystoneManagerImpl.get(player, false);
            if(waystoneManager != null){
                DiscoveredWaystoneTarget discoveredTarget = waystoneManager.get(target);
                if(discoveredTarget != null)
                    ((PersonalWaystoneManagerImpl)waystoneManager).undiscover(discoveredTarget, false, true);
            }
        });
        WaystoneEventsImpl.Remove.invoke(target);
    }

    /**
     * Removes the target of a waystone when one of its blocks is replaced by a different block.
     */
    public static void onBlockChanged(Level level, BlockPos pos, BlockState oldState, BlockState newState){
        if(oldState.is(newState.getBlock()))
            return;
        WaystoneType type = WaystoneType.get(oldState.getBlock());
        if(type == null)
            return;
        WaystoneTarget target = GlobalWaystoneManager.getTargetAt(level, type.getTargetPos(oldState, pos));
        if(target != null && target.block().is(oldState.getBlock()))
            remove(target);
    }

    public static void rename(WaystoneTarget target, String name, @Nullable ServerPlayer player){
        name = name.trim();
        if(name.length() < WaystoneTarget.MIN_NAME_LENGTH || name.length() > WaystoneTarget.MAX_NAME_LENGTH)
            throw new IllegalArgumentException("Name must be between " + WaystoneTarget.MIN_NAME_LENGTH + " and " + WaystoneTarget.MAX_NAME_LENGTH + " characters, not " + name.length() + "!");
        if(!((WaystoneTargetImpl)target).isValid())
            throw new IllegalArgumentException("Target has been removed!");
        String oldName = target.name();
        ((WaystoneTargetImpl)target).setName(name);
        RenameTargetPacket renamePacket = new RenameTargetPacket(target.id(), name);
        forAllOnlinePlayers(target.discoverers(), p -> TravelersWaystones.CHANNEL.sendToPlayer(p, renamePacket));
        WaystoneEventsImpl.Rename.invoke(target, player, oldName, false);
    }

    public static void move(WaystoneTarget target, ServerPlayer player, ServerLevel level, BlockPos to, BlockState block, ResourceKey<Biome> biome, ServerLevel oldLevel){
        if(WaystoneType.get(block.getBlock()) == null)
            throw new IllegalStateException("No waystone type registered for block '" + block.getBlock() + "'!");
        ResourceKey<Level> oldDimension = target.dimension();
        BlockPos oldPos = target.pos();
        BlockState oldBlock = target.block();
        ResourceKey<Biome> oldBiome = target.biome();
        // Remove target
        forDimensionImpl(target.dimension()).remove(target);
        forAllOnlinePlayers(target.discoverers(), p -> {
            PersonalWaystoneManagerImpl waystoneManager = (PersonalWaystoneManagerImpl)PersonalWaystoneManagerImpl.get(p, true);
            //noinspection DataFlowIssue
            waystoneManager.updateTargetPositionPre(waystoneManager.get(target));
        });
        // Update position
        ((WaystoneTargetImpl)target).setBiomePosAndBlock(biome, to, block);
        // Re-add target
        forDimensionImpl(target.dimension()).add(target);
        ChunkPos oldChunk = ChunkPos.containing(oldPos), newChunk = ((WaystoneTargetImpl)target).chunkPos();
        sendChunkTargets(oldDimension, oldChunk);
        if(!newChunk.equals(oldChunk))
            sendChunkTargets(target.dimension(), newChunk);
        UpdateTargetPositionPacket packet = new UpdateTargetPositionPacket(target.id(), to, block, biome);
        forAllOnlinePlayers(target.discoverers(), p -> {
            PersonalWaystoneManagerImpl waystoneManager = (PersonalWaystoneManagerImpl)PersonalWaystoneManagerImpl.get(p, true);
            //noinspection DataFlowIssue
            waystoneManager.updateTargetPositionPost(waystoneManager.get(target));
            TravelersWaystones.CHANNEL.sendToPlayer(p, packet);
        });
        // Events
        WaystoneEventsImpl.Move.invoke(target, level, player, oldLevel, oldPos, oldBlock, oldBiome);
        WaystoneEventsImpl.UpdateLocation.invoke(target, oldDimension, oldPos, oldBlock, oldBiome, false);
    }

    public static void addDiscovered(WaystoneTarget target, UUID discoverer){
        if(target.discoverers().contains(discoverer))
            return;
        BY_DISCOVERER.computeIfAbsent(discoverer, _ -> new ObjectOpenHashSet<>()).add(target);
        ((WaystoneTargetImpl)target).addDiscoverer(discoverer);
    }

    public static void removeDiscovered(DiscoveredWaystoneTarget target, UUID discoverer){
        if(!target.target().discoverers().contains(discoverer))
            return;
        // There's no entry anymore if the target itself is being removed and it was the last one for this player
        Set<WaystoneTarget> targets = BY_DISCOVERER.get(discoverer);
        if(targets != null){
            targets.remove(target.target());
            if(targets.isEmpty())
                BY_DISCOVERER.remove(discoverer);
        }
        ((WaystoneTargetImpl)target.target()).removeDiscoverer(discoverer);
    }

    @Nullable
    public static WaystoneTarget getTargetForId(int id){
        return BY_ID.get(id);
    }

    public static Set<WaystoneTarget> getAll(){
        return ALL_VIEW;
    }

    public static Set<WaystoneTarget> getAllForOwner(UUID owner){
        return BY_OWNER.get(owner);
    }

    public static Dimensional forDimension(ResourceKey<Level> dimension){
        return forDimensionImpl(dimension);
    }

    private static DimensionalImpl forDimensionImpl(ResourceKey<Level> dimension){
        // As mods may hold on to the returned instance, we can't return an empty dummy as it may become outdated
        // This may lead to issues if some mod spams this for non-existent levels, but that would be that mod's fault
        return BY_DIMENSION.computeIfAbsent(dimension, _ -> new DimensionalImpl());
    }

    public static Map<UUID,Set<WaystoneTarget>> byOwner(){
        return BY_OWNER.getMapView();
    }

    public static Map<ResourceKey<Level>,Dimensional> byDimension(){
        return BY_DIMENSION_VIEW;
    }

    public static Set<WaystoneTarget> getAllDiscoveredBy(UUID player){
        Set<WaystoneTarget> targets = BY_DISCOVERER.get(player);
        return targets == null ? Collections.emptySet() : Collections.unmodifiableSet(targets);
    }

    public static void clear(){
        ALL.clear();
        BY_ID.clear();
        BY_OWNER.clear();
        BY_DIMENSION.clear();
        BY_DISCOVERER.clear();
        UNLOADED_TARGETS.clear();
        nextTargetIndex = 0;
    }

    public static JsonElement serialize(RegistryAccess registryAccess){
        JsonArray targets = new JsonArray(BY_ID.size() + UNLOADED_TARGETS.size());
        for(WaystoneTarget target : BY_ID.values())
            targets.add(WaystoneTargetImpl.serialize(target, registryAccess));
        for(JsonObject target : UNLOADED_TARGETS)
            targets.add(target);
        return targets;
    }

    public static void deserialize(JsonElement element, RegistryAccess registryAccess){
        clear();
        JsonArray targets = element.getAsJsonArray();
        List<Exception> exceptions = new ArrayList<>();
        Set<String> missingBlocks = new TreeSet<>(), missingTypes = new TreeSet<>();
        int missingBlockTargets = 0, missingTypeTargets = 0;
        for(JsonElement targetElement : targets){
            JsonObject targetJson;
            WaystoneTarget target;
            try{
                targetJson = targetElement.getAsJsonObject();
                if(WaystoneTargetImpl.hasMissingBlock(targetJson)){
                    int id = targetJson.get("id").getAsInt();
                    if(id >= nextTargetIndex)
                        nextTargetIndex = id + 1;
                    UNLOADED_TARGETS.add(targetJson);
                    missingBlocks.add(WaystoneTargetImpl.blockName(targetJson));
                    missingBlockTargets++;
                    continue;
                }
                target = WaystoneTargetImpl.deserialize(targetJson, registryAccess);
            }catch(Exception e){
                exceptions.add(e);
                continue;
            }
            if(WaystoneType.get(target.block().getBlock()) == null){
                if(target.id() >= nextTargetIndex)
                    nextTargetIndex = target.id() + 1;
                UNLOADED_TARGETS.add(targetJson);
                missingTypes.add(BuiltInRegistries.BLOCK.getKey(target.block().getBlock()).toString());
                missingTypeTargets++;
                continue;
            }
            if(BY_ID.containsKey(target.id())){
                TravelersWaystones.LOGGER.error("Read duplicate waystone target with id {}! Duplicate will be ignored", target.id());
                continue;
            }
            if(forDimension(target.dimension()).getTargetAt(target.pos()) != null){
                TravelersWaystones.LOGGER.error("Read duplicate waystone target for position ({}) in dimension '{}'! Duplicate will be ignored", target.pos().toShortString(), target.dimension().identifier());
                continue;
            }
            if(target.id() >= nextTargetIndex)
                nextTargetIndex = target.id() + 1;
            ALL.add(target);
            BY_ID.put(target.id(), target);
            BY_OWNER.add(target);
            for(UUID discoverer : target.discoverers())
                BY_DISCOVERER.computeIfAbsent(discoverer, _ -> new ObjectOpenHashSet<>()).add(target);
            forDimensionImpl(target.dimension()).add(target);
        }
        if(missingBlockTargets > 0)
            TravelersWaystones.LOGGER.error("Could not load {} waystone targets because the following blocks do not exist:\n{}", missingBlockTargets, String.join(", ", missingBlocks));
        if(missingTypeTargets > 0)
            TravelersWaystones.LOGGER.error("Could not load {} waystone targets because no waystone type was registered for the following blocks:\n{}", missingTypeTargets, String.join(", ", missingTypes));
        if(!exceptions.isEmpty()){
            String message = exceptions.size() == 1 ? "Failed to deserialize a target!" : "Failed to deserialize " + exceptions.size() + " targets!";
            Exception first = exceptions.getFirst();
            for(int i = 1; i < exceptions.size(); i++)
                first.addSuppressed(exceptions.get(i));
            TravelersWaystones.LOGGER.error(message, first);
            throw new RuntimeException(message, first);
        }
    }

    /**
     * Sends positions targets in a chunk to a player.
     */
    public static void onChunkSent(ServerPlayer player, ServerLevel level, ChunkPos chunk){
        Set<WaystoneTarget> targets = forDimension(level.dimension()).allForChunk(chunk);
        if(!targets.isEmpty())
            TravelersWaystones.CHANNEL.sendToPlayer(player, createChunkTargetsPacket(chunk, targets));
    }

    /**
     * Sends positions targets in a chunk to <b>all</b> players that have the chunk loaded.
     */
    private static void sendChunkTargets(ResourceKey<Level> dimension, ChunkPos chunk){
        ServerLevel level = CommonUtils.getServer().getLevel(dimension);
        if(level == null)
            return;
        List<ServerPlayer> players = level.getChunkSource().chunkMap.getPlayers(chunk, false);
        if(players.isEmpty())
            return;
        ChunkTargetsPacket packet = createChunkTargetsPacket(chunk, forDimension(dimension).allForChunk(chunk));
        for(ServerPlayer player : players)
            TravelersWaystones.CHANNEL.sendToPlayer(player, packet);
    }

    private static ChunkTargetsPacket createChunkTargetsPacket(ChunkPos chunk, Set<WaystoneTarget> targets){
        List<BlockPos> positions = new ArrayList<>(targets.size());
        for(WaystoneTarget target : targets)
            positions.add(target.pos());
        return new ChunkTargetsPacket(chunk, positions);
    }

    private static void forAllOnlinePlayers(Set<UUID> ids, Consumer<Player> action){
        PlayerList playerList = CommonUtils.getServer().getPlayerList();
        if(playerList.getPlayers().size() < ids.size()){
            for(ServerPlayer player : playerList.getPlayers()){
                if(ids.contains(player.getUUID()))
                    action.accept(player);
            }
        }else{
            for(UUID id : ids){
                ServerPlayer player = playerList.getPlayer(id);
                if(player != null)
                    action.accept(player);
            }
        }
    }

    private static class DimensionalImpl implements Dimensional {
        private final Set<WaystoneTarget> all = new ObjectOpenHashSet<>(), allView = Collections.unmodifiableSet(this.all);
        private final PartitionedSet<UUID,WaystoneTarget> byOwner = new PartitionedSet<>(WaystoneTarget::owner);
        private final PartitionedSet<ChunkPos,WaystoneTarget> byChunk = new PartitionedSet<>(t -> ((WaystoneTargetImpl)t).chunkPos());
        private final IndexedSet<BlockPos,WaystoneTarget> byPos = new IndexedSet<>(WaystoneTarget::pos);

        private void add(WaystoneTarget target){
            this.all.add(target);
            this.byOwner.add(target);
            this.byChunk.add(target);
            this.byPos.add(target);
        }

        private void remove(WaystoneTarget target){
            this.all.remove(target);
            this.byOwner.remove(target);
            this.byChunk.remove(target);
            this.byPos.remove(target);
        }

        @Override
        public @Nullable WaystoneTarget getTargetAt(BlockPos pos){
            return this.byPos.get(pos);
        }

        @Override
        public Set<WaystoneTarget> all(){
            return this.allView;
        }

        @Override
        public Set<WaystoneTarget> allForOwner(UUID owner){
            return this.byOwner.get(owner);
        }

        @Override
        public Set<WaystoneTarget> allForChunk(ChunkPos chunk){
            return this.byChunk.get(chunk);
        }

        @Override
        public Map<UUID,Set<WaystoneTarget>> byOwner(){
            return this.byOwner.getMapView();
        }

        @Override
        public Map<ChunkPos,Set<WaystoneTarget>> byChunk(){
            return this.byChunk.getMapView();
        }
    }

    private static class ClaimScreenResultImpl {
        private static final GlobalWaystoneManager.ClaimScreenResult.Success SUCCESS = new GlobalWaystoneManager.ClaimScreenResult.Success() {};

        private record Denied(@Nullable Component reason) implements GlobalWaystoneManager.ClaimScreenResult.Denied {
            @Override
            public Component reasonOrDefault(){
                return this.reason == null ? TextComponents.translation("travelerswaystones.claim.denied").get() : this.reason;
            }
        }
    }

    private static class ClaimResultImpl {
        private record Success(WaystoneTarget target) implements GlobalWaystoneManager.ClaimResult.Success {
        }

        private record Denied(@Nullable Component reason) implements GlobalWaystoneManager.ClaimResult.Denied {
            @Override
            public Component reasonOrDefault(){
                return this.reason == null ? TextComponents.translation("travelerswaystones.claim.denied").get() : this.reason;
            }
        }
    }
}
