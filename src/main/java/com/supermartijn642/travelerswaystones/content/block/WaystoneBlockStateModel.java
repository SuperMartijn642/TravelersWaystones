package com.supermartijn642.travelerswaystones.content.block;

import com.supermartijn642.core.ClientUtils;
import com.supermartijn642.core.util.Pair;
import com.supermartijn642.travelerswaystones.api.WaystoneType;
import com.supermartijn642.travelerswaystones.content.targets.personal.PersonalWaystoneManagerImpl;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BlockModelPart;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.extensions.IForgeBlockStateModel;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.client.model.data.ModelProperty;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.BiFunction;

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
        PersonalWaystoneManagerImpl manager = (PersonalWaystoneManagerImpl)PersonalWaystoneManagerImpl.get(player, false);
        if(manager == null)
            return false;
        ResourceKey<Level> dimension = level instanceof Level ? ((Level)level).dimension() : player.level().dimension();
        return manager.isDiscoveredAt(dimension, type.getTargetPos(state, pos));
    }

    private static final ModelProperty<Pair<BlockStateModel,ModelData>> PROPER_MODEL = new ModelProperty<>();

    private final BlockStateModel original;

    public WaystoneBlockStateModel(BlockStateModel original){
        this.original = original;
    }

    @Override
    public @NotNull ModelData getModelData(@NotNull BlockAndTintGetter level, @NotNull BlockPos pos, @NotNull BlockState state, @NotNull ModelData entityData){
        if(BYPASS.get() != null)
            return ModelData.EMPTY;
        if(state.getBlock() instanceof WaystoneBlock)
            state = state.setValue(WaystoneBlock.DISCOVERED, isDiscovered(level, pos, state));
        BlockStateModel model = ClientUtils.getMinecraft().getModelManager().getBlockModelShaper().getBlockModel(state);
        BYPASS.set(true);
        try{
            return ModelData.builder()
                .with(PROPER_MODEL, Pair.of(model, model.getModelData(level, pos, state, entityData)))
                .build();
        }finally{
            BYPASS.remove();
        }
    }

    private <T> T forProperModel(ModelData modelData, BiFunction<BlockStateModel,ModelData,T> function){
        if(BYPASS.get() != null)
            return function.apply(this.original, ModelData.EMPTY);
        Pair<BlockStateModel,ModelData> properModel = modelData.get(PROPER_MODEL);
        BlockStateModel model = this;
        if(properModel != null){
            model = properModel.left();
            modelData = properModel.right();
        }
        BYPASS.set(true);
        try{
            return function.apply(model, modelData);
        }finally{
            BYPASS.remove();
        }
    }

    @Override
    public void collectParts(RandomSource random, List<BlockModelPart> output){
        this.original.collectParts(random, output);
    }

    @Override
    public void collectParts(RandomSource random, List<BlockModelPart> output, ModelData data, @Nullable RenderType renderType){
        this.forProperModel(
            data,
            (model, properData) -> {
                model.collectParts(random, output, properData, renderType);
                return null;
            }
        );
    }

    @Override
    public TextureAtlasSprite particleIcon(){
        return this.original.particleIcon();
    }

    @Override
    public TextureAtlasSprite particleIcon(@NotNull ModelData data){
        return this.forProperModel(
            data,
            IForgeBlockStateModel::particleIcon
        );
    }
}
