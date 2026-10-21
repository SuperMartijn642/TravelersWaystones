package com.supermartijn642.waystonesreimagined.generators;

import com.supermartijn642.core.generator.ResourceCache;
import com.supermartijn642.core.generator.TagGenerator;
import com.supermartijn642.waystonesreimagined.WaystonesReimagined;
import com.supermartijn642.waystonesreimagined.content.block.WaystoneMaterial;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

public class WaystonesReimaginedTagGenerator extends TagGenerator {

    public WaystonesReimaginedTagGenerator(ResourceCache cache){
        super(WaystonesReimagined.MODID, cache);
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
