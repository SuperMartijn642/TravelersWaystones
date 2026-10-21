package com.supermartijn642.waystonesreimagined;

import com.supermartijn642.core.ClientUtils;
import com.supermartijn642.core.TextComponents;
import com.supermartijn642.core.gui.WidgetScreen;
import com.supermartijn642.core.gui.widget.premade.StackedWidget;
import com.supermartijn642.core.registry.ClientRegistrationHandler;
import com.supermartijn642.waystonesreimagined.api.targets.personal.DiscoveredWaystoneTarget;
import com.supermartijn642.waystonesreimagined.api.targets.personal.PersonalWaystoneManager;
import com.supermartijn642.waystonesreimagined.content.block.WaystoneBlockStateModel;
import com.supermartijn642.waystonesreimagined.content.block.WaystoneMaterial;
import com.supermartijn642.waystonesreimagined.content.particle.ScanParticleProvider;
import com.supermartijn642.waystonesreimagined.content.screen.ClaimWaystoneScreen;
import com.supermartijn642.waystonesreimagined.content.screen.WaystoneTeleportScreen;
import com.supermartijn642.waystonesreimagined.content.screen.components.warning.DangerousWarningWidget;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.particle.v1.ParticleProviderRegistry;
import net.minecraft.client.KeyMapping;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

public class WaystonesReimaginedClient implements ClientModInitializer {

    public static KeyMapping PENDANT_KEYBIND;

    @Override
    public void onInitializeClient(){
        ClientRegistrationHandler handler = ClientRegistrationHandler.get(WaystonesReimagined.MODID);
        for(WaystoneMaterial material : WaystoneMaterial.values())
            handler.registerBlockStateModelOverwrite(material::block, WaystoneBlockStateModel::new);
        ParticleProviderRegistry.getInstance().register(WaystonesReimagined.discoverParticle, ScanParticleProvider::new);
        ParticleProviderRegistry.getInstance().register(WaystonesReimagined.teleportParticle, ScanParticleProvider::new);

        PENDANT_KEYBIND = new KeyMapping("waystonesreimagined.keys.pendant", GLFW.GLFW_KEY_G, KeyMapping.Category.register(WaystonesReimagined.identifier("keys")));
        KeyMappingHelper.registerKeyMapping(PENDANT_KEYBIND);
        ClientTickEvents.END_CLIENT_TICK.register(_ -> {
            if(PENDANT_KEYBIND.consumeClick()){
                Player player = ClientUtils.getPlayer();
                if(ClientUtils.getWorld() == null || player == null
                    || ClientUtils.getMinecraft().gui.screen() != null)
                    return;
                if(!player.getInventory().contains(s -> s.is(WaystonesReimagined.warpPendant)))
                    return;
                PersonalWaystoneManager manager = PersonalWaystoneManager.get(player);
                if(manager.all().isEmpty())
                    player.sendOverlayMessage(TextComponents.translation("waystonesreimagined.pendant.no_targets").get());
                else
                    openTeleportScreen(null);
            }
        });
    }

    public static void openClaimScreen(BlockPos pos, boolean isNewlyPlaced, @Nullable Component nameSuggestion){
        Player player = ClientUtils.getPlayer();
        Level level = player.level();
        String formattedNameSuggestion = nameSuggestion == null ? null : nameSuggestion.getString();
        ClientUtils.displayScreen(ClaimWaystoneScreen.create(
            player.getUUID(),
            level.dimension().identifier(),
            level.getBiome(pos).unwrapKey().orElseThrow().identifier(),
            pos,
            level.getBlockState(pos),
            isNewlyPlaced,
            formattedNameSuggestion
        ));
    }

    public static void openTeleportScreen(@Nullable DiscoveredWaystoneTarget current){
        PersonalWaystoneManager manager = PersonalWaystoneManager.get(ClientUtils.getPlayer());
        ClientUtils.displayScreen(WaystoneTeleportScreen.create(current, manager));
    }

    public static void showDangerousWarning(DiscoveredWaystoneTarget target, Component warning){
        if(!(ClientUtils.getMinecraft().gui.screen() instanceof WidgetScreen<?> screen))
            return;
        if(!(screen.getWidget() instanceof StackedWidget stack))
            return;
        if(!(stack.topWidget() instanceof WaystoneTeleportScreen widget))
            return;
        stack.push(new DangerousWarningWidget(
            widget.width() / 2, widget.height() / 2,
            widget.getCurrentTarget(),
            target,
            warning,
            stack::pop
        ));
    }
}
