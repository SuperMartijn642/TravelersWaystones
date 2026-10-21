package com.supermartijn642.waystonesreimagined.content.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.util.RandomSource;
import org.jetbrains.annotations.Nullable;

/**
 * Created 29/08/2026 by SuperMartijn642
 */
public class ScanParticleProvider implements ParticleProvider<ScanParticleType.Options> {

    private final SpriteSet spriteSet;

    public ScanParticleProvider(SpriteSet spriteSet){
        this.spriteSet = spriteSet;
    }

    @Override
    public @Nullable Particle createParticle(ScanParticleType.Options options, ClientLevel level, double x, double y, double z, double xAux, double yAux, double zAux, RandomSource random){
        return new ScanParticle(level, x, y, z, this.spriteSet.first(), options.width(), options.height());
    }
}
