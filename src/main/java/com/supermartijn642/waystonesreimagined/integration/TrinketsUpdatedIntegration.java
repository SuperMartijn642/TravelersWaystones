package com.supermartijn642.waystonesreimagined.integration;

import com.supermartijn642.waystonesreimagined.WaystonesReimagined;
import eu.pb4.trinkets.api.TrinketsApi;
import net.minecraft.world.entity.player.Player;

public class TrinketsUpdatedIntegration {

    public static boolean hasPendantInTrinketsSlot(Player player) {
        return TrinketsApi.getAttachment(player).isEquipped(WaystonesReimagined.warpPendant);
    }
}
