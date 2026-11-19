package com.supermartijn642.travelerswaystones.content;

import com.supermartijn642.core.TextComponents;
import com.supermartijn642.core.item.BaseItem;
import com.supermartijn642.core.item.ItemProperties;
import com.supermartijn642.travelerswaystones.TravelersWaystones;
import com.supermartijn642.travelerswaystones.TravelersWaystonesClient;
import com.supermartijn642.travelerswaystones.api.targets.personal.PersonalWaystoneManager;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.function.Consumer;

public class WarpPendant extends BaseItem {

    public static boolean hasPendant(Player player){
        return player.getInventory().contains(s -> s.is(TravelersWaystones.warpPendant));
    }

    public WarpPendant(){
        super(ItemProperties.create().maxStackSize(1).group(TravelersWaystones.CREATIVE_GROUP));
    }

    @Override
    public ItemUseResult interact(ItemStack stack, Player player, InteractionHand hand, Level level){
        PersonalWaystoneManager manager = PersonalWaystoneManager.get(player);
        if(manager.all().isEmpty())
            player.displayClientMessage(TextComponents.translation("travelerswaystones.pendant.no_targets").get(), true);
        else if(level.isClientSide())
            TravelersWaystonesClient.openTeleportScreen(null, null, false);
        return ItemUseResult.success(stack);
    }

    @Override
    protected void appendItemInformation(ItemStack stack, Consumer<Component> info, boolean advanced){
        super.appendItemInformation(stack, info, advanced);
        info.accept(TextComponents.translation("travelerswaystones.pendant.description").color(ChatFormatting.GRAY).get());
        info.accept(TextComponents.translation(
            "travelerswaystones.pendant.keybind",
            TextComponents.fromTextComponent(TravelersWaystonesClient.PENDANT_KEYBIND.getTranslatedKeyMessage()).color(ChatFormatting.GOLD).get()
        ).color(ChatFormatting.GRAY).get());
    }
}
