package com.supermartijn642.travelerswaystones.content.packets;

import com.supermartijn642.core.network.BasePacket;
import com.supermartijn642.core.network.PacketContext;
import com.supermartijn642.travelerswaystones.content.targets.ClientTargetPositions;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.ChunkPos;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ChunkTargetsPacket implements BasePacket {

    private ChunkPos chunk;
    private List<BlockPos> positions;

    public ChunkTargetsPacket(ChunkPos chunk, Collection<BlockPos> positions){
        this.chunk = chunk;
        this.positions = new ArrayList<>(positions);
    }

    public ChunkTargetsPacket(){
    }

    @Override
    public void write(FriendlyByteBuf buffer){
        buffer.writeLong(this.chunk.toLong());
        buffer.writeVarInt(this.positions.size());
        for(BlockPos pos : this.positions)
            buffer.writeBlockPos(pos);
    }

    @Override
    public void read(FriendlyByteBuf buffer){
        this.chunk = new ChunkPos(buffer.readLong());
        int size = buffer.readVarInt();
        this.positions = new ArrayList<>(Math.min(size, 256));
        for(int i = 0; i < size; i++)
            this.positions.add(buffer.readBlockPos());
    }

    @Override
    public void handle(PacketContext context){
        ClientTargetPositions.setChunk(context.getPlayer().level(), this.chunk, this.positions);
    }
}
