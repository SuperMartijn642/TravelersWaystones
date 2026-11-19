package com.supermartijn642.travelerswaystones.content.screen.components.warning;

import com.supermartijn642.core.ClientUtils;
import com.supermartijn642.core.TextComponents;
import com.supermartijn642.core.gui.ScreenUtils;
import com.supermartijn642.core.gui.widget.BaseWidget;
import com.supermartijn642.core.gui.widget.WidgetRenderContext;
import com.supermartijn642.core.gui.widget.premade.ButtonWidget;
import com.supermartijn642.core.render.RenderUtils;
import com.supermartijn642.travelerswaystones.TravelersWaystones;
import com.supermartijn642.travelerswaystones.api.targets.personal.DiscoveredWaystoneTarget;
import com.supermartijn642.travelerswaystones.content.packets.TeleportPacket;
import com.supermartijn642.travelerswaystones.util.GuiSprites;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import org.jetbrains.annotations.Nullable;

import java.util.List;

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
    public void renderBackground(WidgetRenderContext context, int mouseX, int mouseY){
        ScreenUtils.drawGuiSprite(BACKGROUND, context.poseStack(), this.x, this.y, this.width, this.backgroundHeight);
        super.renderBackground(context, mouseX, mouseY);
    }

    @Override
    public void render(WidgetRenderContext context, int mouseX, int mouseY){
        super.render(context, mouseX, mouseY);
        GuiSprites.draw(context.poseStack(), DANGEROUS_ICON, this.x + 39, this.y + 3, 8, 8, 0, 0, 1, 0.5f);
        ScreenUtils.drawCenteredString(context.poseStack(), TextComponents.translation("travelerswaystones.screen.teleport.warning.title").get(), this.x + 70, this.y + 3.5f);
        Font font = ClientUtils.getFontRenderer();
        List<FormattedCharSequence> lines = font.split(this.warning, 128);
        int width = 0;
        for(FormattedCharSequence line : lines)
            width = Math.max(width, font.width(line));
        MultiBufferSource.BufferSource bufferSource = RenderUtils.getMainBufferSource();
        for(int i = 0; i < lines.size(); i++)
            font.drawInBatch(lines.get(i), this.x + (this.width - width) / 2f, this.y + 21 + i * font.lineHeight, 0xffffff, true, context.poseStack().last().pose(), bufferSource, Font.DisplayMode.NORMAL, 0, LightTexture.FULL_BRIGHT);
        bufferSource.endBatch();
        ScreenUtils.drawString(context.poseStack(), TextComponents.translation("travelerswaystones.screen.teleport.warning.suppress").get(), this.x + 14, this.y + this.backgroundHeight - 12);
    }
}
