package com.supermartijn642.travelerswaystones.content.screen.components.search;

import com.mojang.blaze3d.vertex.PoseStack;
import com.supermartijn642.core.ClientUtils;
import com.supermartijn642.core.TextComponents;
import com.supermartijn642.core.gui.ScreenUtils;
import com.supermartijn642.core.gui.widget.WidgetRenderContext;
import com.supermartijn642.core.gui.widget.premade.AbstractButtonWidget;
import com.supermartijn642.travelerswaystones.TravelersWaystones;
import com.supermartijn642.travelerswaystones.api.targets.personal.DiscoveredWaystoneTarget;
import com.supermartijn642.travelerswaystones.content.screen.components.InfoCardWidget;
import com.supermartijn642.travelerswaystones.util.GuiSprites;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class TargetEntryWidget extends AbstractButtonWidget {

    public static final ResourceLocation BACKGROUND = TravelersWaystones.identifier("gui/entry");

    private final DiscoveredWaystoneTarget target;
    private final SearchWidget searchWidget;
    private final Icon icon;
    private FavoriteButtonWidget favoriteButton;
    private boolean isSelected = false;
    private boolean enabled = true;
    private boolean filtered = true;

    public TargetEntryWidget(int x, int y, Icon icon, DiscoveredWaystoneTarget target, SearchWidget searchWidget){
        super(x, y, 121, 13, null);
        this.target = target;
        this.searchWidget = searchWidget;
        this.icon = target.owner().equals(ClientUtils.getPlayer().getUUID()) ? Icon.DIMENSION : icon;
    }

    public DiscoveredWaystoneTarget getTarget(){
        return this.target;
    }

    public void setY(int y){
        this.y = y;
        this.favoriteButton.setY(this.y + 1);
    }

    public boolean updateFilter(String filter){
        this.filtered = filter.isEmpty() || this.target.name().toLowerCase().contains(filter);
        return this.filtered;
    }

    public void setEnabled(boolean enabled){
        this.enabled = enabled;
    }

    public boolean isEnabled(){
        return this.enabled && this.filtered;
    }

    public void setSelected(boolean selected){
        this.isSelected = selected;
    }

    private boolean isVisible(){
        return this.isEnabled() && this.searchWidget.isInView(this.y, this.height);
    }

    @Override
    public Component getNarrationMessage(){
        return TextComponents.translation("travelerswaystones.screen.teleport.target_entry.narrate", this.target.name()).get();
    }

    @Override
    public int width(){
        return this.isEnabled() ? super.width() : 0;
    }

    @Override
    public int height(){
        return this.isEnabled() ? super.height() : 0;
    }

    public int getEnabledHeight(){
        return this.height;
    }

    @Override
    protected void addWidgets(){
        this.favoriteButton = this.addWidget(new FavoriteButtonWidget(
            this.x + this.width - 11, this.y + 1,
            this.target::isFavorite,
            () -> {
                this.target.setFavorite(!this.target.isFavorite());
                this.searchWidget.sortCategoryEntries();
            }
        ));
    }

    @Override
    public void renderBackground(WidgetRenderContext context, int mouseX, int mouseY){
        if(!this.isEnabled()){
            this.favoriteButton.setEnabled(false);
            return;
        }
        if(!this.searchWidget.isInView(this.y, this.height))
            return;
        this.favoriteButton.setEnabled(this.target.isFavorite() || (mouseX >= this.x && mouseX < this.x + this.width && mouseY >= this.y && mouseY < this.y + this.height));
        boolean isHovered = this.isFocused() && this.focusedWidget == null;
        GuiSprites.draw(context.poseStack(), BACKGROUND, this.x, this.y, this.width, this.height, 0, (isHovered ? this.isSelected ? 3 : 1 : this.isSelected ? 2 : 0) / 4f, 1, 1 / 4f);
        super.renderBackground(context, mouseX, mouseY);
    }

    @Override
    public void render(WidgetRenderContext context, int mouseX, int mouseY){
        if(!this.isVisible())
            return;
        super.render(context, mouseX, mouseY);
        PoseStack poseStack = context.poseStack();
        switch(this.icon){
            case OWNER -> ScreenUtils.drawPlayerHead(this.target.owner(), poseStack, this.x + 4, this.y + 2, 8, 8, true);
            case DIMENSION -> {
                poseStack.pushPose();
                poseStack.translate(0.5f, 0, 0);
                InfoCardWidget.renderDimensionIcon(poseStack, this.x + 4, this.y + 2, this.target.dimension());
                poseStack.popPose();
            }
        }
        poseStack.pushPose();
        poseStack.translate(0, 0.5f, 0);
        ScreenUtils.drawString(poseStack, TextComponents.string(this.target.name()).get(), this.x + 14, this.y + 2, 0xc8c8c8);
        poseStack.popPose();
    }

    @Override
    public void renderForeground(WidgetRenderContext context, int mouseX, int mouseY){
        if(this.isVisible())
            super.renderForeground(context, mouseX, mouseY);
    }

    @Override
    public void renderOverlay(WidgetRenderContext context, int mouseX, int mouseY){
        if(this.isVisible())
            super.renderOverlay(context, mouseX, mouseY);
    }

    @Override
    public void renderTooltips(WidgetRenderContext context, int mouseX, int mouseY){
        if(this.isVisible())
            super.renderTooltips(context, mouseX, mouseY);
    }

    @Override
    public void onPress(){
        if(!this.isEnabled())
            return;
        this.searchWidget.selectTargetEntry(this);
        super.onPress();
    }

    @Override
    public boolean mousePressed(int mouseX, int mouseY, int button, boolean hasBeenHandled){
        return this.isEnabled() && super.mousePressed(mouseX, mouseY, button, hasBeenHandled);
    }

    public enum Icon {
        OWNER, DIMENSION
    }
}
