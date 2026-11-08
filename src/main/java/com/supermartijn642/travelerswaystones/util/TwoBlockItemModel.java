package com.supermartijn642.travelerswaystones.util;

import com.mojang.math.Transformation;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.supermartijn642.core.util.Pair;
import net.minecraft.client.renderer.block.dispatch.BlockModelRotation;
import net.minecraft.client.renderer.block.dispatch.ModelState;
import net.minecraft.client.renderer.item.CompositeModel;
import net.minecraft.client.renderer.item.CuboidItemModelWrapper;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.ModelRenderProperties;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ResolvedModel;
import net.minecraft.client.resources.model.geometry.QuadCollection;
import net.minecraft.client.resources.model.sprite.TextureSlots;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.model.ComposedModelState;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector3f;

import java.util.List;
import java.util.Optional;

/**
 * Scaling through item model transformations messes up the lighting, hence we manually bake the block state models with scaling.
 */
public class TwoBlockItemModel implements ItemModel.Unbaked {

    public static final MapCodec<TwoBlockItemModel> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
        Identifier.CODEC.fieldOf("top").forGetter(m -> m.top),
        Identifier.CODEC.fieldOf("bottom").forGetter(m -> m.bottom),
        Codec.FLOAT.optionalFieldOf("scale").xmap(o -> o.orElse(0.8f), s -> s == 0.8f ? Optional.empty() : Optional.of(s)).forGetter(m -> m.scale)
    ).apply(i, TwoBlockItemModel::new));

    private final Identifier top, bottom;
    private final float scale;

    public TwoBlockItemModel(Identifier top, Identifier bottom, float scale){
        this.top = top;
        this.bottom = bottom;
        this.scale = scale;
    }

    @Override
    public ItemModel bake(ItemModel.BakingContext context, Matrix4fc transformation){
        Transformation blockModelScaling = new Transformation(null, null, new Vector3f(this.scale), null);
        Pair<QuadCollection,ModelRenderProperties> top = bakeBlockStateModel(this.top, context, blockModelScaling);
        Pair<QuadCollection,ModelRenderProperties> bottom = bakeBlockStateModel(this.bottom, context, blockModelScaling);
        Matrix4f topTransform = new Matrix4f().identity().translate(0, 0.5f * this.scale, 0);
        Matrix4f bottomTransform = new Matrix4f().identity().translate(0, -0.5f * this.scale, 0);
        transformation.mul(topTransform, topTransform);
        transformation.mul(bottomTransform, bottomTransform);
        return new CompositeModel(List.of(
            new CuboidItemModelWrapper(List.of(), top.left(), top.right(), topTransform),
            new CuboidItemModelWrapper(List.of(), bottom.left(), bottom.right(), bottomTransform)
        ));
    }

    private static Pair<QuadCollection,ModelRenderProperties> bakeBlockStateModel(Identifier identifier, ItemModel.BakingContext context, Transformation transformation){
        ModelBaker baker = context.blockModelBaker();
        ResolvedModel resolvedModel = baker.getModel(identifier);
        TextureSlots textureSlots = resolvedModel.getTopTextureSlots();
        ModelState modelTransform = new ComposedModelState(BlockModelRotation.IDENTITY, transformation);
        QuadCollection quads = resolvedModel.bakeTopGeometry(textureSlots, baker, modelTransform);
        ModelRenderProperties properties = ModelRenderProperties.fromResolvedModel(baker, resolvedModel, textureSlots);
        CuboidItemModelWrapper.validateAtlasUsage(quads.getAll());
        return Pair.of(quads, properties);
    }

    @Override
    public void resolveDependencies(Resolver resolver){
        resolver.markDependency(this.top);
        resolver.markDependency(this.bottom);
    }

    @Override
    public MapCodec<? extends ItemModel.Unbaked> type(){
        return CODEC;
    }
}
