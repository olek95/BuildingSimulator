package buildingsimulator;

import com.jme3.input.InputManager;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.InputListener;
import com.jme3.input.controls.KeyTrigger;

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
        LEFT(KeyInput.KEY_H), 
        RIGHT(KeyInput.KEY_K), 
        UP(KeyInput.KEY_U),
        DOWN(KeyInput.KEY_J),
        ACTION(KeyInput.KEY_F),
        PULL_OUT(KeyInput.KEY_E),
        PULL_IN(KeyInput.KEY_SPACE),
        LOWER_HOOK(KeyInput.KEY_R),
        HEIGHTEN_HOOK(KeyInput.KEY_T),
        ATTACH(KeyInput.KEY_Y),
        VERTICAL_ATTACH(KeyInput.KEY_V),
        DETACH(KeyInput.KEY_B),
        PHYSICS(KeyInput.KEY_P),
        FIRST(KeyInput.KEY_1),
        SECOND(KeyInput.KEY_2);
        private Actions(int key){ 
            inputManager.addMapping(toString(), new KeyTrigger(key));
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
    
    public static InputListener getActualListener(){
        return actualListener; 
    }
}
