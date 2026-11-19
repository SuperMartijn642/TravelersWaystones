package com.supermartijn642.travelerswaystones.generators;

import com.supermartijn642.core.generator.ResourceCache;
import com.supermartijn642.core.generator.TagGenerator;
import com.supermartijn642.travelerswaystones.TravelersWaystones;
import com.supermartijn642.travelerswaystones.content.block.WaystoneMaterial;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

public class TravelersWaystonesTagGenerator extends TagGenerator {

    public TravelersWaystonesTagGenerator(ResourceCache cache){
        super(TravelersWaystones.MODID, cache);
    }

    @Override
    public void generate(){
        for(WaystoneMaterial material : WaystoneMaterial.values()){
            this.blockTag("waystones").add(material.block());
            this.itemTag("waystones").add(material.item());
            for(TagKey<Block> tag : material.tags())
                this.blockTag(tag).add(material.block());
        }
    }
}
