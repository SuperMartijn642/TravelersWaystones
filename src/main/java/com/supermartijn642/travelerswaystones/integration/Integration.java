package com.supermartijn642.travelerswaystones.integration;

import net.fabricmc.loader.api.FabricLoader;

public final class Integration {

    public static final boolean isTrinketsUpdatedLoaded = FabricLoader.getInstance().isModLoaded("trinkets_updated");
}
