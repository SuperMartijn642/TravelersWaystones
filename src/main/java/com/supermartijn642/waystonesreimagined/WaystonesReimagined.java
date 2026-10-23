package com.supermartijn642.waystonesreimagined;

import com.supermartijn642.core.CommonUtils;
import com.supermartijn642.core.block.BlockProperties;
import com.supermartijn642.core.item.BaseItem;
import com.supermartijn642.core.item.CreativeItemGroup;
import com.supermartijn642.core.item.ItemProperties;
import com.supermartijn642.core.network.PacketChannel;
import com.supermartijn642.core.network.PacketDirection;
import com.supermartijn642.core.registry.GeneratorRegistrationHandler;
import com.supermartijn642.core.registry.RegistrationHandler;
import com.supermartijn642.core.registry.RegistryEntryAcceptor;
import com.supermartijn642.waystonesreimagined.api.WaystoneType;
import com.supermartijn642.waystonesreimagined.content.WarpPendant;
import com.supermartijn642.waystonesreimagined.content.block.TallWaystoneBlock;
import com.supermartijn642.waystonesreimagined.content.block.TallWaystoneItem;
import com.supermartijn642.waystonesreimagined.content.block.WaystoneMaterial;
import com.supermartijn642.waystonesreimagined.content.packets.*;
import com.supermartijn642.waystonesreimagined.content.particle.ScanParticleType;
import com.supermartijn642.waystonesreimagined.generators.*;
import net.fabricmc.api.ModInitializer;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;

import java.util.Random;

public class WaystonesReimagined implements ModInitializer {

    public static final String MODID = "waystonesreimagined";

    public static Identifier identifier(String path){
        return Identifier.fromNamespaceAndPath(MODID, path);
    }

    public static final Logger LOGGER = CommonUtils.getLogger(MODID);
    public static final PacketChannel CHANNEL = PacketChannel.create(MODID);

    @RegistryEntryAcceptor(namespace = MODID, identifier = "discover", registry = RegistryEntryAcceptor.Registry.PARTICLE_TYPES)
    public static ScanParticleType discoverParticle;
    @RegistryEntryAcceptor(namespace = MODID, identifier = "teleport", registry = RegistryEntryAcceptor.Registry.PARTICLE_TYPES)
    public static ScanParticleType teleportParticle;
    @RegistryEntryAcceptor(namespace = MODID, identifier = "dimensional_pearl", registry = RegistryEntryAcceptor.Registry.ITEMS)
    public static BaseItem dimensionalPearl;
    @RegistryEntryAcceptor(namespace = MODID, identifier = "warp_pendant", registry = RegistryEntryAcceptor.Registry.ITEMS)
    public static WarpPendant warpPendant;

    public static final CreativeItemGroup CREATIVE_GROUP = CreativeItemGroup.create(MODID, () -> WaystoneMaterial.values()[new Random().nextInt(WaystoneMaterial.values().length)].item());

    @Override
    public void onInitialize(){
        CHANNEL.registerMessage(ClaimWaystonePacket.class, ClaimWaystonePacket::new, PacketDirection.CLIENT_TO_SERVER, true);
        CHANNEL.registerMessage(DiscoverTargetPacket.class, DiscoverTargetPacket::new, PacketDirection.SERVER_TO_CLIENT, true);
        CHANNEL.registerMessage(FavoriteTargetPacket.class, FavoriteTargetPacket::new, PacketDirection.BOTH_WAYS, true);
        CHANNEL.registerMessage(MoveWaystonePacket.class, MoveWaystonePacket::new, PacketDirection.CLIENT_TO_SERVER, true);
        CHANNEL.registerMessage(OpenClaimScreenPacket.class, OpenClaimScreenPacket::new, PacketDirection.SERVER_TO_CLIENT, true);
        CHANNEL.registerMessage(PersonalTargetsPacket.class, PersonalTargetsPacket::new, PacketDirection.SERVER_TO_CLIENT, true);
        CHANNEL.registerMessage(RecentTargetPacket.class, RecentTargetPacket::new, PacketDirection.SERVER_TO_CLIENT, true);
        CHANNEL.registerMessage(UndiscoverTargetPacket.class, UndiscoverTargetPacket::new, PacketDirection.BOTH_WAYS, true);
        CHANNEL.registerMessage(RenameTargetPacket.class, RenameTargetPacket::new, PacketDirection.BOTH_WAYS, true);
        CHANNEL.registerMessage(SetTargetSuppressDangerousPacket.class, SetTargetSuppressDangerousPacket::new, PacketDirection.BOTH_WAYS, true);
        CHANNEL.registerMessage(TeleportPacket.class, TeleportPacket::new, PacketDirection.CLIENT_TO_SERVER, true);
        CHANNEL.registerMessage(UpdateTargetPositionPacket.class, UpdateTargetPositionPacket::new, PacketDirection.SERVER_TO_CLIENT, true);
        CHANNEL.registerMessage(WarnDangerousPacket.class, WarnDangerousPacket::new, PacketDirection.SERVER_TO_CLIENT, true);

        register();
        if(CommonUtils.getEnvironmentSide().isClient())
            registerGenerators();
    }

    private static void register(){
        RegistrationHandler handler = RegistrationHandler.get(MODID);
        for(WaystoneMaterial material : WaystoneMaterial.values()){
            handler.registerBlock(material.identifier() + "_waystone", () -> {
                TallWaystoneBlock block = new TallWaystoneBlock(BlockProperties.create()
                    .sound(material.soundType())
                    .destroyTime(material.destroyTime())
                    .explosionResistance(material.explodeResistance())
                    .mapColor(material.mapColor())
                    .lightLevel(material.lightLevel())
                    .requiresCorrectTool(material.requiresToolForDrops()));
                material.setBlock(block);
                WaystoneType.register(block, block);
                return block;
            });
            handler.registerItem(material.identifier() + "_waystone", () -> {
                TallWaystoneItem item = new TallWaystoneItem(material.block(), ItemProperties.create().group(CREATIVE_GROUP));
                material.setItem(item);
                return item;
            });
        }
        handler.registerParticleType("discover", ScanParticleType::new);
        handler.registerParticleType("teleport", ScanParticleType::new);
        handler.registerItem("dimensional_pearl", () -> new BaseItem(ItemProperties.create().maxStackSize(16).group(CREATIVE_GROUP)));
        handler.registerItem("warp_pendant", WarpPendant::new);
    }

    public static void registerGenerators(){
        GeneratorRegistrationHandler handler = GeneratorRegistrationHandler.get(MODID);
        handler.addGenerator(WaystonesReimaginedAtlasSourceGenerator::new);
        handler.addGenerator(WaystonesReimaginedBlockStateGenerator::new);
        handler.addGenerator(WaystonesReimaginedItemInfoGenerator::new);
        handler.addGenerator(WaystonesReimaginedLanguageGenerator::new);
        handler.addGenerator(WaystonesReimaginedLootTableGenerator::new);
        handler.addGenerator(WaystonesReimaginedModelGenerator::new);
        handler.addGenerator(WaystonesReimaginedRecipeGenerator::new);
        handler.addGenerator(WaystonesReimaginedTagGenerator::new);
    }
}
