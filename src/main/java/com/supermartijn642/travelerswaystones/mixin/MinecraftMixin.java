package com.supermartijn642.travelerswaystones.mixin;

import com.supermartijn642.core.ClientUtils;
import com.supermartijn642.core.TextComponents;
import com.supermartijn642.travelerswaystones.api.WaystoneType;
import com.supermartijn642.travelerswaystones.api.targets.personal.DiscoveredWaystoneTarget;
import com.supermartijn642.travelerswaystones.api.targets.personal.PersonalWaystoneManager;
import com.supermartijn642.travelerswaystones.content.WaystoneEventsImpl;
import com.supermartijn642.travelerswaystones.content.WaystoneTypeRegistry;
import com.supermartijn642.travelerswaystones.content.block.WaystoneTypeImpl;
import com.supermartijn642.travelerswaystones.content.moving.WaystoneMovingHandler;
import com.supermartijn642.travelerswaystones.content.moving.WaystoneMovingHandlerClient;
import com.supermartijn642.travelerswaystones.content.targets.ClientTargetPositions;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Minecraft.class)
public class MinecraftMixin {

    @Unique
    private int waystoneMineWarningDelay = 0;
    @Shadow
    private int missTime;

    @Inject(
        method = "<init>",
        at = @At(
            value = "INVOKE",
            target = "Lnet/neoforged/neoforge/client/loading/ClientModLoader;begin(Lnet/minecraft/client/Minecraft;Lnet/minecraft/server/packs/repository/PackRepository;Lnet/minecraft/server/packs/resources/ReloadableResourceManager;)V",
            shift = At.Shift.AFTER
        )
    )
    private void freezeWaystoneRegistry(CallbackInfo ci){
        WaystoneTypeRegistry.freeze();
        WaystoneEventsImpl.freeze();
    }

    @Inject(
        method = "runTick",
        at = @At("HEAD")
    )
    private void runTick(boolean advanceGameTime, CallbackInfo ci){
        WaystoneMovingHandlerClient.update();
    }

    @Inject(
        method = "handleKeybinds",
        at = @At("HEAD")
    )
    private void handleKeybinds(CallbackInfo ci){
        WaystoneMovingHandlerClient.handleInputs();
    }

    @ModifyArg(
        method = "handleKeybinds",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/Minecraft;continueAttack(Z)V"
        )
    )
    private boolean cancelAttack(boolean original){
        return original && !WaystoneMovingHandlerClient.isMoving();
    }

    @Inject(
        method = "disconnect(Lnet/minecraft/client/gui/screens/Screen;Z)V",
        at = @At("TAIL")
    )
    private void disconnect(Screen screen, boolean keepResourcePacks, CallbackInfo ci){
        ClientTargetPositions.clear();
    }

    @Inject(
        method = "clearClientLevel",
        at = @At("TAIL")
    )
    private void clearClientLevel(Screen screen, CallbackInfo ci){
        ClientTargetPositions.clear();
    }

    @Inject(
        method = "tick()V",
        at = @At("HEAD")
    )
    public void tick(CallbackInfo ci){
        if(this.waystoneMineWarningDelay > 0)
            this.waystoneMineWarningDelay--;
    }

    @Inject(
        method = "startAttack()Z",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/multiplayer/MultiPlayerGameMode;startDestroyBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/Direction;)Z",
            shift = At.Shift.BEFORE
        ),
        cancellable = true
    )
    private void cancelLeftClickWaystone(CallbackInfoReturnable<Boolean> cir){
        Player player = ClientUtils.getPlayer();
        BlockHitResult result = (BlockHitResult)ClientUtils.getMinecraft().hitResult;
        BlockPos pos = result.getBlockPos();
        ClientLevel level = ClientUtils.getMinecraft().level;
        if(!level.getWorldBorder().isWithinBounds(pos))
            return;
        BlockState state = level.getBlockState(pos);
        if(!WaystoneTypeImpl.isBreakProtected(player, level, pos, state))
            return;
        pos = WaystoneType.get(state.getBlock()).getTargetPos(state, pos);
        if(this.waystoneMineWarningDelay <= 0){
            DiscoveredWaystoneTarget target = PersonalWaystoneManager.get(player).getTargetAt(level, pos);
            boolean isOwner = target != null && target.owner().equals(player.getUUID());
            String messageKey = isOwner ? "travelerswaystones.break.warning.owner" : "travelerswaystones.break.warning";
            player.displayClientMessage(TextComponents.translation(
                messageKey,
                TextComponents.number(WaystoneMovingHandler.MAX_DISTANCE).color(ChatFormatting.GOLD).get()
            ).color(ChatFormatting.WHITE).get(), false);
            player.displayClientMessage(TextComponents.translation(
                "travelerswaystones.break.warning.bypass",
                TextComponents.translation("key.keyboard.left.shift").color(ChatFormatting.GOLD).get()
            ).color(ChatFormatting.GRAY).italic().get(), false);
            player.swing(InteractionHand.MAIN_HAND);
            this.waystoneMineWarningDelay = 40;
        }
        this.missTime = 40;
        cir.setReturnValue(false);
    }
}
