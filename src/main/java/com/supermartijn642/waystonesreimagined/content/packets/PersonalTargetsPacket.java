package com.supermartijn642.waystonesreimagined.content.packets;

import com.supermartijn642.core.network.BasePacket;
import com.supermartijn642.core.network.PacketContext;
import com.supermartijn642.waystonesreimagined.api.targets.personal.DiscoveredWaystoneTarget;
import com.supermartijn642.waystonesreimagined.api.targets.personal.PersonalWaystoneManager;
import com.supermartijn642.waystonesreimagined.content.targets.personal.DiscoveredWaystoneTargetImpl;
import com.supermartijn642.waystonesreimagined.content.targets.personal.PersonalWaystoneManagerImpl;
import net.minecraft.network.FriendlyByteBuf;

import java.util.ArrayList;
import java.util.List;

/**
 * Created 22/08/2026 by SuperMartijn642
 */
public class PersonalTargetsPacket implements BasePacket {

    private List<DiscoveredWaystoneTarget> targets;
    private int[] recents;

    public PersonalTargetsPacket(PersonalWaystoneManager manager){
        this.targets = new ArrayList<>(manager.all());
        this.recents = new int[manager.recents().size()];
        int index = 0;
        for(DiscoveredWaystoneTarget target : manager.recents()){
            int i = this.targets.indexOf(target);
            if(i != -1)
                this.recents[index++] = i;
        }
    }

    public PersonalTargetsPacket(){
    }

    @Override
    public void write(FriendlyByteBuf buffer){
        buffer.writeVarInt(this.targets.size());
        for(DiscoveredWaystoneTarget target : this.targets)
            DiscoveredWaystoneTargetImpl.encode(target, buffer);
        buffer.writeVarIntArray(this.recents);
    }

    @Override
    public void read(FriendlyByteBuf buffer){
        int size = buffer.readVarInt();
        if(size > 1024)
            throw new IllegalStateException("Received more than 1024 targets!");
        this.targets = new ArrayList<>(size);
        for(int i = 0; i < size; ++i)
            this.targets.add(DiscoveredWaystoneTargetImpl.decode(buffer));
        this.recents = buffer.readVarIntArray(PersonalWaystoneManagerImpl.MAX_RECENTS);
    }

    @Override
    public boolean verify(PacketContext context){
        for(int i : this.recents){
            if(i < 0 || i >= this.targets.size())
                return false;
        }
        return true;
    }

    @Override
    public void handle(PacketContext context){
        ((PersonalWaystoneManagerImpl)PersonalWaystoneManager.get(context.getPlayer())).updateContents(this.targets, this.recents);
    }
}
