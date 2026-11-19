package com.supermartijn642.travelerswaystones.content.screen.components;

import com.mojang.blaze3d.vertex.PoseStack;
import com.supermartijn642.core.TextComponents;
import com.supermartijn642.core.gui.widget.WidgetRenderContext;
import com.supermartijn642.core.gui.widget.premade.AbstractButtonWidget;
import com.supermartijn642.travelerswaystones.TravelersWaystones;
import com.supermartijn642.travelerswaystones.content.moving.WaystoneMovingHandler;
import com.supermartijn642.travelerswaystones.util.GuiSprites;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Consumer;

public class RelocateButton extends AbstractButtonWidget {

    public static final ResourceLocation RELOCATE_ICON = TravelersWaystones.identifier("gui/relocate_icon");

    public RelocateButton(int x, int y, Runnable onPress){
        super(x, y, 8, 9, onPress);
    }

    @Override
    public Component getNarrationMessage(){
        return TextComponents.translation("travelerswaystones.screen.teleport.relocate.narrate").get();
    }

    @Override
    protected void getTooltips(Consumer<Component> tooltips){
        tooltips.accept(TextComponents.translation("travelerswaystones.screen.teleport.relocate").get());
        tooltips.accept(TextComponents.translation(
            "travelerswaystones.screen.teleport.relocate.info",
            TextComponents.number(WaystoneMovingHandler.MAX_DISTANCE).color(ChatFormatting.GOLD).get()
        ).color(ChatFormatting.GRAY).get());
    }

    @Override
    public void renderBackground(WidgetRenderContext context, int mouseX, int mouseY){
        super.renderBackground(context, mouseX, mouseY);
        if(this.isActive()){
            PoseStack poseStack = context.poseStack();
            poseStack.pushPose();
            poseStack.translate(0, 0.5f, 0);
            GuiSprites.draw(poseStack, RELOCATE_ICON, this.x, this.y, 8, 8, 0, this.isFocused() ? 0.5f : 0, 1, 0.5f);
            poseStack.popPose();
        }
    }
}
