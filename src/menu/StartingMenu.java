package menu;

import authorization.Authorization;
import authorization.User;
import buildingsimulator.GameManager;
import com.jme3.input.event.MouseButtonEvent;
import com.jme3.math.ColorRGBA;
import java.sql.SQLException;
import texts.Translator;
import tonegod.gui.controls.buttons.CheckBox;
import tonegod.gui.controls.text.Label;
import tonegod.gui.controls.windows.Window;
import tonegod.gui.core.Screen;

public class StartingMenu extends MainMenu{
    public StartingMenu(){
        super("Interface/main_menu.gui.xml");
        Translator.setTexts(new String[]{"start_game_button",  "authorization_button",
            "cancel_button", "username_label", "password_label", "sending_data_button"},
                new Translator[]{Translator.NEW_GAME, Translator.AUTHORIZATION,
                    Translator.CANCELLATION,Translator.USERNAME, Translator.PASSWORD,
                    Translator.LOGIN}, MainMenu.getScreen());
        ((Window)MainMenu.getScreen().getElementById("authorization_popup")).getDragBar()
                .setIsMovable(false);
        changeAuthorizationPopupState(false);
    }
    
    @Override
    public void start(MouseButtonEvent evt, boolean isToggled) {
        super.start(evt, isToggled); 
        GameManager.runGame();
    }
    
    @Override
    public void showOptions(MouseButtonEvent evt, boolean isToggled){ 
        super.showOptions(evt, isToggled);
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
            Authorization.createDatabase();
            if(((CheckBox)screen.getElementById("registration_check_box")).getIsChecked()){
                String errorInformation = getTextForDataState(login, password); 
                if(errorInformation != null){
                    error.setText(errorInformation);
                }else{
                    if(!Authorization.checkIfUserExists(login)){
                        Authorization.signUp(login, password); 
                        error.setFontColor(ColorRGBA.Green);
                        error.setText(Translator.REGISTRATION_SUCCESSFUL.getValue());
                    }else{
                        error.setText(Translator.DUPLICATED_USER.getValue());
                    }
                }
            }else{
                if(!Authorization.signIn(login, password)){
                    error.setText(Translator.INCORRECT_DATA.getValue());
                }else{
                    error.setText("");
                    changeAuthorizationPopupState(false); 
                    GameManager.setUser(new User(login));
                    screen.getElementById("authorization_button")
                            .setText(Translator.LOGOUT.getValue());
                    screen.getElementById("login_label").setText(login);
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
    public void exit(MouseButtonEvent evt, boolean isToggled){
        System.exit(0);
    }
    
    private void changeAuthorizationPopupState(boolean visible){
        Screen screen = MainMenu.getScreen();
        if(visible){
            ((Window)screen.getElementById("authorization_popup")).showAsModal(true);
        }else{
            screen.getElementById("login_text_field").setText("");
            screen.getElementById("password").setText("");
            ((Window)screen.getElementById("authorization_popup")).hide();
        }
    }
    
    private String getTextForDataState(String login, String password){
        if(login.equals("") && password.equals("")) return Translator.EMPTY_LOGIN_PASSWORD.getValue();
        if(login.equals("")) return Translator.EMPTY_LOGIN.getValue();
        if(password.equals("")) return Translator.EMPTY_PASSWORD.getValue();
        if(login.length() > 20 || password.length() > 20) 
            return Translator.TOO_LONG_LOGIN_PASSWORD.getValue();
        return null; 
    }
}
