package com.supermartijn642.travelerswaystones.api.client;

import com.supermartijn642.travelerswaystones.content.block.WaystoneBlockStateModel;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Client-side helpers for waystones. May only be used on the client.
 */
public final class WaystoneClient {

    /**
     * Whether the client's player has discovered the waystone that the given block belongs to.
     * The blocks around a target are re-rendered when the player discovers or undiscovers it.
     * @param level level or render region that contains the block
     * @param pos   position of the block
     * @param state block of the waystone
     * @return whether the player discovered the waystone, false if the block has no registered waystone type
     */
    public static boolean isDiscovered(BlockAndTintGetter level, BlockPos pos, BlockState state){
        return WaystoneBlockStateModel.isDiscovered(level, pos, state);
    }
}
