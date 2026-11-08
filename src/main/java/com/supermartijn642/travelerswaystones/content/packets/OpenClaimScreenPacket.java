package com.supermartijn642.travelerswaystones.content.packets;

import com.supermartijn642.core.CommonUtils;
import com.supermartijn642.core.network.BasePacket;
import com.supermartijn642.core.network.PacketContext;
import com.supermartijn642.travelerswaystones.TravelersWaystonesClient;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import org.jetbrains.annotations.Nullable;

public class OpenClaimScreenPacket implements BasePacket {

    private BlockPos pos;
    private boolean isNewlyPlaced;
    private @Nullable Component nameSuggestion;

    public OpenClaimScreenPacket(BlockPos pos, boolean isNewlyPlaced, @Nullable Component nameSuggestion){
        this.pos = pos;
        this.isNewlyPlaced = isNewlyPlaced;
        this.nameSuggestion = nameSuggestion;
    }

    public OpenClaimScreenPacket(){
    }

    @Override
    public void write(FriendlyByteBuf buffer){
        buffer.writeBlockPos(this.pos);
        buffer.writeBoolean(this.isNewlyPlaced);
        buffer.writeBoolean(this.nameSuggestion != null);
        if(this.nameSuggestion != null)
            ComponentSerialization.STREAM_CODEC.encode(new RegistryFriendlyByteBuf(buffer, CommonUtils.getRegistryAccess()), this.nameSuggestion);
    }

    @Override
    public void read(FriendlyByteBuf buffer){
        this.pos = buffer.readBlockPos();
        this.isNewlyPlaced = buffer.readBoolean();
        if(buffer.readBoolean())
            this.nameSuggestion = ComponentSerialization.STREAM_CODEC.decode(new RegistryFriendlyByteBuf(buffer, CommonUtils.getRegistryAccess()));
    }

    @Override
    public void handle(PacketContext context){
        TravelersWaystonesClient.openClaimScreen(this.pos, this.isNewlyPlaced, this.nameSuggestion);
    }
}
