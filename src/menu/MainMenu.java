package menu;

import buildingsimulator.BuildingSimulator;
import com.jme3.input.event.MouseButtonEvent;
import texts.Translator;
import tonegod.gui.controls.windows.Window;
import tonegod.gui.core.Screen;

/**
 * Klasa <code>MainMenu</code> reprezentuje menu główne gry. Stanowi nadklasę dla 
 * menu startowego i menu podczas pauzy. Posiada możliwość uruchomienia nowej gry,
 * wczytania zapisanej gry, pokazania statystyk, zmiany ustawień gry, wyjscia z gry. 
 * @author AleksanderSklorz 
 */
public abstract class MainMenu extends Menu {
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
     */
    protected void start() {
        window.hide();
        screen.setUseCustomCursors(false);
    }
    
    /**
     * Wyświetla menu opcji gry. 
     */
    protected void showOptions(){
        window.hide();
        BuildingSimulator.getBuildingSimulator().getGuiNode().removeControl(screen);
        MenuFactory.showMenu(MenuTypes.OPTIONS);
    }
    
    /**
     * Wychodzi z gry. 
     */
    protected void exit(){
        System.exit(0);
    }
    
    public static Screen getScreen() { return screen; }
}
