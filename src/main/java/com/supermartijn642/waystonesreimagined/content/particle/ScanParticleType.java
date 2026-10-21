package com.supermartijn642.waystonesreimagined.content.particle;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public class ScanParticleType extends ParticleType<ScanParticleType.Options> {

    private final MapCodec<Options> codec = RecordCodecBuilder.mapCodec(i -> i.group(
        Codec.DOUBLE.fieldOf("width").forGetter(Options::width),
        Codec.DOUBLE.fieldOf("height").forGetter(Options::height)
    ).apply(i, Options::new));
    private final StreamCodec<ByteBuf,Options> streamCodec = StreamCodec.composite(
        ByteBufCodecs.DOUBLE, Options::width,
        ByteBufCodecs.DOUBLE, Options::height,
        Options::new
    );

    public ScanParticleType(){
        super(false);
    }

    public Options create(double width, double height){
        return new Options(width, height);
    }

    @Override
    public MapCodec<Options> codec(){
        return this.codec;
    }

    @Override
    public StreamCodec<? super RegistryFriendlyByteBuf,Options> streamCodec(){
        return this.streamCodec;
    }

    public class Options implements ParticleOptions {

        private final double width, height;

        private Options(double width, double height){
            this.width = width;
            this.height = height;
        }

        public double width(){
            return this.width;
        }

        public double height(){
            return this.height;
        }

        @Override
        public ParticleType<?> getType(){
            return ScanParticleType.this;
        }
    }
}
