package com.supermartijn642.waystonesreimagined.content.targets.personal;

import com.supermartijn642.core.TextComponents;
import com.supermartijn642.core.util.teleport.TeleportTarget;
import com.supermartijn642.core.util.teleport.Teleporter;
import com.supermartijn642.waystonesreimagined.WaystonesReimagined;
import com.supermartijn642.waystonesreimagined.api.WaystoneEvents;
import com.supermartijn642.waystonesreimagined.api.WaystoneType;
import com.supermartijn642.waystonesreimagined.api.targets.GlobalWaystoneManager;
import com.supermartijn642.waystonesreimagined.api.targets.WaystoneTarget;
import com.supermartijn642.waystonesreimagined.api.targets.personal.DiscoveredWaystoneTarget;
import com.supermartijn642.waystonesreimagined.content.targets.WaystoneTargetImpl;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3d;
import org.joml.Vector3dc;
import org.joml.Vector3f;

import java.util.UUID;

/**
 * Created 21/08/2026 by SuperMartijn642
 */
public class DiscoveredWaystoneTargetImpl implements DiscoveredWaystoneTarget {

    public static void write(DiscoveredWaystoneTarget target, ValueOutput output){
        output.putInt("id", target.id());
        output.putBoolean("favorite", target.isFavorite());
        output.putBoolean("suppressDangerous", target.suppressDangerousWarning());
    }

    @Nullable
    public static DiscoveredWaystoneTarget read(ValueInput input){
        int id = input.getInt("id").orElseThrow();
        WaystoneTarget target = GlobalWaystoneManager.getTargetForId(id);
        if(target == null) // Target was removed
            return null;
        DiscoveredWaystoneTargetImpl discoveredTarget = new DiscoveredWaystoneTargetImpl(target);
        if(input.getBooleanOr("favorite", false))
            discoveredTarget.favorite = true;
        if(input.getBooleanOr("suppressDangerous", false))
            discoveredTarget.suppressDangerousWarning = true;
        return discoveredTarget;
    }

    public static void encode(DiscoveredWaystoneTarget target, FriendlyByteBuf buffer){
        WaystoneTargetImpl.encode(target.target(), buffer);
        buffer.writeBoolean(target.isFavorite());
        buffer.writeBoolean(target.suppressDangerousWarning());
    }

    public static DiscoveredWaystoneTarget decode(FriendlyByteBuf buffer){
        DiscoveredWaystoneTargetImpl target = new DiscoveredWaystoneTargetImpl(WaystoneTargetImpl.decode(buffer));
        target.favorite = buffer.readBoolean();
        target.suppressDangerousWarning = buffer.readBoolean();
        return target;
    }

    private final WaystoneTarget target;
    private PersonalWaystoneManagerImpl manager;
    private boolean favorite;
    private boolean suppressDangerousWarning;
    private boolean valid = true;

    public DiscoveredWaystoneTargetImpl(WaystoneTarget target){
        this.target = target;
    }

    public boolean isValid(){
        return this.valid;
    }

    public void invalidate(){
        this.valid = false;
    }

    public void setManager(PersonalWaystoneManagerImpl manager){
        this.manager = manager;
    }

    @Override
    public WaystoneTarget target(){
        return this.target;
    }

    @Override
    public boolean isFavorite(){
        return this.favorite;
    }

    @Override
    public void setFavorite(boolean favorite){
        this.favorite = favorite;
        this.manager.setFavorite(this, favorite);
    }

    @Override
    public boolean suppressDangerousWarning(){
        return this.suppressDangerousWarning;
    }

    @Override
    public void setSuppressDangerousWarning(boolean suppress){
        this.suppressDangerousWarning = suppress;
        this.manager.setSuppressDangerousWarning(this, suppress);
    }

    @Override
    public void undiscover(){
        this.manager.undiscover(this, false);
    }

    @Override
    public int id(){
        return this.target.id();
    }

    @Override
    public String name(){
        return this.target.name();
    }

    @Override
    public UUID owner(){
        return this.target.owner();
    }

    @Override
    public Identifier dimension(){
        return this.target.dimension();
    }

    @Override
    public Identifier biome(){
        return this.target.biome();
    }

    @Override
    public BlockPos pos(){
        return this.target.pos();
    }

    @Override
    public BlockState block(){
        return this.target.block();
    }

    @Override
    public WaystoneType waystoneType(){
        return this.target.waystoneType();
    }

    @Override
    public TeleportResult teleport(ServerPlayer player, @Nullable DiscoveredWaystoneTarget from, boolean allowDangerous){
        ServerLevel targetLevel = player.level().getServer().getLevel(ResourceKey.create(Registries.DIMENSION, this.dimension()));
        if(targetLevel == null){
            return new TeleportResultImpl.Error(TextComponents.translation(
                "waystonesreimagined.teleport.error.missing_level",
                TextComponents.string(this.dimension().toString()).color(ChatFormatting.GOLD).get()
            ).color(ChatFormatting.RED).get());
        }
        // Find suitable location
        WaystoneType.TeleportPositionResult positionResult = this.waystoneType().getTeleportPosition(this.block(), targetLevel, this.pos(), player);
        if(positionResult instanceof WaystoneType.TeleportPositionResult.Failure failure)
            return new TeleportResultImpl.NoTeleportPosition(failure.reason());
        Vector3dc teleportPos;
        if(positionResult instanceof WaystoneType.TeleportPositionResult.Dangerous dangerous){
            if(!allowDangerous)
                return new TeleportResultImpl.Dangerous(dangerous.warning());
            teleportPos = dangerous.teleportPosition();
        }else if(positionResult instanceof WaystoneType.TeleportPositionResult.Success success)
            teleportPos = success.teleportPosition();
        else
            throw new AssertionError("Unknown teleport result: " + positionResult.getClass().getName());
        float yaw = (float)Math.toDegrees(Math.atan2(this.pos().getZ() + 0.5 - teleportPos.z(), this.pos().getX() + 0.5 - teleportPos.x())) - 90;
        // Event
        WaystoneEvents.AllowTeleport.Result allowTeleport = WaystoneEvents.AllowTeleport.invoke(this, player, targetLevel, teleportPos, yaw, from);
        if(allowTeleport instanceof TeleportResult.Denied denied)
            return new TeleportResultImpl.Denied(denied.reason());
        // Teleport player
        player.closeContainer();
        ServerLevel oldLevel = player.level();
        Vec3 oldPosition = player.position();
        Teleporter.Result result = Teleporter.create(TeleportTarget.dimension(targetLevel).pos(new Vector3f(teleportPos)).headYaw(yaw))
            .includeMount(true, true, true, false)
            .includePassengers(true, false)
            .resetFallDistance()
            .teleport(player);
        if(!result.succeeded())
            return new TeleportResultImpl.Error(TextComponents.translation("waystonesreimagined.teleport.error.teleport_failed").color(ChatFormatting.RED).get());
        player = (ServerPlayer)result.entity().after();
        // Event
        WaystoneEvents.Teleport.invoke(this, player, targetLevel, from, oldLevel, new Vector3d(oldPosition.x, oldPosition.y, oldPosition.z));
        // Effects
        if(from != null)
            this.manager.pushRecent(from);
        oldLevel.playSound(null, oldPosition.x, oldPosition.y, oldPosition.z, SoundEvents.PLAYER_TELEPORT, SoundSource.PLAYERS);
        targetLevel.playSound(null, player, oldLevel == targetLevel ? SoundEvents.PLAYER_TELEPORT : SoundEvents.PORTAL_TRAVEL, SoundSource.PLAYERS, 0.5f, 1);
        float playerWidth = player.getBbWidth(), playerHeight = player.getBbHeight() + 0.2f;
        oldLevel.sendParticles(
            WaystonesReimagined.teleportParticle.create(playerWidth + 0.05, -playerHeight),
            oldPosition.x, oldPosition.y + playerHeight, oldPosition.z,
            1,
            0, 0, 0,
            0
        );
        targetLevel.sendParticles(
            WaystonesReimagined.teleportParticle.create(playerWidth + 0.05, playerHeight),
            teleportPos.x(), teleportPos.y(), teleportPos.z(),
            1,
            0, 0, 0,
            0
        );
        return new TeleportResultImpl.Success(
            player,
            targetLevel,
            teleportPos
        );
    }

    @Override
    public boolean equals(Object o){
        if(o == null || this.getClass() != o.getClass()) return false;

        DiscoveredWaystoneTargetImpl target1 = (DiscoveredWaystoneTargetImpl)o;
        return this.target.equals(target1.target);
    }

    @Override
    public int hashCode(){
        return this.target.hashCode();
    }

    private static class TeleportResultImpl {
        private record Success(Player player, Level level, Vector3dc position) implements TeleportResult.Success {
        }

        private record NoTeleportPosition(@Nullable Component reason) implements TeleportResult.NoTeleportPosition {
            @Override
            public Component reasonOrDefault(){
                return TextComponents.translation("waystonesreimagined.teleport.no_space").color(ChatFormatting.RED).get();
            }
        }

        private record Denied(@Nullable Component reason) implements TeleportResult.Denied {
            @Override
            public Component reasonOrDefault(){
                return TextComponents.translation("waystonesreimagined.teleport.denied").color(ChatFormatting.RED).get();
            }
        }

        private record Dangerous(@Nullable Component warning) implements TeleportResult.Dangerous {
            @Override
            public Component warningOrDefault(){
                return TextComponents.translation("waystonesreimagined.teleport.dangerous").get();
            }
        }

        private record Error(@Nullable Component reason) implements TeleportResult.Error {
        }
    }
}
