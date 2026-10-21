package com.supermartijn642.waystonesreimagined.mixin;

import com.supermartijn642.core.ClientUtils;
import com.supermartijn642.core.TextComponents;
import com.supermartijn642.waystonesreimagined.api.targets.personal.DiscoveredWaystoneTarget;
import com.supermartijn642.waystonesreimagined.api.targets.personal.PersonalWaystoneManager;
import com.supermartijn642.waystonesreimagined.content.WaystoneEventsImpl;
import com.supermartijn642.waystonesreimagined.content.WaystoneTypeRegistry;
import com.supermartijn642.waystonesreimagined.content.block.WaystoneBlock;
import com.supermartijn642.waystonesreimagined.content.moving.WaystoneMovingHandler;
import com.supermartijn642.waystonesreimagined.content.moving.WaystoneMovingHandlerClient;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
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
            target = "Lnet/minecraft/client/tutorial/Tutorial;<init>(Lnet/minecraft/client/Minecraft;Lnet/minecraft/client/Options;)V"
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
        if(player.isSpectator() || player.isShiftKeyDown())
            return;
        BlockHitResult result = (BlockHitResult)ClientUtils.getMinecraft().hitResult;
        BlockPos pos = result.getBlockPos();
        ClientLevel level = ClientUtils.getWorld();
        if(!level.getWorldBorder().isWithinBounds(pos))
            return;
        BlockState state = level.getBlockState(pos);
        if(state.getBlock() instanceof WaystoneBlock waystone){
            pos = waystone.getBasePos(pos, state);
            if(this.waystoneMineWarningDelay <= 0){
                DiscoveredWaystoneTarget target = PersonalWaystoneManager.get(player).getTargetAt(level, pos);
                boolean isOwner = target != null && target.owner().equals(player.getUUID());
                String messageKey = isOwner ? "waystonesreimagined.break.warning.owner" : "waystonesreimagined.break.warning";
                player.sendSystemMessage(TextComponents.translation(
                    messageKey,
                    TextComponents.number(WaystoneMovingHandler.MAX_DISTANCE).color(ChatFormatting.GOLD).get()
                ).color(ChatFormatting.WHITE).get());
                player.sendSystemMessage(TextComponents.translation(
                    "waystonesreimagined.break.warning.bypass",
                    TextComponents.translation("key.keyboard.left.shift").color(ChatFormatting.GOLD).get()
                ).color(ChatFormatting.GRAY).italic().get());
                player.swing(InteractionHand.MAIN_HAND);
                this.waystoneMineWarningDelay = 40;
            }
            this.missTime = 40;
            cir.setReturnValue(false);
        }
    }
}
