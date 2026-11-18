package com.supermartijn642.travelerswaystones.content.targets.personal;

import com.supermartijn642.core.CommonUtils;
import com.supermartijn642.travelerswaystones.TravelersWaystones;
import com.supermartijn642.travelerswaystones.TravelersWaystonesClient;
import com.supermartijn642.travelerswaystones.api.WaystoneType;
import com.supermartijn642.travelerswaystones.api.targets.GlobalWaystoneManager;
import com.supermartijn642.travelerswaystones.api.targets.WaystoneTarget;
import com.supermartijn642.travelerswaystones.api.targets.personal.DiscoveredWaystoneTarget;
import com.supermartijn642.travelerswaystones.api.targets.personal.PersonalWaystoneManager;
import com.supermartijn642.travelerswaystones.content.WaystoneEventsImpl;
import com.supermartijn642.travelerswaystones.content.packets.*;
import com.supermartijn642.travelerswaystones.content.targets.GlobalWaystoneManagerImpl;
import com.supermartijn642.travelerswaystones.content.targets.WaystoneTargetImpl;
import com.supermartijn642.travelerswaystones.extension.TravelersWaystonesPlayer;
import com.supermartijn642.travelerswaystones.util.IndexedSet;
import com.supermartijn642.travelerswaystones.util.PartitionedSet;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ReferenceOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class PersonalWaystoneManagerImpl implements PersonalWaystoneManager {

    @Nullable
    @Contract("_,true -> !null")
    public static PersonalWaystoneManager get(Player player, boolean createIfAbsent){
        if(player.level().isClientSide() && !(player instanceof LocalPlayer))
            throw new IllegalStateException("Waystone data is only available for the client's own player!");
        return ((TravelersWaystonesPlayer)player).travelerswaystones_getWaystoneManager(createIfAbsent);
    }

    public static final int MAX_RECENTS = 6;

    private final Player player;
    private final boolean isClient;
    private final Set<DiscoveredWaystoneTarget> all = new HashSet<>(), allView = Collections.unmodifiableSet(this.all);
    private final Int2ObjectOpenHashMap<DiscoveredWaystoneTarget> byId = new Int2ObjectOpenHashMap<>(); // This is essentially the same as 'all', but I need both a lookup by id and a set of all targets
    private final IndexedSet<WaystoneTarget,DiscoveredWaystoneTarget> byGlobalTarget = new IndexedSet<>(DiscoveredWaystoneTarget::target);
    private final Set<DiscoveredWaystoneTarget> favorites = new HashSet<>(), favoritesView = Collections.unmodifiableSet(this.favorites);
    private final List<DiscoveredWaystoneTarget> recents = new ArrayList<>(), recentsView = Collections.unmodifiableList(this.recents);
    private final PartitionedSet<UUID,DiscoveredWaystoneTarget> byOwner = new PartitionedSet<>(DiscoveredWaystoneTarget::owner);
    private final Map<ResourceKey<Level>,DimensionalImpl> byDimension = new Object2ReferenceOpenHashMap<>();
    private final Map<ResourceKey<Level>,Dimensional> byDimensionView = Collections.unmodifiableMap(this.byDimension);
    private @Nullable Identifier trustedTeleportSource;

    public PersonalWaystoneManagerImpl(Player player){
        this.player = player;
        this.isClient = player.level().isClientSide();
    }

    public void setFavorite(DiscoveredWaystoneTarget target, boolean favorite){
        if(target.isFavorite() != favorite){
            target.setFavorite(favorite);
            return;
        }
        boolean changed;
        if(favorite)
            changed = this.favorites.add(target);
        else
            changed = this.favorites.remove(target);
        if(changed){
            if(this.isClient)
                TravelersWaystones.CHANNEL.sendToServer(new FavoriteTargetPacket(target.id(), favorite));
            else
                TravelersWaystones.CHANNEL.sendToPlayer(this.player, new FavoriteTargetPacket(target.id(), favorite));
        }
    }

    public void setSuppressDangerousWarning(DiscoveredWaystoneTarget target, boolean suppress){
        if(target.suppressDangerousWarning() != suppress){
            target.setSuppressDangerousWarning(suppress);
            return;
        }
        if(this.isClient)
            TravelersWaystones.CHANNEL.sendToServer(new SetTargetSuppressDangerousPacket(target.id(), suppress));
        else
            TravelersWaystones.CHANNEL.sendToPlayer(this.player, new SetTargetSuppressDangerousPacket(target.id(), suppress));
    }

    @Override
    public void openTeleportScreen(@Nullable DiscoveredWaystoneTarget from, @Nullable DiscoveredWaystoneTarget selected){
        this.checkDiscovered(from);
        this.checkDiscovered(selected);
        if(this.isClient)
            TravelersWaystonesClient.openTeleportScreen(from, selected, false);
        else
            TravelersWaystones.CHANNEL.sendToPlayer(this.player, new OpenTeleportScreenPacket(from == null ? null : from.id(), selected == null ? null : selected.id(), false));
    }

    @Override
    public void openTrustedTeleportScreen(Identifier source, @Nullable DiscoveredWaystoneTarget selected){
        if(this.isClient)
            throw new IllegalStateException("Trusted teleport screens can only be opened from the server!");
        this.checkDiscovered(selected);
        this.trustedTeleportSource = source;
        TravelersWaystones.CHANNEL.sendToPlayer(this.player, new OpenTeleportScreenPacket(null, selected == null ? null : selected.id(), true));
    }

    private void checkDiscovered(@Nullable DiscoveredWaystoneTarget target){
        if(target != null && this.getTargetForId(target.id()) != target)
            throw new IllegalArgumentException("Target '" + target.name() + "' was not discovered by player '" + this.player.getName().getString() + "'!");
    }

    @Nullable
    public Identifier getTrustedTeleportSource(){
        return this.trustedTeleportSource;
    }

    public void clearTrustedTeleportSource(){
        this.trustedTeleportSource = null;
    }

    public void pushRecent(DiscoveredWaystoneTarget target){
        this.recents.addFirst(target);
        for(int i = 1; i < this.recents.size(); i++){
            if(this.recents.get(i) == target){
                this.recents.remove(i);
                break;
            }
        }
        if(this.recents.size() > MAX_RECENTS)
            this.recents.removeLast();
        if(!this.isClient)
            TravelersWaystones.CHANNEL.sendToPlayer(this.player, new RecentTargetPacket(target.id()));
    }

    @Override
    public boolean has(WaystoneTarget target){
        return this.byGlobalTarget.containsKey(target);
    }

    @Override
    public @Nullable DiscoveredWaystoneTarget getTargetForId(int id){
        return this.byId.get(id);
    }

    @Override
    public @Nullable DiscoveredWaystoneTarget get(WaystoneTarget target){
        return this.byGlobalTarget.get(target);
    }

    @Override
    public DiscoveredWaystoneTarget discover(WaystoneTarget target, boolean showEffects){
        if(this.get(target) != null)
            throw new IllegalStateException("Target has already been discovered!");
        if(this.isClient)
            throw new AssertionError();
        DiscoveredWaystoneTarget discoveredTarget = this.add(target, showEffects);
        GlobalWaystoneManagerImpl.addDiscovered(target, this.player.getUUID());
        TravelersWaystones.CHANNEL.sendToPlayer(this.player, new DiscoverTargetPacket(target, showEffects));
        return discoveredTarget;
    }

    private void showDiscoverEffects(DiscoveredWaystoneTarget target){
        if(this.isClient){
            ClientDiscoveryEffects.showEffects(this.player, target);
            return;
        }
        Level level = CommonUtils.getLevel(target.dimension());
        if(level != null){
            BlockPos pos = target.pos();
            WaystoneType.Bounds dimensions = target.waystoneType().getBounds(target.block());
            ((ServerLevel)level).sendParticles(
                TravelersWaystones.discoverParticle.create(dimensions.width() + 0.05, dimensions.height()),
                pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5,
                1,
                0, 0, 0,
                0
            );
        }
    }

    public DiscoveredWaystoneTarget add(WaystoneTarget target, boolean showEffects){
        DiscoveredWaystoneTarget discoveredTarget = this.byGlobalTarget.get(target);
        if(discoveredTarget != null)
            return discoveredTarget;
        discoveredTarget = new DiscoveredWaystoneTargetImpl(target);
        ((DiscoveredWaystoneTargetImpl)discoveredTarget).setManager(this);
        this.all.add(discoveredTarget);
        this.byId.put(target.id(), discoveredTarget);
        this.byGlobalTarget.add(discoveredTarget);
        this.byOwner.add(discoveredTarget);
        this.forDimensionImpl(discoveredTarget.dimension()).add(discoveredTarget);
        if(this.isClient && this.player.level().dimension().equals(target.dimension())){
            BlockState state = this.player.level().getBlockState(target.pos());
            this.player.level().setBlocksDirty(target.pos(), state, Blocks.AIR.defaultBlockState());
        }
        WaystoneEventsImpl.Discover.invoke(discoveredTarget, this.player.level(), this.player);
        if(showEffects)
            this.showDiscoverEffects(discoveredTarget);
        return discoveredTarget;
    }

    public void undiscover(DiscoveredWaystoneTarget target, boolean fromServerPacket, boolean removed){
        if(!((DiscoveredWaystoneTargetImpl)target).isValid())
            throw new IllegalArgumentException("Target has already been removed!");
        ((DiscoveredWaystoneTargetImpl)target).invalidate();
        this.all.remove(target);
        this.byId.remove(target.id());
        this.byGlobalTarget.remove(target);
        if(target.isFavorite())
            this.favorites.remove(target);
        this.recents.remove(target);
        this.byOwner.remove(target);
        this.forDimensionImpl(target.dimension()).remove(target);
        if(this.isClient && this.player.level().dimension().equals(target.dimension())){
            BlockState state = this.player.level().getBlockState(target.pos());
            this.player.level().setBlocksDirty(target.pos(), state, Blocks.AIR.defaultBlockState());
        }
        if(this.isClient){
            if(!fromServerPacket)
                TravelersWaystones.CHANNEL.sendToServer(new UndiscoverTargetPacket(target.id(), false));
        }else{
            GlobalWaystoneManagerImpl.removeDiscovered(target, this.player.getUUID());
            TravelersWaystones.CHANNEL.sendToPlayer(this.player, new UndiscoverTargetPacket(target.id(), removed));
        }
        WaystoneEventsImpl.Undiscover.invoke(target, this.player, removed);
    }

    public void copyFrom(PersonalWaystoneManagerImpl manager){
        this.clear();
        this.all.addAll(manager.all);
        this.byId.putAll(manager.byId);
        this.byGlobalTarget.addAll(manager.byGlobalTarget);
        this.favorites.addAll(manager.favorites);
        this.recents.addAll(manager.recents);
        this.byOwner.addAll(manager.byOwner);
        manager.byDimension.forEach((dimension, dimensional) -> this.forDimensionImpl(dimension).copyFrom(dimensional));
        for(DiscoveredWaystoneTarget target : this.all)
            ((DiscoveredWaystoneTargetImpl)target).setManager(this);
    }

    public void updateTargetPositionPre(DiscoveredWaystoneTarget target){
        this.forDimensionImpl(target.dimension()).remove(target);
    }

    public void updateTargetPositionPost(DiscoveredWaystoneTarget target){
        this.forDimensionImpl(target.dimension()).add(target);
    }

    @Override
    public Set<DiscoveredWaystoneTarget> all(){
        return this.allView;
    }

    @Override
    public Set<DiscoveredWaystoneTarget> favorites(){
        return this.favoritesView;
    }

    @Override
    public List<DiscoveredWaystoneTarget> recents(){
        return this.recentsView;
    }

    @Override
    public Set<DiscoveredWaystoneTarget> allForOwner(UUID owner){
        return this.byOwner.get(owner);
    }

    @Override
    public Dimensional forDimension(ResourceKey<Level> dimension){
        return this.forDimensionImpl(dimension);
    }

    private DimensionalImpl forDimensionImpl(ResourceKey<Level> level){
        // As mods may hold on to the returned instance, we can't return an empty dummy as it may become outdated
        // This may lead to issues if some mod spams this for non-existent levels, but that would be that mod's fault
        return this.byDimension.computeIfAbsent(level, _ -> new DimensionalImpl());
    }

    public Map<UUID,Set<DiscoveredWaystoneTarget>> byOwner(){
        return this.byOwner.getMapView();
    }

    @Override
    public Map<ResourceKey<Level>,Dimensional> byDimension(){
        return this.byDimensionView;
    }

    private void clear(){
        this.all.clear();
        this.byId.clear();
        this.byGlobalTarget.clear();
        this.favorites.clear();
        this.recents.clear();
        this.byOwner.clear();
        // Don't discard dimensional instances as this may be called for copying player data
        this.byDimension.values().forEach(DimensionalImpl::clear);
    }

    public void write(ValueOutput output){
        if(this.isClient)
            throw new AssertionError();
        ValueOutput.ValueOutputList targets = output.childrenList("targets");
        int index = 0;
        int[] recents = new int[this.recents.size()];
        Arrays.fill(recents, -1);
        for(DiscoveredWaystoneTarget target : this.all){
            DiscoveredWaystoneTargetImpl.write(target, targets.addChild());
            int recentIndex = this.recents.indexOf(target);
            if(recentIndex != -1)
                recents[recentIndex] = index;
            index++;
        }
        for(int i : recents){
            if(i == -1)
                throw new IllegalStateException("A recents entry was missing!");
        }
        output.putIntArray("recents", recents);
    }

    public void read(ValueInput input){
        if(this.isClient)
            throw new AssertionError();
        this.clear();
        ValueInput.ValueInputList targets = input.childrenListOrEmpty("targets");
        int index = 0;
        int[] recents = input.getIntArray("recents").orElse(new int[0]);
        for(int i = 0; i < recents.length && i < MAX_RECENTS; i++)
            this.recents.add(null);
        for(ValueInput child : targets){
            int targetIndex = index++;
            DiscoveredWaystoneTarget target = DiscoveredWaystoneTargetImpl.read(child);
            if(target == null) // Target was removed
                continue;
            if(!target.target().discoverers().contains(this.player.getUUID())) // Global manager is always correct
                continue;
            ((DiscoveredWaystoneTargetImpl)target).setManager(this);
            this.all.add(target);
            this.byId.put(target.id(), target);
            this.byGlobalTarget.add(target);
            if(target.isFavorite())
                this.favorites.add(target);
            for(int i = 0; i < recents.length; i++){
                if(recents[i] == targetIndex)
                    this.recents.set(i, target);
            }
            this.byOwner.add(target);
            this.forDimensionImpl(target.dimension()).add(target);
        }
        this.populateFromGlobalManager(); // Global manager is always correct
        this.recents.removeIf(Objects::isNull);
    }

    public void populateFromGlobalManager(){
        if(this.isClient)
            throw new AssertionError();
        for(WaystoneTarget target : GlobalWaystoneManager.getAllDiscoveredBy(this.player.getUUID())){
            if(this.byGlobalTarget.containsKey(target))
                continue;
            DiscoveredWaystoneTargetImpl discoveredTarget = new DiscoveredWaystoneTargetImpl(target);
            discoveredTarget.setManager(this);
            this.all.add(discoveredTarget);
            this.byId.put(target.id(), discoveredTarget);
            this.byGlobalTarget.add(discoveredTarget);
            this.byOwner.add(discoveredTarget);
            this.forDimensionImpl(discoveredTarget.dimension()).add(discoveredTarget);
        }
    }

    public void updateContents(List<DiscoveredWaystoneTarget> targets, int[] recents){
        if(!this.isClient)
            throw new AssertionError();
        this.clear();
        this.all.addAll(targets);
        for(DiscoveredWaystoneTarget target : targets){
            ((DiscoveredWaystoneTargetImpl)target).setManager(this);
            this.byId.put(target.id(), target);
            this.byGlobalTarget.add(target);
            if(target.isFavorite())
                this.favorites.add(target);
            this.byOwner.add(target);
            this.forDimensionImpl(target.dimension()).add(target);
        }
        for(int i : recents)
            this.recents.add(targets.get(i));
    }

    private static class DimensionalImpl implements Dimensional {
        private final Set<DiscoveredWaystoneTarget> all = new ObjectOpenHashSet<>(), allView = Collections.unmodifiableSet(this.all);
        private final PartitionedSet<UUID,DiscoveredWaystoneTarget> byOwner = new PartitionedSet<>(DiscoveredWaystoneTarget::owner);
        private final PartitionedSet<ChunkPos,DiscoveredWaystoneTarget> byChunk = new PartitionedSet<>(t -> ((WaystoneTargetImpl)t.target()).chunkPos());
        private final IndexedSet<BlockPos,DiscoveredWaystoneTarget> byPos = new IndexedSet<>(DiscoveredWaystoneTarget::pos);

        private void add(DiscoveredWaystoneTarget target){
            this.all.add(target);
            this.byOwner.add(target);
            this.byChunk.add(target);
            this.byPos.add(target);
        }

        private void remove(DiscoveredWaystoneTarget target){
            this.all.remove(target);
            this.byOwner.remove(target);
            this.byChunk.remove(target);
            this.byPos.remove(target);
        }

        public void copyFrom(DimensionalImpl other){
            this.clear();
            this.all.addAll(other.all);
            this.byOwner.addAll(other.byOwner);
            this.byChunk.addAll(other.byChunk);
            this.byPos.addAll(other.byPos);
        }

        private void clear(){
            this.all.clear();
            this.byOwner.clear();
            this.byChunk.clear();
            this.byPos.clear();
        }

        @Override
        public @Nullable DiscoveredWaystoneTarget getTargetAt(BlockPos pos){
            return this.byPos.get(pos);
        }

        @Override
        public Set<DiscoveredWaystoneTarget> all(){
            return this.allView;
        }

        @Override
        public Set<DiscoveredWaystoneTarget> allForOwner(UUID owner){
            return this.byOwner.get(owner);
        }

        @Override
        public Set<DiscoveredWaystoneTarget> allForChunk(ChunkPos chunk){
            return this.byChunk.get(chunk);
        }

        @Override
        public Map<UUID,Set<DiscoveredWaystoneTarget>> byOwner(){
            return this.byOwner.getMapView();
        }

        @Override
        public Map<ChunkPos,Set<DiscoveredWaystoneTarget>> byChunk(){
            return this.byChunk.getMapView();
        }
    }
}
