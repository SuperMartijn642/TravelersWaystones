package com.supermartijn642.travelerswaystones.content;

import com.supermartijn642.travelerswaystones.TravelersWaystones;
import com.supermartijn642.travelerswaystones.api.WaystoneEvents;
import com.supermartijn642.travelerswaystones.api.WaystoneType;
import com.supermartijn642.travelerswaystones.api.targets.WaystoneTarget;
import com.supermartijn642.travelerswaystones.api.targets.personal.DiscoveredWaystoneTarget;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3dc;

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
        private static final EventHandler<WaystoneEvents.AllowClaim.Context,WaystoneEvents.AllowClaim.Result> HANDLER = new EventHandler<>(WaystoneEvents.AllowClaim.class.getSimpleName(), _ -> ALLOW);

        public static WaystoneEvents.AllowClaim.Result invoke(Player player, Level level, BlockPos pos, BlockState waystone){
            if(WaystoneType.get(waystone.getBlock()) == null)
                throw new IllegalStateException("No waystone type registered for block '" + waystone.getBlock() + "'!");
            return HANDLER.invoke(new ContextImpl(player, level, pos, waystone));
        }

        public static void addListener(Identifier identifier, Function<WaystoneEvents.AllowClaim.Context,WaystoneEvents.AllowClaim.Result> listener){
            HANDLER.addListener(identifier, listener);
        }

        public record ContextImpl(Player player, Level level, BlockPos pos, BlockState waystone) implements WaystoneEvents.AllowClaim.Context {
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

        public static void invoke(WaystoneTarget target, Level level, Player player){
            HANDLER.invoke(new ContextImpl(target, level, player));
        }

        public static void addListener(Identifier identifier, Consumer<WaystoneEvents.Claim.Context> listener){
            HANDLER.addListener(identifier, listener);
        }

        public record ContextImpl(WaystoneTarget target, Level level, Player player) implements WaystoneEvents.Claim.Context {
        }
    }

    public static final class Remove {

        private static final ResultlessEventHandler<WaystoneEvents.Remove.Context> HANDLER = new ResultlessEventHandler<>(WaystoneEvents.Remove.class.getSimpleName());

        public static void invoke(WaystoneTarget target){
            HANDLER.invoke(new ContextImpl(target));
        }

        public static void addListener(Identifier identifier, Consumer<WaystoneEvents.Remove.Context> listener){
            HANDLER.addListener(identifier, listener);
        }

        public record ContextImpl(WaystoneTarget target) implements WaystoneEvents.Remove.Context {
        }
    }

    public static final class AllowMove {

        private static final WaystoneEvents.AllowMove.Result.Allow ALLOW = new WaystoneEvents.AllowMove.Result.Allow() {};
        private static final EventHandler<WaystoneEvents.AllowMove.Context,WaystoneEvents.AllowMove.Result> HANDLER = new EventHandler<>(WaystoneEvents.AllowMove.class.getSimpleName(), _ -> ALLOW);

        public static WaystoneEvents.AllowMove.Result invoke(WaystoneTarget target, Level level, Player player, BlockHitResult to){
            return HANDLER.invoke(new ContextImpl(target, level, player, to));
        }

        public static void addListener(Identifier identifier, Function<WaystoneEvents.AllowMove.Context,WaystoneEvents.AllowMove.Result> listener){
            HANDLER.addListener(identifier, listener);
        }

        public record ContextImpl(WaystoneTarget target, Level level, Player player, BlockHitResult to) implements WaystoneEvents.AllowMove.Context {
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

        public static void invoke(WaystoneTarget target, Level level, Player player, Level oldLevel, BlockPos oldPos, BlockState oldBlock, Identifier oldBiome){
            if(WaystoneType.get(oldBlock.getBlock()) == null)
                throw new IllegalStateException("No waystone type registered for block '" + oldBlock.getBlock() + "'!");
            HANDLER.invoke(new ContextImpl(target, level, player, oldLevel, oldPos, oldBlock, oldBiome));
        }

        public static void addListener(Identifier identifier, Consumer<WaystoneEvents.Move.Context> listener){
            HANDLER.addListener(identifier, listener);
        }

        public record ContextImpl(WaystoneTarget target, Level level, Player player, Level oldLevel, BlockPos oldPos, BlockState oldBlock, Identifier oldBiome) implements WaystoneEvents.Move.Context {
        }
    }

    public static final class UpdateLocation {

        private static final ResultlessEventHandler<WaystoneEvents.UpdateLocation.Context> HANDLER = new ResultlessEventHandler<>(WaystoneEvents.UpdateLocation.class.getSimpleName());

        public static void invoke(WaystoneTarget target, Identifier oldDimension, BlockPos oldPos, BlockState oldBlock, Identifier oldBiome, boolean isClient){
            if(WaystoneType.get(oldBlock.getBlock()) == null)
                throw new IllegalStateException("No waystone type registered for block '" + oldBlock.getBlock() + "'!");
            HANDLER.invoke(new ContextImpl(target, oldDimension, oldPos, oldBlock, oldBiome, isClient));
        }

        public static void addListener(Identifier identifier, Consumer<WaystoneEvents.UpdateLocation.Context> listener){
            HANDLER.addListener(identifier, listener);
        }

        public record ContextImpl(WaystoneTarget target, Identifier oldDimension, BlockPos oldPos, BlockState oldBlock, Identifier oldBiome, boolean isClient) implements WaystoneEvents.UpdateLocation.Context {
        }
    }

    public static final class Discover {

        private static final ResultlessEventHandler<WaystoneEvents.Discover.Context> HANDLER = new ResultlessEventHandler<>(WaystoneEvents.Discover.class.getSimpleName());

        public static void invoke(DiscoveredWaystoneTarget target, Level level, Player player){
            HANDLER.invoke(new ContextImpl(target, level, player));
        }

        public static void addListener(Identifier identifier, Consumer<WaystoneEvents.Discover.Context> listener){
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

        public static void invoke(DiscoveredWaystoneTarget target, Player player){
            HANDLER.invoke(new ContextImpl(target, player));
        }

        public static void addListener(Identifier identifier, Consumer<WaystoneEvents.Undiscover.Context> listener){
            HANDLER.addListener(identifier, listener);
        }

        public record ContextImpl(DiscoveredWaystoneTarget target, Player player) implements WaystoneEvents.Undiscover.Context {
            @Override
            public boolean isClient(){
                return this.player.level().isClientSide();
            }
        }
    }

    public static final class AllowTeleport {

        private static final WaystoneEvents.AllowTeleport.Result.Allow ALLOW = new WaystoneEvents.AllowTeleport.Result.Allow() {};
        private static final EventHandler<WaystoneEvents.AllowTeleport.Context,WaystoneEvents.AllowTeleport.Result> HANDLER = new EventHandler<>(WaystoneEvents.AllowTeleport.class.getSimpleName(), _ -> ALLOW);

        public static WaystoneEvents.AllowTeleport.Result invoke(DiscoveredWaystoneTarget target, ServerPlayer player, Level level, Vector3dc position, float yaw, @Nullable DiscoveredWaystoneTarget from){
            return HANDLER.invoke(new ContextImpl(target, player, level, position, yaw, from));
        }

        public static void addListener(Identifier identifier, Function<WaystoneEvents.AllowTeleport.Context,WaystoneEvents.AllowTeleport.Result> listener){
            HANDLER.addListener(identifier, listener);
        }

        public record ContextImpl(DiscoveredWaystoneTarget target, ServerPlayer player, Level level, Vector3dc position, float yaw, @Nullable DiscoveredWaystoneTarget from) implements WaystoneEvents.AllowTeleport.Context {
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

        public static void invoke(DiscoveredWaystoneTarget target, Player player, Level level, @Nullable DiscoveredWaystoneTarget from, Level oldLevel, Vector3dc oldPosition){
            HANDLER.invoke(new ContextImpl(target, player, level, from, oldLevel, oldPosition));
        }

        public static void addListener(Identifier identifier, Consumer<WaystoneEvents.Teleport.Context> listener){
            HANDLER.addListener(identifier, listener);
        }

        public record ContextImpl(DiscoveredWaystoneTarget target, Player player, Level level, @Nullable DiscoveredWaystoneTarget from, Level oldLevel, Vector3dc oldPosition) implements WaystoneEvents.Teleport.Context {
        }
    }

    public static final class Rename {

        private static final ResultlessEventHandler<WaystoneEvents.Rename.Context> HANDLER = new ResultlessEventHandler<>(WaystoneEvents.Rename.class.getSimpleName());

        public static void invoke(WaystoneTarget target, @Nullable Player player, String oldName, boolean isClient){
            HANDLER.invoke(new ContextImpl(target, player, oldName, isClient));
        }

        public static void addListener(Identifier identifier, Consumer<WaystoneEvents.Rename.Context> listener){
            HANDLER.addListener(identifier, listener);
        }

        public record ContextImpl(WaystoneTarget target, @Nullable Player player, String oldName, boolean isClient) implements WaystoneEvents.Rename.Context {
        }
    }

    private static class EventHandler<C, R> {

        private final String name;
        private final Function<C,R> defaultResult;
        @SuppressWarnings("unchecked")
        private Function<C,R>[] listeners = new Function[0]; // Sorted by identifier
        private Identifier[] identifiers = new Identifier[0];

        private EventHandler(String name, Function<C,R> defaultResult){
            this.name = name;
            this.defaultResult = defaultResult;
        }

        public R invoke(C context){
            for(int i = 0; i < this.listeners.length; i++){
                try{
                    R result = this.listeners[i].apply(context);
                    if(result != null)
                        return result;
                }catch(Exception e){
                    TravelersWaystones.LOGGER.error("Listener '{}' for event '{}' threw an exception!", this.identifiers[i], this.name, e);
                }
            }
            return this.defaultResult.apply(context);
        }

        public synchronized void addListener(Identifier identifier, Function<C,R> listener){
            if(frozen)
                throw new IllegalStateException("Listeners must be registered during initialization!");
            for(Identifier i : this.identifiers){
                if(i.equals(identifier))
                    throw new IllegalStateException("Duplicate listeners with identifier '" + identifier + "'!");
            }
            // Find insertion index
            int insertionIndex = 0;
            while(insertionIndex < this.listeners.length && this.identifiers[insertionIndex].compareTo(identifier) < 0)
                insertionIndex++;
            // Insert
            int newLength = this.listeners.length + 1;
            //noinspection unchecked
            Function<C,R>[] listeners = new Function[newLength];
            Identifier[] identifiers = new Identifier[newLength];
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
            super(name, _ -> null);
        }

        public void addListener(Identifier identifier, Consumer<C> listener){
            this.addListener(identifier, c -> {
                listener.accept(c);
                return null;
            });
        }
    }
}
