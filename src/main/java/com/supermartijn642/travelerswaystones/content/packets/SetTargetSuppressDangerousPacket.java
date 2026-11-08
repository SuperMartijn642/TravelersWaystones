package com.supermartijn642.travelerswaystones.content.packets;

import com.supermartijn642.core.network.BasePacket;
import com.supermartijn642.core.network.PacketContext;
import com.supermartijn642.travelerswaystones.api.targets.personal.DiscoveredWaystoneTarget;
import com.supermartijn642.travelerswaystones.api.targets.personal.PersonalWaystoneManager;
import net.minecraft.network.FriendlyByteBuf;

public class SetTargetSuppressDangerousPacket implements BasePacket {

    private int id;
    private boolean suppress;

    public SetTargetSuppressDangerousPacket(int id, boolean suppress){
        this.id = id;
        this.suppress = suppress;
    }

    public SetTargetSuppressDangerousPacket(){
    }

    @Override
    public void write(FriendlyByteBuf buffer){
        buffer.writeVarInt(this.id);
        buffer.writeBoolean(this.suppress);
    }

    @Override
    public void read(FriendlyByteBuf buffer){
        this.id = buffer.readVarInt();
        this.suppress = buffer.readBoolean();
    }

    @Override
    public void handle(PacketContext context){
        DiscoveredWaystoneTarget target = PersonalWaystoneManager.get(context.getPlayer()).getTargetForId(this.id);
        if(target != null && target.suppressDangerousWarning() != this.suppress)
            target.setSuppressDangerousWarning(this.suppress);
    }
}
