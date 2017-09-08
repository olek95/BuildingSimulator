package menu;

import authorization.User;
import buildingsimulator.GameManager;
import com.jme3.input.event.MouseButtonEvent;
import texts.Translator;
import tonegod.gui.core.Screen;

public class PauseMenu extends MainMenu{
    public PauseMenu() {
        super("Interface/pause_menu.gui.xml");
        Screen screen = MainMenu.getScreen(); 
        Translator.setTexts(new String[]{"start_game_button", "exit_label", 
            "exit_game_button", "cancel_button"}, new Translator[]{Translator.GAME_CONTINUATION,
                Translator.EXIT_WARNING, Translator.EXIT_DESKTOP, Translator.CANCELLATION}, screen);
        MainMenu.getScreen().getElementById("exit_popup").hide();
        User user = GameManager.getUser();
        screen.getElementById("login_label").setText(user == null ? "Anonim" :
                user.getLogin());
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
    
    @Override
    public void exit(MouseButtonEvent evt, boolean isToggled){
        super.exit(evt, isToggled);
    }
    
    public void showExitPopup(MouseButtonEvent evt, boolean isToggled){
        MainMenu.getScreen().getElementById("exit_popup").showAsModal(true);
    }
    
    public void cancel(MouseButtonEvent evt, boolean isToggled){
        MainMenu.getScreen().getElementById("exit_popup").hide();
    }
}
