package com.supermartijn642.travelerswaystones.content.block;

import com.supermartijn642.core.item.ItemProperties;
import net.minecraft.world.level.block.Block;

public class TallWaystoneItem extends WaystoneItem {

    public TallWaystoneItem(Block block, ItemProperties properties){
        super(block, properties);
    }

    @Override
    protected boolean mustSurvive(){
        return false;
    }
}
