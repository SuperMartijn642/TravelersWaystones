package com.supermartijn642.waystonesreimagined.content.packets;

import com.supermartijn642.core.network.BasePacket;
import com.supermartijn642.core.network.PacketContext;
import com.supermartijn642.waystonesreimagined.api.targets.personal.DiscoveredWaystoneTarget;
import com.supermartijn642.waystonesreimagined.api.targets.personal.PersonalWaystoneManager;
import net.minecraft.network.FriendlyByteBuf;

public class FavoriteTargetPacket implements BasePacket {

    private int id;
    private boolean favorite;

    public FavoriteTargetPacket(int id, boolean favorite){
        this.id = id;
        this.favorite = favorite;
    }

    public FavoriteTargetPacket(){
    }

    @Override
    public void write(FriendlyByteBuf buffer){
        buffer.writeVarInt(this.id);
        buffer.writeBoolean(this.favorite);
    }

    @Override
    public void read(FriendlyByteBuf buffer){
        this.id = buffer.readVarInt();
        this.favorite = buffer.readBoolean();
    }

    @Override
    public void handle(PacketContext context){
        DiscoveredWaystoneTarget target = PersonalWaystoneManager.get(context.getPlayer()).getTargetForId(this.id);
        if(target != null)
            target.setFavorite(this.favorite);
    }
}
