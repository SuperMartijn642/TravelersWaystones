package com.supermartijn642.waystonesreimagined.content.screen.components.search;

import com.supermartijn642.core.ClientUtils;
import com.supermartijn642.core.TextComponents;
import com.supermartijn642.core.gui.GuiGraphicsHelper;
import com.supermartijn642.core.gui.widget.WidgetRenderContext;
import com.supermartijn642.core.gui.widget.premade.AbstractButtonWidget;
import com.supermartijn642.waystonesreimagined.WaystonesReimagined;
import com.supermartijn642.waystonesreimagined.api.targets.personal.DiscoveredWaystoneTarget;
import com.supermartijn642.waystonesreimagined.content.screen.components.InfoCardWidget;
import net.minecraft.client.input.MouseButtonInfo;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.joml.Matrix3x2fStack;

public class TargetEntryWidget extends AbstractButtonWidget {

    public static final Identifier BACKGROUND = WaystonesReimagined.identifier("gui/entry");

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

    @Override
    public Component getNarrationMessage(){
        return TextComponents.translation("waystonesreimagined.screen.teleport.target_entry.narrate", this.target.name()).get();
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
    public void renderBackground(WidgetRenderContext context, GuiGraphicsHelper graphics, int mouseX, int mouseY){
        if(!this.isEnabled()){
            this.favoriteButton.setEnabled(false);
            return;
        }
        this.favoriteButton.setEnabled(this.target.isFavorite() || (mouseX >= this.x && mouseX < this.x + this.width && mouseY >= this.y && mouseY < this.y + this.height));
        boolean isHovered = this.isFocused() && this.focusedWidget == null;
        graphics.submitSprite(BACKGROUND, this.x, this.y, this.width, this.height, p -> p.uv(0, (isHovered ? this.isSelected ? 3 : 1 : this.isSelected ? 2 : 0) / 4f, 1, 1 / 4f));
        super.renderBackground(context, graphics, mouseX, mouseY);
    }

    @Override
    public void render(WidgetRenderContext context, GuiGraphicsHelper graphics, int mouseX, int mouseY){
        if(!this.isEnabled())
            return;
        super.render(context, graphics, mouseX, mouseY);
        Matrix3x2fStack poseStack = graphics.poseStack();
        switch(this.icon){
            case OWNER -> graphics.submitPlayerHead(this.target.owner(), this.x + 4, this.y + 2, 8, 8, true);
            case DIMENSION -> {
                poseStack.pushMatrix();
                poseStack.translate(0.5f, 0);
                InfoCardWidget.renderDimensionIcon(graphics, this.x + 4, this.y + 2, this.target.dimension());
                poseStack.popMatrix();
            }
        }
        poseStack.pushMatrix();
        poseStack.translate(0, 0.5f);
        graphics.submitText(TextComponents.string(this.target.name()).get(), this.x + 14, this.y + 2, p -> p.color(200, 200, 200));
        poseStack.popMatrix();
    }

    @Override
    public void onPress(){
        if(!this.isEnabled())
            return;
        this.searchWidget.selectTargetEntry(this);
        super.onPress();
    }

    @Override
    public boolean mousePressed(int mouseX, int mouseY, MouseButtonInfo info, boolean isDoubleClick, boolean hasBeenHandled){
        return this.isEnabled() && super.mousePressed(mouseX, mouseY, info, isDoubleClick, hasBeenHandled);
    }

    public enum Icon {
        OWNER, DIMENSION
    }
}
