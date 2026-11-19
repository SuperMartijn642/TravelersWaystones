package com.supermartijn642.travelerswaystones.integration;

import com.supermartijn642.travelerswaystones.TravelersWaystones;
import net.minecraft.world.entity.player.Player;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;

public class CuriosApiIntegration {

    public static boolean hasPendantInCuriosSlot(Player player){
        ICuriosItemHandler handler = CuriosApi.getCuriosInventory(player).orElse(null);
        return handler != null && handler.isEquipped(TravelersWaystones.warpPendant);
    }
}
