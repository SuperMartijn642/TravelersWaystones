package com.supermartijn642.travelerswaystones.content.screen.components;

import com.supermartijn642.core.ClientUtils;
import com.supermartijn642.core.TextComponents;
import com.supermartijn642.core.gui.GuiGraphicsHelper;
import com.supermartijn642.core.gui.widget.BaseWidget;
import com.supermartijn642.core.gui.widget.WidgetRenderContext;
import com.supermartijn642.core.util.playerprofile.PlayerProfileCache;
import com.supermartijn642.travelerswaystones.TravelersWaystones;
import com.supermartijn642.travelerswaystones.api.targets.WaystoneTarget;
import com.supermartijn642.travelerswaystones.api.targets.personal.DiscoveredWaystoneTarget;
import com.supermartijn642.travelerswaystones.content.moving.WaystoneMovingHandlerClient;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.BlockPos;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Matrix3x2fStack;
import org.joml.Quaternionf;

import java.util.function.BiConsumer;
import java.util.function.Supplier;

public class InfoCardWidget extends BaseWidget {

    public static final Identifier BACKGROUND_STANDALONE = TravelersWaystones.identifier("gui/card_standalone");
    public static final Identifier BACKGROUND_ATTACHED = TravelersWaystones.identifier("gui/card_attached");
    public static final Identifier OWNER_ICON = TravelersWaystones.identifier("gui/owner_icon");
    public static final Identifier DIMENSION_ICON = TravelersWaystones.identifier("gui/dimension_icon");
    public static final Identifier BIOME_ICON = TravelersWaystones.identifier("gui/biome_icon");
    public static final Identifier POSITION_ICON = TravelersWaystones.identifier("gui/position_icon");

    private final boolean standalone;
    private final Component title;
    private final Supplier<DiscoveredWaystoneTarget> target;
    private final boolean allowRelocate;
    private RelocateButton relocateButton;
    private final boolean showSuppressedWarnings;
    private final boolean alwaysShowNameField;
    private final BiConsumer<DiscoveredWaystoneTarget,String> updateName;
    private EditNameButton editNameButton;
    private EditNameTextField editNameField;
    private DiscoveredWaystoneTarget editingTarget;
    private boolean selectNameField;

    public InfoCardWidget(int x, int y, boolean standalone, String title, Supplier<DiscoveredWaystoneTarget> target, boolean allowRelocate, boolean showSuppressedWarnings, boolean alwaysShowNameField, BiConsumer<DiscoveredWaystoneTarget,String> updateName){
        super(x, y, 140, 82);
        this.standalone = standalone;
        this.title = TextComponents.translation(title).get();
        this.target = target;
        this.allowRelocate = allowRelocate;
        this.showSuppressedWarnings = showSuppressedWarnings;
        this.alwaysShowNameField = alwaysShowNameField;
        this.updateName = updateName;
    }

    @Override
    public Component getNarrationMessage(){
        return TextComponents.translation("travelerswaystones.screen.info_card.narrate", this.title).get();
    }

    public void selectNameField(){
        if(this.editNameField == null)
            this.selectNameField = true;
        else if(this.editNameField.isEnabled())
            this.editNameField.setSelected(true);
    }

    @Override
    protected void addWidgets(){
        WaystonePreviewWidget preview = this.addWidget(new WaystonePreviewWidget(this.x + 5, this.y + 34, 22, 43, this.target));
        if(this.showSuppressedWarnings)
            preview.addWidget(new WarningsSuppressedIndicator(this.x + 18, this.y + 69, this.target));
        if(this.allowRelocate)
            this.relocateButton = this.addWidget(new RelocateButton(this.x + 126, this.y + 68, () -> {
                WaystoneMovingHandlerClient.startMoving(this.target.get().target());
                ClientUtils.closeScreen();
            }));
        this.editNameField = this.addWidget(new EditNameTextField(this.x + 4, this.y + 16, 132, 14, "", WaystoneTarget.MAX_NAME_LENGTH, name -> {
            if(this.alwaysShowNameField)
                this.updateName.accept(this.target.get(), name);
        }, name -> {
            if(this.editingTarget != null && !name.trim().equals(this.editingTarget.name()))
                this.updateName.accept(this.editingTarget, name);
            this.editingTarget = null;
        }));
        this.editNameField.setSuggestion(I18n.get("travelerswaystones.screen.claim.name.enter"));
        if(!this.alwaysShowNameField){
            this.editNameField.setEnabled(false);
            this.editNameButton = this.addWidget(new EditNameButton(this.x + 126, this.y + 19, () -> {
                this.editingTarget = this.target.get();
                this.editNameField.setEnabled(true);
                this.editNameField.setTextSuppressed(this.target.get().name());
                if(this.focusedWidget != null)
                    this.focusedWidget.setFocused(false);
                this.focusedWidget = this.editNameField;
                this.editNameField.setFocused(true);
                this.editNameField.setSelected(true);
                this.editNameField.moveCursorToEnd();
            }));
        }
    }

    @Override
    public void renderBackground(WidgetRenderContext context, GuiGraphicsHelper graphics, int mouseX, int mouseY){
        DiscoveredWaystoneTarget target = this.target.get();
        boolean isOwner = target.owner().equals(ClientUtils.getPlayer().getUUID());
        if(this.allowRelocate)
            this.relocateButton.setActive(isOwner && target.waystoneType().isMoveable(target.block()));
        if(!this.alwaysShowNameField){
            this.editNameButton.setActive(isOwner);
            if(this.editNameField.isEnabled() && !this.editNameField.isSelected())
                this.editNameField.setEnabled(false);
        }
        if(this.selectNameField){
            if(this.editNameField.isEnabled())
                this.editNameField.setSelected(true);
            this.selectNameField = false;
        }

        graphics.submitSprite(this.standalone ? BACKGROUND_STANDALONE : BACKGROUND_ATTACHED, this.x, this.y, this.width, this.height);
        super.renderBackground(context, graphics, mouseX, mouseY);
    }

    @Override
    public void render(WidgetRenderContext context, GuiGraphicsHelper graphics, int mouseX, int mouseY){
        Matrix3x2fStack poseStack = graphics.poseStack();
        if(!graphics.submitPlayerHead(this.target.get().owner(), this.x + 31, this.y + 35, 8, 8, false)){
            poseStack.pushMatrix();
            poseStack.translate(0.5f, 0);
            graphics.submitSprite(OWNER_ICON, this.x + 30, this.y + 35, 8, 8);
            poseStack.popMatrix();
        }
        renderDimensionIcon(graphics, this.x + 31, this.y + 46, this.target.get().dimension());
        poseStack.pushMatrix();
        poseStack.translate(0.5f, 0.5f);
        graphics.submitSprite(BIOME_ICON, this.x + 30, this.y + 57, 8, 8);
        poseStack.popMatrix();
        poseStack.pushMatrix();
        poseStack.translate(0.5f, 0.5f);
        graphics.submitSprite(POSITION_ICON, this.x + 31, this.y + 68, 7, 7);
        poseStack.popMatrix();
        DiscoveredWaystoneTarget waystone = this.target.get();
        graphics.submitText(TextComponents.string(waystone.name()).get(), this.x + 7, this.y + 19, p -> p.shadow().color(255, 255, 255));
        super.render(context, graphics, mouseX, mouseY);
    }

    @Override
    public void renderForeground(WidgetRenderContext context, GuiGraphicsHelper graphics, int mouseX, int mouseY){
        graphics.submitText(this.title, this.x + 70, this.y + 8, p -> p.center());
        Matrix3x2fStack poseStack = graphics.poseStack();
        poseStack.pushMatrix();
        poseStack.translate(0, 0.5f);
        DiscoveredWaystoneTarget waystone = this.target.get();
        String username = PlayerProfileCache.get(true).getUsername(waystone.owner());
        Component owner = username == null ?
            TextComponents.translation("travelerswaystones.screen.teleport.owner.unknown").get() :
            TextComponents.string(username).get();
        graphics.submitText(owner, this.x + 43, this.y + 35);
        Identifier dimension = waystone.dimension().identifier();
        Component dimensionName = Language.getInstance().getOrDefault(dimension.toLanguageKey("dimension"), null) == null ?
            TextComponents.string(dimension.getPath()).get() :
            TextComponents.translation(dimension.toLanguageKey("dimension")).get();
        graphics.submitText(dimensionName, this.x + 43, this.y + 46);
        Identifier biome = waystone.biome().identifier();
        Component biomeName = Language.getInstance().getOrDefault(biome.toLanguageKey("biome"), null) == null ?
            TextComponents.string(biome.getPath()).get() :
            TextComponents.translation(biome.toLanguageKey("biome")).get();
        graphics.submitText(biomeName, this.x + 43, this.y + 57);
        BlockPos pos = waystone.pos();
        graphics.submitText(TextComponents.string(pos.getX() + ", " + pos.getY() + ", " + pos.getZ()).get(), this.x + 43, this.y + 68);
        poseStack.popMatrix();
        super.renderForeground(context, graphics, mouseX, mouseY);
    }

    @Override
    public void renderTooltips(WidgetRenderContext context, GuiGraphicsHelper graphics, int mouseX, int mouseY){
        super.renderTooltips(context, graphics, mouseX, mouseY);
        if(!this.isFocused() || this.focusedWidget != null)
            return;
        mouseX -= this.x;
        mouseY -= this.y;
        DiscoveredWaystoneTarget waystone = this.target.get();
        if(mouseX >= 41 && mouseX < 41 + 94 && mouseY >= 32 && mouseY < 32 + 10){
            if(PlayerProfileCache.get(true).getUsername(waystone.owner()) == null)
                graphics.submitTooltipForTopStratum(t -> t.literal(waystone.owner().toString()), mouseX + this.x, mouseY + this.y);
        }else if(mouseX >= 41 && mouseX < 41 + 94 && mouseY >= 43 && mouseY < 43 + 10)
            graphics.submitTooltipForTopStratum(t -> t.literal(waystone.dimension().identifier().toString()), mouseX + this.x, mouseY + this.y);
        else if(mouseX >= 41 && mouseX < 41 + 94 && mouseY >= 54 && mouseY < 54 + 10)
            graphics.submitTooltipForTopStratum(t -> t.literal(waystone.biome().identifier().toString()), mouseX + this.x, mouseY + this.y);
    }

    public static void renderDimensionIcon(GuiGraphicsHelper graphics, int x, int y, ResourceKey<Level> dimension){
        Block block = null;
        if(Level.OVERWORLD.equals(dimension))
            block = Blocks.GRASS_BLOCK;
        else if(Level.NETHER.equals(dimension))
            block = Blocks.NETHERRACK;
        else if(Level.END.equals(dimension))
            block = Blocks.END_STONE;
        if(block == null){
            graphics.submitSprite(DIMENSION_ICON, x, y, 8, 8);
            return;
        }
        BlockState state = block.defaultBlockState();
        Matrix3x2fStack poseStack2d = graphics.poseStack();
        poseStack2d.pushMatrix();
        poseStack2d.translate(0.5f, 0);
        int size = 10;
        float scale = size / (float)Math.sqrt(3);
        graphics.submitCustomRendering(x - 2, y - 1, size, size, (poseStack, bufferSource) -> {
            poseStack.pushPose();
            poseStack.translate(size / 2f, size / 2f, 0);
            poseStack.scale(scale, -scale, -scale);
            poseStack.mulPose(new Quaternionf().rotateX((float)Math.toRadians(30)));
            poseStack.mulPose(new Quaternionf().rotateY((float)Math.toRadians(45)));
            poseStack.translate(-0.5f, -0.5f, -0.5f);
            WaystonePreviewWidget.renderBlock(poseStack, bufferSource, state, BlockPos.ZERO);
            poseStack.popPose();
        });
        poseStack2d.popMatrix();
    }
}
