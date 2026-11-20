package com.supermartijn642.travelerswaystones.content.block;

import com.supermartijn642.core.ClientUtils;
import com.supermartijn642.travelerswaystones.api.WaystoneType;
import com.supermartijn642.travelerswaystones.content.targets.personal.PersonalWaystoneManagerImpl;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.DelegateBakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.neoforged.neoforge.client.model.data.ModelProperty;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class WaystoneBlockStateModel extends DelegateBakedModel {

    private static final ModelProperty<BlockState> PROPER_STATE = new ModelProperty<>();

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

    public WaystoneBlockStateModel(BakedModel original){
        super(original);
    }

    @Override
    public ModelData getModelData(BlockAndTintGetter level, BlockPos pos, BlockState state, ModelData modelData){
        if(state.getBlock() instanceof WaystoneBlock)
            state = state.setValue(WaystoneBlock.DISCOVERED, isDiscovered(level, pos, state));
        return modelData.derive().with(PROPER_STATE, state).build();
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, RandomSource random, ModelData data, @Nullable RenderType renderType){
        BlockState properState = data.get(PROPER_STATE);
        if(properState == null || properState == state)
            return this.parent.getQuads(state, side, random, data, renderType);
        BakedModel model = ClientUtils.getMinecraft().getModelManager().getBlockModelShaper().getBlockModel(properState);
        return model.getQuads(properState, side, random, data, renderType);
    }

    @Override
    public TextureAtlasSprite getParticleIcon(ModelData data){
        BlockState properState = data.get(PROPER_STATE);
        if(properState == null)
            return this.parent.getParticleIcon(data);
        BakedModel model = ClientUtils.getMinecraft().getModelManager().getBlockModelShaper().getBlockModel(properState);
        if(model instanceof WaystoneBlockStateModel waystoneModel)
            return waystoneModel.parent.getParticleIcon(data);
        return model.getParticleIcon(data);
    }
}
