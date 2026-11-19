package com.supermartijn642.travelerswaystones.content.screen.components;

import com.supermartijn642.core.TextComponents;
import com.supermartijn642.core.gui.widget.WidgetRenderContext;
import com.supermartijn642.core.gui.widget.premade.AbstractButtonWidget;
import com.supermartijn642.travelerswaystones.TravelersWaystones;
import com.supermartijn642.travelerswaystones.util.GuiSprites;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class EditNameButton extends AbstractButtonWidget {

    public static final ResourceLocation EDIT_ICON = TravelersWaystones.identifier("gui/edit_icon");

    public EditNameButton(int x, int y, Runnable onPress){
        super(x, y, 8, 8, onPress);
    }

    @Override
    public Component getNarrationMessage(){
        return TextComponents.translation("travelerswaystones.screen.info_card.edit_name.narrate").get();
    }

    @Override
    public void renderBackground(WidgetRenderContext context, int mouseX, int mouseY){
        super.renderBackground(context, mouseX, mouseY);
        if(this.isActive())
            GuiSprites.draw(context.poseStack(), EDIT_ICON, this.x, this.y, this.width, this.height, 0, this.isFocused() ? 0.5f : 0, 1, 0.5f);
    }
}
