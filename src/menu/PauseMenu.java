package menu;

import authorization.DBManager;
import authorization.User;
import buildingsimulator.BuildingSimulator;
import buildingsimulator.GameManager;
import buildingsimulator.SavedData;
import com.jme3.export.binary.BinaryExporter;
import com.jme3.input.event.MouseButtonEvent;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import texts.Translator;
import tonegod.gui.controls.buttons.Button;
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
        Translator.setTexts(new String[]{"start_game_button", "save_game_button",
            "exit_label", "exit_game_button", "cancel_button",
            "return_starting_menu_button"},
                new Translator[]{Translator.GAME_CONTINUATION, Translator.SAVE_GAME,
                    Translator.EXIT_WARNING, Translator.EXIT_DESKTOP,
                    Translator.CANCELLATION, Translator.RETURN_TO_STARTING_MENU},
                screen);
        MainMenu.getScreen().getElementById("exit_popup").hide();
        String login = GameManager.getUser().getLogin(); 
        screen.getElementById("login_label").setText(login);
        ((Button)MainMenu.getScreen().getElementById("save_game_button"))
                .setIgnoreMouse(login.equals(User.DEFAULT_LOGIN));
        ((Window)screen.getElementById("exit_popup")).getDragBar().setIsMovable(false);
    }
    
    /**
     * Kontynuuje grę. 
     * @param evt
     * @param isToggled 
     */
    public void start(MouseButtonEvent evt, boolean isToggled) {
        super.start(); 
        GameManager.continueGame();
    }
    
    public void save(MouseButtonEvent evt, boolean isToggled) {
        BinaryExporter exporter = BinaryExporter.getInstance();
        File file = new File("./game saves/" + GameManager.getUser().getLogin() 
                + "/save.j3o");
        try {
            exporter.save(new SavedData(), file);
            savePoints();
            setLoadingButtonState(false);
        } catch (IOException ex) {
            Logger.getLogger(PauseMenu.class.getName()).log(Level.SEVERE, null, ex);
        } 
    }
    
    public void load(MouseButtonEvent evt, boolean isToggled) {
        GameManager.deleteGame();
        load();
    }
    
    /**
     * Pokazuje statystyki. 
     * @param evt
     * @param isToggled 
     */
    public void showStatistics(MouseButtonEvent evt, boolean isToggled) {
        goNextMenu(MainMenu.getScreen(), MenuTypes.STATISTICS);
    }
    
    /**
     * Pokazuje opcje gry. 
     * @param evt
     * @param isToggled 
     */
    public void showOptions(MouseButtonEvent evt, boolean isToggled){ 
        goNextMenu(MainMenu.getScreen(), MenuTypes.OPTIONS);
    }
    
    /**
     * Wychodzi z gry. 
     * @param evt
     * @param isToggled 
     */
    public void exit(MouseButtonEvent evt, boolean isToggled){ 
        savePoints(); 
        exit(); 
    }
    
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
    
    /**
     * Powraca do menu startowego. 
     * @param evt
     * @param isToggled 
     */
    public void returnToStartingMenu(MouseButtonEvent evt, boolean isToggled) {
        getWindow().hide();
        if(!savePoints()) GameManager.setUser(null);
        Element exitPopup = MainMenu.getScreen().getElementById("exit_popup"); 
        exitPopup.hide();
        Screen screen = MainMenu.getScreen(); 
        screen.removeElement(exitPopup);
        BuildingSimulator.getBuildingSimulator().getGuiNode().removeControl(screen);
        GameManager.deleteGame();
        MenuFactory.showMenu(MenuTypes.STARTING_MENU);
    }
    
    private boolean savePoints() {
        User user = GameManager.getUser(); 
        if(!user.getLogin().equals(User.DEFAULT_LOGIN)) {
            try {
                DBManager.savePoints(user);
            }catch(SQLException|ClassNotFoundException ex) {
                ex.printStackTrace();
            }
            return true; 
        } else return false; 
    }
}
