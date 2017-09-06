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
        SECOND;
        private char key;
        private Actions(){
            Properties control = new Properties();
            try(InputStream input = new FileInputStream("src/settings/control.properties")){
                control.load(input);
                key = control.getProperty(toString()).charAt(0);
                inputManager.addMapping(toString(), new KeyTrigger(AwtKeyInput
                        .convertAwtKey(key)));
            }catch(IOException ex){
                ex.printStackTrace();
            }
        }
        
        public String getValue(){
            return Translator.valueOf(toString()).getValue();
        }
        
        public char getKey() {
            return key; 
        }
        
        public static void saveSettings(String[] keys){
            Properties control = new Properties();
            try(OutputStream output = new FileOutputStream("src/settings/control.properties")){
                Actions[] values = values(); 
                for(int i = 0; i < values.length; i++){
                    char newKey = keys[i].charAt(0);
                    values[i].key = newKey;
                    control.setProperty(values[i].toString(), newKey + "");
                }
                control.store(output, null);
            }catch(IOException ex){
                ex.printStackTrace();
            }
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
