package com.supermartijn642.travelerswaystones.content.packets;

import com.supermartijn642.core.network.BasePacket;
import com.supermartijn642.core.network.PacketContext;
import com.supermartijn642.travelerswaystones.TravelersWaystonesClient;
import com.supermartijn642.travelerswaystones.api.targets.personal.DiscoveredWaystoneTarget;
import com.supermartijn642.travelerswaystones.api.targets.personal.PersonalWaystoneManager;
import net.minecraft.network.FriendlyByteBuf;
import org.jetbrains.annotations.Nullable;

public class OpenTeleportScreenPacket implements BasePacket {

    private @Nullable Integer fromId;
    private boolean trusted;

    public OpenTeleportScreenPacket(@Nullable Integer fromId, boolean trusted){
        this.fromId = fromId;
        this.trusted = trusted;
    }

    public OpenTeleportScreenPacket(){
    }

    @Override
    public void write(FriendlyByteBuf buffer){
        buffer.writeBoolean(this.fromId != null);
        if(this.fromId != null)
            buffer.writeInt(this.fromId);
        buffer.writeBoolean(this.trusted);
    }

    @Override
    public void read(FriendlyByteBuf buffer){
        this.fromId = buffer.readBoolean() ? buffer.readInt() : null;
        this.trusted = buffer.readBoolean();
    }

    @Override
    public void handle(PacketContext context){
        DiscoveredWaystoneTarget from = null;
        if(this.fromId != null){
            from = PersonalWaystoneManager.get(context.getPlayer()).getTargetForId(this.fromId);
            if(from == null)
                return;
        }
        TravelersWaystonesClient.openTeleportScreen(from, this.trusted);
    }
}
