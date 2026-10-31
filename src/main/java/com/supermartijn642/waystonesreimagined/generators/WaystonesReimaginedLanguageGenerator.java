package com.supermartijn642.waystonesreimagined.generators;

import com.supermartijn642.core.generator.LanguageGenerator;
import com.supermartijn642.core.generator.ResourceCache;
import com.supermartijn642.waystonesreimagined.WaystonesReimagined;
import com.supermartijn642.waystonesreimagined.content.block.WaystoneMaterial;
import net.minecraft.world.level.Level;

public class WaystonesReimaginedLanguageGenerator extends LanguageGenerator {

    public WaystonesReimaginedLanguageGenerator(ResourceCache cache){
        super(WaystonesReimagined.MODID, cache, "en_us");
    }

    @Override
    public void generate(){
        // Items
        this.item(WaystonesReimagined.dimensionalPearl, "Dimensional Pearl");
        this.item(WaystonesReimagined.warpPendant, "Warp Pendant");
        this.translation("waystonesreimagined.pendant.description", "Allows teleporting to waystones from anywhere");
        this.translation("waystonesreimagined.pendant.keybind", "Keybind: %s");
        this.translation("waystonesreimagined.pendant.no_targets", "Discover waystones to warp to them");
        this.itemGroup(WaystonesReimagined.CREATIVE_GROUP, "Waystones Reimagined");

        // Waystones
        for(WaystoneMaterial material : WaystoneMaterial.values())
            this.block(material.block(), material.translation());
        this.translation("waystonesreimagined.break.warning", "Breaking a waystone will remove the target for all players. The owner can move the waystone up to %s blocks from within its gui without others needing to re-discover it");
        this.translation("waystonesreimagined.break.warning.owner", "Breaking a waystone will remove the target for all players. You can move the waystone up to %s blocks from within its gui without others needing to re-discover it");
        this.translation("waystonesreimagined.break.warning.bypass", "If you do wish to break the waystone, you can do so by holding %s");

        // Discovery
        this.translation("waystonesreimagined.discovery.name", "%s");
        this.translation("waystonesreimagined.discovery.by", "By %s %s");

        // Screen
        this.translation("waystonesreimagined.screen.teleport.current", "Current");
        this.translation("waystonesreimagined.screen.teleport.destination", "Destination");
        this.translation("waystonesreimagined.screen.teleport.owner.unknown", "Unknown");
        this.translation("waystonesreimagined.screen.teleport.search.hint", "Search");
        this.translation("waystonesreimagined.screen.teleport.search.narrate", "Waystone search field: %s");
        this.translation("waystonesreimagined.screen.teleport.category.recents", "Recent");
        this.translation("waystonesreimagined.screen.teleport.category.recents.narrate", "Recent waystones category: %s");
        this.translation("waystonesreimagined.screen.teleport.category.favorites", "Favorites");
        this.translation("waystonesreimagined.screen.teleport.category.favorites.narrate", "Favorite waystones category: %s");
        this.translation("waystonesreimagined.screen.teleport.category.player.narrate", "Player waystones category: %s, %s");
        this.translation("waystonesreimagined.screen.teleport.category.narrate.expanded", "Expanded");
        this.translation("waystonesreimagined.screen.teleport.category.narrate.collapsed", "Collapsed");
        this.translation("waystonesreimagined.screen.teleport.target_entry.narrate", "Waystone entry: %s");
        this.translation("waystonesreimagined.screen.teleport.favorite.narrate", "Favorite entry button");
        this.translation("waystonesreimagined.screen.teleport.relocate", "Move waystone");
        this.translation("waystonesreimagined.screen.teleport.relocate.info", "Waystones can be moved up to %s blocks without others needing to rediscover it");
        this.translation("waystonesreimagined.screen.teleport.relocate.narrate", "Relocate waystone button");
        this.translation("waystonesreimagined.screen.teleport.teleport", "Teleport");
        this.translation("waystonesreimagined.screen.teleport.warning.title", "Warning");
        this.translation("waystonesreimagined.screen.teleport.warning.narrate", "Warning message: %s");
        this.translation("waystonesreimagined.screen.teleport.warning.suppress", "Don't show again");
        this.translation("waystonesreimagined.screen.teleport.warning.suppress.hint", "Warning will not be shown again for this target");
        this.translation("waystonesreimagined.screen.teleport.warning.suppress.narrate", "Suppress warnings button");
        this.translation("waystonesreimagined.screen.teleport.warning.teleport", "Teleport");
        this.translation("waystonesreimagined.screen.teleport.warning.cancel", "Cancel");
        this.translation("waystonesreimagined.screen.teleport.warning.cancel.narrate", "Cancel button");
        this.translation("waystonesreimagined.screen.teleport.warning_icon.hint", "Warnings are disabled for this target");
        this.translation("waystonesreimagined.screen.teleport.warning_icon.hint.reenable", "Click to re-enable warnings");
        this.translation("waystonesreimagined.screen.teleport.warning_icon.narrate", "Warnings suppressed icon");
        this.translation("waystonesreimagined.screen.teleport.narrate", "Waystone teleport screen");
        this.translation("waystonesreimagined.screen.teleport.preview.narrate", "Waystone preview");
        this.translation("waystonesreimagined.screen.teleport.warning.narrate", "Waystone preview");
        this.translation("waystonesreimagined.screen.claim.title", "Waystone");
        this.translation("waystonesreimagined.screen.claim.claim", "Claim");
        this.translation("waystonesreimagined.screen.claim.create", "Create");
        this.translation("waystonesreimagined.screen.claim.name.enter", "Enter name");
        this.translation("waystonesreimagined.screen.claim.name.invalid", "Name must contain at least 3 characters");
        this.translation("waystonesreimagined.screen.claim.narrate", "Claim waystone screen");
        this.translation("waystonesreimagined.screen.info_card.narrate", "Waystone info: %s");
        this.translation("waystonesreimagined.screen.info_card.name_field.narrate", "Name text field");
        this.translation("waystonesreimagined.screen.info_card.edit_name.narrate", "Edit name button");

        // Keybinds
        this.translation("key.category.waystonesreimagined.keys", "Waystones Reimagined");
        this.translation("waystonesreimagined.keys.pendant", "Use Warp Pendant");

        // Claiming waystones
        this.translation("waystonesreimagined.claim.denied", "Cannot claim waystone");

        // Moving waystones
        this.translation("waystonesreimagined.moving.denied", "Cannot move waystone");
        this.translation("waystonesreimagined.moving.hint", "%s to place - %s to cancel");
        this.translation("waystonesreimagined.moving.no_placement", "Block cannot be placed here");
        this.translation("waystonesreimagined.moving.too_far", "Waystone cannot be moved further than %s blocks");
        this.translation("waystonesreimagined.moving.cancelled", "Cancelled moving waystone");
        this.translation("waystonesreimagined.moving.failed", "Failed to move waystone for block '%s'");
        this.translation("waystonesreimagined.moving.failed.reason", "Failed to move waystone for block '%s': %s");
        this.translation("waystonesreimagined.moving.success", "%s moved to %s, %s, %s");

        // Teleport
        this.translation("waystonesreimagined.teleport.error", "Could not teleport to waystone: %s");
        this.translation("waystonesreimagined.teleport.error.missing_level", "Level '%s' is not present");
        this.translation("waystonesreimagined.teleport.error.teleport_failed", "Failed to teleport player");
        this.translation("waystonesreimagined.teleport.dangerous", "Destination may be dangerous");
        this.translation("waystonesreimagined.teleport.no_space", "Space around waystone is obstructed");
        this.translation("waystonesreimagined.teleport.denied", "Cannot teleport to waystone");

        // Tags
        this.translation("tag.block.waystonesreimagined.waystones", "Waystones");
        this.translation("tag.item.waystonesreimagined.waystones", "Waystones");

        // Vanilla dimension names
        this.translation(Level.OVERWORLD.identifier().toLanguageKey("dimension"), "Overworld");
        this.translation(Level.NETHER.identifier().toLanguageKey("dimension"), "Nether");
        this.translation(Level.END.identifier().toLanguageKey("dimension"), "End");
    }
}
