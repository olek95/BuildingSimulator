package menu;

import authorization.Authorization;
import buildingsimulator.BuildingSimulator;
import buildingsimulator.GameManager;
import authorization.User;
import com.jme3.app.state.AbstractAppState;
import com.jme3.input.event.MouseButtonEvent;
import com.jme3.math.ColorRGBA;
import tonegod.gui.controls.buttons.CheckBox;
import tonegod.gui.controls.text.Label;
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
        changeAuthorizationPopupState(false); 
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
    
    /**
     * Pozwala na autoryzację lub wylogowanie. 
     * @param evt
     * @param isToggled 
     */
    public void authorize(MouseButtonEvent evt, boolean isToggled){
        if(GameManager.getUser() == null)
            changeAuthorizationPopupState(true); 
        else{
            GameManager.setUser(null);
            screen.getElementById("authorization_button").setText("Autoryzacja");
            screen.getElementById("login_label").setText("");
        }
    }
    
    /**
     * Przesyła dane do bazy danych. Pozwala na zalogowanie się lub na rejestrację. 
     * @param evt
     * @param isToggled 
     */
    public void sendData(MouseButtonEvent evt, boolean isToggled){
        String login = screen.getElementById("login_text_field").getText(),
                password = screen.getElementById("password").getText();
        boolean registration = ((CheckBox)screen.getElementById("registration_check_box"))
                .getIsChecked();
        Authorization.createDatabase();
        Label error = (Label)screen.getElementById("error_label");
        error.setFontColor(ColorRGBA.Red);
        if(registration){
            if(!Authorization.checkIfUserExists(login)){
                Authorization.signUp(login, password); 
                error.setText("");
            }else{
                error.setText("Uzytkownik juz istnieje!");
            }
        }else{
            if(!Authorization.signIn(login, password)){
                error.setText("Zly login lub haslo!");
            }else{
                error.setText("");
                changeAuthorizationPopupState(false); 
                GameManager.setUser(new User(login));
                screen.getElementById("authorization_button").setText("Wyloguj");
                screen.getElementById("login_label").setText(login);
            }
        }
    }
    
    /**
     * Zamyka okienko autoryzacji. 
     * @param evt
     * @param isToggled 
     */
    public void cancel(MouseButtonEvent evt, boolean isToggled){
         changeAuthorizationPopupState(false); 
    }
    
    private void changeAuthorizationPopupState(boolean visible){
        if(visible)  ((Window)screen.getElementById("authorization_popup")).show();
        else{
            screen.getElementById("login_text_field").setText("");
            screen.getElementById("password").setText("");
            ((Window)screen.getElementById("authorization_popup")).hide();
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
