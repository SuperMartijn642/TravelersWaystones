package com.supermartijn642.waystonesreimagined.content.particle;

import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.renderer.state.level.QuadParticleRenderState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.util.LightCoordsUtil;
import org.joml.Quaternionf;

public class ScanParticle extends SingleQuadParticle {

    private static final double SPEED = 0.2;

    private final Quaternionf rotation = new Quaternionf();
    private final double width, height;
    private final float warmupTime, degradeTime;

    ScanParticle(ClientLevel level, double x, double y, double z, TextureAtlasSprite sprite, double width, double height){
        super(level, x, y - Math.signum(height) * width / 2, z, sprite);
        this.width = width;
        this.height = height;
        this.hasPhysics = false;
        this.lifetime = (int)((Math.abs(height) + 0.2) / SPEED);
        this.quadSize = (float)this.width / 2;
        this.xd = this.zd = 0;
        this.yd = Math.signum(height) * SPEED;
        this.warmupTime = 0.5f / (float)SPEED;
        this.degradeTime = 0.5f / (float)SPEED;
    }

    @Override
    public void tick(){
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        if(this.age++ >= this.lifetime){
            this.remove();
            return;
        }
        this.move(this.xd, this.yd, this.zd);
    }

    @Override
    protected int getLightCoords(float a){
        return LightCoordsUtil.FULL_BRIGHT;
    }

    @Override
    public void extract(QuadParticleRenderState renderState, Camera camera, float partialTicks){
        float age = Math.max(0, this.age - 1 + partialTicks);
        if(age < this.warmupTime)
            this.alpha = Math.clamp(age / this.warmupTime, 0, 1);
        else if(this.age > this.lifetime - this.degradeTime)
            this.alpha = Math.clamp(1 - (age - (this.lifetime - this.degradeTime)) / this.degradeTime, 0, 1);
        else
            this.alpha = 1;
        this.extract(renderState, camera, partialTicks, Direction.NORTH);
        this.extract(renderState, camera, partialTicks, Direction.EAST);
        this.extract(renderState, camera, partialTicks, Direction.SOUTH);
        this.extract(renderState, camera, partialTicks, Direction.WEST);
    }

    public void extract(QuadParticleRenderState renderState, Camera camera, float partialTicks, Direction side){
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
        this.extractRotatedQuad(renderState, camera, this.rotation, partialTicks);
        this.rotation.rotateY(org.joml.Math.PI_f);
        this.extractRotatedQuad(renderState, camera, this.rotation, partialTicks);
        this.xo = oldOldX;
        this.zo = oldOldZ;
        this.x = oldX;
        this.z = oldZ;
    }

    @Override
    public ParticleRenderType getGroup(){
        return ParticleRenderType.SINGLE_QUADS;
    }

    @Override
    protected Layer getLayer(){
        return Layer.TRANSLUCENT;
    }
}
