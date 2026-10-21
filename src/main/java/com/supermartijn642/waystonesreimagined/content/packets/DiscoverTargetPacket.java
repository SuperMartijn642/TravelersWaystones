package com.supermartijn642.waystonesreimagined.content.packets;

import com.supermartijn642.core.network.BasePacket;
import com.supermartijn642.core.network.PacketContext;
import com.supermartijn642.waystonesreimagined.api.targets.WaystoneTarget;
import com.supermartijn642.waystonesreimagined.api.targets.personal.PersonalWaystoneManager;
import com.supermartijn642.waystonesreimagined.content.targets.WaystoneTargetImpl;
import com.supermartijn642.waystonesreimagined.content.targets.personal.PersonalWaystoneManagerImpl;
import net.minecraft.network.FriendlyByteBuf;

/**
 * Created 22/08/2026 by SuperMartijn642
 */
public class DiscoverTargetPacket implements BasePacket {

    private WaystoneTarget target;
    private boolean showEffects;

    public DiscoverTargetPacket(WaystoneTarget target, boolean showEffects){
        this.target = target;
        this.showEffects = showEffects;
    }

    public DiscoverTargetPacket(){
    }

    @Override
    public void write(FriendlyByteBuf buffer){
        WaystoneTargetImpl.encode(this.target, buffer);
        buffer.writeBoolean(this.showEffects);
    }

    @Override
    public void read(FriendlyByteBuf buffer){
        this.target = WaystoneTargetImpl.decode(buffer);
        this.showEffects = buffer.readBoolean();
    }

    @Override
    public void handle(PacketContext context){
        ((PersonalWaystoneManagerImpl)PersonalWaystoneManager.get(context.getPlayer())).add(this.target, this.showEffects);
    }
}
