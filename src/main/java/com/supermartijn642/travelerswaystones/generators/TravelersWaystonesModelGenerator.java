package com.supermartijn642.travelerswaystones.generators;

import com.supermartijn642.core.generator.ModelGenerator;
import com.supermartijn642.core.generator.ResourceCache;
import com.supermartijn642.travelerswaystones.TravelersWaystones;
import com.supermartijn642.travelerswaystones.content.block.WaystoneMaterial;

public class TravelersWaystonesModelGenerator extends ModelGenerator {

    public TravelersWaystonesModelGenerator(ResourceCache cache){
        super(TravelersWaystones.MODID, cache);
    }

    @Override
    public void generate(){
        this.itemGenerated(TravelersWaystones.dimensionalPearl, TravelersWaystones.identifier("item/dimensional_pearl"));
        this.itemGenerated(TravelersWaystones.warpPendant, TravelersWaystones.identifier("item/warp_pendant"));

        for(WaystoneMaterial material : WaystoneMaterial.values()){
            this.model("block/" + material.identifier() + "_waystone_bottom")
                .parent("block/waystone_bottom")
                .texture("waystone", "block/" + material.identifier() + "_waystone_bottom")
                .particleTexture("#waystone");
            this.model("block/" + material.identifier() + "_waystone_bottom_off")
                .parent("block/waystone_bottom_off")
                .texture("waystone", "block/" + material.identifier() + "_waystone_bottom")
                .particleTexture("#waystone");
            this.model("block/" + material.identifier() + "_waystone_top")
                .parent("block/waystone_top")
                .texture("waystone", "block/" + material.identifier() + "_waystone_top")
                .particleTexture("#waystone");
            this.model("block/" + material.identifier() + "_waystone_top_off")
                .parent("block/waystone_top_off")
                .texture("waystone", "block/" + material.identifier() + "_waystone_top")
                .particleTexture("#waystone");
        }
    }
}
