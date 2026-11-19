package com.supermartijn642.travelerswaystones.util;

import com.mojang.math.Transformation;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.renderer.item.BlockModelWrapper;
import net.minecraft.client.renderer.item.CompositeModel;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.model.SimpleModelState;
import org.joml.Vector3f;

import java.util.List;
import java.util.Optional;

/**
 * Scaling through item model transformations messes up the lighting, hence we manually bake the block state models with scaling.
 */
public class TwoBlockItemModel implements ItemModel.Unbaked {

    public static final MapCodec<TwoBlockItemModel> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
        ResourceLocation.CODEC.fieldOf("top").forGetter(m -> m.top),
        ResourceLocation.CODEC.fieldOf("bottom").forGetter(m -> m.bottom),
        Codec.FLOAT.optionalFieldOf("scale").xmap(o -> o.orElse(0.8f), s -> s == 0.8f ? Optional.empty() : Optional.of(s)).forGetter(m -> m.scale)
    ).apply(i, TwoBlockItemModel::new));

    private final ResourceLocation top, bottom;
    private final float scale;

    public TwoBlockItemModel(ResourceLocation top, ResourceLocation bottom, float scale){
        this.top = top;
        this.bottom = bottom;
        this.scale = scale;
    }

    @Override
    public ItemModel bake(ItemModel.BakingContext context){
        Transformation topTransformation = new Transformation(new Vector3f(0, 0.5f * this.scale, 0), null, new Vector3f(this.scale), null);
        Transformation bottomTransformation = new Transformation(new Vector3f(0, -0.5f * this.scale, 0), null, new Vector3f(this.scale), null);
        return new CompositeModel(List.of(
            bakeBlockStateModel(this.top, context, topTransformation),
            bakeBlockStateModel(this.bottom, context, bottomTransformation)
        ));
    }

    private static ItemModel bakeBlockStateModel(ResourceLocation identifier, ItemModel.BakingContext context, Transformation transformation){
        ModelState modelTransform = new SimpleModelState(transformation);
        return new BlockModelWrapper(context.blockModelBaker().bake(identifier, modelTransform), List.of());
    }

    @Override
    public void resolveDependencies(Resolver resolver){
        resolver.resolve(this.top);
        resolver.resolve(this.bottom);
    }

    @Override
    public MapCodec<? extends ItemModel.Unbaked> type(){
        return CODEC;
    }
}
