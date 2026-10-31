package com.supermartijn642.waystonesreimagined.content.screen.components.search;

import com.supermartijn642.core.TextComponents;
import com.supermartijn642.core.gui.widget.premade.TextFieldWidget;
import net.minecraft.network.chat.Component;

import java.util.function.Consumer;

public class SearchBarWidget extends TextFieldWidget {

    // TODO keep track of and show recent searches

    public SearchBarWidget(int x, int y, int width, int height, String defaultText, int maxLength, Consumer<String> changeListener){
        super(x, y, width, height, defaultText, maxLength, changeListener);
    }

    @Override
    public Component getNarrationMessage(){
        return TextComponents.translation("waystonesreimagined.screen.teleport.search.narrate", this.getText()).get();
    }
}
