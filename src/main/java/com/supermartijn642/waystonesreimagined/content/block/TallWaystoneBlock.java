package com.supermartijn642.waystonesreimagined.content.block;

import com.supermartijn642.core.block.BlockProperties;
import com.supermartijn642.core.block.BlockShape;
import com.supermartijn642.waystonesreimagined.api.WaystoneType;
import com.supermartijn642.waystonesreimagined.api.targets.WaystoneTarget;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.function.UnaryOperator;

public class TallWaystoneBlock extends WaystoneBlock implements WaystoneType, SimpleWaterloggedBlock {

    public static final BooleanProperty FLIPPED = BooleanProperty.create("flipped");
    public static final EnumProperty<DoubleBlockHalf> HALF = BlockStateProperties.DOUBLE_BLOCK_HALF;
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

    private static final BlockShape SHAPE_BOTTOM, SHAPE_TOP, SHAPE_BOTTOM_FLIPPED, SHAPE_TOP_FLIPPED;
    private static final BlockShape SIMPLE_SHAPE_BOTTOM, SIMPLE_SHAPE_TOP, SIMPLE_SHAPE_BOTTOM_FLIPPED, SIMPLE_SHAPE_TOP_FLIPPED;

    static{
        BlockShape shape = BlockShape.or(
            BlockShape.createBlockShape(1, 0, 1, 15, 1, 15),
            BlockShape.createBlockShape(2, 1, 2, 14, 5, 14),
            BlockShape.createBlockShape(3, 5, 3, 13, 7, 13),
            BlockShape.createBlockShape(4, 7, 4, 12, 19, 12),
            BlockShape.createBlockShape(3, 7, 5, 13, 23, 11),
            BlockShape.createBlockShape(5, 7, 3, 11, 23, 13),
            BlockShape.createBlockShape(3.5, 19, 3.5, 5.5, 21, 5.5),
            BlockShape.createBlockShape(3.5, 19, 10.5, 5.5, 21, 12.5),
            BlockShape.createBlockShape(10.5, 19, 10.5, 12.5, 21, 12.5),
            BlockShape.createBlockShape(10.5, 19, 3.5, 12.5, 21, 5.5),
            BlockShape.createBlockShape(2.5, 21, 2.5, 5.5, 23, 5.5),
            BlockShape.createBlockShape(2.5, 21, 10.5, 5.5, 23, 13.5),
            BlockShape.createBlockShape(10.5, 21, 10.5, 13.5, 23, 13.5),
            BlockShape.createBlockShape(10.5, 21, 2.5, 13.5, 23, 5.5),
            BlockShape.createBlockShape(2, 23, 2, 14, 25, 14),
            BlockShape.createBlockShape(3, 25, 3, 13, 29, 13),
            BlockShape.createBlockShape(2, 25, 2, 4, 29, 4),
            BlockShape.createBlockShape(2, 25, 12, 4, 29, 14),
            BlockShape.createBlockShape(12, 25, 12, 14, 29, 14),
            BlockShape.createBlockShape(12, 25, 2, 14, 29, 4),
            BlockShape.createBlockShape(2, 29, 2, 14, 31, 14),
            BlockShape.createBlockShape(4, 31, 4, 12, 32, 12)
        );
        SHAPE_BOTTOM = shape;
        SHAPE_TOP = shape.offset(0, -1, 0);
        BlockShape shapeFlipped = shape.flip(Direction.Axis.Y);
        SHAPE_BOTTOM_FLIPPED = shapeFlipped;
        SHAPE_TOP_FLIPPED = shapeFlipped.offset(0, 1, 0);
        SIMPLE_SHAPE_BOTTOM = BlockShape.or(
            BlockShape.createBlockShape(1, 0, 1, 15, 5, 15),
            BlockShape.createBlockShape(3, 5, 3, 13, 16, 13)
        );
        SIMPLE_SHAPE_TOP = BlockShape.or(
            BlockShape.createBlockShape(3, 0, 3, 13, 7, 13),
            BlockShape.createBlockShape(2, 7, 2, 14, 9, 14),
            BlockShape.createBlockShape(2, 7, 2, 14, 15, 14),
            BlockShape.createBlockShape(4, 15, 4, 12, 16, 12)
        );
        SIMPLE_SHAPE_BOTTOM_FLIPPED = SIMPLE_SHAPE_BOTTOM.flip(Direction.Axis.Y);
        SIMPLE_SHAPE_TOP_FLIPPED = SIMPLE_SHAPE_TOP.flip(Direction.Axis.Y);
    }

    public TallWaystoneBlock(BlockProperties properties){
        super(properties.pushReaction(PushReaction.IMMOVEABLE));
        this.registerDefaultState(
            this.defaultBlockState()
                .setValue(FLIPPED, false)
                .setValue(HALF, DoubleBlockHalf.LOWER)
                .setValue(WATERLOGGED, false)
        );
    }

    @Override
    public Map<BlockPos,BlockState> getPreview(BlockState state){
        BlockPos nonBasePos = state.getValue(FLIPPED) ? BlockPos.ZERO.below() : BlockPos.ZERO.above();
        return Map.of(
            BlockPos.ZERO, state.setValue(HALF, DoubleBlockHalf.LOWER).setValue(WATERLOGGED, false).setValue(DISCOVERED, true),
            nonBasePos, state.setValue(HALF, DoubleBlockHalf.UPPER).setValue(WATERLOGGED, false).setValue(DISCOVERED, true)
        );
    }

    @Override
    public BlockPos getBasePos(BlockPos pos, BlockState state){
        return state.getValue(HALF) == DoubleBlockHalf.LOWER ? pos : state.getValue(FLIPPED) ? pos.above() : pos.below();
    }

    @Override
    public Map<BlockPos,BlockState> getPlacementPreview(BlockState waystone, Level level, Player player, BlockHitResult hitResult){
        BlockPlaceContext context = new BlockPlaceContext(level, player, InteractionHand.MAIN_HAND, ItemStack.EMPTY, hitResult);
        if(!context.canPlace())
            return Map.of();
        BlockState state = this.getStateForPlacement(context);
        if(state == null)
            return Map.of();
        state = state.setValue(WATERLOGGED, false);
        BlockPos pos = context.getClickedPos();
        boolean flipped = state.getValue(FLIPPED);
        DoubleBlockHalf half = state.getValue(HALF);
        BlockPos otherHalfPos = (half == DoubleBlockHalf.LOWER) == flipped ? pos.below() : pos.above();
        return Map.of(
            pos, state,
            otherHalfPos, state.setValue(HALF, half.getOtherHalf())
        );
    }

    @Override
    public MoveResult moveWaystone(BlockState waystone, Level level, Player player, WaystoneTarget current, Level toLevel, BlockHitResult toPosition){
        // Place new blocks
        BlockPlaceContext context = new BlockPlaceContext(toLevel, player, InteractionHand.MAIN_HAND, ItemStack.EMPTY, toPosition);
        if(!context.canPlace())
            return MoveResult.failure(null);
        BlockState state = this.getStateForPlacement(context);
        if(state == null)
            return MoveResult.failure(null);
        BlockPos pos = context.getClickedPos();
        boolean flipped = state.getValue(FLIPPED);
        DoubleBlockHalf half = state.getValue(HALF);
        BlockPos otherHalfPos = (half == DoubleBlockHalf.LOWER) == flipped ? pos.below() : pos.above();
        if(!toLevel.setBlock(pos, state, Block.UPDATE_ALL)
            || !toLevel.setBlock(otherHalfPos, state.setValue(HALF, half.getOtherHalf()), Block.UPDATE_ALL))
            return MoveResult.failure(null);

        // Remove old blocks
        pos = current.pos();
        flipped = waystone.getValue(FLIPPED);
        half = waystone.getValue(HALF);
        otherHalfPos = (half == DoubleBlockHalf.LOWER) == flipped ? pos.below() : pos.above();
        if(!level.removeBlock(pos, false) || !level.removeBlock(otherHalfPos, false))
            return MoveResult.failure(null);
        return MoveResult.success(context.getClickedPos(), state);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context){
        return (state.getValue(FLIPPED) ?
            state.getValue(HALF) == DoubleBlockHalf.LOWER ? SHAPE_BOTTOM_FLIPPED : SHAPE_TOP_FLIPPED :
            state.getValue(HALF) == DoubleBlockHalf.LOWER ? SHAPE_BOTTOM : SHAPE_TOP).getUnderlying();
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context){
        return (state.getValue(FLIPPED) ?
            state.getValue(HALF) == DoubleBlockHalf.LOWER ? SIMPLE_SHAPE_BOTTOM_FLIPPED : SIMPLE_SHAPE_TOP_FLIPPED :
            state.getValue(HALF) == DoubleBlockHalf.LOWER ? SIMPLE_SHAPE_BOTTOM : SIMPLE_SHAPE_TOP).getUnderlying();
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context){
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        boolean waterlogged = context.getLevel().getFluidState(pos).getType() == Fluids.WATER;
        BlockState state = super.getStateForPlacement(context).setValue(WATERLOGGED, waterlogged);
        boolean isBelowClear = level.getBlockState(pos.below()).canBeReplaced();
        if(context.getClickedFace() == Direction.DOWN && isBelowClear)
            return state.setValue(FLIPPED, true).setValue(HALF, DoubleBlockHalf.LOWER);
        boolean isBelowBelowSolid = level.getBlockState(pos.below().below()).isFaceSturdy(level, pos.below().below(), Direction.UP);
        if(isBelowClear && isBelowBelowSolid)
            return state.setValue(FLIPPED, false).setValue(HALF, DoubleBlockHalf.UPPER);
        boolean isBelowSolid = level.getBlockState(pos.below()).isFaceSturdy(level, pos.below(), Direction.UP);
        boolean isAboveClear = level.getBlockState(pos.above()).canBeReplaced();
        if(isBelowSolid && isAboveClear)
            return state.setValue(FLIPPED, false).setValue(HALF, DoubleBlockHalf.LOWER);
        boolean isAboveSolid = level.getBlockState(pos.above()).isFaceSturdy(level, pos.below(), Direction.DOWN);
        if(isBelowClear && isAboveSolid)
            return state.setValue(FLIPPED, true).setValue(HALF, DoubleBlockHalf.LOWER);
        boolean isAboveAboveSolid = level.getBlockState(pos.below().below()).isFaceSturdy(level, pos.below().below(), Direction.DOWN);
        if(isAboveClear && isAboveAboveSolid)
            return state.setValue(FLIPPED, true).setValue(HALF, DoubleBlockHalf.UPPER);
        if(isAboveClear)
            return state.setValue(FLIPPED, false).setValue(HALF, DoubleBlockHalf.LOWER);
        if(isBelowClear)
            return state.setValue(FLIPPED, false).setValue(HALF, DoubleBlockHalf.UPPER);
        return null;
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack){
        boolean flipped = state.getValue(FLIPPED);
        DoubleBlockHalf half = state.getValue(HALF);
        BlockPos otherHalfPos = (half == DoubleBlockHalf.LOWER) == flipped ? pos.below() : pos.above();
        boolean waterlogged = level.getFluidState(otherHalfPos).getType() == Fluids.WATER;
        level.setBlockAndUpdate(
            otherHalfPos,
            state.setValue(HALF, half.getOtherHalf())
                .setValue(WATERLOGGED, waterlogged)
        );
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos){
        boolean flipped = state.getValue(FLIPPED);
        DoubleBlockHalf half = state.getValue(HALF);
        BlockPos otherHalfPos = (half == DoubleBlockHalf.LOWER) == flipped ? pos.below() : pos.above();
        BlockState otherHalf = level.getBlockState(otherHalfPos);
        return otherHalf.is(this) && otherHalf.getValue(FLIPPED) == flipped && otherHalf.getValue(HALF) != half;
    }

    @Override
    public void destroy(LevelAccessor level, BlockPos pos, BlockState state){
        super.destroy(level, pos, state);
        this.destroyOtherHalf(level, pos, state, null);
    }

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player){
        BlockState adjustedState = super.playerWillDestroy(level, pos, state, player);
        this.destroyOtherHalf(level, pos, state, player);
        return adjustedState;
    }

    private void destroyOtherHalf(LevelAccessor level, BlockPos pos, BlockState state, @Nullable Player player){
        if(!level.isClientSide()){
            boolean flipped = state.getValue(FLIPPED);
            DoubleBlockHalf half = state.getValue(HALF);
            BlockPos otherHalfPos = (half == DoubleBlockHalf.LOWER) == flipped ? pos.below() : pos.above();
            BlockState otherHalf = level.getBlockState(otherHalfPos);
            if(otherHalf.is(this) && otherHalf.getValue(FLIPPED) == flipped && otherHalf.getValue(HALF) != half){
                level.setBlock(
                    otherHalfPos,
                    otherHalf.getValue(WATERLOGGED) ? Blocks.WATER.defaultBlockState() : Blocks.AIR.defaultBlockState(),
                    Block.UPDATE_CLIENTS | Block.UPDATE_SUPPRESS_DROPS
                );
                level.levelEvent(player, LevelEvent.PARTICLES_DESTROY_BLOCK, otherHalfPos, Block.getId(otherHalf));
            }
        }
    }

    @Override
    public FluidState getFluidState(BlockState state){
        return state.getValue(BlockStateProperties.WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> builder){
        super.createBlockStateDefinition(builder);
        builder.add(FLIPPED);
        builder.add(HALF);
        builder.add(WATERLOGGED);
    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rotation){
        return this.transform(super.rotate(state, rotation), rotation::rotate);
    }

    @Override
    protected BlockState mirror(BlockState state, Mirror mirror){
        return this.transform(super.mirror(state, mirror), mirror::mirror);
    }

    private BlockState transform(BlockState state, UnaryOperator<Direction> transformation){
        boolean flipped = state.getValue(FLIPPED);
        Direction down = flipped ? Direction.UP : Direction.DOWN;
        if(transformation.apply(down) == down.getOpposite())
            return state.setValue(FLIPPED, !state.getValue(FLIPPED)).setValue(HALF, state.getValue(HALF).getOtherHalf());
        return state;
    }

    @Override
    protected long getSeed(BlockState state, BlockPos pos){
        if(state.getValue(HALF) == DoubleBlockHalf.UPPER)
            pos = state.getValue(FLIPPED) ? pos.above() : pos.below();
        return super.getSeed(state, pos);
    }
}
