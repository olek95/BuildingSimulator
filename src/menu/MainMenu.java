package menu;

import buildingsimulator.BuildingSimulator;
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
    private static AudioNode sound;
    public MainMenu(String layoutName){
        screen = new Screen(BuildingSimulator.getBuildingSimulator());
        screen.parseLayout(layoutName, this);
        Window window = (Window)screen.getElementById("main_menu");
        window.getDragBar().removeFromParent();
        setWindow(window); 
        Translator.setTexts(new String[]{"load_game_button", "statistics_button",
            "options_button", "exit_button"},
            new Translator[]{Translator.LOAD_GAME, Translator.STATISTICS,
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
        sound.stop();
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
        if(sound == null) {
            BuildingSimulator game = BuildingSimulator.getBuildingSimulator();
            sound = new AudioNode(game.getAssetManager(), "Sounds/constructions.wav",
                    false, true);
            sound.setVolume(0.4f);
            sound.setLooping(true);
            game.getRootNode().attachChild(sound);
        }
        sound.play();
    }
}
