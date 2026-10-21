package com.supermartijn642.waystonesreimagined.generators;

import com.supermartijn642.core.generator.RecipeGenerator;
import com.supermartijn642.core.generator.ResourceCache;
import com.supermartijn642.waystonesreimagined.WaystonesReimagined;
import com.supermartijn642.waystonesreimagined.content.block.WaystoneMaterial;
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalItemTags;

/**
 * Created 30/08/2026 by SuperMartijn642
 */
public class WaystonesReimaginedRecipeGenerator extends RecipeGenerator {

    public WaystonesReimaginedRecipeGenerator(ResourceCache cache){
        super(WaystonesReimagined.MODID, cache);
    }

    @Override
    public void generate(){
        this.shaped(WaystonesReimagined.dimensionalPearl)
            .pattern(" D ")
            .pattern("DED")
            .pattern(" D ")
            .input('D', ConventionalItemTags.DIAMOND_GEMS)
            .input('E', ConventionalItemTags.ENDER_PEARLS)
            .unlockedBy(ConventionalItemTags.ENDER_PEARLS);
        this.shaped(WaystonesReimagined.warpPendant)
            .pattern("GGG")
            .pattern("GSG")
            .pattern(" D ")
            .input('G', ConventionalItemTags.GOLD_INGOTS)
            .input('S', ConventionalItemTags.NETHER_STARS)
            .input('D', WaystonesReimagined.dimensionalPearl)
            .unlockedBy(WaystonesReimagined.dimensionalPearl);

        // Waystones
        for(WaystoneMaterial material : WaystoneMaterial.values()){
            this.shaped(material.block())
                .pattern(" S ")
                .pattern(" D ")
                .pattern("SSS")
                .input('S', material.craftingInput().get())
                .input('D', WaystonesReimagined.dimensionalPearl)
                .unlockedBy(WaystonesReimagined.dimensionalPearl);
        }
    }
}
