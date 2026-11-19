package com.supermartijn642.travelerswaystones.content.packets;

import com.supermartijn642.core.TextComponents;
import com.supermartijn642.core.network.BasePacket;
import com.supermartijn642.core.network.PacketContext;
import com.supermartijn642.travelerswaystones.TravelersWaystones;
import com.supermartijn642.travelerswaystones.api.targets.personal.DiscoveredWaystoneTarget;
import com.supermartijn642.travelerswaystones.api.targets.personal.PersonalWaystoneManager;
import com.supermartijn642.travelerswaystones.content.WarpPendant;
import com.supermartijn642.travelerswaystones.content.targets.personal.PersonalWaystoneManagerImpl;
import net.minecraft.ChatFormatting;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

public class TeleportPacket implements BasePacket {

    private Integer fromId;
    private int toId;
    private boolean allowDangerous;
    private boolean trusted;

    public TeleportPacket(@Nullable Integer fromId, int toId, boolean allowDangerous, boolean trusted){
        this.fromId = fromId;
        this.toId = toId;
        this.allowDangerous = allowDangerous;
        this.trusted = trusted;
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
        buffer.writeBoolean(this.trusted);
    }

    @Override
    public void read(FriendlyByteBuf buffer){
        if(buffer.readBoolean())
            this.fromId = buffer.readVarInt();
        this.toId = buffer.readVarInt();
        this.allowDangerous = buffer.readBoolean();
        this.trusted = buffer.readBoolean();
    }

    @Override
    public void handle(PacketContext context){
        Player player = context.getPlayer();
        PersonalWaystoneManager manager = PersonalWaystoneManager.get(player);
        DiscoveredWaystoneTarget from = this.fromId == null ? null : manager.getTargetForId(this.fromId);
        DiscoveredWaystoneTarget to = manager.getTargetForId(this.toId);
        if(to == null)
            return;
        // The player must be at the fromId waystone, have a pendant when not teleporting from a waystone or use a trusted screen
        Identifier source;
        if(this.trusted){
            source = ((PersonalWaystoneManagerImpl)manager).getTrustedTeleportSource();
            if(source == null || this.fromId != null)
                return;
        }else if(this.fromId == null){
            if(!WarpPendant.hasPendant(player)){
                player.displayClientMessage(TextComponents.translation("travelerswaystones.teleport.no_pendant").color(ChatFormatting.RED).get(), false);
                return;
            }
            source = DiscoveredWaystoneTarget.SOURCE_PENDANT;
        }else{
            double maxDistance = player.blockInteractionRange() + 10;
            if(from == null || !from.dimension().equals(player.level().dimension())
                || from.pos().distToCenterSqr(player.position()) > maxDistance * maxDistance)
                return;
            source = DiscoveredWaystoneTarget.SOURCE_WAYSTONE;
        }
        DiscoveredWaystoneTarget.TeleportResult result = to.teleport((ServerPlayer)player, from, source, this.allowDangerous);
        switch(result){
            case DiscoveredWaystoneTarget.TeleportResult.Dangerous dangerous -> TravelersWaystones.CHANNEL.sendToPlayer(player, new WarnDangerousPacket(to.id(), dangerous.warningOrDefault()));
            case DiscoveredWaystoneTarget.TeleportResult.Success success -> {
                if(this.trusted)
                    ((PersonalWaystoneManagerImpl)manager).clearTrustedTeleportSource();
            }
            case DiscoveredWaystoneTarget.TeleportResult.NoTeleportPosition error -> player.displayClientMessage(error.reasonOrDefault(), false);
            case DiscoveredWaystoneTarget.TeleportResult.Denied error -> player.displayClientMessage(error.reasonOrDefault(), false);
            case DiscoveredWaystoneTarget.TeleportResult.Error error -> player.displayClientMessage(TextComponents.translation("travelerswaystones.teleport.error", error.reason()).color(ChatFormatting.RED).get(), false);
            default -> throw new IllegalStateException("Unexpected result: " + result.getClass().getName());
        }
    }
}
