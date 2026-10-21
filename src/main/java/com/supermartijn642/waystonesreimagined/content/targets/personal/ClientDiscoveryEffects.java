package com.supermartijn642.waystonesreimagined.content.targets.personal;

import com.supermartijn642.core.ClientUtils;
import com.supermartijn642.core.TextComponents;
import com.supermartijn642.core.util.playerprofile.PlayerProfileCache;
import com.supermartijn642.waystonesreimagined.api.targets.personal.DiscoveredWaystoneTarget;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Hud;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.network.chat.contents.objects.PlayerSprite;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.component.ResolvableProfile;

public class ClientDiscoveryEffects {

    public static void showEffects(Player player, DiscoveredWaystoneTarget target){
        Hud hud = ClientUtils.getMinecraft().gui.hud;
        hud.setTitle(TextComponents.translation(
            "waystonesreimagined.discovery.name",
            TextComponents.string(target.name()).get()
        ).get());
        String owner = PlayerProfileCache.getUsername(target.owner());
        Component ownerText = owner == null ?
            TextComponents.translation("waystonesreimagined.screen.teleport.owner.unknown").color(ChatFormatting.GRAY).get() :
            TextComponents.string(owner).color(ChatFormatting.GRAY).get();
        hud.setSubtitle(TextComponents.translation(
            "waystonesreimagined.discovery.by",
            Component.object(new PlayerSprite(ResolvableProfile.createUnresolved(target.owner()), true)).withColor(TextColor.WHITE),
            ownerText
        ).color(ChatFormatting.GRAY).get());
    }
}
