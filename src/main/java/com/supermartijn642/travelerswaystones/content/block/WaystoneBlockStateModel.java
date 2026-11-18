package com.supermartijn642.travelerswaystones.content.block;

import com.supermartijn642.core.ClientUtils;
import com.supermartijn642.travelerswaystones.api.WaystoneType;
import com.supermartijn642.travelerswaystones.api.targets.personal.PersonalWaystoneManager;
import com.supermartijn642.travelerswaystones.content.targets.personal.PersonalWaystoneManagerImpl;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.QuadEmitter;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Predicate;

public class WaystoneBlockStateModel implements BlockStateModel {

    private static final ThreadLocal<Boolean> BYPASS = new ThreadLocal<>();

    public static boolean isDiscovered(BlockAndTintGetter level, BlockPos pos, BlockState state){
        WaystoneType type = WaystoneType.get(state.getBlock());
        if(type == null)
            return false;
        if(state.getBlock() instanceof WaystoneBlock && state.getValue(WaystoneBlock.DISCOVERED))
            return true;
        Player player = ClientUtils.getPlayer();
        if(player == null)
            return false;
        PersonalWaystoneManager manager = PersonalWaystoneManagerImpl.get(player, false);
        if(manager == null)
            return false;
        ResourceKey<Level> dimension = level instanceof Level ? ((Level)level).dimension() : player.level().dimension();
        return manager.getTargetAt(dimension, type.getTargetPos(state, pos)) != null;
    }

    private final BlockStateModel original;

    public WaystoneBlockStateModel(BlockStateModel original){
        this.original = original;
    }

    private <T> T forProperModel(BlockAndTintGetter level, BlockPos pos, BlockState state, BiFunction<BlockStateModel,BlockState,T> function){
        if(BYPASS.get() != null)
            return function.apply(this.original, state);
        if(state.getBlock() instanceof WaystoneBlock)
            state = state.setValue(WaystoneBlock.DISCOVERED, isDiscovered(level, pos, state));
        BlockStateModel model = ClientUtils.getMinecraft().getModelManager().getBlockStateModelSet().get(state);
        BYPASS.set(true);
        try{
            return function.apply(model, state);
        }finally{
            BYPASS.remove();
        }
    }

    @Override
    public void collectParts(RandomSource random, List<BlockStateModelPart> output){
        this.original.collectParts(random, output);
    }

    @Override
    public void emitQuads(QuadEmitter emitter, BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random, Predicate<@Nullable Direction> cullTest){
        this.forProperModel(
            level, pos, state,
            (model, properState) -> {
                model.emitQuads(emitter, level, pos, properState, random, cullTest);
                return null;
            }
        );
    }

    @Override
    public @Nullable Object createGeometryKey(BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random){
        return this.forProperModel(
            level, pos, state,
            (model, properState) -> model.createGeometryKey(level, pos, properState, random)
        );
    }

    @Override
    public @BakedQuad.MaterialFlags int materialFlags(){
        return this.original.materialFlags();
    }

    @Override
    public @BakedQuad.MaterialFlags int materialFlags(BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random){
        return this.forProperModel(
            level, pos, state,
            (model, properState) -> model.materialFlags(level, pos, properState, random)
        );
    }

    @Override
    public boolean hasMaterialFlag(@BakedQuad.MaterialFlags int flag){
        return this.original.hasMaterialFlag(flag);
    }

    @Override
    public boolean hasMaterialFlag(BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random, @BakedQuad.MaterialFlags int flag){
        return this.forProperModel(
            level, pos, state,
            (model, properState) -> model.hasMaterialFlag(level, pos, properState, random, flag)
        );
    }

    @Override
    public Material.Baked particleMaterial(){
        return this.original.particleMaterial();
    }

    @Override
    public Material.Baked particleMaterial(BlockAndTintGetter level, BlockPos pos, BlockState state){
        return this.forProperModel(
            level, pos, state,
            (model, properState) -> model.particleMaterial(level, pos, properState)
        );
    }
}
