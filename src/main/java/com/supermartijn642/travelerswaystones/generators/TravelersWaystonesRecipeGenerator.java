package com.supermartijn642.travelerswaystones.generators;

import com.supermartijn642.core.generator.RecipeGenerator;
import com.supermartijn642.core.generator.ResourceCache;
import com.supermartijn642.travelerswaystones.TravelersWaystones;
import com.supermartijn642.travelerswaystones.content.block.WaystoneMaterial;
import net.minecraftforge.common.Tags;

public class TravelersWaystonesRecipeGenerator extends RecipeGenerator {

    public TravelersWaystonesRecipeGenerator(ResourceCache cache){
        super(TravelersWaystones.MODID, cache);
    }

    @Override
    public void generate(){
        this.shaped(TravelersWaystones.dimensionalPearl)
            .pattern(" D ")
            .pattern("DED")
            .pattern(" D ")
            .input('D', Tags.Items.GEMS_DIAMOND)
            .input('E', Tags.Items.ENDER_PEARLS)
            .unlockedBy(Tags.Items.ENDER_PEARLS);
        this.shaped(TravelersWaystones.warpPendant)
            .pattern("GGG")
            .pattern("GSG")
            .pattern(" D ")
            .input('G', Tags.Items.INGOTS_GOLD)
            .input('S', Tags.Items.NETHER_STARS)
            .input('D', TravelersWaystones.dimensionalPearl)
            .unlockedBy(TravelersWaystones.dimensionalPearl);

        // Waystones
        for(WaystoneMaterial material : WaystoneMaterial.values()){
            this.shaped(material.block())
                .pattern(" S ")
                .pattern(" D ")
                .pattern("SSS")
                .input('S', material.craftingInput().get())
                .input('D', TravelersWaystones.dimensionalPearl)
                .unlockedBy(TravelersWaystones.dimensionalPearl);
        }
    }
}
