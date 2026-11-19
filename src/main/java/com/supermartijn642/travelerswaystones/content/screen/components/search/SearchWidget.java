package com.supermartijn642.travelerswaystones.content.screen.components.search;

import com.supermartijn642.core.ClientUtils;
import com.supermartijn642.core.gui.GuiGraphicsHelper;
import com.supermartijn642.core.gui.widget.BaseWidget;
import com.supermartijn642.core.gui.widget.WidgetRenderContext;
import com.supermartijn642.core.gui.widget.premade.ScissorWidget;
import com.supermartijn642.core.gui.widget.premade.ScrollbarWidget;
import com.supermartijn642.core.util.playerprofile.PlayerProfileCache;
import com.supermartijn642.travelerswaystones.TravelersWaystones;
import com.supermartijn642.travelerswaystones.api.targets.personal.DiscoveredWaystoneTarget;
import com.supermartijn642.travelerswaystones.api.targets.personal.PersonalWaystoneManager;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class SearchWidget extends BaseWidget {

    public static final ResourceLocation BACKGROUND = TravelersWaystones.identifier("gui/search_background");
    public static final ResourceLocation SCROLLER = TravelersWaystones.identifier("gui/scroller");
    private static final int MIN_HEIGHT = 92, MAX_HEIGHT = 220;
    private static final float SCROLL_SPEED = 5;

    private final List<CategoryWidget> categories;
    private DiscoveredWaystoneTarget selectedTarget;
    private float scrollOffset, scrollTarget;

    public SearchWidget(int x, int y, PersonalWaystoneManager manager, @Nullable DiscoveredWaystoneTarget selected){
        super(x, y, 134, MIN_HEIGHT);
        // Create categories
        this.categories = new ArrayList<>();
        this.categories.add(new CategoryWidget.Recents(this.x + 4, this.y + 23, manager.recents(), this));
        if(manager.byOwner().size() > 1)
            this.categories.add(new CategoryWidget.Favorites(this.x + 4, this.y + 23, manager.favorites(), this));
        for(Map.Entry<UUID,Set<DiscoveredWaystoneTarget>> entry : manager.byOwner().entrySet()){
            this.categories.add(new CategoryWidget.Owner(
                this.x + 4, this.y + 23,
                entry.getValue(),
                this,
                entry.getKey()
            ));
        }
        this.sortCategories();
        // Calculate height
        int height = 27;
        for(CategoryWidget category : this.categories)
            height += category.getExpandedHeight();
        this.height = Math.clamp(height, MIN_HEIGHT, MAX_HEIGHT);
        // Select target
        for(CategoryWidget category : this.categories){
            if(!category.isExpanded())
                continue;
            List<TargetEntryWidget> entries = category.getEntries();
            if(!entries.isEmpty()){
                this.selectedTarget = entries.getFirst().getTarget();
                entries.getFirst().setSelected(true);
                break;
            }
        }
        if(this.selectedTarget == null){
            for(CategoryWidget category : this.categories){
                List<TargetEntryWidget> entries = category.getEntries();
                if(!entries.isEmpty()){
                    this.selectedTarget = entries.getFirst().getTarget();
                    entries.getFirst().setSelected(true);
                    break;
                }
            }
        }
        if(selected != null)
            this.selectTarget(selected);
    }

    private void selectTarget(DiscoveredWaystoneTarget target){
        for(CategoryWidget category : this.categories){
            for(TargetEntryWidget entry : category.getEntries()){
                if(entry.getTarget() != target)
                    continue;
                if(!category.isExpanded())
                    category.setExpanded(true, false, true, false);
                this.selectTargetEntry(entry);
                // Scroll to the entry
                int offset = 0;
                for(CategoryWidget c : this.categories){
                    if(c == category)
                        break;
                    offset += c.height();
                }
                for(TargetEntryWidget e : category.getEntries()){
                    if(e == entry)
                        break;
                    offset += e.height();
                }
                this.setScrollOffset(offset, true);
                return;
            }
        }
    }

    public DiscoveredWaystoneTarget getSelectedTarget(){
        return this.selectedTarget;
    }

    public void selectTargetEntry(TargetEntryWidget entry){
        for(CategoryWidget category : this.categories){
            for(TargetEntryWidget e : category.getEntries()){
                e.setSelected(e == entry);
            }
        }
        this.selectedTarget = entry.getTarget();
    }

    @Override
    public Component getNarrationMessage(){
        return null;
    }

    private void setScrollOffset(float offset, boolean instant){
        // Limit offset
        if(offset <= 0)
            offset = 0;
        else{
            float max = this.maxScrollOffset();
            if(offset > max)
                offset = max;
        }
        this.scrollTarget = offset;
        if(instant){
            this.scrollOffset = this.scrollTarget;
            this.updateContentPositions();
        }
    }

    public void adjustScrollOffset(float amount, boolean instant){
        this.setScrollOffset(this.scrollOffset + amount, instant);
    }

    public float maxScrollOffset(){
        int contentArea = this.height - 27;
        int contentHeight = 0;
        for(CategoryWidget category : this.categories)
            contentHeight += category.height();
        return Math.max(0, contentHeight - contentArea);
    }

    public boolean isInView(int y, int height){
        return y + height > this.y + 23 && y < this.y + this.height - 4;
    }

    public void updateContentPositions(){
        int y = this.y + 23 - (int)this.scrollOffset;
        for(CategoryWidget category : this.categories){
            category.setY(y);
            y += category.height();
        }
    }

    private void sortCategories(){
        UUID self = ClientUtils.getPlayer().getUUID();
        this.categories.sort((a, b) -> {
            boolean isARecents = a instanceof CategoryWidget.Recents, isBRecents = b instanceof CategoryWidget.Recents;
            if(isARecents || isBRecents)
                return Boolean.compare(isBRecents, isARecents);
            boolean isAFavorites = a instanceof CategoryWidget.Favorites, isBFavorites = b instanceof CategoryWidget.Favorites;
            if(isAFavorites || isBFavorites)
                return Boolean.compare(isBFavorites, isAFavorites);
            UUID ownerA = ((CategoryWidget.Owner)a).getOwner(), ownerB = ((CategoryWidget.Owner)b).getOwner();
            boolean isASelf = ownerA.equals(self), isBSelf = ownerB.equals(self);
            if(isASelf || isBSelf)
                return Boolean.compare(isBSelf, isASelf);
            String usernameA = PlayerProfileCache.get(true).getUsername(ownerA), usernameB = PlayerProfileCache.get(true).getUsername(ownerB);
            if(usernameA == null){
                if(usernameB == null)
                    return ownerA.compareTo(ownerB);
                return 1;
            }
            return usernameB == null ? -1 : usernameA.compareTo(usernameB);
        });
        this.updateContentPositions();
    }

    public void sortCategoryEntries(){
        for(CategoryWidget category : this.categories)
            category.sortEntries();
    }

    public void onCategoryCollapse(CategoryWidget category){
        this.updateContentPositions();
        this.setScrollOffset(this.scrollOffset, true);
    }

    public void onCategoryExpand(CategoryWidget category){
        this.updateContentPositions();
        int contentArea = this.height - 27;
        int categoryTop = 0;
        for(CategoryWidget c : this.categories){
            if(c == category)
                break;
            categoryTop += c.getStaticHeight();
        }
        int idealMinScrollOffset = categoryTop + category.getStaticHeight() - contentArea;
        if(idealMinScrollOffset >= categoryTop)
            this.setScrollOffset(categoryTop, false);
        else
            this.setScrollOffset(Math.clamp(this.scrollOffset, idealMinScrollOffset, categoryTop), false);
    }

    public boolean isMouseBelowCategory(CategoryWidget category, int mouseY){
        int y = this.y + 23 - (int)this.scrollOffset;
        for(CategoryWidget c : this.categories){
            y += c.height();
            if(y > mouseY)
                return false;
            if(c == category)
                break;
        }
        return true;
    }

    private void updateFilter(String filter){
        filter = filter.trim().toLowerCase(Locale.ROOT);
        int contentHeightBefore = 0;
        for(CategoryWidget c : this.categories)
            contentHeightBefore += c.height();
        for(CategoryWidget category : this.categories)
            category.updateFilter(filter);
        int contentHeightAfter = 0;
        for(CategoryWidget c : this.categories)
            contentHeightAfter += c.height();
        if(contentHeightBefore != contentHeightAfter)
            this.setScrollOffset(0, true);
    }

    @Override
    protected void addWidgets(){
        SearchBarWidget searchBar = this.addWidget(new SearchBarWidget(this.x + 4, this.y + 4, 126, 16, "", 20, this::updateFilter));
        searchBar.setSuggestion(Language.getInstance().getOrDefault("travelerswaystones.screen.teleport.search.hint"));
        ScissorWidget scissorWidget = ScissorWidget.create(this.x + 4, this.y + 23, this.width - 13, this.height - 27);
        this.categories.forEach(scissorWidget::addWidget);
        this.addWidget(scissorWidget);
        this.addWidget(
            ScrollbarWidget.builder(this.height - 25)
                .width(4)
                .scrollerHeight(40)
                .position(this.x + this.width - 7,this.y + 22)
                .noBackground()
                .scroller(SCROLLER)
                .scrollValue(() -> this.scrollOffset, () -> 0, this::maxScrollOffset)
                .smoothScrolling()
                .onChange(v -> this.setScrollOffset((float)v, true))
                .build()
        );
    }

    @Override
    public void renderBackground(WidgetRenderContext context, GuiGraphicsHelper graphics, int mouseX, int mouseY){
        // Update scrolling
        if(this.scrollOffset != this.scrollTarget){
            if(Math.abs(this.scrollOffset - this.scrollTarget) <= SCROLL_SPEED)
                this.scrollOffset = this.scrollTarget;
            else
                this.scrollOffset += Math.signum(this.scrollTarget - this.scrollOffset) * SCROLL_SPEED;
            this.updateContentPositions();
        }

        graphics.submitSprite(BACKGROUND, this.x, this.y, this.width, this.height);
        super.renderBackground(context, graphics, mouseX, mouseY);
    }

    @Override
    public boolean mouseScrolled(int mouseX, int mouseY, double scrollAmount, boolean hasBeenHandled){
        if(!hasBeenHandled && mouseX >= this.x + 8 && mouseX < this.x + 145 && mouseY >= this.y + 23 && mouseY < this.y + this.height - 4){
            this.adjustScrollOffset(-(float)scrollAmount * 5, false);
            hasBeenHandled = true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollAmount, hasBeenHandled);
    }
}
