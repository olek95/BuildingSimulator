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
        switch(type) {
            case STARTING_MENU: 
                menu = new StartingMenu(); 
                break;
            case OPTIONS: 
                menu = new Options();
                break;
            case CONTROL_CONFIGURATION: 
                menu = new ControlConfigurationMenu(); 
                break;
            case PAUSE_MENU:
                menu = new PauseMenu(); 
                break;
        }
        menu.getWindow().show();
    }
}
