package menu;


public class MenuFactory {
    public static void showMenu(MenuTypes type) {
        Menu menu = null;
        if(type.equals(MenuTypes.MAIN_MENU)) menu = new MainMenu(); 
        else if(type.equals(MenuTypes.OPTIONS)) menu = new Options();
        else if(type.equals(MenuTypes.CONTROL_CONFIGURATION)) menu = new ControlConfigurationMenu();
        menu.getWindow().show();
    }
}
