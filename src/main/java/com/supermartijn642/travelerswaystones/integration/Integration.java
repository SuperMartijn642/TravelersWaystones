package com.supermartijn642.travelerswaystones.integration;

import net.fabricmc.loader.api.FabricLoader;

public final class Integration {

    public static final boolean isTrinketsLoaded = FabricLoader.getInstance().isModLoaded("trinkets");
}
