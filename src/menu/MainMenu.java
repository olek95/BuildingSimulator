package menu;

import buildingsimulator.BuildingSimulator;
import buildingsimulator.GameManager;
import com.jme3.app.Application;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.input.event.MouseButtonEvent;
import tonegod.gui.controls.windows.Window;
import tonegod.gui.core.Screen;

/**
 * Singleton <code>MainMenu</code> reprezentuje menu główne gry. Posiada możliwość
 * uruchomienia nowej gry, wczytania zapisanej gry, pokazania statystyk, zmiany 
 * ustawień gry, wyjscia z gry oraz logowania i rejestracji. 
 * @author AleksanderSklorz 
 */
public class MainMenu extends AbstractAppState {
    private static Window mainMenu;
    private static Screen screen = new Screen(BuildingSimulator.getBuildingSimulator());
    private static MainMenu menu;
    private MainMenu(){
        screen.parseLayout("Interface/main_menu.gui.xml", this);
        mainMenu = (Window)screen.getElementById("main_menu");
        ((Window)screen.getElementById("authorization_popup")).hide();
        screen.setUseCustomCursors(true);
        
    }
    
    /**
     * Pokazuje menu główne gry. 
     */
    public static void showMenu(){
        if(menu == null) menu = new MainMenu(); 
        BuildingSimulator.getBuildingSimulator().getGuiNode().addControl(screen);
    }
    
    /** 
     * Uruchamia grę 
     * @param evt 
     * @param isToggled 
     */
    public void start(MouseButtonEvent evt, boolean isToggled) {
        mainMenu.hide();
        screen.setUseCustomCursors(false);
        GameManager.runGame();
    }
    
    public void signIn(MouseButtonEvent evt, boolean isToggled){
         ((Window)screen.getElementById("authorization_popup")).show();
    }
    
    public void sendData(MouseButtonEvent evt, boolean isToggled){
        String login = screen.getElementById("login_text_field").getText(),
                password = screen.getElementById("password").getText();
        Authorization.createDatabase();
        if(Authorization.checkIfUserExists(login, password)){
            
        }
    }
    
//    @Override
//    public void initialize(AppStateManager stateManager, Application app) {
//        super.initialize(stateManager, app);
//
//        // Useful place for running load effects
//        inventory.showWithEffect();
//    }
}
