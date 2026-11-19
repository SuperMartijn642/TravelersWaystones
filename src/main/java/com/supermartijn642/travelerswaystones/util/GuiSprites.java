package com.supermartijn642.travelerswaystones.util;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.supermartijn642.core.ClientUtils;
import com.supermartijn642.core.render.RenderUtils;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ARGB;
import org.joml.Matrix4f;

/**
 * Draws a part of a gui sprite, {@link com.supermartijn642.core.gui.ScreenUtils#drawGuiSprite} can only draw the whole sprite.
 */
public class GuiSprites {

    public static void draw(PoseStack poseStack, ResourceLocation sprite, float x, float y, float width, float height, float u, float v, float uWidth, float vHeight){
        draw(poseStack, sprite, x, y, width, height, u, v, uWidth, vHeight, 255);
    }

    public static void draw(PoseStack poseStack, ResourceLocation sprite, float x, float y, float width, float height, float u, float v, float uWidth, float vHeight, int alpha){
        TextureAtlasSprite atlasSprite = ClientUtils.getMinecraft().getGuiSprites().getSprite(sprite);
        float minU = atlasSprite.getU(u), maxU = atlasSprite.getU(u + uWidth);
        float minV = atlasSprite.getV(v), maxV = atlasSprite.getV(v + vHeight);
        int color = ARGB.color(alpha, 255, 255, 255);
        Matrix4f matrix = poseStack.last().pose();
        VertexConsumer buffer = RenderUtils.getMainBufferSource().getBuffer(RenderType.guiTextured(atlasSprite.atlasLocation()));
        buffer.addVertex(matrix, x, y + height, 0).setUv(minU, maxV).setColor(color);
        buffer.addVertex(matrix, x + width, y + height, 0).setUv(maxU, maxV).setColor(color);
        buffer.addVertex(matrix, x + width, y, 0).setUv(maxU, minV).setColor(color);
        buffer.addVertex(matrix, x, y, 0).setUv(minU, minV).setColor(color);
    }
}
