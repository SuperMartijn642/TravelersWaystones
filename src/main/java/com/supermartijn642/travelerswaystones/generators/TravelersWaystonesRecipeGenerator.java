package com.supermartijn642.travelerswaystones.generators;

import com.supermartijn642.core.generator.RecipeGenerator;
import com.supermartijn642.core.generator.ResourceCache;
import com.supermartijn642.travelerswaystones.TravelersWaystones;
import com.supermartijn642.travelerswaystones.content.block.WaystoneMaterial;
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalItemTags;
import net.minecraft.world.item.Items;

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
            .input('D', ConventionalItemTags.DIAMOND_GEMS)
            .input('E', ConventionalItemTags.ENDER_PEARLS)
            .unlockedBy(ConventionalItemTags.ENDER_PEARLS);
        this.shaped(TravelersWaystones.warpPendant)
            .pattern("GGG")
            .pattern("GSG")
            .pattern(" D ")
            .input('G', ConventionalItemTags.GOLD_INGOTS)
            .input('S', Items.NETHER_STAR)
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
