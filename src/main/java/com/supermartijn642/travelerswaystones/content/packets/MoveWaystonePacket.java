package com.supermartijn642.travelerswaystones.content.packets;

import com.supermartijn642.core.network.BasePacket;
import com.supermartijn642.core.network.PacketContext;
import com.supermartijn642.travelerswaystones.api.targets.GlobalWaystoneManager;
import com.supermartijn642.travelerswaystones.api.targets.WaystoneTarget;
import com.supermartijn642.travelerswaystones.content.moving.WaystoneMovingHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public class MoveWaystonePacket implements BasePacket {

    private BlockPos current;
    private BlockHitResult to;

    public MoveWaystonePacket(BlockPos current, BlockHitResult to){
        this.current = current;
        this.to = to;
    }

    public MoveWaystonePacket(){
    }

    @Override
    public void write(FriendlyByteBuf buffer){
        buffer.writeBlockPos(this.current);
        Vec3 location = this.to.getLocation();
        buffer.writeDouble(location.x);
        buffer.writeDouble(location.y);
        buffer.writeDouble(location.z);
        buffer.writeEnum(this.to.getDirection());
        buffer.writeBlockPos(this.to.getBlockPos());
        buffer.writeBoolean(this.to.isInside());
    }

    @Override
    public void read(FriendlyByteBuf buffer){
        this.current = buffer.readBlockPos();
        Vec3 location = new Vec3(
            buffer.readDouble(),
            buffer.readDouble(),
            buffer.readDouble()
        );
        Direction direction = buffer.readEnum(Direction.class);
        BlockPos pos = buffer.readBlockPos();
        boolean isInside = buffer.readBoolean();
        this.to = new BlockHitResult(location, direction, pos, isInside);
    }

    @Override
    public boolean verify(PacketContext context){
        // Check hit result location and block pos match up
        Vec3 location = this.to.getLocation();
        BlockPos pos = this.to.getBlockPos();
        return location.x >= pos.getX() - 1 && location.x <= pos.getX() + 2
            && location.y >= pos.getY() - 1 && location.y <= pos.getY() + 2
            && location.z >= pos.getZ() - 1 && location.z <= pos.getZ() + 2;
    }

    @Override
    public void handle(PacketContext context){
        // Check distance between player and hit location
        Player player = context.getPlayer();
        double reach = player.blockInteractionRange() + 3;
        Vec3 location = this.to.getLocation();
        Vec3 playerPos = player.getEyePosition();
        if(location.distanceToSqr(playerPos) > reach * reach)
            return;

        // Check waystone exists
        Level level = player.level();
        WaystoneTarget target = GlobalWaystoneManager.getTargetAt(level, this.current);
        if(target == null || !target.waystoneType().isMoveable(target.block()))
            return;
        if(!target.owner().equals(player.getUUID()))
            return;

        // Move waystone
        WaystoneMovingHandler.move((ServerLevel)level, (ServerPlayer)player, target, this.to);
    }
}
