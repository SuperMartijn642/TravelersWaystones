package com.supermartijn642.travelerswaystones.content.block;

import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.MapColor;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;
import java.util.function.Supplier;

public enum WaystoneMaterial {

    ANDESITE("Andesite Waystone", SoundType.STONE, 1.5f, 6, true, MapColor.STONE, 0, () -> Items.ANDESITE, BlockTags.MINEABLE_WITH_PICKAXE),
    BLACKSTONE("Blackstone Waystone", SoundType.STONE, 1.5f, 6, true, MapColor.COLOR_BLACK, 0, () -> Items.BLACKSTONE, BlockTags.MINEABLE_WITH_PICKAXE),
    COBBLESTONE("Cobblestone Waystone", SoundType.STONE, 2, 6, true, MapColor.STONE, 0, () -> Items.COBBLESTONE, BlockTags.MINEABLE_WITH_PICKAXE),
    COBBLED_DEEPSLATE("Cobbled Deepslate Waystone", SoundType.DEEPSLATE, 3.5f, 6, true, MapColor.DEEPSLATE, 10, () -> Items.COBBLED_DEEPSLATE, BlockTags.MINEABLE_WITH_PICKAXE),
    CRYING_OBSIDIAN("Crying Obsidian Waystone", SoundType.STONE, 50, 1200, true, MapColor.COLOR_BLACK, 10, () -> Items.CRYING_OBSIDIAN, BlockTags.MINEABLE_WITH_PICKAXE),
    DEEPSLATE_BRICK("Deepslate Brick Waystone", SoundType.DEEPSLATE_BRICKS, 3.5f, 6, true, MapColor.DEEPSLATE, 10, () -> Items.DEEPSLATE_BRICKS, BlockTags.MINEABLE_WITH_PICKAXE),
    DIORITE("Diorite Waystone", SoundType.STONE, 1.5f, 6, true, MapColor.QUARTZ, 0, () -> Items.DIORITE, BlockTags.MINEABLE_WITH_PICKAXE),
    END_STONE("End Stone Waystone", SoundType.STONE, 3, 9, true, MapColor.SAND, 0, () -> Items.END_STONE, BlockTags.MINEABLE_WITH_PICKAXE),
    GRANITE("Granite Waystone", SoundType.STONE, 1.5f, 6, true, MapColor.DIRT, 0, () -> Items.GRANITE, BlockTags.MINEABLE_WITH_PICKAXE),
    MUD_BRICK("Mud Brick Waystone", SoundType.MUD_BRICKS, 1.5f, 3, true, MapColor.TERRACOTTA_LIGHT_GRAY, 0, () -> Items.MUD_BRICKS, BlockTags.MINEABLE_WITH_PICKAXE),
    NETHERRACK("Netherrack Waystone", SoundType.NETHERRACK, 0.4f, 0.4f, true, MapColor.NETHER, 0, () -> Items.NETHERRACK, BlockTags.MINEABLE_WITH_PICKAXE),
    NETHER_BRICK("Nether Brick Waystone", SoundType.NETHER_BRICKS, 2, 6, true, MapColor.NETHER, 0, () -> Items.NETHER_BRICK, BlockTags.MINEABLE_WITH_PICKAXE),
    OBSIDIAN("Obsidian Waystone", SoundType.STONE, 50, 1200, true, MapColor.COLOR_BLACK, 0, () -> Items.OBSIDIAN, BlockTags.MINEABLE_WITH_PICKAXE),
    PURPUR("Purpur Waystone", SoundType.STONE, 1.5f, 6, true, MapColor.COLOR_MAGENTA, 0, () -> Items.PURPUR_BLOCK, BlockTags.MINEABLE_WITH_PICKAXE),
    RED_NETHER_BRICK("Red Nether Brick Waystone", SoundType.NETHER_BRICKS, 2, 6, true, MapColor.NETHER, 0, () -> Items.RED_NETHER_BRICKS, BlockTags.MINEABLE_WITH_PICKAXE),
    RED_SANDSTONE("Red Sandstone Waystone", SoundType.STONE, 0.8f, 0.8f, true, MapColor.COLOR_ORANGE, 0, () -> Items.RED_SANDSTONE, BlockTags.MINEABLE_WITH_PICKAXE),
    SANDSTONE("Sandstone Waystone", SoundType.STONE, 0.8f, 0.8f, true, MapColor.SAND, 0, () -> Items.SANDSTONE, BlockTags.MINEABLE_WITH_PICKAXE),
    STONE("Stone Waystone", SoundType.STONE, 1.5f, 6, true, MapColor.STONE, 0, () -> Items.STONE, BlockTags.MINEABLE_WITH_PICKAXE),
    TUFF("Tuff Waystone", SoundType.TUFF, 1.5f, 6, true, MapColor.TERRACOTTA_GRAY, 0, () -> Items.TUFF_BRICKS, BlockTags.MINEABLE_WITH_PICKAXE);

    private final String identifier = this.name().toLowerCase(Locale.ROOT);
    private final String translation;
    private final SoundType soundType;
    private final float destroyTime;
    private final float explosionResistance;
    private final boolean requiresToolForDrops;
    private final MapColor mapColor;
    private final int lightLevel;
    private final Supplier<ItemLike> craftingInput;
    private final TagKey<Block>[] tags;
    private TallWaystoneBlock block;
    private TallWaystoneItem item;

    @SafeVarargs
    WaystoneMaterial(String translation, SoundType soundType, float destroyTime, float explosionResistance, boolean requiresToolForDrops, MapColor mapColor, int lightLevel, Supplier<ItemLike> craftingInput, @Nullable TagKey<Block>... tags) {
        this.translation = translation;
        this.soundType = soundType;
        this.destroyTime = destroyTime;
        this.explosionResistance = explosionResistance;
        this.requiresToolForDrops = requiresToolForDrops;
        this.mapColor = mapColor;
        this.lightLevel = lightLevel;
        this.craftingInput = craftingInput;
        this.tags = tags;
    }

    public String identifier(){
        return this.identifier;
    }

    public String translation(){
        return this.translation;
    }

    public SoundType soundType(){
        return this.soundType;
    }

    public float destroyTime(){
        return this.destroyTime;
    }

    public float explodeResistance(){
        return this.explosionResistance;
    }

    public boolean requiresToolForDrops(){
        return this.requiresToolForDrops;
    }

    public MapColor mapColor(){
        return this.mapColor;
    }

    public int lightLevel(){
        return this.lightLevel;
    }

    public Supplier<ItemLike> craftingInput(){
        return this.craftingInput;
    }

    public TagKey<Block>[] tags(){
        return this.tags;
    }

    public void setBlock(TallWaystoneBlock block){
        this.block = block;
    }

    public TallWaystoneBlock block(){
        return this.block;
    }

    public void setItem(TallWaystoneItem item){
        this.item = item;
    }

    public TallWaystoneItem item(){
        return this.item;
    }
}
