package settings;

import buildingsimulator.BuildingSimulator;
import buildingsimulator.Controllable;
import com.jme3.input.InputManager;
import com.jme3.input.KeyInput;
import com.jme3.input.MouseInput;
import com.jme3.input.awt.AwtKeyInput;
import com.jme3.input.controls.InputListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.MouseButtonTrigger;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import texts.Translator;

/**
 * Klasa <code>Control</code> reprezentuje sterowanie elementami gry. Zezwala
 * na przydzielanie lub odbieranie słuchaczy klawiszy do danego obiektu.
 * Klasa zawiera typ wyliczeniowy zawierający wszystkie możliwe akcje. 
 * @author AleksanderSklorz
 */
public class Control {
    private static InputManager inputManager = BuildingSimulator.getGameInputManager();
    private static InputListener actualListener;
    /**
     * Typ wyliczeniowy zawierający wszystkie możliwe akcje, które może wykonać 
     * użytkownik w grze. Do każdej możliwej akcji przypisany jest jakiś klawisz. 
     */
    public enum Actions{
        LEFT, 
        RIGHT, 
        UP,
        DOWN,
        ACTION,
        PULL_OUT,
        PULL_IN,
        LOWER_HOOK,
        HEIGHTEN_HOOK,
        ATTACH,
        VERTICAL_ATTACH,
        DETACH,
        MERGE,
        MERGE_PROTRUDING,
        PHYSICS,
        CRANE_CHANGING,
        PAUSE,
        SHOW_CURSOR,
        MOVE_CRANE,
        CHANGE_CAMERA,
        COPY_BUILDING,
        BUY_BUILDING,
        FLYCAM_Forward,
        FLYCAM_Backward,
        FLYCAM_StrafeLeft,
        FLYCAM_StrafeRight,
        FLYCAM_Rise, 
        FLYCAM_Lower,
        CHANGING_CONTROLS_HUD_VISIBILITY,
        SHOW_SHOP(KeyInput.KEY_F2, false),
        SHOW_CLEANING_DIALOG_WINDOW(KeyInput.KEY_F1, false),
        SELL_BUILDINGS(KeyInput.KEY_F3, false),
        SELECT_WAREHOUSE(MouseInput.BUTTON_LEFT, true),
        CANCEL_BIRDS_EYE_VIEW(MouseInput.BUTTON_RIGHT, true);
        private String key;
        private static Properties keysProperties;
        private Actions(){
            createProperties();
            key = getKeyFromPropertiesFile();
            String mappingName = toString(); 
            inputManager.addMapping(mappingName, new KeyTrigger(getJmeKeyCode(key)));
        }
        
        private Actions(int keyCode, boolean mouse) {
            if(mouse) inputManager.addMapping(toString(), new MouseButtonTrigger(keyCode));
            else {
                inputManager.addMapping(toString(), new KeyTrigger(keyCode));
            }
        }
        
        /**
         * Zwraca odpowiednią dla użytkownika nazwę akcji, w aktualnym języku. 
         * @return odpowiednia dla użytkownika nazwa akcji  
         */
        public String getValue(){
            return Translator.valueOf(toString()).getValue();
        }
        
        /**
         * Zwraca przycisk przypisany do danej czynności. 
         * @return przycisk dla danej czynności 
         */
        public String getKey() { return key; }
        
        /**
         * Zamienia kod przycisku na nazwę przycisku. W AWT znaki są kodowane według ASCII.
         * Przyciski nie występujące w ASCII są zastępowane kodem ASCII przycisku 
         * który nie może być wybrany (jest w górnej części przycisku - przyciski z dwoma znakami).
         * Przykładem są strzałki - lewa to %, prawa to ', górna to &, dolna to (
         * @param keyCode kod przycisku 
         * @return nazwa przycisku 
         */
        public static String getKey(int keyCode){
            char convertedChar = (char)AwtKeyInput.convertJmeCode(keyCode);
            /* W AWT znaki są kodowane według ASCII. Przyciski nie występujące w ASCII
             * są zastępowane kodem ASCII przycisku który nie może być wybrany (jest w 
             * górnej części przycisku - przyciski z dwoma znakami). Przykładem są strzałki. 
             */ 
            if(convertedChar == 27) return "ESC";
            if(convertedChar == 32) return "SPACE";
            if(convertedChar == 37) return "LEFT";
            if(convertedChar == 39) return "RIGHT";
            if(convertedChar == 38) return "UP";
            if(convertedChar == 40) return "DOWN";
            if(convertedChar == 16) {
                if(keyCode == 42) return "LSHIFT";
                if(keyCode == 54) return "RSHIFT";
            }
            return convertedChar + "";
        }
        
        /**
         * Zapisuje ustawienia sterowania do pliku. Dodatkowo ustawia przycisk dla 
         * akcji. 
         * @param keys nowe przyciski dla czynności 
         */
        public static void saveSettings(String[] keys){
            try(PrintWriter output = new PrintWriter(new FileWriter("settings/control.properties"))){
                Actions[] values = values();
                for(int i = 0; i < values.length - 5; i++){
                    values[i].key = keys[i];
                    String actionName = values[i].toString();
                    keysProperties.setProperty(actionName, keys[i]);
                    inputManager.deleteMapping(actionName);
                    inputManager.addMapping(actionName, new KeyTrigger(getJmeKeyCode(keys[i])));
                }
                keysProperties.store(output, null);
            } catch (IOException ex) {
                Logger.getLogger(Control.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        private static int getJmeKeyCode(String keyName) {
            if(keyName.length() > 1){
                if(keyName.equals("ESC")) return KeyInput.KEY_ESCAPE;
                if(keyName.equals("SPACE")) return KeyInput.KEY_SPACE;
                if(keyName.equals("LEFT")) return KeyInput.KEY_LEFT;
                if(keyName.equals("RIGHT")) return KeyInput.KEY_RIGHT;
                if(keyName.equals("UP")) return KeyInput.KEY_UP;
                if(keyName.equals("DOWN")) return KeyInput.KEY_DOWN;
                if(keyName.equals("LSHIFT")) return KeyInput.KEY_LSHIFT;
                if(keyName.equals("RSHIFT")) return KeyInput.KEY_RSHIFT;
            }
            return AwtKeyInput.convertAwtKey(keyName.charAt(0)); 
        }
        
        private void createProperties() {
            if(keysProperties == null) {
                try(FileReader file = new FileReader("settings/control.properties")) {
                    keysProperties = new Properties();
                    keysProperties.load(new BufferedReader(file));
                } catch (IOException ex) {
                    keysProperties = CreatorMockSettings
                            .createDefaultProperties(new String[]{"HEIGHTEN_HOOK",
                                "MOVE_CRANE", "MERGE_PROTRUDING", "UP",
                                "VERTICAL_ATTACH", "RIGHT", "PULL_OUT", "LEFT", 
                                "PULL_IN", "SHOW_CURSOR", "PHYSICS", "ACTION", 
                                "DOWN", "PAUSE", "MERGE", "ATTACH", "LOWER_HOOK",
                                "CRANE_CHANGING", "DETACH", "CHANGE_CAMERA", "COPY_BUILDING", 
                                "BUY_BUILDING", "FLYCAM_Forward", "FLYCAM_Backward",
                                "FLYCAM_StrafeLeft", "FLYCAM_StrafeRight", "FLYCAM_Rise",
                                "FLYCAM_Lower", "CHANGING_CONTROLS_HUD_VISIBILITY"},
                            new String[]{"T", "O", "L", "U", "V", "K", "E", "H",
                                "SPACE", "LSHIFT", "P", "F", "J", "ESC", "N", "Y",
                                "R", "1", "B", "C", "5", "6", "W", "S", "A", "D",
                                "Q", "Z", "7"}, "settings/control.properties"); 
                }
            }
        }
        
        private String getKeyFromPropertiesFile() {
            return keysProperties.getProperty(toString());
        }
    }
    
    /**
     * Dodaje nowego słuchacza klawiszy. 
     * @param listener słuchacz 
     * @param setAsActualListener true jeśli ten słuchacz ma być zapamiętany jako 
     * aktualny, false jeśli ma zostać tylko ustawiony bez zapamiętywania
     */
    public static void addListener(InputListener listener, boolean setAsActualListener){
        Actions[] names = ((Controllable)listener).getAvailableActions();
        for(int i = 0; i < names.length; i++) {
            if(!inputManager.hasMapping(names[i].toString())) {
                inputManager.addMapping(names[i].toString(), 
                        new KeyTrigger(Actions.getJmeKeyCode(names[i].key)));
            }
            inputManager.addListener(listener, names[i].toString());

        }
        if(setAsActualListener) actualListener = listener;
    }
    
    /**
     * Usuwa danego słuchacza. 
     * @param listener słuchacz 
     */
    public static void removeListener(InputListener listener){
        inputManager.removeListener(listener);
    }
    
    /**
     * Zwraca słuchacza dla aktualnie używanej jednostki. 
     * @return aktualny słuchacz 
     */
    public static InputListener getActualListener(){ return actualListener; }
}
