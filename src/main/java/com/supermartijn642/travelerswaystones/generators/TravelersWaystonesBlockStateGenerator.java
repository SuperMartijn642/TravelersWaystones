package com.supermartijn642.travelerswaystones.generators;

import com.supermartijn642.core.generator.BlockStateGenerator;
import com.supermartijn642.core.generator.ResourceCache;
import com.supermartijn642.travelerswaystones.TravelersWaystones;
import com.supermartijn642.travelerswaystones.content.block.TallWaystoneBlock;
import com.supermartijn642.travelerswaystones.content.block.WaystoneBlock;
import com.supermartijn642.travelerswaystones.content.block.WaystoneMaterial;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;

public class TravelersWaystonesBlockStateGenerator extends BlockStateGenerator {

    public TravelersWaystonesBlockStateGenerator(ResourceCache cache){
        super(TravelersWaystones.MODID, cache);
    }

    @Override
    public void generate(){
        for(WaystoneMaterial material : WaystoneMaterial.values()){
            this.blockState(material.block()).variantsForAllExcept((state, variant) -> {
                boolean discovered = state.get(WaystoneBlock.DISCOVERED);
                boolean flipped = state.get(TallWaystoneBlock.FLIPPED);
                DoubleBlockHalf half = state.get(TallWaystoneBlock.HALF);
                String model = "block/" + material.identifier() + (half == DoubleBlockHalf.LOWER ? "_waystone_bottom" : "_waystone_top");
                if(!discovered)
                    model += "_off";
                variant.model(
                    model,
                    flipped ? 180 : 0,
                    0
                );
            }, TallWaystoneBlock.WATERLOGGED);
        }
    }
}
