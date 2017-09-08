package menu;

import texts.Translator;

public class PauseMenu extends MainMenu{
    public PauseMenu() {
        super("Interface/pause_menu.gui.xml");
        Translator.setTexts(new String[]{"start_game_button"},
                new Translator[]{Translator.GAME_CONTINUATION}, MainMenu.getScreen());
    }
}
