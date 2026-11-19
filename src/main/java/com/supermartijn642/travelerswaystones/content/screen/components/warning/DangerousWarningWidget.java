package com.supermartijn642.travelerswaystones.content.screen.components.warning;

import com.supermartijn642.core.ClientUtils;
import com.supermartijn642.core.TextComponents;
import com.supermartijn642.core.gui.GuiGraphicsHelper;
import com.supermartijn642.core.gui.widget.BaseWidget;
import com.supermartijn642.core.gui.widget.WidgetRenderContext;
import com.supermartijn642.core.gui.widget.premade.ButtonWidget;
import com.supermartijn642.travelerswaystones.TravelersWaystones;
import com.supermartijn642.travelerswaystones.api.targets.personal.DiscoveredWaystoneTarget;
import com.supermartijn642.travelerswaystones.content.packets.TeleportPacket;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

public class DangerousWarningWidget extends BaseWidget {

    public static final ResourceLocation DANGEROUS_ICON = TravelersWaystones.identifier("gui/dangerous_icon");
    public static final ResourceLocation BACKGROUND = TravelersWaystones.identifier("gui/warning_background");

    private final @Nullable DiscoveredWaystoneTarget current;
    private final boolean trusted;
    private final DiscoveredWaystoneTarget target;
    private final Component warning;
    private final Runnable close;
    private final int backgroundHeight;
    private SuppressCheckbox suppressCheckbox;

    public DangerousWarningWidget(int offsetX, int offsetY, @Nullable DiscoveredWaystoneTarget current, boolean trusted, DiscoveredWaystoneTarget target, Component warning, Runnable close){
        super(0, 0, 140, 0);
        this.current = current;
        this.trusted = trusted;
        this.target = target;
        this.warning = warning;
        this.backgroundHeight = ClientUtils.getFontRenderer().wordWrapHeight(warning, 128) + 39;
        this.close = close;
        this.height = this.backgroundHeight + 29;
        this.x = offsetX - this.width / 2;
        this.y = offsetY - this.height / 2;
    }

    @Override
    protected void addWidgets(){
        this.suppressCheckbox = this.addWidget(new SuppressCheckbox(this.x + 5, this.y + this.backgroundHeight - 12));
        this.addWidget(new CancelButton(this.x + 18, this.y + this.backgroundHeight + 10, this.close));
        this.addWidget(new ButtonWidget(
            this.x + 41, this.y + this.backgroundHeight + 10,
            80, 19,
            TextComponents.translation("travelerswaystones.screen.teleport.warning.teleport").get(),
            () -> {
                if(this.suppressCheckbox.isChecked())
                    this.target.setSuppressDangerousWarning(true);
                TravelersWaystones.CHANNEL.sendToServer(new TeleportPacket(this.current == null ? null : this.current.id(), this.target.id(), true, this.trusted));
                this.close.run();
            }
        ));
    }

    @Override
    public Component getNarrationMessage(){
        return TextComponents.translation("travelerswaystones.screen.teleport.warning.narrate", this.warning).get();
    }

    @Override
    public void renderBackground(WidgetRenderContext context, GuiGraphicsHelper graphics, int mouseX, int mouseY){
        graphics.submitSprite(BACKGROUND, this.x, this.y, this.width, this.backgroundHeight);
        super.renderBackground(context, graphics, mouseX, mouseY);
    }

    @Override
    public void render(WidgetRenderContext context, GuiGraphicsHelper graphics, int mouseX, int mouseY){
        super.render(context, graphics, mouseX, mouseY);
        graphics.submitSprite(DANGEROUS_ICON, this.x + 39, this.y + 3, 8, 8, p -> p.uv(0, 0, 1, 0.5f));
        graphics.submitText(TextComponents.translation("travelerswaystones.screen.teleport.warning.title").get(), this.x + 70, this.y + 8, p -> p.center());
        graphics.submitText(this.warning, this.x + this.width / 2f, this.y + 21, p -> p.wrap(128).centerHorizontally().color(255, 255, 255).shadow());
        graphics.submitText(TextComponents.translation("travelerswaystones.screen.teleport.warning.suppress").get(), this.x + 14, this.y + this.backgroundHeight - 12);
    }
}
