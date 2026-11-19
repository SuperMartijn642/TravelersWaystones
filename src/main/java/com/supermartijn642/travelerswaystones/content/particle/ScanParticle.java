package com.supermartijn642.travelerswaystones.content.particle;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.supermartijn642.core.ClientUtils;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import org.joml.Quaternionf;

public class ScanParticle extends TextureSheetParticle {

    private static final double SPEED = 0.2;
    private static final int MAX_WAIT_TIME = 100;

    private final Quaternionf rotation = new Quaternionf();
    private final double width, height;
    private final float warmupTime, degradeTime;
    private int delay, waitTime;

    ScanParticle(ClientLevel level, double x, double y, double z, TextureAtlasSprite sprite, double width, double height, int delay){
        super(level, x, y - Math.signum(height) * width / 2, z);
        this.setSprite(sprite);
        this.width = width;
        this.height = height;
        this.hasPhysics = false;
        this.lifetime = (int)((Math.abs(height) + 0.2) / SPEED);
        this.quadSize = (float)this.width / 2;
        this.xd = this.zd = 0;
        this.yd = Math.signum(height) * SPEED;
        this.warmupTime = 0.5f / (float)SPEED;
        this.degradeTime = 0.5f / (float)SPEED;
        this.delay = delay;
    }

    @Override
    public void tick(){
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        // Wait for area to be rendered
        if(this.age == 0 && this.waitTime < MAX_WAIT_TIME && !this.isAreaVisible()){
            this.waitTime++;
            return;
        }
        if(this.delay > 0){
            this.delay--;
            return;
        }
        if(this.age++ >= this.lifetime){
            this.remove();
            return;
        }
        this.move(this.xd, this.yd, this.zd);
    }

    private boolean isAreaVisible(){
        return ClientUtils.getMinecraft().levelRenderer.isSectionCompiled(BlockPos.containing(this.x, this.y, this.z));
    }

    @Override
    protected int getLightColor(float a){
        return LightTexture.FULL_BRIGHT;
    }

    @Override
    public void render(VertexConsumer vertexConsumer, Camera camera, float partialTicks){
        float age = Math.max(0, this.age - 1 + partialTicks);
        if(age < this.warmupTime)
            this.alpha = Math.clamp(age / this.warmupTime, 0, 1);
        else if(this.age > this.lifetime - this.degradeTime)
            this.alpha = Math.clamp(1 - (age - (this.lifetime - this.degradeTime)) / this.degradeTime, 0, 1);
        else
            this.alpha = 1;
        this.render(vertexConsumer, camera, partialTicks, Direction.NORTH);
        this.render(vertexConsumer, camera, partialTicks, Direction.EAST);
        this.render(vertexConsumer, camera, partialTicks, Direction.SOUTH);
        this.render(vertexConsumer, camera, partialTicks, Direction.WEST);
    }

    public void render(VertexConsumer vertexConsumer, Camera camera, float partialTicks, Direction side){
        double oldOldX = this.xo, oldOldZ = this.zo;
        double oldX = this.x, oldZ = this.z;
        this.xo += side.getStepX() * this.width / 2;
        this.zo += side.getStepZ() * this.width / 2;
        this.x += side.getStepX() * this.width / 2;
        this.z += side.getStepZ() * this.width / 2;
        this.rotation.setAngleAxis(-Math.toRadians(side.toYRot()), 0, 1, 0);
        if(this.height < 0){
            if(side.getAxis() == Direction.Axis.X)
                this.rotation.rotateX(org.joml.Math.PI_f);
            else
                this.rotation.rotateZ(org.joml.Math.PI_f);
        }
        this.renderRotatedQuad(vertexConsumer, camera, this.rotation, partialTicks);
        this.rotation.rotateY(org.joml.Math.PI_f);
        this.renderRotatedQuad(vertexConsumer, camera, this.rotation, partialTicks);
        this.xo = oldOldX;
        this.zo = oldOldZ;
        this.x = oldX;
        this.z = oldZ;
    }

    @Override
    public ParticleRenderType getRenderType(){
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }
}
