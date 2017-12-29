package menu;

import buildingsimulator.BuildingSimulator;
import buildingsimulator.GameManager;
import com.jme3.audio.AudioNode;
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
        Window window = (Window)screen.getElementById("main_menu");
        window.getDragBar().removeFromParent();
        setWindow(window); 
        Translator.setTexts(new String[]{"statistics_button", 
            "options_button", "exit_button"},
            new Translator[]{Translator.STATISTICS, 
                Translator.SETTINGS, Translator.QUIT_GAME}, screen);
        screen.setUseCustomCursors(true);
        BuildingSimulator.getBuildingSimulator().getGuiNode().addControl(screen);
        startBackgroundSound();
    }
    
    public static Screen getScreen() { return screen; }
    
    /** 
     * Uruchamia grę 
     */
    protected void start() {
        AudioNode backgroundSound = getBackgroundSound();
        GameManager.stopSound(backgroundSound, true);
        setBackgroundSound(null);
        getWindow().hide();
        screen.setUseCustomCursors(false);
    }
    
    /**
     * Wychodzi z gry. 
     */
    protected void exit(){
        System.exit(0);
    }
    
    private void startBackgroundSound() {
        AudioNode backgroundSound = getBackgroundSound();
        if(backgroundSound == null) {
            backgroundSound = GameManager.createSound("Sounds/constructions.wav",
                    GameManager.getGameSoundVolume(), true, null);
            setBackgroundSound(backgroundSound);
        }
        backgroundSound.play();
    }
}
