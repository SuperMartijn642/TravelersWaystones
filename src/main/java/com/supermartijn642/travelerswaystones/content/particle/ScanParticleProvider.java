package com.supermartijn642.travelerswaystones.content.particle;

import com.supermartijn642.travelerswaystones.TravelersWaystones;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.util.RandomSource;
import org.jetbrains.annotations.Nullable;

public class ScanParticleProvider implements ParticleProvider<ScanParticleType.Options> {

    private final SpriteSet spriteSet;

    public ScanParticleProvider(SpriteSet spriteSet){
        this.spriteSet = spriteSet;
    }

    @Override
    public @Nullable Particle createParticle(ScanParticleType.Options options, ClientLevel level, double x, double y, double z, double xAux, double yAux, double zAux, RandomSource random){
        int delay = options.getType() == TravelersWaystones.teleportParticle && options.height() > 0 ? 6 : 0;
        return new ScanParticle(level, x, y, z, this.spriteSet.first(), options.width(), options.height(), delay);
    }
}
