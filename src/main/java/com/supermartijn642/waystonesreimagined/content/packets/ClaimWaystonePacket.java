package com.supermartijn642.waystonesreimagined.content.packets;

import com.supermartijn642.core.network.BasePacket;
import com.supermartijn642.core.network.PacketContext;
import com.supermartijn642.waystonesreimagined.api.targets.GlobalWaystoneManager;
import com.supermartijn642.waystonesreimagined.api.targets.WaystoneTarget;
import com.supermartijn642.waystonesreimagined.api.targets.personal.PersonalWaystoneManager;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

/**
 * Created 04/09/2026 by SuperMartijn642
 */
public class ClaimWaystonePacket implements BasePacket {

    private BlockPos pos;
    private String name;

    public ClaimWaystonePacket(BlockPos pos, String name){
        this.pos = pos;
        this.name = name;
    }

    public ClaimWaystonePacket(){
    }

    @Override
    public void write(FriendlyByteBuf buffer){
        buffer.writeBlockPos(this.pos);
        buffer.writeUtf(this.name);
    }

    @Override
    public void read(FriendlyByteBuf buffer){
        this.pos = buffer.readBlockPos();
        this.name = buffer.readUtf(WaystoneTarget.MAX_NAME_LENGTH);
    }

    @Override
    public boolean verify(PacketContext context){
        return this.name.length() >= WaystoneTarget.MIN_NAME_LENGTH && this.name.length() <= WaystoneTarget.MAX_NAME_LENGTH;
    }

    @Override
    public void handle(PacketContext context){
        Player player = context.getPlayer();
        double maxDistance = player.blockInteractionRange() + 10;
        if(player.blockPosition().distSqr(this.pos) > maxDistance * maxDistance)
            return;
        Level level = player.level();
        WaystoneTarget target = GlobalWaystoneManager.getTargetAt(level, this.pos);
        if(target != null)
            return;
        GlobalWaystoneManager.ClaimResult result = GlobalWaystoneManager.claim(player, level, this.pos, this.name);
        if(result instanceof GlobalWaystoneManager.ClaimResult.Success success)
            PersonalWaystoneManager.get(player).discover(success.target());
    }
}
