package com.supermartijn642.travelerswaystones.content.screen;

import com.supermartijn642.core.TextComponents;
import com.supermartijn642.core.gui.WidgetScreen;
import com.supermartijn642.core.gui.widget.BaseWidget;
import com.supermartijn642.core.gui.widget.WidgetRenderContext;
import com.supermartijn642.core.gui.widget.premade.ButtonWidget;
import com.supermartijn642.core.gui.widget.premade.StackedWidget;
import com.supermartijn642.travelerswaystones.TravelersWaystones;
import com.supermartijn642.travelerswaystones.api.targets.WaystoneTarget;
import com.supermartijn642.travelerswaystones.api.targets.personal.DiscoveredWaystoneTarget;
import com.supermartijn642.travelerswaystones.api.targets.personal.PersonalWaystoneManager;
import com.supermartijn642.travelerswaystones.content.packets.RenameTargetPacket;
import com.supermartijn642.travelerswaystones.content.packets.TeleportPacket;
import com.supermartijn642.travelerswaystones.content.screen.components.InfoCardWidget;
import com.supermartijn642.travelerswaystones.content.screen.components.search.SearchWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiConsumer;

public class WaystoneTeleportScreen extends BaseWidget {

    public static Screen create(@Nullable DiscoveredWaystoneTarget current, @Nullable DiscoveredWaystoneTarget selected, boolean trusted, PersonalWaystoneManager waystones){
        return WidgetScreen.of(StackedWidget.create(new WaystoneTeleportScreen(current, selected, trusted, waystones)));
    }

    private final @Nullable DiscoveredWaystoneTarget current;
    private final @Nullable DiscoveredWaystoneTarget selected;
    private final boolean trusted;
    private final PersonalWaystoneManager waystones;
    private SearchWidget searchWidget;
    private ButtonWidget teleportButton;

    private WaystoneTeleportScreen(@Nullable DiscoveredWaystoneTarget current, @Nullable DiscoveredWaystoneTarget selected, boolean trusted, PersonalWaystoneManager waystones){
        super(0, 0, 415, 194);
        this.current = current;
        this.selected = selected;
        this.trusted = trusted;
        this.waystones = waystones;
    }

    @Nullable
    public DiscoveredWaystoneTarget getCurrentTarget(){
        return this.current;
    }

    public boolean isTrusted(){
        return this.trusted;
    }

    @Override
    public Component getNarrationMessage(){
        return TextComponents.translation("travelerswaystones.screen.teleport.narrate").get();
    }

    @Override
    protected void addWidgets(){
        boolean hasCurrent = this.current != null;
        SearchWidget searchWidget = new SearchWidget(hasCurrent ? 142 : 0, 0, this.waystones, this.selected);
        int cardY = Math.max((searchWidget.height() - 80) / 2 - 20, 10);
        BiConsumer<DiscoveredWaystoneTarget,String> nameUpdater = (target, name) -> {
            name = name.trim();
            if(name.length() >= WaystoneTarget.MIN_NAME_LENGTH && name.length() <= WaystoneTarget.MAX_NAME_LENGTH)
                TravelersWaystones.CHANNEL.sendToServer(new RenameTargetPacket(target.id(), name));
        };
        if(hasCurrent)
            this.addWidget(new InfoCardWidget(0, cardY, true, "travelerswaystones.screen.teleport.current", () -> this.current, true, false, false, nameUpdater));
        this.searchWidget = this.addWidget(searchWidget);
        InfoCardWidget destinationCard = this.addWidget(new InfoCardWidget(hasCurrent ? 275 : 133, cardY, false, "travelerswaystones.screen.teleport.destination", () -> this.searchWidget.getSelectedTarget(), false, true, false, nameUpdater));
        this.teleportButton = this.addWidget(new ButtonWidget(
            destinationCard.left() + (destinationCard.width() - 80) / 2, destinationCard.bottom() + 10, 80, 18,
            TextComponents.translation("travelerswaystones.screen.teleport.teleport").get(),
            () -> {
                DiscoveredWaystoneTarget target = this.searchWidget.getSelectedTarget();
                if(target != null)
                    TravelersWaystones.CHANNEL.sendToServer(new TeleportPacket(this.current == null ? null : this.current.id(), target.id(), target.suppressDangerousWarning(), this.trusted));
            }
        ));
    }

    @Override
    public int height(){
        return Math.max(
            this.searchWidget.height(),
            this.teleportButton.bottom()
        );
    }

    @Override
    public int width(){
        return this.current == null ?
            this.searchWidget.width() + 139 :
            142 + this.searchWidget.width() + 139;
    }

    @Override
    public void renderBackground(WidgetRenderContext context, int mouseX, int mouseY){
        DiscoveredWaystoneTarget target = this.searchWidget.getSelectedTarget();
        this.teleportButton.setActive(target != null && target != this.current);
        super.renderBackground(context, mouseX, mouseY);
    }

    @Override
    public void render(WidgetRenderContext context, int mouseX, int mouseY){
        super.render(context, mouseX, mouseY);
    }

    @Override
    public void renderForeground(WidgetRenderContext context, int mouseX, int mouseY){
        super.renderForeground(context, mouseX, mouseY);
    }
}
