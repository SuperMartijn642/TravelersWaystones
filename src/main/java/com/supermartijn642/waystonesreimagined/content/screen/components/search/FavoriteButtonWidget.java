package com.supermartijn642.waystonesreimagined.content.screen.components.search;

import com.supermartijn642.core.gui.GuiGraphicsHelper;
import com.supermartijn642.core.gui.widget.WidgetRenderContext;
import com.supermartijn642.core.gui.widget.premade.AbstractButtonWidget;
import com.supermartijn642.waystonesreimagined.WaystonesReimagined;
import net.minecraft.client.input.MouseButtonInfo;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import java.util.function.BooleanSupplier;

/**
 * Created 22/08/2026 by SuperMartijn642
 */
public class FavoriteButtonWidget extends AbstractButtonWidget {

    public static final Identifier STAR = WaystonesReimagined.identifier("gui/star_icon");

    private final BooleanSupplier on;
    private boolean enabled = true;

    public FavoriteButtonWidget(int x, int y, BooleanSupplier on, Runnable onPress){
        super(x, y, 10, 10, onPress);
        this.on = on;
    }

    public void setY(int y){
        this.y = y;
    }

    public void setEnabled(boolean enabled){
        this.enabled = enabled;
    }

    public boolean isEnabled(){
        return this.enabled;
    }

    @Override
    public Component getNarrationMessage(){
        return null;
    }

    @Override
    public int width(){
        return this.enabled ? super.width() : 0;
    }

    @Override
    public int height(){
        return this.enabled ? super.height() : 0;
    }

    @Override
    public void onPress(){
        if(!this.enabled)
            return;
        super.onPress();
    }

    @Override
    public void renderBackground(WidgetRenderContext context, GuiGraphicsHelper graphics, int mouseX, int mouseY){
        super.renderBackground(context, graphics, mouseX, mouseY);
        if(!this.enabled)
            return;
        graphics.submitSprite(STAR, this.x, this.y, this.width, this.height, p -> p.uv(this.on.getAsBoolean() ? 0 : 0.5f, this.isFocused() ? 0.5f : 0, 0.5f, 0.5f));
    }

    @Override
    public boolean mousePressed(int mouseX, int mouseY, MouseButtonInfo info, boolean isDoubleClick, boolean hasBeenHandled){
        return this.enabled && super.mousePressed(mouseX, mouseY, info, isDoubleClick, hasBeenHandled);
    }
}
