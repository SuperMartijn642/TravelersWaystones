package com.supermartijn642.travelerswaystones.util;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.supermartijn642.core.ClientUtils;
import com.supermartijn642.core.render.RenderUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;

/**
 * Draws gui sprites, Core Lib's {@link com.supermartijn642.core.gui.ScreenUtils} can only draw textures before Minecraft 1.21.2.
 */
public class GuiSprites {

    private static GuiGraphics guiGraphics;

    public static void draw(PoseStack poseStack, ResourceLocation sprite, float x, float y, float width, float height){
        if(guiGraphics == null)
            guiGraphics = new GuiGraphics(ClientUtils.getMinecraft(), RenderUtils.getMainBufferSource());
        guiGraphics.pose().pushPose();
        guiGraphics.pose().last().pose().set(poseStack.last().pose());
        RenderSystem.enableBlend();
        guiGraphics.blitSprite(sprite, (int)x, (int)y, (int)width, (int)height);
        RenderSystem.disableBlend();
        guiGraphics.pose().popPose();
    }

    public static void draw(PoseStack poseStack, ResourceLocation sprite, float x, float y, float width, float height, float u, float v, float uWidth, float vHeight){
        draw(poseStack, sprite, x, y, width, height, u, v, uWidth, vHeight, 255);
    }

    public static void draw(PoseStack poseStack, ResourceLocation sprite, float x, float y, float width, float height, float u, float v, float uWidth, float vHeight, int alpha){
        TextureAtlasSprite atlasSprite = ClientUtils.getMinecraft().getGuiSprites().getSprite(sprite);
        float minU = atlasSprite.getU(u), maxU = atlasSprite.getU(u + uWidth);
        float minV = atlasSprite.getV(v), maxV = atlasSprite.getV(v + vHeight);
        float alphaF = alpha / 255f;
        Matrix4f matrix = poseStack.last().pose();
        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        RenderSystem.setShaderTexture(0, atlasSprite.atlasLocation());
        RenderSystem.enableBlend();
        BufferBuilder buffer = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
        buffer.addVertex(matrix, x, y + height, 0).setUv(minU, maxV).setColor(1, 1, 1, alphaF);
        buffer.addVertex(matrix, x + width, y + height, 0).setUv(maxU, maxV).setColor(1, 1, 1, alphaF);
        buffer.addVertex(matrix, x + width, y, 0).setUv(maxU, minV).setColor(1, 1, 1, alphaF);
        buffer.addVertex(matrix, x, y, 0).setUv(minU, minV).setColor(1, 1, 1, alphaF);
        BufferUploader.drawWithShader(buffer.buildOrThrow());
        RenderSystem.disableBlend();
    }
}
