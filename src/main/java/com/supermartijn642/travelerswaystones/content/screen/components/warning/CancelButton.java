package com.supermartijn642.travelerswaystones.content.screen.components.warning;

import com.supermartijn642.core.TextComponents;
import com.supermartijn642.core.gui.widget.WidgetRenderContext;
import com.supermartijn642.core.gui.widget.premade.AbstractButtonWidget;
import com.supermartijn642.travelerswaystones.TravelersWaystones;
import com.supermartijn642.travelerswaystones.util.GuiSprites;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Consumer;

public class CancelButton extends AbstractButtonWidget {

    public static final ResourceLocation BACKGROUND = TravelersWaystones.identifier("gui/cancel_button");

    public CancelButton(int x, int y, Runnable onPress){
        super(x, y, 19, 19, onPress);
    }

    @Override
    public Component getNarrationMessage(){
        return TextComponents.translation("travelerswaystones.screen.teleport.warning.cancel.narrate").get();
    }

    @Override
    protected void getTooltips(Consumer<Component> tooltips){
        tooltips.accept(TextComponents.translation("travelerswaystones.screen.teleport.warning.cancel").get());
    }

    @Override
    public void renderBackground(WidgetRenderContext context, int mouseX, int mouseY){
        super.renderBackground(context, mouseX, mouseY);
        GuiSprites.draw(context.poseStack(), BACKGROUND, this.x, this.y, this.width, this.height, 0, this.isActive() ? this.isFocused() ? 1/3f : 0 : 2/3f, 1, 1/3f);
    }
}
