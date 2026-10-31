package com.supermartijn642.waystonesreimagined.content.screen.components;

import com.supermartijn642.core.TextComponents;
import com.supermartijn642.core.gui.GuiGraphicsHelper;
import com.supermartijn642.core.gui.widget.WidgetRenderContext;
import com.supermartijn642.core.gui.widget.premade.AbstractButtonWidget;
import com.supermartijn642.waystonesreimagined.WaystonesReimagined;
import com.supermartijn642.waystonesreimagined.api.targets.personal.DiscoveredWaystoneTarget;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class WarningsSuppressedIndicator extends AbstractButtonWidget {

    public static final Identifier DANGEROUS_ICON = WaystonesReimagined.identifier("gui/dangerous_icon");

    private final Supplier<DiscoveredWaystoneTarget> target;

    public WarningsSuppressedIndicator(int x, int y, Supplier<DiscoveredWaystoneTarget> target){
        super(x, y, 8, 8, () -> target.get().setSuppressDangerousWarning(false));
        this.target = target;
    }

    @Override
    protected boolean isClickable(){
        DiscoveredWaystoneTarget target = this.target.get();
        return target != null && target.suppressDangerousWarning();
    }

    @Override
    public int width(){
        return this.isClickable() ? super.width() : 0;
    }

    @Override
    public int height(){
        return this.isClickable() ? super.height() : 0;
    }

    @Override
    public Component getNarrationMessage(){
        return TextComponents.translation("waystonesreimagined.screen.teleport.warning_icon.narrate").get();
    }

    @Override
    protected void getTooltips(Consumer<Component> tooltips){
        tooltips.accept(TextComponents.translation("waystonesreimagined.screen.teleport.warning_icon.hint").get());
        tooltips.accept(TextComponents.translation("waystonesreimagined.screen.teleport.warning_icon.hint.reenable").color(ChatFormatting.GRAY).get());
    }

    @Override
    public void render(WidgetRenderContext context, GuiGraphicsHelper graphics, int mouseX, int mouseY){
        super.render(context, graphics, mouseX, mouseY);
        if(this.isClickable())
            graphics.submitSprite(DANGEROUS_ICON, this.x, this.y, this.width, this.height, p -> p.uv(0, this.isFocused() ? 0.5f : 0, 1, 0.5f));
    }
}
