package com.supermartijn642.waystonesreimagined.content.block;

import com.supermartijn642.core.ClientUtils;
import com.supermartijn642.waystonesreimagined.api.targets.personal.PersonalWaystoneManager;
import com.supermartijn642.waystonesreimagined.content.targets.personal.PersonalWaystoneManagerImpl;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.BiFunction;

public class WaystoneBlockStateModel implements BlockStateModel {

    private static final ThreadLocal<Boolean> BYPASS = new ThreadLocal<>();

    private static boolean isDiscovered(BlockAndTintGetter level, BlockPos pos, BlockState state){
        if(!(state.getBlock() instanceof WaystoneBlock waystone))
            return false;
        if(state.getValue(WaystoneBlock.DISCOVERED))
            return true;
        Player player = ClientUtils.getPlayer();
        if(player == null)
            return false;
        PersonalWaystoneManager manager = PersonalWaystoneManagerImpl.get(player, false);
        if(manager == null)
            return false;
        Identifier dimension = level instanceof Level ? ((Level)level).dimension().identifier() : player.level().dimension().identifier();
        return manager.getTargetAt(dimension, waystone.getBasePos(pos, state)) != null;
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
    public void collectParts(BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random, List<BlockStateModelPart> parts){
        this.forProperModel(
            level, pos, state,
            (model, properState) -> {
                model.collectParts(level, pos, properState, random, parts);
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
    public @BakedQuad.MaterialFlags int materialFlags(BlockAndTintGetter level, BlockPos pos, BlockState state){
        return this.forProperModel(
            level, pos, state,
            (model, properState) -> model.materialFlags(level, pos, properState)
        );
    }

    @Override
    public boolean hasMaterialFlag(@BakedQuad.MaterialFlags int flag){
        return this.original.hasMaterialFlag(flag);
    }

    @Override
    public boolean hasMaterialFlag(BlockAndTintGetter level, BlockPos pos, BlockState state, @BakedQuad.MaterialFlags int flag){
        return this.forProperModel(
            level, pos, state,
            (model, properState) -> model.hasMaterialFlag(level, pos, properState, flag)
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
