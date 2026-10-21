package com.supermartijn642.waystonesreimagined.content.screen.components.search;

import com.supermartijn642.core.gui.widget.premade.TextFieldWidget;

import java.util.function.Consumer;

/**
 * Created 22/08/2026 by SuperMartijn642
 */
public class SearchBarWidget extends TextFieldWidget {

    // TODO keep track of and show recent searches

    public SearchBarWidget(int x, int y, int width, int height, String defaultText, int maxLength, Consumer<String> changeListener){
        super(x, y, width, height, defaultText, maxLength, changeListener);
    }
}
