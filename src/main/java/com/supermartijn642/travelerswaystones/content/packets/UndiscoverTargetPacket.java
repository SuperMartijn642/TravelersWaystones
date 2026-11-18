package com.supermartijn642.travelerswaystones.content.packets;

import com.supermartijn642.core.network.BasePacket;
import com.supermartijn642.core.network.PacketContext;
import com.supermartijn642.travelerswaystones.api.targets.personal.DiscoveredWaystoneTarget;
import com.supermartijn642.travelerswaystones.api.targets.personal.PersonalWaystoneManager;
import com.supermartijn642.travelerswaystones.content.targets.personal.PersonalWaystoneManagerImpl;
import net.minecraft.network.FriendlyByteBuf;

public class UndiscoverTargetPacket implements BasePacket {

    private int id;
    private boolean removed;

    public UndiscoverTargetPacket(int id, boolean removed){
        this.id = id;
        this.removed = removed;
    }

    public UndiscoverTargetPacket(){
    }

    @Override
    public void write(FriendlyByteBuf buffer){
        buffer.writeVarInt(this.id);
        buffer.writeBoolean(this.removed);
    }

    @Override
    public void read(FriendlyByteBuf buffer){
        this.id = buffer.readVarInt();
        this.removed = buffer.readBoolean();
    }

    @Override
    public void handle(PacketContext context){
        PersonalWaystoneManagerImpl manager = (PersonalWaystoneManagerImpl)PersonalWaystoneManager.get(context.getPlayer());
        DiscoveredWaystoneTarget target = manager.getTargetForId(this.id);
        if(target == null)
            return;
        boolean isClient = context.getHandlingSide().isClient();
        manager.undiscover(target, isClient, isClient && this.removed);
    }
}
