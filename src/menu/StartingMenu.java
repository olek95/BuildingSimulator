package menu;

import authorization.DBManager;
import authorization.User;
import buildingsimulator.GameManager;
import com.jme3.input.event.MouseButtonEvent;
import com.jme3.math.ColorRGBA;
import java.sql.SQLException;
import texts.Translator;
import tonegod.gui.controls.buttons.Button;
import tonegod.gui.controls.buttons.CheckBox;
import tonegod.gui.controls.text.Label;
import tonegod.gui.controls.windows.Window;
import tonegod.gui.core.Screen;

/**
 * Menu wyświetlane po uruchomieniu gry. Dodatkowo posiada możliwość logowania 
 * i rejestracji. 
 * @author AleksanderSklorz
 */
public class StartingMenu extends MainMenu{
    public StartingMenu(){
        super("Interface/starting_menu.gui.xml");
        Translator.setTexts(new String[]{"start_game_button",  "authorization_button",
            "cancel_button", "username_label", "password_label", "sending_data_button"},
                new Translator[]{Translator.NEW_GAME, Translator.AUTHORIZATION,
                    Translator.CANCELLATION,Translator.USERNAME, Translator.PASSWORD,
                    Translator.LOGIN}, MainMenu.getScreen());
        Window authorizationPopup = ((Window)MainMenu.getScreen().getElementById("authorization_popup"));
        authorizationPopup.getDragBar().setIsMovable(false);
        authorizationPopup.setWindowTitle(Translator.AUTHORIZATION.getValue());
        changeAuthorizationPopupState(false);
        User user = GameManager.getUser(); 
        if(user != null && !user.getLogin().equals(User.DEFAULT_LOGIN)) {
            setUser();
        } 
    }
    
    /**
     * Uruchamia grę. 
     * @param evt
     * @param isToggled 
     */
    public void start(MouseButtonEvent evt, boolean isToggled) {
        Screen screen = MainMenu.getScreen(); 
        User user = GameManager.getUser();
        if(user == null) {
            screen.addElement(createNotSavedChangesAlert(MainMenu.getScreen(),
                    Translator.NOT_LOGGED_IN_ALERT.getValue(), null));
        } else {
            super.start(); 
            user.setPoints(999);
            user.resetAllTime();
            GameManager.runGame(null);
        }
    }
    
    public void load(MouseButtonEvent evt, boolean isToggled) {
        load();
    }
    
    /**
     * Wyświetla opcje gry. 
     * @param evt
     * @param isToggled 
     */
    public void showOptions(MouseButtonEvent evt, boolean isToggled){ 
        goNextMenu(MainMenu.getScreen(), MenuTypes.OPTIONS);
    }
    
    /**
     * Wyświetla statystyki. 
     * @param evt
     * @param isToggled 
     */
    public void showStatistics(MouseButtonEvent evt, boolean isToggled) {
        goNextMenu(MainMenu.getScreen(), MenuTypes.STATISTICS); 
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
            Screen screen = MainMenu.getScreen();
            screen.getElementById("authorization_button").setText(Translator
                    .AUTHORIZATION.getValue());
            screen.getElementById("login_label").setText("");
            setLoadingButtonState(true);
        }
    }
    
    /**
     * Przesyła dane do bazy danych. Pozwala na zalogowanie się lub na rejestrację. 
     * @param evt
     * @param isToggled 
     */
    public void sendData(MouseButtonEvent evt, boolean isToggled){
        Screen screen = MainMenu.getScreen();
        String login = screen.getElementById("login_text_field").getText(),
                password = screen.getElementById("password").getText();
        Label error = (Label)screen.getElementById("error_label");
        error.setFontColor(ColorRGBA.Red);
        try{
            DBManager.createDatabase();
            if(((CheckBox)screen.getElementById("registration_check_box")).getIsChecked()){
                String errorInformation = getTextForDataState(login, password); 
                if(errorInformation != null){
                    error.setText(errorInformation);
                }else{
                    if(!DBManager.checkIfUserExists(login)){
                        DBManager.signUp(login, password); 
                        error.setFontColor(ColorRGBA.Green);
                        error.setText(Translator.REGISTRATION_SUCCESSFUL.getValue());
                    }else{
                        error.setText(Translator.DUPLICATED_USER.getValue());
                    }
                }
            }else{
                User user = DBManager.signIn(login, password);
                if(user == null){
                    error.setText(Translator.INCORRECT_DATA.getValue());
                }else{
                    error.setText("");
                    changeAuthorizationPopupState(false); 
                    GameManager.setUser(user);
                    setUser();
                    if(GameManager.checkIfFileExists("game saves/" + login + "/save.j3o")) 
                        setLoadingButtonState(false);
                }
            }
        }catch(ClassNotFoundException | SQLException ex){
            error.setText(Translator.DB_EXCEPTION.getValue());
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
        MainMenu.getScreen().getElementById("sending_data_button").setText(isToggled 
                ? Translator.REGISTERING.getValue() : Translator.LOGIN.getValue());
    }
    
    /**
     * Wychodzi z gry. 
     * @param evt
     * @param isToggled 
     */
    public void exit(MouseButtonEvent evt, boolean isToggled){ exit(); }
    
    @Override
    protected void doWhenAcceptedExit(Screen screen, MenuTypes type) {
        super.doWhenAcceptedExit(screen, null); 
        GameManager.setUser(new User(User.DEFAULT_LOGIN, 999, "00:00:00"));
        super.start(); 
        GameManager.runGame(null);
    }
    
    private void changeAuthorizationPopupState(boolean visible){
        Screen screen = MainMenu.getScreen();
        if(visible){
            ((Window)screen.getElementById("authorization_popup")).showAsModal(true);
        }else{
            screen.getElementById("login_text_field").setText("");
            screen.getElementById("password").setText("");
            screen.getElementById("error_label").setText("");
            ((Window)screen.getElementById("authorization_popup")).hide();
        }
    }
    
    private String getTextForDataState(String login, String password){
        if(login.equals("")) {
            if(password.equals("")) return Translator.EMPTY_LOGIN_PASSWORD.getValue();
            return Translator.EMPTY_LOGIN.getValue(); 
        }
        if(password.equals("")) return Translator.EMPTY_PASSWORD.getValue();
        if(login.length() > 20 || password.length() > 20) 
            return Translator.TOO_LONG_LOGIN_PASSWORD.getValue();
        if(login.equalsIgnoreCase(User.DEFAULT_LOGIN)) 
            return Translator.NOT_ALLOWED_USERNAME.getValue();
        return null; 
    }
    
    private void setUser() {
        Screen screen = MainMenu.getScreen(); 
        screen.getElementById("authorization_button").setText(Translator.LOGOUT.getValue());
        screen.getElementById("login_label").setText(GameManager.getUser().getLogin());
    }
}
