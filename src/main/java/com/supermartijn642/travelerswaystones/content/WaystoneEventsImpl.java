package com.supermartijn642.travelerswaystones.content;

import com.supermartijn642.travelerswaystones.TravelersWaystones;
import com.supermartijn642.travelerswaystones.api.WaystoneEvents;
import com.supermartijn642.travelerswaystones.api.WaystoneType;
import com.supermartijn642.travelerswaystones.api.targets.WaystoneTarget;
import com.supermartijn642.travelerswaystones.api.targets.personal.DiscoveredWaystoneTarget;
import com.supermartijn642.travelerswaystones.api.targets.personal.PersonalWaystoneManager;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3dc;

import java.util.Comparator;
import java.util.function.Consumer;
import java.util.function.Function;

public class WaystoneEventsImpl {

    private static boolean frozen = false;

    public static void freeze(){
        if(frozen)
            throw new IllegalStateException("Waystone registry has already been finalized!");
        synchronized(WaystoneTypeRegistry.class){
            if(frozen)
                throw new IllegalStateException("Waystone registry has already been finalized!");
            frozen = true;
        }
    }

    public static final class AllowClaim {

        private static final WaystoneEvents.AllowClaim.Result.Allow ALLOW = new WaystoneEvents.AllowClaim.Result.Allow() {};
        private static final EventHandler<WaystoneEvents.AllowClaim.Context,WaystoneEvents.AllowClaim.Result> HANDLER = new EventHandler<>(WaystoneEvents.AllowClaim.class.getSimpleName(), o -> ALLOW);

        public static WaystoneEvents.AllowClaim.Result invoke(ServerPlayer player, ServerLevel level, BlockPos pos, BlockState waystone){
            if(WaystoneType.get(waystone.getBlock()) == null)
                throw new IllegalStateException("No waystone type registered for block '" + waystone.getBlock() + "'!");
            return HANDLER.invoke(new ContextImpl(player, level, pos, waystone));
        }

        public static void addListener(ResourceLocation identifier, Function<WaystoneEvents.AllowClaim.Context,WaystoneEvents.AllowClaim.Result> listener){
            HANDLER.addListener(identifier, listener);
        }

        public record ContextImpl(ServerPlayer player, ServerLevel level, BlockPos pos, BlockState waystone) implements WaystoneEvents.AllowClaim.Context {
        }

        public static WaystoneEvents.AllowClaim.Result.Allow allow(){
            return ALLOW;
        }

        public static WaystoneEvents.AllowClaim.Result.Deny deny(@Nullable Component reason){
            return () -> reason;
        }
    }

    public static final class Claim {

        private static final ResultlessEventHandler<WaystoneEvents.Claim.Context> HANDLER = new ResultlessEventHandler<>(WaystoneEvents.Claim.class.getSimpleName());

        public static void invoke(WaystoneTarget target, ServerLevel level, @Nullable ServerPlayer player){
            HANDLER.invoke(new ContextImpl(target, level, player));
        }

        public static void addListener(ResourceLocation identifier, Consumer<WaystoneEvents.Claim.Context> listener){
            HANDLER.addListener(identifier, listener);
        }

        public record ContextImpl(WaystoneTarget target, ServerLevel level, @Nullable ServerPlayer player) implements WaystoneEvents.Claim.Context {
        }
    }

    public static final class Remove {

        private static final ResultlessEventHandler<WaystoneEvents.Remove.Context> HANDLER = new ResultlessEventHandler<>(WaystoneEvents.Remove.class.getSimpleName());

        public static void invoke(WaystoneTarget target){
            HANDLER.invoke(new ContextImpl(target));
        }

        public static void addListener(ResourceLocation identifier, Consumer<WaystoneEvents.Remove.Context> listener){
            HANDLER.addListener(identifier, listener);
        }

        public record ContextImpl(WaystoneTarget target) implements WaystoneEvents.Remove.Context {
        }
    }

    public static final class AllowMove {

        private static final WaystoneEvents.AllowMove.Result.Allow ALLOW = new WaystoneEvents.AllowMove.Result.Allow() {};
        private static final EventHandler<WaystoneEvents.AllowMove.Context,WaystoneEvents.AllowMove.Result> HANDLER = new EventHandler<>(WaystoneEvents.AllowMove.class.getSimpleName(), o -> ALLOW);

        public static WaystoneEvents.AllowMove.Result invoke(WaystoneTarget target, ServerLevel level, ServerPlayer player, BlockHitResult to){
            return HANDLER.invoke(new ContextImpl(target, level, player, to));
        }

        public static void addListener(ResourceLocation identifier, Function<WaystoneEvents.AllowMove.Context,WaystoneEvents.AllowMove.Result> listener){
            HANDLER.addListener(identifier, listener);
        }

        public record ContextImpl(WaystoneTarget target, ServerLevel level, ServerPlayer player, BlockHitResult to) implements WaystoneEvents.AllowMove.Context {
        }

        public static WaystoneEvents.AllowMove.Result.Allow allow(){
            return ALLOW;
        }

        public static WaystoneEvents.AllowMove.Result.Deny deny(@Nullable Component reason){
            return () -> reason;
        }
    }

    public static final class Move {

        private static final ResultlessEventHandler<WaystoneEvents.Move.Context> HANDLER = new ResultlessEventHandler<>(WaystoneEvents.Move.class.getSimpleName());

        public static void invoke(WaystoneTarget target, ServerLevel level, ServerPlayer player, ServerLevel oldLevel, BlockPos oldPos, BlockState oldBlock, ResourceKey<Biome> oldBiome){
            if(WaystoneType.get(oldBlock.getBlock()) == null)
                throw new IllegalStateException("No waystone type registered for block '" + oldBlock.getBlock() + "'!");
            HANDLER.invoke(new ContextImpl(target, level, player, oldLevel, oldPos, oldBlock, oldBiome));
        }

        public static void addListener(ResourceLocation identifier, Consumer<WaystoneEvents.Move.Context> listener){
            HANDLER.addListener(identifier, listener);
        }

        public record ContextImpl(WaystoneTarget target, ServerLevel level, ServerPlayer player, ServerLevel oldLevel, BlockPos oldPos, BlockState oldBlock, ResourceKey<Biome> oldBiome) implements WaystoneEvents.Move.Context {
        }
    }

    public static final class UpdateLocation {

        private static final ResultlessEventHandler<WaystoneEvents.UpdateLocation.Context> HANDLER = new ResultlessEventHandler<>(WaystoneEvents.UpdateLocation.class.getSimpleName());

        public static void invoke(WaystoneTarget target, ResourceKey<Level> oldDimension, BlockPos oldPos, BlockState oldBlock, ResourceKey<Biome> oldBiome, boolean isClient){
            if(WaystoneType.get(oldBlock.getBlock()) == null)
                throw new IllegalStateException("No waystone type registered for block '" + oldBlock.getBlock() + "'!");
            HANDLER.invoke(new ContextImpl(target, oldDimension, oldPos, oldBlock, oldBiome, isClient));
        }

        public static void addListener(ResourceLocation identifier, Consumer<WaystoneEvents.UpdateLocation.Context> listener){
            HANDLER.addListener(identifier, listener);
        }

        public record ContextImpl(WaystoneTarget target, ResourceKey<Level> oldDimension, BlockPos oldPos, BlockState oldBlock, ResourceKey<Biome> oldBiome, boolean isClient) implements WaystoneEvents.UpdateLocation.Context {
        }
    }

    public static final class AllowDiscover {

        private static final WaystoneEvents.AllowDiscover.Result.Allow ALLOW = new WaystoneEvents.AllowDiscover.Result.Allow() {};
        private static final EventHandler<WaystoneEvents.AllowDiscover.Context,WaystoneEvents.AllowDiscover.Result> HANDLER = new EventHandler<>(WaystoneEvents.AllowDiscover.class.getSimpleName(), o -> ALLOW);

        public static WaystoneEvents.AllowDiscover.Result invoke(ServerPlayer player, WaystoneTarget target){
            return HANDLER.invoke(new ContextImpl(player, target));
        }

        public static void addListener(ResourceLocation identifier, Function<WaystoneEvents.AllowDiscover.Context,WaystoneEvents.AllowDiscover.Result> listener){
            HANDLER.addListener(identifier, listener);
        }

        public record ContextImpl(ServerPlayer player, WaystoneTarget target) implements WaystoneEvents.AllowDiscover.Context {
        }

        public static WaystoneEvents.AllowDiscover.Result.Allow allow(){
            return ALLOW;
        }

        public static WaystoneEvents.AllowDiscover.Result.Deny deny(@Nullable Component reason){
            return () -> reason;
        }
    }

    public static final class Discover {

        private static final ResultlessEventHandler<WaystoneEvents.Discover.Context> HANDLER = new ResultlessEventHandler<>(WaystoneEvents.Discover.class.getSimpleName());

        public static void invoke(DiscoveredWaystoneTarget target, Level level, Player player){
            HANDLER.invoke(new ContextImpl(target, level, player));
        }

        public static void addListener(ResourceLocation identifier, Consumer<WaystoneEvents.Discover.Context> listener){
            HANDLER.addListener(identifier, listener);
        }

        public record ContextImpl(DiscoveredWaystoneTarget target, Level level, Player player) implements WaystoneEvents.Discover.Context {
            @Override
            public boolean isClient(){
                return this.level.isClientSide();
            }
        }
    }

    public static final class Undiscover {

        private static final ResultlessEventHandler<WaystoneEvents.Undiscover.Context> HANDLER = new ResultlessEventHandler<>(WaystoneEvents.Undiscover.class.getSimpleName());

        public static void invoke(DiscoveredWaystoneTarget target, Player player, boolean removed){
            HANDLER.invoke(new ContextImpl(target, player, removed));
        }

        public static void addListener(ResourceLocation identifier, Consumer<WaystoneEvents.Undiscover.Context> listener){
            HANDLER.addListener(identifier, listener);
        }

        public record ContextImpl(DiscoveredWaystoneTarget target, Player player, boolean removed) implements WaystoneEvents.Undiscover.Context {
            @Override
            public boolean isClient(){
                return this.player.level().isClientSide();
            }
        }
    }

    public static final class AllowTeleport {

        private static final WaystoneEvents.AllowTeleport.Result.Allow ALLOW = new WaystoneEvents.AllowTeleport.Result.Allow() {};
        private static final EventHandler<WaystoneEvents.AllowTeleport.Context,WaystoneEvents.AllowTeleport.Result> HANDLER = new EventHandler<>(WaystoneEvents.AllowTeleport.class.getSimpleName(), o -> ALLOW);

        public static WaystoneEvents.AllowTeleport.Result invoke(DiscoveredWaystoneTarget target, ServerPlayer player, ServerLevel level, Vector3dc position, float yaw, @Nullable DiscoveredWaystoneTarget from, ResourceLocation source){
            return HANDLER.invoke(new ContextImpl(target, player, level, position, yaw, from, source));
        }

        public static void addListener(ResourceLocation identifier, Function<WaystoneEvents.AllowTeleport.Context,WaystoneEvents.AllowTeleport.Result> listener){
            HANDLER.addListener(identifier, listener);
        }

        public record ContextImpl(DiscoveredWaystoneTarget target, ServerPlayer player, ServerLevel level, Vector3dc position, float yaw, @Nullable DiscoveredWaystoneTarget from, ResourceLocation source) implements WaystoneEvents.AllowTeleport.Context {
        }

        public static WaystoneEvents.AllowTeleport.Result.Allow allow(){
            return ALLOW;
        }

        public static WaystoneEvents.AllowTeleport.Result.Deny deny(@Nullable Component reason){
            return () -> reason;
        }
    }

    public static final class Teleport {

        private static final ResultlessEventHandler<WaystoneEvents.Teleport.Context> HANDLER = new ResultlessEventHandler<>(WaystoneEvents.Teleport.class.getSimpleName());

        public static void invoke(DiscoveredWaystoneTarget target, ServerPlayer player, ServerLevel level, @Nullable DiscoveredWaystoneTarget from, ServerLevel oldLevel, Vector3dc oldPosition, ResourceLocation source){
            HANDLER.invoke(new ContextImpl(target, player, level, from, oldLevel, oldPosition, source));
        }

        public static void addListener(ResourceLocation identifier, Consumer<WaystoneEvents.Teleport.Context> listener){
            HANDLER.addListener(identifier, listener);
        }

        public record ContextImpl(DiscoveredWaystoneTarget target, ServerPlayer player, ServerLevel level, @Nullable DiscoveredWaystoneTarget from, ServerLevel oldLevel, Vector3dc oldPosition, ResourceLocation source) implements WaystoneEvents.Teleport.Context {
        }
    }

    public static final class Rename {

        private static final ResultlessEventHandler<WaystoneEvents.Rename.Context> HANDLER = new ResultlessEventHandler<>(WaystoneEvents.Rename.class.getSimpleName());

        public static void invoke(WaystoneTarget target, @Nullable Player player, String oldName, boolean isClient){
            HANDLER.invoke(new ContextImpl(target, player, oldName, isClient));
        }

        public static void addListener(ResourceLocation identifier, Consumer<WaystoneEvents.Rename.Context> listener){
            HANDLER.addListener(identifier, listener);
        }

        public record ContextImpl(WaystoneTarget target, @Nullable Player player, String oldName, boolean isClient) implements WaystoneEvents.Rename.Context {
        }
    }

    public static final class Sync {

        private static final ResultlessEventHandler<WaystoneEvents.Sync.Context> HANDLER = new ResultlessEventHandler<>(WaystoneEvents.Sync.class.getSimpleName());

        public static void invoke(Player player, PersonalWaystoneManager manager){
            HANDLER.invoke(new ContextImpl(player, manager));
        }

        public static void addListener(ResourceLocation identifier, Consumer<WaystoneEvents.Sync.Context> listener){
            HANDLER.addListener(identifier, listener);
        }

        public record ContextImpl(Player player, PersonalWaystoneManager manager) implements WaystoneEvents.Sync.Context {
        }
    }

    private static class EventHandler<C, R> {

        // ResourceLocation#compareTo compares the path before the namespace
        private static final Comparator<ResourceLocation> IDENTIFIER_ORDER = Comparator.comparing(ResourceLocation::getNamespace).thenComparing(ResourceLocation::getPath);

        private final String name;
        private final Function<C,R> defaultResult;
        @SuppressWarnings("unchecked")
        private Function<C,R>[] listeners = new Function[0]; // Sorted by identifier
        private ResourceLocation[] identifiers = new ResourceLocation[0];

        private EventHandler(String name, Function<C,R> defaultResult){
            this.name = name;
            this.defaultResult = defaultResult;
        }

        public R invoke(C context){
            R defaultResult = this.defaultResult.apply(context);
            for(int i = 0; i < this.listeners.length; i++){
                try{
                    R result = this.listeners[i].apply(context);
                    // Allow results should not stop other listeners from denying
                    if(result != null && result != defaultResult)
                        return result;
                }catch(Exception e){
                    TravelersWaystones.LOGGER.error("Listener '{}' for event '{}' threw an exception!", this.identifiers[i], this.name, e);
                }
            }
            return defaultResult;
        }

        public synchronized void addListener(ResourceLocation identifier, Function<C,R> listener){
            if(frozen)
                throw new IllegalStateException("Listeners must be registered during initialization!");
            for(ResourceLocation i : this.identifiers){
                if(i.equals(identifier))
                    throw new IllegalStateException("Duplicate listeners with identifier '" + identifier + "'!");
            }
            // Find insertion index
            int insertionIndex = 0;
            while(insertionIndex < this.listeners.length && IDENTIFIER_ORDER.compare(this.identifiers[insertionIndex], identifier) < 0)
                insertionIndex++;
            // Insert
            int newLength = this.listeners.length + 1;
            //noinspection unchecked
            Function<C,R>[] listeners = new Function[newLength];
            ResourceLocation[] identifiers = new ResourceLocation[newLength];
            System.arraycopy(this.listeners, 0, listeners, 0, insertionIndex);
            System.arraycopy(this.identifiers, 0, identifiers, 0, insertionIndex);
            listeners[insertionIndex] = listener;
            identifiers[insertionIndex] = identifier;
            System.arraycopy(this.listeners, insertionIndex, listeners, insertionIndex + 1, newLength - insertionIndex - 1);
            System.arraycopy(this.identifiers, insertionIndex, identifiers, insertionIndex + 1, newLength - insertionIndex - 1);
            this.listeners = listeners;
            this.identifiers = identifiers;
        }
    }

    private static final class ResultlessEventHandler<C> extends EventHandler<C,Void> {
        private ResultlessEventHandler(String name){
            super(name, o -> null);
        }

        public void addListener(ResourceLocation identifier, Consumer<C> listener){
            this.addListener(identifier, c -> {
                listener.accept(c);
                return null;
            });
        }
    }
}
