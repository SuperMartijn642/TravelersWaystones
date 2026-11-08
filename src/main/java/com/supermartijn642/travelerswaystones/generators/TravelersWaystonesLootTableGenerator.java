package com.supermartijn642.travelerswaystones.generators;

import com.supermartijn642.core.generator.LootTableGenerator;
import com.supermartijn642.core.generator.ResourceCache;
import com.supermartijn642.travelerswaystones.TravelersWaystones;
import com.supermartijn642.travelerswaystones.content.block.WaystoneMaterial;

public class TravelersWaystonesLootTableGenerator extends LootTableGenerator {

    public TravelersWaystonesLootTableGenerator(ResourceCache cache){
        super(TravelersWaystones.MODID, cache);
    }

    @Override
    public void generate(){
        for(WaystoneMaterial material : WaystoneMaterial.values())
            this.dropSelf(material.block());
    }
}
