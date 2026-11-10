package com.supermartijn642.travelerswaystones.content.block;

import com.supermartijn642.core.TextComponents;
import com.supermartijn642.core.block.BaseBlock;
import com.supermartijn642.core.block.BlockProperties;
import com.supermartijn642.travelerswaystones.api.WaystoneType;
import com.supermartijn642.travelerswaystones.api.targets.GlobalWaystoneManager;
import com.supermartijn642.travelerswaystones.api.targets.WaystoneTarget;
import com.supermartijn642.travelerswaystones.api.targets.personal.DiscoveredWaystoneTarget;
import com.supermartijn642.travelerswaystones.api.targets.personal.PersonalWaystoneManager;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class WaystoneBlock extends BaseBlock implements WaystoneType {

    public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty DISCOVERED = BooleanProperty.create("discovered");

    public WaystoneBlock(BlockProperties properties){
        super(false, properties);
        this.registerDefaultState(this.defaultBlockState().setValue(FACING, Direction.NORTH).setValue(DISCOVERED, false));
    }

    @Override
    protected InteractionFeedback interact(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, Direction hitSide, Vec3 hitLocation){
        PersonalWaystoneManager manager = PersonalWaystoneManager.get(player);
        BlockPos basePos = this.getTargetPos(state, pos);
        DiscoveredWaystoneTarget discoveredTarget = manager.getTargetAt(level, basePos);
        if(discoveredTarget == null){
            if(player.level() != level)
                return InteractionFeedback.CONSUME;
            if(!level.isClientSide()){
                WaystoneTarget target = GlobalWaystoneManager.getTargetAt(level, basePos);
                if(target == null){
                    GlobalWaystoneManager.ClaimScreenResult result = GlobalWaystoneManager.displayClaimScreen(player, level, basePos, false, null);
                    if(result instanceof GlobalWaystoneManager.ClaimScreenResult.Denied denied)
                        player.sendOverlayMessage(TextComponents.fromTextComponent(denied.reasonOrDefault()).color(ChatFormatting.RED).get());
                }else
                    manager.discover(target);
            }
        }else if(level.isClientSide())
            manager.openTeleportScreen(discoveredTarget);
        return InteractionFeedback.SUCCESS;
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context){
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> builder){
        super.createBlockStateDefinition(builder);
        builder.add(FACING, DISCOVERED);
    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rotation){
        Direction facing = rotation.rotate(state.getValue(FACING));
        return facing.getAxis() == Direction.Axis.Y ? state : state.setValue(FACING, facing);
    }

    @Override
    protected BlockState mirror(BlockState state, Mirror mirror){
        Direction facing = mirror.mirror(state.getValue(FACING));
        return facing.getAxis() == Direction.Axis.Y ? state : state.setValue(FACING, facing);
    }
}
