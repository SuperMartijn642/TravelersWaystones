package com.supermartijn642.waystonesreimagined.generators;

import com.supermartijn642.core.generator.AtlasSourceGenerator;
import com.supermartijn642.core.generator.ResourceCache;
import com.supermartijn642.waystonesreimagined.WaystonesReimagined;
import com.supermartijn642.waystonesreimagined.content.screen.ClaimWaystoneScreen;
import com.supermartijn642.waystonesreimagined.content.screen.components.*;
import com.supermartijn642.waystonesreimagined.content.screen.components.search.CategoryWidget;
import com.supermartijn642.waystonesreimagined.content.screen.components.search.FavoriteButtonWidget;
import com.supermartijn642.waystonesreimagined.content.screen.components.search.SearchWidget;
import com.supermartijn642.waystonesreimagined.content.screen.components.search.TargetEntryWidget;
import com.supermartijn642.waystonesreimagined.content.screen.components.warning.CancelButton;
import com.supermartijn642.waystonesreimagined.content.screen.components.warning.DangerousWarningWidget;
import com.supermartijn642.waystonesreimagined.content.screen.components.warning.SuppressCheckbox;

/**
 * Created 18/08/2026 by SuperMartijn642
 */
public class WaystonesReimaginedAtlasSourceGenerator extends AtlasSourceGenerator {

    public WaystonesReimaginedAtlasSourceGenerator(ResourceCache cache){
        super(WaystonesReimagined.MODID, cache);
    }

    @Override
    public void generate(){
        this.guiAtlas().texture(InfoCardWidget.BACKGROUND_STANDALONE)
            .texture(InfoCardWidget.BACKGROUND_ATTACHED)
            .texture(InfoCardWidget.OWNER_ICON)
            .texture(InfoCardWidget.DIMENSION_ICON)
            .texture(InfoCardWidget.BIOME_ICON)
            .texture(InfoCardWidget.POSITION_ICON)
            .texture(SearchWidget.BACKGROUND)
            .texture(SearchWidget.SCROLLER)
            .texture(CategoryWidget.BACKGROUND)
            .texture(CategoryWidget.Recents.ICON)
            .texture(FavoriteButtonWidget.STAR)
            .texture(TargetEntryWidget.BACKGROUND)
            .texture(EditNameButton.EDIT_ICON)
            .texture(EditNameTextField.BACKGROUND)
            .texture(ClaimWaystoneScreen.BUTTON_TAB)
            .texture(RelocateButton.RELOCATE_ICON)
            .texture(DangerousWarningWidget.BACKGROUND)
            .texture(DangerousWarningWidget.DANGEROUS_ICON)
            .texture(SuppressCheckbox.ICONS)
            .texture(CancelButton.BACKGROUND)
            .texture(WarningsSuppressedIndicator.DANGEROUS_ICON);
    }
}
