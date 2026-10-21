package com.supermartijn642.waystonesreimagined.extension;

import com.supermartijn642.waystonesreimagined.api.targets.personal.PersonalWaystoneManager;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

public interface WaystonesReimaginedPlayer {

    @Nullable
    @Contract("true -> !null")
    PersonalWaystoneManager waystonesreimagined_getWaystoneManager(boolean createIfAbsent);
}
