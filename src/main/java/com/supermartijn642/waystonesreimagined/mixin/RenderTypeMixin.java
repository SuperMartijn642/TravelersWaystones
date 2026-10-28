package com.supermartijn642.waystonesreimagined.mixin;

import com.supermartijn642.waystonesreimagined.extension.WaystonesReimaginedRenderType;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import org.joml.Vector4f;
import org.joml.Vector4fc;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(RenderType.class)
public class RenderTypeMixin implements WaystonesReimaginedRenderType {

    @Unique
    private Vector4f colorModulator;
    @Unique
    private Vector4f dummyColor;

    @Final
    @Shadow
    private RenderSetup state;

    @Override
    public void waystonesreimagined_setColorModulator(Vector4f color){
        this.colorModulator = color;
    }

    @ModifyArg(
        method = "draw",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/DynamicUniforms;writeTransform(Lorg/joml/Matrix4fc;Lorg/joml/Vector4fc;Lorg/joml/Vector3fc;Lorg/joml/Matrix4fc;)Lcom/mojang/blaze3d/buffers/GpuBufferSlice;"
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
