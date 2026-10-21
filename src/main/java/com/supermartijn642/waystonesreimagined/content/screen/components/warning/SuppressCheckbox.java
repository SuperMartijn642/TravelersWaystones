package com.supermartijn642.waystonesreimagined.content.screen.components.warning;

import com.supermartijn642.core.TextComponents;
import com.supermartijn642.core.gui.GuiGraphicsHelper;
import com.supermartijn642.core.gui.widget.WidgetRenderContext;
import com.supermartijn642.core.gui.widget.premade.AbstractButtonWidget;
import com.supermartijn642.waystonesreimagined.WaystonesReimagined;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import java.util.function.Consumer;

public class SuppressCheckbox extends AbstractButtonWidget {

    public static final Identifier ICONS = WaystonesReimagined.identifier("gui/checkbox");

    private boolean checked;

    public SuppressCheckbox(int x, int y){
        super(x, y, 8, 8, null);
    }

    public boolean isChecked(){
        return this.checked;
    }

    @Override
    public void onPress(){
        super.onPress();
        this.checked = !this.checked;
    }

    @Override
    public Component getNarrationMessage(){
        return null;
    }

    @Override
    protected void getTooltips(Consumer<Component> tooltips){
        tooltips.accept(TextComponents.translation("waystonesreimagined.screen.teleport.warning.suppress.hint").get());
    }

    @Override
    public void renderBackground(WidgetRenderContext context, GuiGraphicsHelper graphics, int mouseX, int mouseY){
        super.renderBackground(context, graphics, mouseX, mouseY);
        graphics.submitSprite(ICONS, this.x - 1, this.y - 1, this.width + 1, this.height + 1, p -> p.uv(this.checked ? 0.5f : 0, this.isFocused() ? 0.5f : 0, 0.5f, 0.5f));
    }
}
