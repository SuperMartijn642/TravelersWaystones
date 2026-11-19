package com.supermartijn642.travelerswaystones.integration;

import com.supermartijn642.travelerswaystones.TravelersWaystones;
import dev.emi.trinkets.api.TrinketsApi;
import net.minecraft.world.entity.player.Player;

public class TrinketsIntegration {

    public static boolean hasPendantInTrinketsSlot(Player player) {
        return TrinketsApi.getTrinketComponent(player).map(component -> component.isEquipped(TravelersWaystones.warpPendant)).orElse(false);
    }
}
