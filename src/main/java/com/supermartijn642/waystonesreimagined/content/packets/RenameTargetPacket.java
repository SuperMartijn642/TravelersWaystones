package com.supermartijn642.waystonesreimagined.content.packets;

import com.supermartijn642.core.network.BasePacket;
import com.supermartijn642.core.network.PacketContext;
import com.supermartijn642.waystonesreimagined.api.targets.WaystoneTarget;
import com.supermartijn642.waystonesreimagined.api.targets.personal.DiscoveredWaystoneTarget;
import com.supermartijn642.waystonesreimagined.api.targets.personal.PersonalWaystoneManager;
import com.supermartijn642.waystonesreimagined.content.targets.GlobalWaystoneManagerImpl;
import com.supermartijn642.waystonesreimagined.content.targets.WaystoneTargetImpl;
import net.minecraft.network.FriendlyByteBuf;

public class RenameTargetPacket implements BasePacket {

    private int id;
    private String name;

    public RenameTargetPacket(int id, String name){
        this.id = id;
        this.name = name;
    }

    public RenameTargetPacket(){
    }

    @Override
    public void write(FriendlyByteBuf buffer){
        buffer.writeVarInt(this.id);
        buffer.writeUtf(this.name, WaystoneTarget.MAX_NAME_LENGTH);
    }

    @Override
    public void read(FriendlyByteBuf buffer){
        this.id = buffer.readVarInt();
        this.name = buffer.readUtf(WaystoneTarget.MAX_NAME_LENGTH);
    }

    @Override
    public boolean verify(PacketContext context){
        this.name = this.name.trim();
        return this.name.length() >= WaystoneTarget.MIN_NAME_LENGTH && this.name.length() <= WaystoneTarget.MAX_NAME_LENGTH;
    }

    @Override
    public void handle(PacketContext context){
        DiscoveredWaystoneTarget target = PersonalWaystoneManager.get(context.getPlayer()).getTargetForId(this.id);
        if(target == null || !target.owner().equals(context.getPlayer().getUUID()))
            return;
        if(context.getHandlingSide().isClient())
            ((WaystoneTargetImpl)target.target()).setName(this.name);
        else
            GlobalWaystoneManagerImpl.rename(target.target(), this.name, context.getPlayer());
    }
}
