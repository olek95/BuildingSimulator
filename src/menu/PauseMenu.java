package menu;

import buildingsimulator.GameManager;
import com.jme3.input.event.MouseButtonEvent;
import texts.Translator;

public class PauseMenu extends MainMenu{
    public PauseMenu() {
        super("Interface/pause_menu.gui.xml");
        Translator.setTexts(new String[]{"start_game_button"},
                new Translator[]{Translator.GAME_CONTINUATION}, MainMenu.getScreen());
    }
    
    @Override
    public void start(MouseButtonEvent evt, boolean isToggled) {
        super.start(evt, isToggled); 
        GameManager.continueGame();
    }
    
    @Override
    public void showOptions(MouseButtonEvent evt, boolean isToggled){ 
        super.showOptions(evt, isToggled);
    }
}
