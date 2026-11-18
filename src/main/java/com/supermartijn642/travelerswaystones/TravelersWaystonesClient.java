package com.supermartijn642.travelerswaystones;

import com.supermartijn642.core.ClientUtils;
import com.supermartijn642.core.TextComponents;
import com.supermartijn642.core.gui.WidgetScreen;
import com.supermartijn642.core.gui.widget.premade.StackedWidget;
import com.supermartijn642.core.registry.ClientRegistrationHandler;
import com.supermartijn642.travelerswaystones.api.targets.personal.DiscoveredWaystoneTarget;
import com.supermartijn642.travelerswaystones.api.targets.personal.PersonalWaystoneManager;
import com.supermartijn642.travelerswaystones.content.WarpPendant;
import com.supermartijn642.travelerswaystones.content.block.WaystoneBlockStateModel;
import com.supermartijn642.travelerswaystones.content.block.WaystoneMaterial;
import com.supermartijn642.travelerswaystones.content.particle.ScanParticleProvider;
import com.supermartijn642.travelerswaystones.content.screen.ClaimWaystoneScreen;
import com.supermartijn642.travelerswaystones.content.screen.WaystoneTeleportScreen;
import com.supermartijn642.travelerswaystones.content.screen.components.warning.DangerousWarningWidget;
import com.supermartijn642.travelerswaystones.util.TwoBlockItemModel;
import net.minecraft.client.KeyMapping;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.util.function.Consumer;

@Mod(value = TravelersWaystones.MODID, dist = Dist.CLIENT)
public class TravelersWaystonesClient {

    public static KeyMapping PENDANT_KEYBIND;

    public TravelersWaystonesClient(IEventBus eventBus){
        ClientRegistrationHandler handler = ClientRegistrationHandler.get(TravelersWaystones.MODID);
        for(WaystoneMaterial material : WaystoneMaterial.values())
            handler.registerBlockStateModelOverwrite(material::block, WaystoneBlockStateModel::new);
        handler.registerItemModelType("two_block", TwoBlockItemModel.CODEC);
        eventBus.addListener((Consumer<RegisterParticleProvidersEvent>)e -> {
            e.registerSpriteSet(TravelersWaystones.discoverParticle, ScanParticleProvider::new);
            e.registerSpriteSet(TravelersWaystones.teleportParticle, ScanParticleProvider::new);
        });

        eventBus.addListener((Consumer<RegisterKeyMappingsEvent>)e -> {
            KeyMapping.Category category = new KeyMapping.Category(TravelersWaystones.identifier("keys"));
            e.registerCategory(category);
            e.register(PENDANT_KEYBIND = new KeyMapping("travelerswaystones.keys.pendant", GLFW.GLFW_KEY_K, category));
        });
        NeoForge.EVENT_BUS.addListener((Consumer<ClientTickEvent.Post>)_ -> {
            if(PENDANT_KEYBIND.consumeClick()){
                Player player = ClientUtils.getPlayer();
                if(ClientUtils.getWorld() == null || player == null
                    || ClientUtils.getMinecraft().gui.screen() != null)
                    return;
                boolean hasPendant = WarpPendant.hasPendant(player);
                if(!hasPendant)
                    return;
                PersonalWaystoneManager manager = PersonalWaystoneManager.get(player);
                if(manager.all().isEmpty())
                    player.sendOverlayMessage(TextComponents.translation("travelerswaystones.pendant.no_targets").get());
                else
                    openTeleportScreen(null, null, false);
            }
        });
    }

    public static void openClaimScreen(BlockPos pos, boolean isNewlyPlaced, @Nullable Component nameSuggestion){
        Player player = ClientUtils.getPlayer();
        Level level = player.level();
        String formattedNameSuggestion = nameSuggestion == null ? null : nameSuggestion.getString();
        ClientUtils.displayScreen(ClaimWaystoneScreen.create(
            player.getUUID(),
            level.dimension(),
            level.getBiome(pos).unwrapKey().orElseThrow(),
            pos,
            level.getBlockState(pos),
            isNewlyPlaced,
            formattedNameSuggestion
        ));
    }

    public static void openTeleportScreen(@Nullable DiscoveredWaystoneTarget current, @Nullable DiscoveredWaystoneTarget selected, boolean trusted){
        Player player = ClientUtils.getPlayer();
        PersonalWaystoneManager manager = PersonalWaystoneManager.get(player);
        if(manager.all().isEmpty()){
            player.sendOverlayMessage(TextComponents.translation("travelerswaystones.pendant.no_targets").get());
            return;
        }
        ClientUtils.displayScreen(WaystoneTeleportScreen.create(current, selected, trusted, manager));
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
            widget.isTrusted(),
            target,
            warning,
            stack::pop
        ));
    }
}
