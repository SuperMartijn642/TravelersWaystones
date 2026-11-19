package com.supermartijn642.travelerswaystones.content.screen.components;

import com.supermartijn642.core.TextComponents;
import com.supermartijn642.core.gui.GuiGraphicsHelper;
import com.supermartijn642.core.gui.widget.WidgetRenderContext;
import com.supermartijn642.core.gui.widget.premade.TextFieldWidget;
import com.supermartijn642.travelerswaystones.TravelersWaystones;
import com.supermartijn642.travelerswaystones.api.targets.WaystoneTarget;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ARGB;

import java.util.function.Consumer;

public class EditNameTextField extends TextFieldWidget {

    public static final ResourceLocation BACKGROUND = TravelersWaystones.identifier("gui/name_field");

    private final int properWidth, properHeight;
    private final Consumer<String> confirmListener;
    private boolean hasHadText = false;
    private boolean enabled = true;

    public EditNameTextField(int x, int y, int width, int height, String defaultText, int maxLength, Consumer<String> changeListener, Consumer<String> confirmListener){
        super(x, y, width, height, defaultText, maxLength, changeListener);
        this.properWidth = width;
        this.properHeight = height;
        this.confirmListener = confirmListener;
        this.setTextColors(-1, -1, ARGB.color(200, 200, 200));
        this.textShadow = true;
    }

    @Override
    public Component getNarrationMessage(){
        return TextComponents.translation("travelerswaystones.screen.info_card.name_field.narrate").get();
    }

    public void setEnabled(boolean enabled){
        this.enabled = enabled;
        this.width = enabled ? this.properWidth : 0;
        this.height = enabled ? this.properHeight : 0;
    }

    public boolean isEnabled(){
        return this.enabled;
    }

    @Override
    public void setSelected(boolean selected){
        if(this.isSelected() && !selected)
            this.confirmListener.accept(this.getText());
        super.setSelected(selected);
    }

    public void moveCursorToEnd(){
        this.cursorPosition = this.selectionPos = this.getText().length();
        this.moveLineOffsetToCursor();
    }

    @Override
    public void renderBackground(WidgetRenderContext context, GuiGraphicsHelper graphics, int mouseX, int mouseY){
        if(!this.getText().isEmpty())
            this.hasHadText = true;
        if(this.hasHadText){
            String name = this.getText().trim();
            boolean isValidName = name.length() >= WaystoneTarget.MIN_NAME_LENGTH && name.length() <= WaystoneTarget.MAX_NAME_LENGTH;
            this.setTextColors(isValidName ? -1 : ARGB.color(255, 100, 100), -1);
        }
        super.renderBackground(context, graphics, mouseX, mouseY);
    }

    @Override
    public void render(WidgetRenderContext context, GuiGraphicsHelper graphics, int mouseX, int mouseY){
        if(this.enabled)
            super.render(context, graphics, mouseX, mouseY);
    }

    @Override
    protected void drawBackground(GuiGraphicsHelper graphics){
        graphics.submitSprite(BACKGROUND, this.x, this.y, this.width, this.height);
    }

    @Override
    public boolean mousePressed(int mouseX, int mouseY, int button, boolean hasBeenHandled){
        if(hasBeenHandled && this.isFocused())
            return false;
        return super.mousePressed(mouseX, mouseY, button, hasBeenHandled);
    }
}
