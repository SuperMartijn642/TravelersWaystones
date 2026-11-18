package com.supermartijn642.travelerswaystones.content.block;

import com.supermartijn642.core.item.BaseBlockItem;
import com.supermartijn642.core.item.ItemProperties;
import com.supermartijn642.travelerswaystones.TravelersWaystonesClient;
import com.supermartijn642.travelerswaystones.api.WaystoneType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class WaystoneItem extends BaseBlockItem {

    public WaystoneItem(Block block, ItemProperties properties){
        super(block, properties);
    }

    @Override
    public InteractionResult place(BlockPlaceContext context){
        InteractionResult result = super.place(context);
        if(result == InteractionResult.SUCCESS && context.getLevel().isClientSide()){
            BlockPos pos = context.getClickedPos();
            BlockState state = context.getLevel().getBlockState(pos);
            TravelersWaystonesClient.openClaimScreen(WaystoneType.get(state.getBlock()).getTargetPos(state, pos), true, null);
        }
        return result;
    }
}
