package com.supermartijn642.travelerswaystones.content.packets;

import com.supermartijn642.core.TextComponents;
import com.supermartijn642.core.network.BasePacket;
import com.supermartijn642.core.network.PacketContext;
import com.supermartijn642.travelerswaystones.api.WaystoneType;
import com.supermartijn642.travelerswaystones.api.targets.GlobalWaystoneManager;
import com.supermartijn642.travelerswaystones.api.targets.WaystoneTarget;
import com.supermartijn642.travelerswaystones.api.targets.personal.PersonalWaystoneManager;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

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
        this.name = buffer.readUtf(WaystoneTarget.MAX_NAME_LENGTH).trim();
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
        BlockState state = level.getBlockState(this.pos);
        WaystoneType type = WaystoneType.get(state.getBlock());
        if(type == null || !type.getTargetPos(state, this.pos).equals(this.pos))
            return;
        WaystoneTarget target = GlobalWaystoneManager.getTargetAt(level, this.pos);
        if(target != null)
            return;
        GlobalWaystoneManager.ClaimResult result = GlobalWaystoneManager.claim((ServerPlayer)player, (ServerLevel)level, this.pos, this.name);
        if(result instanceof GlobalWaystoneManager.ClaimResult.Success success)
            PersonalWaystoneManager.get(player).discover(success.target());
        else if(result instanceof GlobalWaystoneManager.ClaimResult.Denied denied)
            player.displayClientMessage(TextComponents.fromTextComponent(denied.reasonOrDefault()).color(ChatFormatting.RED).get(), true);
    }
}
