package com.supermartijn642.waystonesreimagined.content.packets;

import com.supermartijn642.core.TextComponents;
import com.supermartijn642.core.network.BasePacket;
import com.supermartijn642.core.network.PacketContext;
import com.supermartijn642.waystonesreimagined.WaystonesReimagined;
import com.supermartijn642.waystonesreimagined.api.targets.personal.DiscoveredWaystoneTarget;
import com.supermartijn642.waystonesreimagined.api.targets.personal.PersonalWaystoneManager;
import net.minecraft.ChatFormatting;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

/**
 * Created 26/08/2026 by SuperMartijn642
 */
public class TeleportPacket implements BasePacket {

    private Integer fromId;
    private int toId;
    private boolean allowDangerous;

    public TeleportPacket(@Nullable Integer fromId, int toId, boolean allowDangerous){
        this.fromId = fromId;
        this.toId = toId;
        this.allowDangerous = allowDangerous;
    }

    public TeleportPacket(){
    }

    @Override
    public void write(FriendlyByteBuf buffer){
        buffer.writeBoolean(this.fromId != null);
        if(this.fromId != null)
            buffer.writeVarInt(this.fromId);
        buffer.writeVarInt(this.toId);
        buffer.writeBoolean(this.allowDangerous);
    }

    @Override
    public void read(FriendlyByteBuf buffer){
        if(buffer.readBoolean())
            this.fromId = buffer.readVarInt();
        this.toId = buffer.readVarInt();
        this.allowDangerous = buffer.readBoolean();
    }

    @Override
    public void handle(PacketContext context){
        Player player = context.getPlayer();
        PersonalWaystoneManager manager = PersonalWaystoneManager.get(player);
        DiscoveredWaystoneTarget from = this.fromId == null ? null : manager.getTargetForId(this.fromId);
        DiscoveredWaystoneTarget to = manager.getTargetForId(this.toId);
        if(to == null)
            return;
        DiscoveredWaystoneTarget.TeleportResult result = to.teleport((ServerPlayer)player, from, this.allowDangerous);
        switch(result){
            case DiscoveredWaystoneTarget.TeleportResult.Dangerous dangerous -> WaystonesReimagined.CHANNEL.sendToPlayer(player, new WarnDangerousPacket(to.id(), dangerous.warningOrDefault()));
            case DiscoveredWaystoneTarget.TeleportResult.Success _ -> {}
            case DiscoveredWaystoneTarget.TeleportResult.NoTeleportPosition error -> player.sendSystemMessage(error.reasonOrDefault());
            case DiscoveredWaystoneTarget.TeleportResult.Denied error -> player.sendSystemMessage(error.reasonOrDefault());
            case DiscoveredWaystoneTarget.TeleportResult.Error error -> player.sendSystemMessage(TextComponents.translation("waystonesreimagined.teleport.error", error.reason()).color(ChatFormatting.RED).get());
            default -> throw new IllegalStateException("Unexpected result: " + result.getClass().getName());
        }
    }
}
