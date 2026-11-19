package com.supermartijn642.travelerswaystones.mixin;

import com.supermartijn642.travelerswaystones.extension.TravelersWaystonesRenderType;
import net.minecraft.client.renderer.RenderType;
import org.joml.Vector4f;
import org.joml.Vector4fc;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(RenderType.CompositeRenderType.class)
public class RenderTypeMixin implements TravelersWaystonesRenderType {

    @Unique
    private Vector4f colorModulator;
    @Unique
    private Vector4f dummyColor;

    @Override
    public void travelerswaystones_setColorModulator(Vector4f color){
        this.colorModulator = color;
    }

    @ModifyArg(
        method = "draw",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/DynamicUniforms;writeTransform(Lorg/joml/Matrix4fc;Lorg/joml/Vector4fc;Lorg/joml/Vector3fc;Lorg/joml/Matrix4fc;F)Lcom/mojang/blaze3d/buffers/GpuBufferSlice;"
        ),
        index = 1
    )
    private Vector4fc modifyColorModulator(Vector4fc original){
        if(this.colorModulator == null)
            return original;
        if(this.dummyColor == null)
            this.dummyColor = new Vector4f();
        return this.dummyColor.set(original).mul(this.colorModulator);
    }
}
