package com.supermartijn642.travelerswaystones.content.screen.components.search;

import com.mojang.blaze3d.vertex.PoseStack;
import com.supermartijn642.core.ClientUtils;
import com.supermartijn642.core.TextComponents;
import com.supermartijn642.core.gui.ScreenUtils;
import com.supermartijn642.core.gui.widget.WidgetRenderContext;
import com.supermartijn642.core.gui.widget.premade.AbstractButtonWidget;
import com.supermartijn642.core.gui.widget.premade.ScissorWidget;
import com.supermartijn642.core.util.playerprofile.PlayerProfileCache;
import com.supermartijn642.travelerswaystones.TravelersWaystones;
import com.supermartijn642.travelerswaystones.api.targets.personal.DiscoveredWaystoneTarget;
import com.supermartijn642.travelerswaystones.util.GuiSprites;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.*;

public abstract class CategoryWidget extends AbstractButtonWidget {

    public static final ResourceLocation BACKGROUND = TravelersWaystones.identifier("gui/category");

    protected final SearchWidget searchWidget;
    protected final List<TargetEntryWidget> entries = new ArrayList<>();
    private final boolean sortEntries;
    protected ScissorWidget scissorWidget;
    protected boolean expanded = true;
    protected float expansionProgress = 1;
    protected boolean enabled = true;
    protected float activeTransition = 1;

    protected CategoryWidget(int x, int y, TargetEntryWidget.Icon icon, Collection<DiscoveredWaystoneTarget> targets, boolean sortTargets, SearchWidget searchWidget){
        super(x, y, 121, 18, null);
        this.searchWidget = searchWidget;
        this.sortEntries = sortTargets;
        for(DiscoveredWaystoneTarget target : targets)
            this.entries.add(new TargetEntryWidget(this.x, this.y, icon, target, searchWidget));
        this.scissorWidget = ScissorWidget.create(this.x, this.y + this.height, this.width, 0);
        this.entries.forEach(this.scissorWidget::addWidget);
        this.sortEntries();
        if(targets.isEmpty()){
            this.setActive(false);
            this.activeTransition = 0;
            this.expanded = false;
        }else if(!this.getManuallyExpanded())
            this.setExpanded(false, false, true, false);
    }

    public boolean updateFilter(String filter){
        boolean match = false;
        for(TargetEntryWidget entry : this.entries)
            match |= entry.updateFilter(filter);
        this.setEnabled(match || filter.isEmpty());
        if(filter.isEmpty())
            this.setExpanded(this.getManuallyExpanded(), false, true, false);
        else if(match){
            this.expanded = true;
            this.entries.forEach(entry -> entry.setEnabled(true));
        }
        this.expansionProgress = this.expanded ? 1 : 0;
        return match;
    }

    public List<TargetEntryWidget> getEntries(){
        return this.entries;
    }

    public void setY(int y){
        this.y = y;
        this.scissorWidget.setY(y + this.height);
    }

    public void setEnabled(boolean enabled){
        this.enabled = enabled;
    }

    public boolean isEnabled(){
        return this.enabled;
    }

    public void setExpanded(boolean expanded, boolean userInput, boolean instant, boolean updateLayout){
        if(this.entries.isEmpty())
            expanded = false;
        this.expanded = expanded;
        if(userInput)
            this.setManuallyExpanded(expanded);
        if(instant){
            this.expansionProgress = this.expanded ? 1 : 0;
            int height = 1;
            for(TargetEntryWidget entry : this.entries){
                height += entry.height();
                entry.setEnabled(this.expanded);
            }
            this.scissorWidget.setHeight((int)(height * this.expansionProgress));
        }
        if(updateLayout){
            if(this.expanded)
                this.searchWidget.onCategoryExpand(this);
            else
                this.searchWidget.onCategoryCollapse(this);
        }
    }

    public boolean isExpanded(){
        return this.expanded;
    }

    protected abstract void setManuallyExpanded(boolean expanded);

    protected abstract boolean getManuallyExpanded();

    @Override
    public int height(){
        if(!this.enabled)
            return 0;
        if(!this.expanded && this.expansionProgress == 0)
            return this.height;
        int entriesHeight = 1;
        for(TargetEntryWidget entry : this.entries){
            if(entry.isEnabled())
                entriesHeight += entry.height();
        }
        entriesHeight = (int)(entriesHeight * this.expansionProgress);
        return this.height + entriesHeight;
    }

    public int getStaticHeight(){
        if(!this.enabled)
            return 0;
        if(!this.expanded)
            return this.height;
        int height = this.height;
        for(TargetEntryWidget entry : this.entries){
            if(entry.isEnabled())
                height += entry.height();
        }
        return height + 1;
    }

    public int getExpandedHeight(){
        if(!this.enabled)
            return 0;
        if(this.entries.isEmpty())
            return this.height;
        int height = this.height;
        for(TargetEntryWidget entry : this.entries)
            height += entry.getEnabledHeight();
        return height + 1;
    }

    @Override
    protected void addWidgets(){
        this.addWidget(this.scissorWidget);
    }

    @Override
    public void renderBackground(WidgetRenderContext context, int mouseX, int mouseY){
        if(!this.enabled){
            this.expansionProgress = this.expanded ? 1 : 0;
            this.activeTransition = this.isActive() ? 1 : 0;
            return;
        }

        int y = this.y + this.height;
        for(TargetEntryWidget entry : this.entries){
            if(entry.isEnabled()){
                entry.setY(y);
                y += entry.height();
            }
        }
        if(this.expanded ? this.expansionProgress < 1 : this.expansionProgress > 0){
            float progress = 0.3f * context.partialTicks();
            float target = this.expanded ? 1 : 0;
            this.expansionProgress = Math.abs(target - this.expansionProgress) <= progress ? target : this.expansionProgress + Math.signum(target - this.expansionProgress) * progress;
            for(TargetEntryWidget entry : this.entries)
                entry.setEnabled(this.expansionProgress > 0);
            this.searchWidget.updateContentPositions();
        }
        int entriesHeight = y - this.y - this.height + 1;
        this.scissorWidget.setHeight((int)(entriesHeight * this.expansionProgress));

        if(this.isActive() ? this.activeTransition < 1 : this.activeTransition > 0){
            float progress = 0.2f * context.partialTicks();
            float target = this.isActive() ? 1 : 0;
            this.activeTransition = Math.abs(target - this.activeTransition) <= progress ? target : this.activeTransition + Math.signum(target - this.activeTransition) * progress;
        }
        boolean focused = this.isFocused() && this.focusedWidget == null;
        if(this.activeTransition < 1)
            GuiSprites.draw(context.poseStack(), BACKGROUND, this.x, this.y, this.width, this.height, this.expanded ? 0.5f : 0, 2 / 3f, 0.5f, 1 / 3f, (int)(255 * (1 - this.activeTransition)));
        if(this.activeTransition > 0)
            GuiSprites.draw(context.poseStack(), BACKGROUND, this.x, this.y, this.width, this.height, this.expanded ? 0.5f : 0, focused ? 1 / 3f : 0, 0.5f, 1 / 3f, (int)(255 * this.activeTransition));
        super.renderBackground(context, mouseX, mouseY);
    }

    @Override
    public void onPress(){
        super.onPress();
        this.setExpanded(!this.expanded, true, false, true);
    }

    public void sortEntries(){
        if(!this.sortEntries)
            return;
        UUID self = ClientUtils.getPlayer().getUUID();
        this.entries.sort(
            Comparator.<TargetEntryWidget,Boolean>comparing(e -> !e.getTarget().isFavorite())
                .thenComparing(e -> !e.getTarget().owner().equals(self))
                .thenComparing(e -> e.getTarget().owner())
                .thenComparing(e -> e.getTarget().name())
        );
    }

    public static class Owner extends CategoryWidget {

        private static final Set<UUID> COLLAPSED = new HashSet<>();

        private final UUID owner;

        public Owner(int x, int y, Collection<DiscoveredWaystoneTarget> targets, SearchWidget searchWidget, UUID owner){
            super(x, y, TargetEntryWidget.Icon.DIMENSION, targets, true, searchWidget);
            this.owner = owner;
            // Owner is not available yet in the super constructor
            if(!targets.isEmpty() && !this.getManuallyExpanded())
                this.setExpanded(false, false, true, false);
        }

        public UUID getOwner(){
            return this.owner;
        }

        @Override
        public boolean updateFilter(String filter){
            boolean match = super.updateFilter(filter);
            String username = PlayerProfileCache.get(true).getUsername(this.owner);
            if(username != null && !filter.isEmpty() && username.toLowerCase(Locale.ROOT).contains(filter)){
                // Show all waystones of the player when searching for their name
                this.entries.forEach(entry -> {
                    entry.updateFilter("");
                    entry.setEnabled(true);
                });
                this.setEnabled(true);
                this.expanded = true;
                this.expansionProgress = 1;
                match = true;
            }
            return match;
        }

        @Override
        protected void setManuallyExpanded(boolean expanded){
            if(expanded)
                COLLAPSED.remove(this.owner);
            else
                COLLAPSED.add(this.owner);
        }

        @Override
        protected boolean getManuallyExpanded(){
            return !COLLAPSED.contains(this.owner);
        }

        @Override
        public void render(WidgetRenderContext context, int mouseX, int mouseY){
            if(!this.enabled)
                return;
            super.render(context, mouseX, mouseY);
            PoseStack poseStack = context.poseStack();
            poseStack.pushPose();
            poseStack.translate(this.x + 12, this.y + 4, 0);
            poseStack.scale(10 / 8f, 10 / 8f, 1);
            ScreenUtils.drawPlayerHead(this.owner, poseStack, 0, 0, 8, 8, true);
            poseStack.popPose();
            String username = PlayerProfileCache.get(true).getUsername(this.owner);
            Component owner = username == null ?
                TextComponents.translation("travelerswaystones.screen.teleport.owner.unknown").get() :
                TextComponents.string(username).get();
            poseStack.pushPose();
            poseStack.translate(0, 0.5f, 0);
            int textBrightness = this.isActive() ? 255 : 180;
            ScreenUtils.drawStringWithShadow(poseStack, owner, this.x + 24, this.y + 5, textBrightness << 16 | textBrightness << 8 | textBrightness);
            poseStack.popPose();
        }

        @Override
        public void renderTooltips(WidgetRenderContext context, int mouseX, int mouseY){
            if(!this.enabled)
                return;
            super.renderTooltips(context, mouseX, mouseY);
            if(!this.isFocused() || this.focusedWidget != null)
                return;
            if(mouseX >= this.x && mouseX < this.x + this.width && mouseY >= this.y && mouseY < this.y + this.height
                && PlayerProfileCache.get(true).getUsername(this.owner) == null)
                ScreenUtils.drawTooltip(context.poseStack(), this.owner.toString(), mouseX, mouseY);
        }

        @Override
        public Component getNarrationMessage(){
            String username = PlayerProfileCache.get(true).getUsername(this.owner);
            Component owner = username == null ?
                TextComponents.translation("travelerswaystones.screen.teleport.owner.unknown").get() :
                TextComponents.string(username).get();
            Component expanded = this.expanded ?
                TextComponents.translation("travelerswaystones.screen.teleport.category.narrate.expanded").get() :
                TextComponents.translation("travelerswaystones.screen.teleport.category.narrate.collapsed").get();
            return TextComponents.translation(
                "travelerswaystones.screen.teleport.category.player.narrate",
                owner,
                expanded
            ).get();
        }
    }

    public static class Recents extends CategoryWidget {

        public static final ResourceLocation ICON = TravelersWaystones.identifier("gui/recents_icon");
        private static boolean manuallyExpanded = true;

        public Recents(int x, int y, Collection<DiscoveredWaystoneTarget> targets, SearchWidget searchWidget){
            super(x, y, TargetEntryWidget.Icon.OWNER, targets, false, searchWidget);
        }

        @Override
        protected void setManuallyExpanded(boolean expanded){
            manuallyExpanded = expanded;
        }

        @Override
        protected boolean getManuallyExpanded(){
            return manuallyExpanded;
        }

        @Override
        public void render(WidgetRenderContext context, int mouseX, int mouseY){
            if(!this.enabled)
                return;
            super.render(context, mouseX, mouseY);
            ScreenUtils.drawGuiSprite(ICON, context.poseStack(), this.x + 12, this.y + 4, 10, 10);
            PoseStack poseStack = context.poseStack();
            poseStack.pushPose();
            poseStack.translate(0, 0.5f, 0);
            int textBrightness = this.isActive() ? 255 : 180;
            ScreenUtils.drawStringWithShadow(poseStack, TextComponents.translation("travelerswaystones.screen.teleport.category.recents").get(), this.x + 24, this.y + 5, textBrightness << 16 | textBrightness << 8 | textBrightness);
            poseStack.popPose();
        }

        @Override
        public Component getNarrationMessage(){
            Component expanded = this.expanded ?
                TextComponents.translation("travelerswaystones.screen.teleport.category.narrate.expanded").get() :
                TextComponents.translation("travelerswaystones.screen.teleport.category.narrate.collapsed").get();
            return TextComponents.translation(
                "travelerswaystones.screen.teleport.category.recents.narrate",
                expanded
            ).get();
        }
    }

    public static class Favorites extends CategoryWidget {

        private static boolean manuallyExpanded = true;

        private final Collection<DiscoveredWaystoneTarget> targets;
        private String filter = "";

        public Favorites(int x, int y, Collection<DiscoveredWaystoneTarget> targets, SearchWidget searchWidget){
            super(x, y, TargetEntryWidget.Icon.OWNER, targets, true, searchWidget);
            this.targets = targets;
        }

        @Override
        protected void setManuallyExpanded(boolean expanded){
            manuallyExpanded = expanded;
        }

        @Override
        protected boolean getManuallyExpanded(){
            return manuallyExpanded;
        }

        @Override
        public boolean updateFilter(String filter){
            this.filter = filter;
            return super.updateFilter(filter);
        }

        @Override
        public void renderBackground(WidgetRenderContext context, int mouseX, int mouseY){
            if(!this.enabled)
                return;
            // Check if favorite targets changed
            int oldHeight = this.height();
            boolean shouldAdjustScrollOffset = this.searchWidget.isMouseBelowCategory(this, mouseY);
            boolean changed = false;
            for(int i = 0; i < this.entries.size(); i++){
                TargetEntryWidget entry = this.entries.get(i);
                if(!this.targets.contains(entry.getTarget())){
                    this.scissorWidget.removeWidget(this.entries.remove(i--));
                    changed = true;
                }
            }
            if(this.targets.size() != this.entries.size()){
                loop:
                for(DiscoveredWaystoneTarget target : this.targets){
                    for(TargetEntryWidget entry : this.entries){
                        if(entry.getTarget() == target)
                            continue loop;
                    }
                    TargetEntryWidget entry = new TargetEntryWidget(this.x, this.y, TargetEntryWidget.Icon.OWNER, target, this.searchWidget);
                    this.entries.add(entry);
                    this.scissorWidget.addWidget(entry);
                    entry.initialize();
                    changed = true;
                }
            }
            if(changed){
                if(this.entries.isEmpty()){
                    this.setExpanded(false, false, true, false);
                    this.setActive(false);
                }else{
                    this.setExpanded(this.manuallyExpanded, false, true, false);
                    this.setActive(true);
                    this.sortEntries();
                    this.updateFilter(this.filter);
                }
                if(shouldAdjustScrollOffset)
                    this.searchWidget.adjustScrollOffset(this.height() - oldHeight, true);
                else
                    this.searchWidget.updateContentPositions();
            }
            super.renderBackground(context, mouseX, mouseY);
        }

        @Override
        public void render(WidgetRenderContext context, int mouseX, int mouseY){
            if(!this.enabled)
                return;
            super.render(context, mouseX, mouseY);
            GuiSprites.draw(context.poseStack(), FavoriteButtonWidget.STAR, this.x + 12, this.y + 4, 10, 10, 1 / 44f, 1 / 44f, 20 / 44f, 20 / 44f);
            PoseStack poseStack = context.poseStack();
            poseStack.pushPose();
            poseStack.translate(0, 0.5f, 0);
            int textBrightness = (int)(this.activeTransition * 255 + (1 - this.activeTransition) * 180);
            ScreenUtils.drawStringWithShadow(poseStack, TextComponents.translation("travelerswaystones.screen.teleport.category.favorites").get(), this.x + 24, this.y + 5, textBrightness << 16 | textBrightness << 8 | textBrightness);
            poseStack.popPose();
        }

        @Override
        public Component getNarrationMessage(){
            Component expanded = this.expanded ?
                TextComponents.translation("travelerswaystones.screen.teleport.category.narrate.expanded").get() :
                TextComponents.translation("travelerswaystones.screen.teleport.category.narrate.collapsed").get();
            return TextComponents.translation(
                "travelerswaystones.screen.teleport.category.favorites.narrate",
                expanded
            ).get();
        }
    }
}
