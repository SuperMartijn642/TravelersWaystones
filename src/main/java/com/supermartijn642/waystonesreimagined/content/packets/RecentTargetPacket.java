package com.supermartijn642.waystonesreimagined.content.packets;

import com.supermartijn642.core.network.BasePacket;
import com.supermartijn642.core.network.PacketContext;
import com.supermartijn642.waystonesreimagined.api.targets.personal.DiscoveredWaystoneTarget;
import com.supermartijn642.waystonesreimagined.api.targets.personal.PersonalWaystoneManager;
import com.supermartijn642.waystonesreimagined.content.targets.personal.PersonalWaystoneManagerImpl;
import net.minecraft.network.FriendlyByteBuf;

public class RecentTargetPacket implements BasePacket {

    private int id;

    public RecentTargetPacket(int id){
        this.id = id;
    }

    public RecentTargetPacket(){
    }

    @Override
    public void write(FriendlyByteBuf buffer){
        buffer.writeVarInt(this.id);
    }

    @Override
    public void read(FriendlyByteBuf buffer){
        this.id = buffer.readVarInt();
    }

    @Override
    public void handle(PacketContext context){
        PersonalWaystoneManager manager = PersonalWaystoneManager.get(context.getPlayer());
        DiscoveredWaystoneTarget target = manager.getTargetForId(this.id);
        if(target != null)
            ((PersonalWaystoneManagerImpl)manager).pushRecent(target);
    }
}
