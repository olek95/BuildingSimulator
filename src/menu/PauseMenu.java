package menu;

import authorization.User;
import buildingsimulator.BuildingSimulator;
import buildingsimulator.GameManager;
import com.jme3.input.event.MouseButtonEvent;
import texts.Translator;
import tonegod.gui.controls.windows.Window;
import tonegod.gui.core.Element;
import tonegod.gui.core.Screen;

/**
 * Klasa <code>PauseMenu</code> reprezentuje menu podczas pauzy gry. 
 * @author AleksaderSklorz
 */
public class PauseMenu extends MainMenu{
    public PauseMenu() {
        super("Interface/pause_menu.gui.xml");
        Screen screen = MainMenu.getScreen(); 
        Translator.setTexts(new String[]{"start_game_button", "exit_label", 
            "exit_game_button", "cancel_button", "return_starting_menu_button"},
                new Translator[]{Translator.GAME_CONTINUATION, Translator.EXIT_WARNING,
                    Translator.EXIT_DESKTOP, Translator.CANCELLATION,
                    Translator.RETURN_TO_STARTING_MENU}, screen);
        MainMenu.getScreen().getElementById("exit_popup").hide();
        User user = GameManager.getUser();
        screen.getElementById("login_label").setText(user.getLogin());
        ((Window)screen.getElementById("exit_popup")).getDragBar().setIsMovable(false);
    }
    
    public void start(MouseButtonEvent evt, boolean isToggled) {
        super.start(); 
        GameManager.continueGame();
    }
    
    public void showOptions(MouseButtonEvent evt, boolean isToggled){ showOptions(); }
    
    
    public void exit(MouseButtonEvent evt, boolean isToggled){ exit(); }
    
    /**
     * Wyświetla okienko ostrzegające przed wyjściem z gry. Umożliwia wyjście 
     * do pulpitu lub do menu startowego. 
     * @param evt
     * @param isToggled 
     */
    public void showExitPopup(MouseButtonEvent evt, boolean isToggled){
        MainMenu.getScreen().getElementById("exit_popup").showAsModal(true);
    }
    
    /**
     * Anuluje wyjście z gry. 
     * @param evt
     * @param isToggled 
     */
    public void cancel(MouseButtonEvent evt, boolean isToggled){
        MainMenu.getScreen().getElementById("exit_popup").hide();
    }
    
    public void returnToStartingMenu(MouseButtonEvent evt, boolean isToggled) {
        window.hide();
        Element exitPopup = MainMenu.getScreen().getElementById("exit_popup"); 
        exitPopup.hide();
        Screen screen = MainMenu.getScreen(); 
        screen.removeElement(exitPopup);
        BuildingSimulator.getBuildingSimulator().getGuiNode().removeControl(screen);
        GameManager.deleteGame();
        MenuFactory.showMenu(MenuTypes.STARTING_MENU);
    }
}
