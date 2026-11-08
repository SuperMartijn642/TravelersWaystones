package com.supermartijn642.travelerswaystones.extension;

import com.supermartijn642.travelerswaystones.api.targets.personal.PersonalWaystoneManager;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

public interface TravelersWaystonesPlayer {

    @Nullable
    @Contract("true -> !null")
    PersonalWaystoneManager travelerswaystones_getWaystoneManager(boolean createIfAbsent);
}
