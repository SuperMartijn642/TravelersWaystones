package com.supermartijn642.waystonesreimagined.content;

import com.supermartijn642.core.TextComponents;
import com.supermartijn642.core.item.BaseItem;
import com.supermartijn642.core.item.ItemProperties;
import com.supermartijn642.waystonesreimagined.WaystonesReimagined;
import com.supermartijn642.waystonesreimagined.WaystonesReimaginedClient;
import com.supermartijn642.waystonesreimagined.api.targets.personal.PersonalWaystoneManager;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.function.Consumer;

/**
 * Created 30/08/2026 by SuperMartijn642
 */
public class WarpPendant extends BaseItem {

    public WarpPendant(){
        super(ItemProperties.create().maxStackSize(1).group(WaystonesReimagined.CREATIVE_GROUP));
    }

    @Override
    public ItemUseResult interact(ItemStack stack, Player player, InteractionHand hand, Level level){
        PersonalWaystoneManager manager = PersonalWaystoneManager.get(player);
        if(manager.all().isEmpty())
            player.sendOverlayMessage(TextComponents.translation("waystonesreimagined.pendant.no_targets").get());
        else if(level.isClientSide())
            WaystonesReimaginedClient.openTeleportScreen(null);
        return ItemUseResult.success(stack);
    }

    @Override
    protected void appendItemInformation(ItemStack stack, Consumer<Component> info, boolean advanced){
        super.appendItemInformation(stack, info, advanced);
        info.accept(TextComponents.translation("waystonesreimagined.pendant.description").color(ChatFormatting.GRAY).get());
        info.accept(TextComponents.translation(
            "waystonesreimagined.pendant.keybind",
            TextComponents.fromTextComponent(WaystonesReimaginedClient.PENDANT_KEYBIND.getTranslatedKeyMessage()).color(ChatFormatting.GOLD).get()
        ).color(ChatFormatting.GRAY).get());
    }
}
