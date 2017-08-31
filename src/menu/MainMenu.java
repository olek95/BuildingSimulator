package menu;

import authorization.Authorization;
import buildingsimulator.BuildingSimulator;
import buildingsimulator.GameManager;
import authorization.User;
import com.jme3.app.state.AbstractAppState;
import com.jme3.input.event.MouseButtonEvent;
import com.jme3.math.ColorRGBA;
import com.jme3.scene.Node;
import java.sql.SQLException;
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
    private static Screen screen;
    private MainMenu(){
        screen = new Screen(BuildingSimulator.getBuildingSimulator());
        screen.parseLayout("Interface/main_menu.gui.xml", this);
        mainMenu = (Window)screen.getElementById("main_menu");
        mainMenu.getDragBar().setIsMovable(false);
        ((Window)screen.getElementById("authorization_popup")).getDragBar()
                .setIsMovable(false);
        changeAuthorizationPopupState(false); 
        screen.setUseCustomCursors(true);
    }
    
    /**
     * Pokazuje menu główne gry. 
     */
    public static void showMenu(){
        Node guiNode = BuildingSimulator.getBuildingSimulator().getGuiNode();
        guiNode.removeControl(screen);
        new MainMenu();
        guiNode.addControl(screen);
        mainMenu.show();
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
        Label error = (Label)screen.getElementById("error_label");
        error.setFontColor(ColorRGBA.Red);
        try{
            Authorization.createDatabase();
            if(registration){
                String errorInformation = getTextForDataState(login, password); 
                if(errorInformation != null){
                    error.setText(errorInformation);
                }else{
                    if(!Authorization.checkIfUserExists(login)){
                        Authorization.signUp(login, password); 
                        error.setFontColor(ColorRGBA.Green);
                        error.setText("Rejestracja zakonczona powodzeniem");
                    }else{
                        error.setText("Uzytkownik juz istnieje!");
                    }
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
        }catch(ClassNotFoundException | SQLException ex){
            error.setText("Problem z polaczeniem z baza danych");
            ex.printStackTrace();
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
    
    /**
     * Zmienia etykietę przycisku logowania/rejestracji. 
     * @param evt
     * @param isToggled true jeśli etykieita rejestracji, false w przeciwnym przypadku 
     */
    public void changeDataSendingButtonLabel(MouseButtonEvent evt, boolean isToggled){
        screen.getElementById("sending_data_button").setText(isToggled 
                ? "Zarejestruj sie" : "Zaloguj sie");
    }
    
    /**
     * Wychodzi z gry. 
     * @param evt
     * @param isToggled 
     */
    public void exit(MouseButtonEvent evt, boolean isToggled){
        System.exit(0);
    }
    
    public void showOptions(MouseButtonEvent evt, boolean isToggled){
        mainMenu.hide();
        Options.showOptions();
    }
    
    private void changeAuthorizationPopupState(boolean visible){
        if(visible){
            ((Window)screen.getElementById("authorization_popup")).show();
        }else{
            screen.getElementById("login_text_field").setText("");
            screen.getElementById("password").setText("");
            ((Window)screen.getElementById("authorization_popup")).hide();
        }
    }
    
    private String getTextForDataState(String login, String password){
        if(login.equals("") && password.equals("")) return "Login i hasło nie mogą być puste";
        if(login.equals("")) return "Login nie moze byc pusty!";
        if(password.equals("")) return "Haslo nie moze byc puste!";
        if(login.length() > 20 || password.length() > 20) return "Za dlugi login lub haslo";
        return null; 
    }
    
//    @Override
//    public void initialize(AppStateManager stateManager, Application app) {
//        super.initialize(stateManager, app);
//
//        // Useful place for running load effects
//        inventory.showWithEffect();
//    }
}
