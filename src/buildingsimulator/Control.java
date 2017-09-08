package buildingsimulator;

import com.jme3.input.InputManager;
import com.jme3.input.KeyInput;
import com.jme3.input.awt.AwtKeyInput;
import com.jme3.input.controls.InputListener;
import com.jme3.input.controls.KeyTrigger;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Properties;
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
        PAUSE;
        private String key;
        private Actions(){
            Properties control = new Properties();
            try(InputStream input = new FileInputStream("src/settings/control.properties")){
                control.load(input);
//                String value = control.getProperty(toString());
                key = control.getProperty(toString()); 
                //key = value.length() > 1 ? getValueForNotCharKey(value) : value.charAt(0);
                inputManager.addMapping(toString(), new KeyTrigger(getJmeKeyCode(key)));
            }catch(IOException ex){
                ex.printStackTrace();
            }
        }
        
        public static int getJmeKeyCode(String keyName) {
            if(keyName.equals("ESC")) return KeyInput.KEY_ESCAPE;
            return AwtKeyInput.convertAwtKey(keyName.charAt(0)); 
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
        
        public static String getKey(int keyCode){
            char convertedChar = (char)AwtKeyInput.convertJmeCode(keyCode);
            if(convertedChar == 27) return "ESC";
            return convertedChar + "";
        }
        
        /**
         * Zapisuje ustawienia sterowania do pliku. 
         * @param keys nowe przyciski dla czynności 
         */
        public static void saveSettings(String[] keys){
            Properties control = new Properties();
            try(OutputStream output = new FileOutputStream("src/settings/control.properties")){
                Actions[] values = values(); 
                for(int i = 0; i < values.length; i++){
                    System.out.println(keys[i]);
                    values[i].key = keys[i];
                    control.setProperty(values[i].toString(), keys[i]);
                    System.out.println(values[i] + " " + keys[i]);
                    inputManager.addMapping(values[i].toString(),
                            new KeyTrigger(getJmeKeyCode(keys[i])));
                }
                control.store(output, null);
            }catch(IOException ex){
                ex.printStackTrace();
            }
        }
        
        private int getValueForNotCharKey(String key) {
            if(key.equals("ESC")) return KeyEvent.VK_ESCAPE;
            return 0; 
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
