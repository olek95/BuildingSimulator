package menu;

import authorization.Authorization;
import buildingsimulator.BuildingSimulator;
import buildingsimulator.GameManager;
import authorization.User;
import com.jme3.input.event.MouseButtonEvent;
import com.jme3.math.ColorRGBA;
import java.sql.SQLException;
import texts.Translator;
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
public class MainMenu extends Menu {
    private static Screen screen;
    public MainMenu(String layoutName){
        screen = new Screen(BuildingSimulator.getBuildingSimulator());
        screen.parseLayout(layoutName, this);
        window = (Window)screen.getElementById("main_menu");
        window.getDragBar().setIsMovable(false);
        Translator.setTexts(new String[]{"load_game_button", "statistics_button",
            "options_button", "exit_button"},
            new Translator[]{Translator.LOAD_GAME, Translator.STATISTICS,
            Translator.SETTINGS, Translator.QUIT_GAME}, screen);
        screen.setUseCustomCursors(true);
        BuildingSimulator.getBuildingSimulator().getGuiNode().addControl(screen);
    }
    
    /** 
     * Uruchamia grę 
     * @param evt 
     * @param isToggled 
     */
    public void start(MouseButtonEvent evt, boolean isToggled) {
        window.hide();
        screen.setUseCustomCursors(false);
    }
    
    public void showOptions(MouseButtonEvent evt, boolean isToggled){
        window.hide();
        BuildingSimulator.getBuildingSimulator().getGuiNode().removeControl(screen);
        MenuFactory.showMenu(MenuTypes.OPTIONS);
    }
    
    /**
     * Wychodzi z gry. 
     * @param evt
     * @param isToggled 
     */
    public void exit(MouseButtonEvent evt, boolean isToggled){
        System.exit(0);
    }
    
    public static Screen getScreen() { return screen; }
    
//    @Override
//    public void initialize(AppStateManager stateManager, Application app) {
//        super.initialize(stateManager, app);
//
//        // Useful place for running load effects
//        inventory.showWithEffect();
//    }

    @Override
    public void closeWindow() {}
}
