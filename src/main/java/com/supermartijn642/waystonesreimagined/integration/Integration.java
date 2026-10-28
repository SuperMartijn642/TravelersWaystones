package com.supermartijn642.waystonesreimagined.integration;

import net.neoforged.fml.ModList;

public final class Integration {

    public static final boolean isTrinketsUpdatedLoaded = ModList.get().isLoaded("trinkets_updated");
    public static final boolean isCuriosAPILoaded = ModList.get().isLoaded("curios");
}
