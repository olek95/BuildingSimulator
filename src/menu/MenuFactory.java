package menu;

import com.jme3.app.state.AbstractAppState;

public class MenuFactory {
    public static void showMenu(MenuTypes type) {
        Menu menu = null;
        if(type.equals(MenuTypes.MAIN_MENU)) menu = new MainMenu(); 
        else if(type.equals(MenuTypes.OPTIONS)) menu = new Options();
        menu.getWindow().show();
    }
}
