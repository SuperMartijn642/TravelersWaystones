package com.supermartijn642.waystonesreimagined.generators;

import com.supermartijn642.core.generator.LootTableGenerator;
import com.supermartijn642.core.generator.ResourceCache;
import com.supermartijn642.waystonesreimagined.WaystonesReimagined;
import com.supermartijn642.waystonesreimagined.content.block.WaystoneMaterial;

/**
 * Created 30/08/2026 by SuperMartijn642
 */
public class WaystonesReimaginedLootTableGenerator extends LootTableGenerator {

    public WaystonesReimaginedLootTableGenerator(ResourceCache cache){
        super(WaystonesReimagined.MODID, cache);
    }

    @Override
    public void generate(){
        for(WaystoneMaterial material : WaystoneMaterial.values())
            this.dropSelf(material.block());
    }
}
