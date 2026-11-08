package com.supermartijn642.travelerswaystones.content.packets;

import com.supermartijn642.core.CommonUtils;
import com.supermartijn642.core.network.BasePacket;
import com.supermartijn642.core.network.PacketContext;
import com.supermartijn642.travelerswaystones.TravelersWaystonesClient;
import com.supermartijn642.travelerswaystones.api.targets.personal.DiscoveredWaystoneTarget;
import com.supermartijn642.travelerswaystones.api.targets.personal.PersonalWaystoneManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;

public class WarnDangerousPacket implements BasePacket {

    private int id;
    private Component warning;

    public WarnDangerousPacket(int id, Component warning){
        this.id = id;
        this.warning = warning;
    }

    public WarnDangerousPacket(){
    }

    @Override
    public void write(FriendlyByteBuf buffer){
        buffer.writeVarInt(this.id);
        ComponentSerialization.STREAM_CODEC.encode(new RegistryFriendlyByteBuf(buffer, CommonUtils.getRegistryAccess()), this.warning);
    }

    @Override
    public void read(FriendlyByteBuf buffer){
        this.id = buffer.readVarInt();
        this.warning = ComponentSerialization.STREAM_CODEC.decode(new RegistryFriendlyByteBuf(buffer, CommonUtils.getRegistryAccess()));
    }

    @Override
    public void handle(PacketContext context){
        DiscoveredWaystoneTarget target = PersonalWaystoneManager.get(context.getPlayer()).getTargetForId(this.id);
        if(target == null)
            return;
        TravelersWaystonesClient.showDangerousWarning(target, this.warning);
    }
}
