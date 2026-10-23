package com.supermartijn642.waystonesreimagined.generators;

import com.supermartijn642.core.generator.ItemInfoGenerator;
import com.supermartijn642.core.generator.ResourceCache;
import com.supermartijn642.waystonesreimagined.WaystonesReimagined;
import com.supermartijn642.waystonesreimagined.content.block.WaystoneMaterial;
import com.supermartijn642.waystonesreimagined.util.TwoBlockItemModel;

public class WaystonesReimaginedItemInfoGenerator extends ItemInfoGenerator {

    public WaystonesReimaginedItemInfoGenerator(ResourceCache cache){
        super(WaystonesReimagined.MODID, cache);
    }

    @Override
    public void generate(){
        for(WaystoneMaterial material : WaystoneMaterial.values()){
            this.info(material.item())
                .model(new TwoBlockItemModel(
                    WaystonesReimagined.identifier("block/" + material.identifier() + "_waystone_top"),
                    WaystonesReimagined.identifier("block/" + material.identifier() + "_waystone_bottom"),
                    0.75f
                ));
        }
        this.simpleInfo(WaystonesReimagined.dimensionalPearl, "item/dimensional_pearl");
        this.simpleInfo(WaystonesReimagined.warpPendant, "item/warp_pendant");
    }
}
