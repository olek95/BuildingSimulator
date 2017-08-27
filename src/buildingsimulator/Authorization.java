package buildingsimulator;

import com.jme3.app.state.AbstractAppState;
import tonegod.gui.controls.windows.Window;
import tonegod.gui.core.Screen;

/**
 * Klasa <code>Authorization</code> odpowiada za obsługę logowania i rejestracji
 * użytkownika. Wyświetla stosowne do tej czynności okienko. 
 * @author AleksanderSklorz 
 */
public class Authorization extends AbstractAppState{
    public static void showPopup(Screen mainMenuScreen){
        ((Window)mainMenuScreen.getElementById("authorization_popup")).show();
    }
}
