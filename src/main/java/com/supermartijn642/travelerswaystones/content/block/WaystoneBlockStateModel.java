package com.supermartijn642.travelerswaystones.content.block;

import com.supermartijn642.core.ClientUtils;
import com.supermartijn642.core.util.Pair;
import com.supermartijn642.travelerswaystones.api.WaystoneType;
import com.supermartijn642.travelerswaystones.api.targets.personal.PersonalWaystoneManager;
import com.supermartijn642.travelerswaystones.content.targets.personal.PersonalWaystoneManagerImpl;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.extensions.IForgeBakedModel;
import net.minecraftforge.client.model.BakedModelWrapper;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.client.model.data.ModelProperty;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.BiFunction;

public class WaystoneBlockStateModel extends BakedModelWrapper<BakedModel> {

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

    private static final ModelProperty<Pair<BakedModel,ModelData>> PROPER_MODEL = new ModelProperty<>();

    public WaystoneBlockStateModel(BakedModel original){
        super(original);
    }

    @Override
    public @NotNull ModelData getModelData(@NotNull BlockAndTintGetter level, @NotNull BlockPos pos, @NotNull BlockState state, @NotNull ModelData entityData){
        if(BYPASS.get() != null)
            return ModelData.EMPTY;
        if(state.getBlock() instanceof WaystoneBlock)
            state = state.setValue(WaystoneBlock.DISCOVERED, isDiscovered(level, pos, state));
        BakedModel model = ClientUtils.getMinecraft().getModelManager().getBlockModelShaper().getBlockModel(state);
        BYPASS.set(true);
        try{
            return ModelData.builder()
                .with(PROPER_MODEL, Pair.of(model, model.getModelData(level, pos, state, entityData)))
                .build();
        }finally{
            BYPASS.remove();
        }
    }

    private <T> T forProperModel(ModelData modelData, BiFunction<BakedModel,ModelData,T> function){
        if(BYPASS.get() != null)
            return function.apply(this.originalModel, ModelData.EMPTY);
        Pair<BakedModel,ModelData> properModel = modelData.get(PROPER_MODEL);
        BakedModel model = this;
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
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, RandomSource random, ModelData data, @Nullable RenderType renderType){
        return this.forProperModel(
            data,
            (model, properData) -> model.getQuads(state, side, random, properData, renderType)
        );
    }

    @Override
    public TextureAtlasSprite getParticleIcon(@NotNull ModelData data){
        return this.forProperModel(
            data,
            IForgeBakedModel::getParticleIcon
        );
    }
}
