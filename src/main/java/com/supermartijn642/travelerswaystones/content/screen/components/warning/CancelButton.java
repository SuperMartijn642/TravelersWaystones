package com.supermartijn642.travelerswaystones.content.screen.components.warning;

import com.supermartijn642.core.TextComponents;
import com.supermartijn642.core.gui.GuiGraphicsHelper;
import com.supermartijn642.core.gui.widget.WidgetRenderContext;
import com.supermartijn642.core.gui.widget.premade.AbstractButtonWidget;
import com.supermartijn642.travelerswaystones.TravelersWaystones;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import java.util.function.Consumer;

public class CancelButton extends AbstractButtonWidget {

    public static final Identifier BACKGROUND = TravelersWaystones.identifier("gui/cancel_button");

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
    public void renderBackground(WidgetRenderContext context, GuiGraphicsHelper graphics, int mouseX, int mouseY){
        super.renderBackground(context, graphics, mouseX, mouseY);
        graphics.submitSprite(BACKGROUND, this.x, this.y, this.width, this.height, p -> p.uv(0, this.isActive() ? this.isFocused() ? 1/3f : 0 : 2/3f, 1, 1/3f));
    }
}
