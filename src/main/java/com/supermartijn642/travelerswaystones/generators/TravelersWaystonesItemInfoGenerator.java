package com.supermartijn642.travelerswaystones.generators;

import com.supermartijn642.core.generator.ItemInfoGenerator;
import com.supermartijn642.core.generator.ResourceCache;
import com.supermartijn642.travelerswaystones.TravelersWaystones;
import com.supermartijn642.travelerswaystones.content.block.WaystoneMaterial;
import com.supermartijn642.travelerswaystones.util.TwoBlockItemModel;

public class TravelersWaystonesItemInfoGenerator extends ItemInfoGenerator {

    public TravelersWaystonesItemInfoGenerator(ResourceCache cache){
        super(TravelersWaystones.MODID, cache);
    }

    @Override
    public void generate(){
        for(WaystoneMaterial material : WaystoneMaterial.values()){
            this.info(material.item())
                .model(new TwoBlockItemModel(
                    TravelersWaystones.identifier("block/" + material.identifier() + "_waystone_top"),
                    TravelersWaystones.identifier("block/" + material.identifier() + "_waystone_bottom"),
                    0.75f
                ));
        }
        this.simpleInfo(TravelersWaystones.dimensionalPearl, "item/dimensional_pearl");
        this.simpleInfo(TravelersWaystones.warpPendant, "item/warp_pendant");
    }
}
