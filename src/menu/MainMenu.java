package menu;

import authorization.DBManager;
import authorization.User;
import buildingsimulator.BuildingSimulator;
import buildingsimulator.GameManager;
import buildingsimulator.SavedData;
import com.jme3.audio.AudioNode;
import com.jme3.export.binary.BinaryImporter;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import texts.Translator;
import tonegod.gui.controls.buttons.Button;
import tonegod.gui.controls.windows.Window;
import tonegod.gui.core.Screen;

/**
 * Klasa <code>MainMenu</code> reprezentuje menu główne gry. Stanowi nadklasę dla 
 * menu startowego i menu podczas pauzy. Posiada możliwość uruchomienia nowej gry,
 * wczytania zapisanej gry, pokazania statystyk, zmiany ustawień gry, wyjścia z gry. 
 * @author AleksanderSklorz 
 */
public abstract class MainMenu extends Menu {
    public MainMenu(String layoutName){
        BuildingSimulator game = BuildingSimulator.getBuildingSimulator();
        Screen screen = new Screen(game);
        setScreen(screen);
        parseLayout(layoutName);
        Window window = (Window)screen.getElementById("main_menu");
        window.getDragBar().removeFromParent();
        setWindow(window); 
        Translator.setTexts(new String[]{"load_game_button", "statistics_button",
            "options_button", "exit_button"},
            new Translator[]{Translator.LOAD_GAME, Translator.STATISTICS,
                Translator.SETTINGS, Translator.QUIT_GAME}, screen);
        screen.setUseCustomCursors(true);
        game.getGuiNode().addControl(screen);
        startBackgroundSound();
        User user = GameManager.getUser(); 
        if(user != null) {
            String login = user.getLogin();
            if(!login.equals(User.DEFAULT_LOGIN) && GameManager.checkIfFileExists("game saves/" + login + "/save.j3o")) {
                setLoadingButtonState(false);
            } else {
                setLoadingButtonState(true);
            }
        } else {
            setLoadingButtonState(true);
        }
    }
    
    /** 
     * Uruchamia grę 
     */
    public static void start() {
        AudioNode backgroundSound = getBackgroundSound();
        GameManager.stopSound(backgroundSound, true);
        setBackgroundSound(null);
        getWindow().hide();
        getScreen().setUseCustomCursors(false);
    }
    
    /**
     * Wychodzi z gry. 
     */
    protected void exit(){ System.exit(0); }
    
    /**
     * Wczytuje zapisaną grę. 
     */
    protected void load() {
        BinaryImporter importer = BinaryImporter.getInstance();
        importer.setAssetManager(BuildingSimulator.getGameAssetManager());
        User user = GameManager.getUser();
        GameManager.setUser(DBManager.getUser(user.getLogin(), user.getPassword()));
        File file = new File("./game saves/" + user.getLogin() + "/save.j3o");
        try {
            SavedData data = (SavedData)importer.load(file);
            GameManager.runGame(data);
        } catch (IOException ex) {
            Logger.getLogger(StartingMenu.class.getName()).log(Level.SEVERE, null, ex);
        }
        start();
    }
    
    /**
     * Ustawia (blokuje bądź odblokowuje) stan przycisku ładowania zapisanej gry. 
     * @param state false jeśli przycisk ma być odblokowany, true w przeciwnym przypadku 
     */
    protected final void setLoadingButtonState(boolean state) {
        ((Button)MainMenu.getScreen().getElementById("load_game_button")).setIgnoreMouse(state);
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
