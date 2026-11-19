package com.supermartijn642.travelerswaystones.content.screen.components.warning;

import com.supermartijn642.core.TextComponents;
import com.supermartijn642.core.gui.GuiGraphicsHelper;
import com.supermartijn642.core.gui.widget.WidgetRenderContext;
import com.supermartijn642.core.gui.widget.premade.AbstractButtonWidget;
import com.supermartijn642.travelerswaystones.TravelersWaystones;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Consumer;

public class SuppressCheckbox extends AbstractButtonWidget {

    public static final ResourceLocation ICONS = TravelersWaystones.identifier("gui/checkbox");

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
        return TextComponents.translation("travelerswaystones.screen.teleport.warning.suppress.narrate").get();
    }

    @Override
    protected void getTooltips(Consumer<Component> tooltips){
        tooltips.accept(TextComponents.translation("travelerswaystones.screen.teleport.warning.suppress.hint").get());
    }

    @Override
    public void renderBackground(WidgetRenderContext context, GuiGraphicsHelper graphics, int mouseX, int mouseY){
        super.renderBackground(context, graphics, mouseX, mouseY);
        graphics.submitSprite(ICONS, this.x - 1, this.y - 1, this.width + 1, this.height + 1, p -> p.uv(this.checked ? 0.5f : 0, this.isFocused() ? 0.5f : 0, 0.5f, 0.5f));
    }
}
