package menu;

import java.util.Properties;

/**
 * Klasa <code>MenuFactory</code> reprezentuje fabrykę służącą do wyświetlania 
 * wybranego menu. 
 * @author AleksanderSklorz 
 */
public class MenuFactory {
    /**
     * Pokazuje wybrane menu. 
     * @param type typ wybranego menu 
     */
    public static void showMenu(MenuTypes type) {
        Menu menu = null;
        if(type.equals(MenuTypes.MAIN_MENU)) menu = new MainMenu(); 
        else if(type.equals(MenuTypes.OPTIONS)) menu = new Options();
        else if(type.equals(MenuTypes.CONTROL_CONFIGURATION)) 
            menu = new ControlConfigurationMenu();
        menu.getWindow().show();
    }
}
