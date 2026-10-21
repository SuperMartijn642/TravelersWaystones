package com.supermartijn642.waystonesreimagined.generators;

import com.mojang.math.Transformation;
import com.supermartijn642.core.generator.ItemInfoGenerator;
import com.supermartijn642.core.generator.ResourceCache;
import com.supermartijn642.waystonesreimagined.WaystonesReimagined;
import com.supermartijn642.waystonesreimagined.content.block.WaystoneMaterial;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public class WaystonesReimaginedItemInfoGenerator extends ItemInfoGenerator {

    public WaystonesReimaginedItemInfoGenerator(ResourceCache cache){
        super(WaystonesReimagined.MODID, cache);
    }

    @Override
    public void generate(){
        for(WaystoneMaterial material : WaystoneMaterial.values()){
            this.info(material.item())
                .model(
                    this.compositeModel()
                        .setTransformation(new Transformation(new Matrix4f().identity().translate(0.5f, 0.5f, 0.5f).scale(0.75f).translate(-0.5f, -0.5f, -0.5f)))
                        .addModel(this.model("block/" + material.identifier() + "_waystone_bottom").setTransformation(new Transformation(new Vector3f(0, -0.5f, 0), null, null, null)))
                        .addModel(this.model("block/" + material.identifier() + "_waystone_top").setTransformation(new Transformation(new Vector3f(0, 0.5f, 0), null, null, null)))
                );
        }
        this.simpleInfo(WaystonesReimagined.dimensionalPearl, "item/dimensional_pearl");
        this.simpleInfo(WaystonesReimagined.warpPendant, "item/warp_pendant");
    }
}
