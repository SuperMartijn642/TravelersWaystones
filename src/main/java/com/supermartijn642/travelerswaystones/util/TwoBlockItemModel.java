package com.supermartijn642.travelerswaystones.util;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.supermartijn642.core.ClientUtils;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Scaling through item model transformations messes up the lighting, hence we manually scale the quads of the block models.
 */
public class TwoBlockItemModel implements BakedModel {

    private static final int VERTEX_SIZE = DefaultVertexFormat.BLOCK.getVertexSize() / 4;
    private static final Direction[] DIRECTIONS = {Direction.DOWN, Direction.UP, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST, null};

    private final ResourceLocation top, bottom;
    private final float scale;
    private List<BakedQuad> quads;

    public TwoBlockItemModel(ResourceLocation top, ResourceLocation bottom, float scale){
        this.top = top;
        this.bottom = bottom;
        this.scale = scale;
    }

    private BakedModel topModel(){
        return ClientUtils.getMinecraft().getModelManager().getModel(ModelResourceLocation.standalone(this.top));
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, RandomSource random){
        if(side != null)
            return List.of();
        if(this.quads == null){
            List<BakedQuad> quads = new ArrayList<>();
            this.addQuads(this.topModel(), 0.5f * this.scale, quads);
            this.addQuads(ClientUtils.getMinecraft().getModelManager().getModel(ModelResourceLocation.standalone(this.bottom)), -0.5f * this.scale, quads);
            this.quads = quads;
        }
        return this.quads;
    }

    private void addQuads(BakedModel model, float yOffset, List<BakedQuad> output){
        RandomSource random = RandomSource.create();
        for(Direction direction : DIRECTIONS){
            random.setSeed(42);
            for(BakedQuad quad : model.getQuads(null, direction, random)){
                int[] vertices = quad.getVertices().clone();
                for(int i = 0; i < 4; i++){
                    int offset = i * VERTEX_SIZE;
                    vertices[offset] = Float.floatToRawIntBits(0.5f + (Float.intBitsToFloat(vertices[offset]) - 0.5f) * this.scale);
                    vertices[offset + 1] = Float.floatToRawIntBits(0.5f + (Float.intBitsToFloat(vertices[offset + 1]) - 0.5f) * this.scale + yOffset);
                    vertices[offset + 2] = Float.floatToRawIntBits(0.5f + (Float.intBitsToFloat(vertices[offset + 2]) - 0.5f) * this.scale);
                }
                output.add(new BakedQuad(vertices, quad.getTintIndex(), quad.getDirection(), quad.getSprite(), quad.isShade()));
            }
        }
    }

    @Override
    public boolean useAmbientOcclusion(){
        return this.topModel().useAmbientOcclusion();
    }

    @Override
    public boolean isGui3d(){
        return this.topModel().isGui3d();
    }

    @Override
    public boolean usesBlockLight(){
        return this.topModel().usesBlockLight();
    }

    @Override
    public boolean isCustomRenderer(){
        return false;
    }

    @Override
    public TextureAtlasSprite getParticleIcon(){
        return this.topModel().getParticleIcon();
    }

    @Override
    public ItemTransforms getTransforms(){
        return this.topModel().getTransforms();
    }

    @Override
    public ItemOverrides getOverrides(){
        return ItemOverrides.EMPTY;
    }
}
