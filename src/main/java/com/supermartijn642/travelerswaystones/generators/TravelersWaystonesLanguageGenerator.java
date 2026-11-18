package com.supermartijn642.travelerswaystones.generators;

import com.supermartijn642.core.generator.LanguageGenerator;
import com.supermartijn642.core.generator.ResourceCache;
import com.supermartijn642.travelerswaystones.TravelersWaystones;
import com.supermartijn642.travelerswaystones.content.block.WaystoneMaterial;
import net.minecraft.world.level.Level;

public class TravelersWaystonesLanguageGenerator extends LanguageGenerator {

    public TravelersWaystonesLanguageGenerator(ResourceCache cache){
        super(TravelersWaystones.MODID, cache, "en_us");
    }

    @Override
    public void generate(){
        // Items
        this.item(TravelersWaystones.dimensionalPearl, "Dimensional Pearl");
        this.item(TravelersWaystones.warpPendant, "Warp Pendant");
        this.translation("travelerswaystones.pendant.description", "Allows teleporting to waystones from anywhere");
        this.translation("travelerswaystones.pendant.keybind", "Keybind: %s");
        this.translation("travelerswaystones.pendant.no_targets", "Discover waystones to warp to them");
        this.itemGroup(TravelersWaystones.CREATIVE_GROUP, "Traveler's Waystones");

        // Waystones
        for(WaystoneMaterial material : WaystoneMaterial.values())
            this.block(material.block(), material.translation());
        this.translation("travelerswaystones.break.warning", "Breaking a waystone will remove the target for all players. The owner can move the waystone up to %s blocks from within its gui without others needing to re-discover it");
        this.translation("travelerswaystones.break.warning.owner", "Breaking a waystone will remove the target for all players. You can move the waystone up to %s blocks from within its gui without others needing to re-discover it");
        this.translation("travelerswaystones.break.warning.bypass", "If you do wish to break the waystone, you can do so by holding %s");

        // Discovery
        this.translation("travelerswaystones.discovery.name", "%s");
        this.translation("travelerswaystones.discovery.by", "By %s %s");

        // Screen
        this.translation("travelerswaystones.screen.teleport.current", "Current");
        this.translation("travelerswaystones.screen.teleport.destination", "Destination");
        this.translation("travelerswaystones.screen.teleport.owner.unknown", "Unknown");
        this.translation("travelerswaystones.screen.teleport.search.hint", "Search");
        this.translation("travelerswaystones.screen.teleport.search.narrate", "Waystone search field: %s");
        this.translation("travelerswaystones.screen.teleport.category.recents", "Recent");
        this.translation("travelerswaystones.screen.teleport.category.recents.narrate", "Recent waystones category: %s");
        this.translation("travelerswaystones.screen.teleport.category.favorites", "Favorites");
        this.translation("travelerswaystones.screen.teleport.category.favorites.narrate", "Favorite waystones category: %s");
        this.translation("travelerswaystones.screen.teleport.category.player.narrate", "Player waystones category: %s, %s");
        this.translation("travelerswaystones.screen.teleport.category.narrate.expanded", "Expanded");
        this.translation("travelerswaystones.screen.teleport.category.narrate.collapsed", "Collapsed");
        this.translation("travelerswaystones.screen.teleport.target_entry.narrate", "Waystone entry: %s");
        this.translation("travelerswaystones.screen.teleport.favorite.narrate", "Favorite entry button");
        this.translation("travelerswaystones.screen.teleport.relocate", "Move waystone");
        this.translation("travelerswaystones.screen.teleport.relocate.info", "Waystones can be moved up to %s blocks without others needing to rediscover it");
        this.translation("travelerswaystones.screen.teleport.relocate.narrate", "Relocate waystone button");
        this.translation("travelerswaystones.screen.teleport.teleport", "Teleport");
        this.translation("travelerswaystones.screen.teleport.warning.title", "Warning");
        this.translation("travelerswaystones.screen.teleport.warning.narrate", "Warning message: %s");
        this.translation("travelerswaystones.screen.teleport.warning.suppress", "Don't show again");
        this.translation("travelerswaystones.screen.teleport.warning.suppress.hint", "Warning will not be shown again for this target");
        this.translation("travelerswaystones.screen.teleport.warning.suppress.narrate", "Suppress warnings button");
        this.translation("travelerswaystones.screen.teleport.warning.teleport", "Teleport");
        this.translation("travelerswaystones.screen.teleport.warning.cancel", "Cancel");
        this.translation("travelerswaystones.screen.teleport.warning.cancel.narrate", "Cancel button");
        this.translation("travelerswaystones.screen.teleport.warning_icon.hint", "Warnings are disabled for this target");
        this.translation("travelerswaystones.screen.teleport.warning_icon.hint.reenable", "Click to re-enable warnings");
        this.translation("travelerswaystones.screen.teleport.warning_icon.narrate", "Warnings suppressed icon");
        this.translation("travelerswaystones.screen.teleport.narrate", "Waystone teleport screen");
        this.translation("travelerswaystones.screen.teleport.preview.narrate", "Waystone preview");
        this.translation("travelerswaystones.screen.teleport.warning.narrate", "Waystone preview");
        this.translation("travelerswaystones.screen.claim.title", "Waystone");
        this.translation("travelerswaystones.screen.claim.claim", "Claim");
        this.translation("travelerswaystones.screen.claim.create", "Create");
        this.translation("travelerswaystones.screen.claim.name.enter", "Enter name");
        this.translation("travelerswaystones.screen.claim.name.invalid", "Name must contain at least 3 characters");
        this.translation("travelerswaystones.screen.claim.narrate", "Claim waystone screen");
        this.translation("travelerswaystones.screen.info_card.narrate", "Waystone info: %s");
        this.translation("travelerswaystones.screen.info_card.name_field.narrate", "Name text field");
        this.translation("travelerswaystones.screen.info_card.edit_name.narrate", "Edit name button");

        // Keybinds
        this.translation("key.category.travelerswaystones.keys", "Traveler's Waystones");
        this.translation("travelerswaystones.keys.pendant", "Use Warp Pendant");

        // Claiming waystones
        this.translation("travelerswaystones.claim.denied", "Cannot claim waystone");

        // Discovering waystones
        this.translation("travelerswaystones.discover.denied", "Cannot discover waystone");

        // Moving waystones
        this.translation("travelerswaystones.moving.denied", "Cannot move waystone");
        this.translation("travelerswaystones.moving.hint", "%s to place - %s to cancel");
        this.translation("travelerswaystones.moving.no_placement", "Block cannot be placed here");
        this.translation("travelerswaystones.moving.too_far", "Waystone cannot be moved further than %s blocks");
        this.translation("travelerswaystones.moving.cancelled", "Cancelled moving waystone");
        this.translation("travelerswaystones.moving.failed", "Failed to move waystone for block '%s'");
        this.translation("travelerswaystones.moving.failed.reason", "Failed to move waystone for block '%s': %s");
        this.translation("travelerswaystones.moving.success", "%s moved to %s, %s, %s");

        // Teleport
        this.translation("travelerswaystones.teleport.error", "Could not teleport to waystone: %s");
        this.translation("travelerswaystones.teleport.error.missing_level", "Level '%s' is not present");
        this.translation("travelerswaystones.teleport.error.teleport_failed", "Failed to teleport player");
        this.translation("travelerswaystones.teleport.dangerous", "Destination may be dangerous");
        this.translation("travelerswaystones.teleport.no_space", "Space around waystone is obstructed");
        this.translation("travelerswaystones.teleport.denied", "Cannot teleport to waystone");
        this.translation("travelerswaystones.teleport.no_pendant", "Teleporting without a waystone requires a warp pendant");

        // Tags
        this.translation("tag.block.travelerswaystones.waystones", "Waystones");
        this.translation("tag.item.travelerswaystones.waystones", "Waystones");

        // Vanilla dimension names
        this.translation(Level.OVERWORLD.identifier().toLanguageKey("dimension"), "Overworld");
        this.translation(Level.NETHER.identifier().toLanguageKey("dimension"), "Nether");
        this.translation(Level.END.identifier().toLanguageKey("dimension"), "End");
    }
}
