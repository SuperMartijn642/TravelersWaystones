package com.supermartijn642.waystonesreimagined.content.screen.components;

import com.supermartijn642.core.TextComponents;
import com.supermartijn642.core.gui.GuiGraphicsHelper;
import com.supermartijn642.core.gui.widget.WidgetRenderContext;
import com.supermartijn642.core.gui.widget.premade.AbstractButtonWidget;
import com.supermartijn642.waystonesreimagined.WaystonesReimagined;
import com.supermartijn642.waystonesreimagined.content.moving.WaystoneMovingHandler;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.joml.Matrix3x2fStack;

import java.util.function.Consumer;

/**
 * Created 03/10/2026 by SuperMartijn642
 */
public class RelocateButton extends AbstractButtonWidget {

    public static final Identifier RELOCATE_ICON = WaystonesReimagined.identifier("gui/relocate_icon");

    public RelocateButton(int x, int y, Runnable onPress){
        super(x, y, 8, 9, onPress);
    }

    @Override
    public Component getNarrationMessage(){
        return null;
    }

    @Override
    protected void getTooltips(Consumer<Component> tooltips){
        tooltips.accept(TextComponents.translation("waystonesreimagined.screen.teleport.relocate").get());
        tooltips.accept(TextComponents.translation(
            "waystonesreimagined.screen.teleport.relocate.info",
            TextComponents.number(WaystoneMovingHandler.MAX_DISTANCE).color(ChatFormatting.GOLD).get()
        ).color(ChatFormatting.GRAY).get());
    }

    @Override
    public void renderBackground(WidgetRenderContext context, GuiGraphicsHelper graphics, int mouseX, int mouseY){
        super.renderBackground(context, graphics, mouseX, mouseY);
        if(this.isActive()){
            Matrix3x2fStack poseStack = graphics.poseStack();
            poseStack.pushMatrix();
            poseStack.translate(0, 0.5f);
            graphics.submitSprite(RELOCATE_ICON, this.x, this.y, 8, 8, p -> p.uv(0, this.isFocused() ? 0.5f : 0, 1, 0.5f));
            poseStack.popMatrix();
        }
    }
}
