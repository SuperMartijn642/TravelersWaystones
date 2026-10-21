package com.supermartijn642.waystonesreimagined.generators;

import com.supermartijn642.core.generator.ModelGenerator;
import com.supermartijn642.core.generator.ResourceCache;
import com.supermartijn642.waystonesreimagined.WaystonesReimagined;
import com.supermartijn642.waystonesreimagined.content.block.WaystoneMaterial;

/**
 * Created 30/08/2026 by SuperMartijn642
 */
public class WaystonesReimaginedModelGenerator extends ModelGenerator {

    public WaystonesReimaginedModelGenerator(ResourceCache cache){
        super(WaystonesReimagined.MODID, cache);
    }

    @Override
    public void generate(){
        this.itemGenerated(WaystonesReimagined.dimensionalPearl, WaystonesReimagined.identifier("item/dimensional_pearl"));
        this.itemGenerated(WaystonesReimagined.warpPendant, WaystonesReimagined.identifier("item/warp_pendant"));

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
