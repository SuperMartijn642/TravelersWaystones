package com.supermartijn642.travelerswaystones.content.packets;

import com.supermartijn642.core.network.BasePacket;
import com.supermartijn642.core.network.PacketContext;
import com.supermartijn642.travelerswaystones.api.targets.personal.DiscoveredWaystoneTarget;
import com.supermartijn642.travelerswaystones.api.targets.personal.PersonalWaystoneManager;
import com.supermartijn642.travelerswaystones.content.WaystoneEventsImpl;
import com.supermartijn642.travelerswaystones.content.targets.WaystoneTargetImpl;
import com.supermartijn642.travelerswaystones.content.targets.personal.PersonalWaystoneManagerImpl;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class UpdateTargetPositionPacket implements BasePacket {

    private int id;
    private BlockPos newPos;
    private BlockState newBlock;
    private Identifier newBiome;

    public UpdateTargetPositionPacket(int id, BlockPos newPos, BlockState newBlock, Identifier newBiome){
        this.id = id;
        this.newPos = newPos;
        this.newBlock = newBlock;
        this.newBiome = newBiome;
    }

    public UpdateTargetPositionPacket(){
    }

    @Override
    public void write(FriendlyByteBuf buffer){
        buffer.writeVarInt(this.id);
        buffer.writeBlockPos(this.newPos);
        buffer.writeVarInt(Block.getId(this.newBlock));
        buffer.writeIdentifier(this.newBiome);
    }

    @Override
    public void read(FriendlyByteBuf buffer){
        this.id = buffer.readVarInt();
        this.newPos = buffer.readBlockPos();
        this.newBlock = Block.stateById(buffer.readVarInt());
        this.newBiome = buffer.readIdentifier();
    }

    @Override
    public void handle(PacketContext context){
        PersonalWaystoneManagerImpl manager = (PersonalWaystoneManagerImpl)PersonalWaystoneManager.get(context.getPlayer());
        DiscoveredWaystoneTarget target = manager.getTargetForId(this.id);
        if(target == null)
            return;
        ResourceKey<Level> oldDimension = target.dimension();
        BlockPos oldPos = target.pos();
        BlockState oldBlock = target.block();
        Identifier oldBiome = target.biome();
        manager.updateTargetPositionPre(target);
        ((WaystoneTargetImpl)target.target()).setBiomePosAndBlock(this.newBiome, this.newPos, this.newBlock);
        manager.updateTargetPositionPost(target);
        WaystoneEventsImpl.UpdateLocation.invoke(target.target(), oldDimension, oldPos, oldBlock, oldBiome, true);
    }
}
