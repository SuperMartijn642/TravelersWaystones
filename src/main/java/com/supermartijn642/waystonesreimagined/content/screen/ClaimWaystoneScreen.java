package com.supermartijn642.waystonesreimagined.content.screen;

import com.supermartijn642.core.ClientUtils;
import com.supermartijn642.core.TextComponents;
import com.supermartijn642.core.gui.GuiGraphicsHelper;
import com.supermartijn642.core.gui.WidgetScreen;
import com.supermartijn642.core.gui.widget.BaseWidget;
import com.supermartijn642.core.gui.widget.WidgetRenderContext;
import com.supermartijn642.core.gui.widget.premade.ButtonWidget;
import com.supermartijn642.waystonesreimagined.WaystonesReimagined;
import com.supermartijn642.waystonesreimagined.api.targets.WaystoneTarget;
import com.supermartijn642.waystonesreimagined.api.targets.personal.DiscoveredWaystoneTarget;
import com.supermartijn642.waystonesreimagined.content.packets.ClaimWaystonePacket;
import com.supermartijn642.waystonesreimagined.content.screen.components.InfoCardWidget;
import com.supermartijn642.waystonesreimagined.content.targets.WaystoneTargetImpl;
import com.supermartijn642.waystonesreimagined.content.targets.personal.DiscoveredWaystoneTargetImpl;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class ClaimWaystoneScreen extends BaseWidget {

    public static Screen create(UUID owner, Identifier dimension, Identifier biome, BlockPos pos, BlockState state, boolean isNewlyPlaced, @Nullable String nameSuggestion) {
        return WidgetScreen.of(new ClaimWaystoneScreen(owner, dimension, biome, pos, state, isNewlyPlaced, nameSuggestion));
    }

    public static final Identifier BUTTON_TAB = WaystonesReimagined.identifier("gui/card_button_tab");

    private final boolean isNewlyPlaced;
    private final DiscoveredWaystoneTarget dummyTarget;
    private ButtonWidget confirmButton;

    public ClaimWaystoneScreen(UUID owner, Identifier dimension, Identifier biome, BlockPos pos, BlockState state, boolean isNewlyPlaced, @Nullable String nameSuggestion) {
        super(0, 0, 140, 106);
        this.isNewlyPlaced = isNewlyPlaced;
        this.dummyTarget = new DiscoveredWaystoneTargetImpl(new WaystoneTargetImpl(true, 0, nameSuggestion == null ? "" : nameSuggestion, owner, dimension, biome, pos, state));
    }

    @Override
    public Component getNarrationMessage(){
        return TextComponents.translation("waystonesreimagined.screen.claim.narrate").get();
    }

    @Override
    protected void addWidgets(){
        InfoCardWidget cardWidget = this.addWidget(new InfoCardWidget(
            0, 0,
            true,
            "waystonesreimagined.screen.claim.title",
            () -> this.dummyTarget,
            false,
            false,
            true,
            (target, name) -> ((WaystoneTargetImpl)target.target()).setName(name)
        ));
        cardWidget.selectNameField();
        this.confirmButton = this.addWidget(new ButtonWidget(
            30, 84, 80, 18,
            TextComponents.translation(this.isNewlyPlaced ? "waystonesreimagined.screen.claim.create" : "waystonesreimagined.screen.claim.claim").get(),
            () -> {
                WaystonesReimagined.CHANNEL.sendToServer(new ClaimWaystonePacket(this.dummyTarget.pos(), this.dummyTarget.name()));
                ClientUtils.closeScreen();
            }
        ));
        this.confirmButton.setActive(false);
    }

    @Override
    public void renderBackground(WidgetRenderContext context, GuiGraphicsHelper graphics, int mouseX, int mouseY){
        String name = this.dummyTarget.name().trim();
        boolean isValidName = name.length() >= WaystoneTarget.MIN_NAME_LENGTH && name.length() <= WaystoneTarget.MAX_NAME_LENGTH;
        this.confirmButton.setActive(isValidName);

        graphics.submitSprite(BUTTON_TAB, 26, 82, 88, 24);
        super.renderBackground(context, graphics, mouseX, mouseY);
    }

    @Override
    public void renderTooltips(WidgetRenderContext context, GuiGraphicsHelper graphics, int mouseX, int mouseY){
        super.renderTooltips(context, graphics, mouseX, mouseY);
        if(mouseX >= this.confirmButton.left() && mouseX < this.confirmButton.right()
            && mouseY >= this.confirmButton.top() && mouseY < this.confirmButton.bottom()){
            String name = this.dummyTarget.name().trim();
            boolean isValidName = name.length() >= WaystoneTarget.MIN_NAME_LENGTH && name.length() <= WaystoneTarget.MAX_NAME_LENGTH;
            if(!isValidName)
                graphics.submitTooltipForTopStratum(t -> t.text(TextComponents.translation("waystonesreimagined.screen.claim.name.invalid").get()), mouseX, mouseY);
        }
    }
}
