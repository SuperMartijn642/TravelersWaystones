package com.supermartijn642.travelerswaystones.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.MeshData;
import com.supermartijn642.travelerswaystones.extension.TravelersWaystonesRenderType;
import net.minecraft.client.renderer.RenderType;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RenderType.CompositeRenderType.class)
public class RenderTypeMixin implements TravelersWaystonesRenderType {

    @Unique
    private Vector4f colorModulator;
    @Unique
    private Vector4f originalColor;

    @Override
    public void travelerswaystones_setColorModulator(Vector4f color){
        this.colorModulator = color;
    }

    @Inject(
        method = "draw",
        at = @At("HEAD")
    )
    private void applyColorModulator(MeshData meshData, CallbackInfo ci){
        if(this.colorModulator == null)
            return;
        if(this.originalColor == null)
            this.originalColor = new Vector4f();
        float[] color = RenderSystem.getShaderColor();
        this.originalColor.set(color[0], color[1], color[2], color[3]);
        RenderSystem.setShaderColor(color[0] * this.colorModulator.x, color[1] * this.colorModulator.y, color[2] * this.colorModulator.z, color[3] * this.colorModulator.w);
    }

    @Inject(
        method = "draw",
        at = @At("TAIL")
    )
    private void resetColorModulator(MeshData meshData, CallbackInfo ci){
        if(this.colorModulator != null)
            RenderSystem.setShaderColor(this.originalColor.x, this.originalColor.y, this.originalColor.z, this.originalColor.w);
    }
}
