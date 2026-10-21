package com.supermartijn642.waystonesreimagined.content.screen.components;

import com.supermartijn642.core.gui.GuiGraphicsHelper;
import com.supermartijn642.core.gui.widget.WidgetRenderContext;
import com.supermartijn642.core.gui.widget.premade.AbstractButtonWidget;
import com.supermartijn642.waystonesreimagined.WaystonesReimagined;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

/**
 * Created 30/08/2026 by SuperMartijn642
 */
public class EditNameButton extends AbstractButtonWidget {

    public static final Identifier EDIT_ICON = WaystonesReimagined.identifier("gui/edit_icon");

    public EditNameButton(int x, int y, Runnable onPress){
        super(x, y, 8, 8, onPress);
    }

    @Override
    public Component getNarrationMessage(){
        return null;
    }

    @Override
    public void renderBackground(WidgetRenderContext context, GuiGraphicsHelper graphics, int mouseX, int mouseY){
        super.renderBackground(context, graphics, mouseX, mouseY);
        if(this.isActive())
            graphics.submitSprite(EDIT_ICON, this.x, this.y, this.width, this.height, p -> p.uv(0, this.isFocused() ? 0.5f : 0, 1, 0.5f));
    }
}
