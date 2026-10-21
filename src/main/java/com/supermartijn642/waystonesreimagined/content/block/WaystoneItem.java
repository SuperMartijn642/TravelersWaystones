package com.supermartijn642.waystonesreimagined.content.block;

import com.supermartijn642.core.item.BaseBlockItem;
import com.supermartijn642.core.item.ItemProperties;
import com.supermartijn642.waystonesreimagined.WaystonesReimaginedClient;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;

/**
 * Created 04/09/2026 by SuperMartijn642
 */
public class WaystoneItem extends BaseBlockItem {

    public WaystoneItem(Block block, ItemProperties properties){
        super(block, properties);
    }

    @Override
    public InteractionResult place(BlockPlaceContext context){
        InteractionResult result = super.place(context);
        if(result == InteractionResult.SUCCESS && context.getLevel().isClientSide())
            WaystonesReimaginedClient.openClaimScreen(context.getClickedPos(), true, null);
        return result;
    }
}
