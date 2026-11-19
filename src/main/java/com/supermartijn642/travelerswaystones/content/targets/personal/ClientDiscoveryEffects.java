package com.supermartijn642.travelerswaystones.content.targets.personal;

import com.supermartijn642.core.ClientUtils;
import com.supermartijn642.core.TextComponents;
import com.supermartijn642.core.util.playerprofile.PlayerProfileCache;
import com.supermartijn642.travelerswaystones.api.targets.personal.DiscoveredWaystoneTarget;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.PlayerFaceRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.UUID;

public class ClientDiscoveryEffects {

    private static final String OWNER_HEAD_INSERTION = "travelerswaystones:owner_head";

    private static Component subtitle;
    private static UUID owner;

    public static void showEffects(Player player, DiscoveredWaystoneTarget target){
        Gui gui = ClientUtils.getMinecraft().gui;
        gui.setTitle(TextComponents.translation(
            "travelerswaystones.discovery.name",
            TextComponents.string(target.name()).get()
        ).get());
        String owner = PlayerProfileCache.get(true).getUsername(target.owner());
        Component ownerText = owner == null ?
            TextComponents.translation("travelerswaystones.screen.teleport.owner.unknown").color(ChatFormatting.GRAY).get() :
            TextComponents.string(owner).color(ChatFormatting.GRAY).get();
        // Leave space for the owner's head, it gets drawn in GuiMixin
        subtitle = TextComponents.translation(
            "travelerswaystones.discovery.by",
            Component.literal("  ").withStyle(style -> style.withInsertion(OWNER_HEAD_INSERTION)),
            ownerText
        ).color(ChatFormatting.GRAY).get();
        ClientDiscoveryEffects.owner = target.owner();
        gui.setSubtitle(subtitle);
    }

    public static void renderOwnerHead(GuiGraphics guiGraphics, @Nullable Component currentSubtitle, int color){
        if(currentSubtitle == null || currentSubtitle != subtitle)
            return;
        // Find the space left for the head
        Font font = ClientUtils.getFontRenderer();
        int[] offset = {0};
        Optional<Boolean> found = subtitle.visit((style, text) -> {
            if(OWNER_HEAD_INSERTION.equals(style.getInsertion()))
                return Optional.of(true);
            offset[0] += font.width(FormattedText.of(text, style));
            return Optional.empty();
        }, Style.EMPTY);
        if(found.isEmpty())
            return;
        PlayerProfileCache.Skin skin = PlayerProfileCache.get(true).getSkinOrDefault(owner);
        PlayerFaceRenderer.draw(guiGraphics, skin.body(), -font.width(subtitle) / 2 + offset[0], 5, 8, true, false, color);
    }
}
