package com.supermartijn642.travelerswaystones.content.screen.components;

import com.supermartijn642.core.TextComponents;
import com.supermartijn642.core.gui.widget.WidgetRenderContext;
import com.supermartijn642.core.gui.widget.premade.AbstractButtonWidget;
import com.supermartijn642.travelerswaystones.TravelersWaystones;
import com.supermartijn642.travelerswaystones.api.targets.personal.DiscoveredWaystoneTarget;
import com.supermartijn642.travelerswaystones.util.GuiSprites;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class WarningsSuppressedIndicator extends AbstractButtonWidget {

    public static final ResourceLocation DANGEROUS_ICON = TravelersWaystones.identifier("gui/dangerous_icon");

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
        return TextComponents.translation("travelerswaystones.screen.teleport.warning_icon.narrate").get();
    }

    @Override
    protected void getTooltips(Consumer<Component> tooltips){
        tooltips.accept(TextComponents.translation("travelerswaystones.screen.teleport.warning_icon.hint").get());
        tooltips.accept(TextComponents.translation("travelerswaystones.screen.teleport.warning_icon.hint.reenable").color(ChatFormatting.GRAY).get());
    }

    @Override
    public void render(WidgetRenderContext context, int mouseX, int mouseY){
        super.render(context, mouseX, mouseY);
        if(this.isClickable())
            GuiSprites.draw(context.poseStack(), DANGEROUS_ICON, this.x, this.y, this.width, this.height, 0, this.isFocused() ? 0.5f : 0, 1, 0.5f);
    }
}
