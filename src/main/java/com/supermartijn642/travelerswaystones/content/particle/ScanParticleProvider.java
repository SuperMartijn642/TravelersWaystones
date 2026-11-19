package com.supermartijn642.travelerswaystones.content.particle;

import com.supermartijn642.travelerswaystones.TravelersWaystones;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SpriteSet;
import org.jetbrains.annotations.Nullable;

public class ScanParticleProvider implements ParticleProvider<ScanParticleType.Options> {

    private final SpriteSet spriteSet;

    public ScanParticleProvider(SpriteSet spriteSet){
        this.spriteSet = spriteSet;
    }

    @Override
    public @Nullable Particle createParticle(ScanParticleType.Options options, ClientLevel level, double x, double y, double z, double xAux, double yAux, double zAux){
        int delay = options.getType() == TravelersWaystones.teleportParticle && options.height() > 0 ? 6 : 0;
        return new ScanParticle(level, x, y, z, this.spriteSet.get(0, 1), options.width(), options.height(), delay);
    }
}
