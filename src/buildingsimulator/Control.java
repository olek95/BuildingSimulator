package buildingsimulator;

import com.jme3.input.InputManager;
import com.jme3.input.KeyInput;
import com.jme3.input.awt.AwtKeyInput;
import com.jme3.input.controls.InputListener;
import com.jme3.input.controls.KeyTrigger;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import texts.Translator;

/**
 * Klasa <code>Control</code> reprezentuje sterowanie elementami gry. Zezwala
 * na przydzielanie lub odbieranie słuchaczy klawiszy do danego obiektu.
 * Klasa zawiera typ wyliczeniowy zawierający wszystkie możliwe akcje. 
 * @author AleksanderSklorz
 */
public class Control {
    private static InputManager inputManager = BuildingSimulator.getBuildingSimulator()
            .getInputManager();
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
        PHYSICS,
        FIRST,
        SECOND,
        PAUSE,
        SHOW_CURSOR;
        private String key;
        private Actions(){
            key = FilesManager.getValue(toString(), "settings/control.properties");
            inputManager.addMapping(toString(), new KeyTrigger(getJmeKeyCode(key)));
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
        public String getKey() {
            return key; 
        }
        
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
                for(int i = 0; i < values.length; i++){
                    values[i].key = keys[i];
                    String actionName = values[i].toString();
                    output.println(actionName + "=" + keys[i]);
                    inputManager.deleteMapping(actionName);
                    inputManager.addMapping(actionName, new KeyTrigger(getJmeKeyCode(keys[i])));
                }
            }catch(IOException ex){
                ex.printStackTrace();
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
            }
            return AwtKeyInput.convertAwtKey(keyName.charAt(0)); 
        }
    }
    
    /**
     * Dodaje nowego słuchacza klawiszy. 
     * @param o słuchacz 
     */
    public static void addListener(InputListener o){
        if(o instanceof Controllable){
            Actions[] names = ((Controllable)o).getAvailableActions();
            for(int i = 0; i < names.length; i++)
                inputManager.addListener(o, names[i].toString());
            actualListener = o;
        }else{
            inputManager.addListener(o, Actions.PHYSICS.toString());
            inputManager.addListener(o, Actions.FIRST.toString());
            inputManager.addListener(o, Actions.SECOND.toString());
            inputManager.addListener(o, Actions.PAUSE.toString());
            inputManager.addListener(o, Actions.SHOW_CURSOR.toString());
        }
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
    public static InputListener getActualListener(){
        return actualListener; 
    }
}
