package buildingsimulator;

import com.jme3.input.InputManager;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.InputListener;
import com.jme3.input.controls.KeyTrigger;

public class Control {
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
        PHYSICS,
        FIRST,
        SECOND;
    }
    public static void setupKeys(Object o){
        InputManager inputManager = BuildingSimulator.getBuildingSimulator().getInputManager();
        if(!inputManager.hasMapping(Actions.LEFT.toString())){
            inputManager.addMapping(Actions.LEFT.toString(), new KeyTrigger(KeyInput.KEY_H));
            inputManager.addMapping(Actions.RIGHT.toString(), new KeyTrigger(KeyInput.KEY_K));
            inputManager.addMapping(Actions.UP.toString(), new KeyTrigger(KeyInput.KEY_U));
            inputManager.addMapping(Actions.DOWN.toString(), new KeyTrigger(KeyInput.KEY_J));
            inputManager.addMapping(Actions.ACTION.toString(), new KeyTrigger(KeyInput.KEY_F));
            inputManager.addMapping(Actions.PULL_OUT.toString(), new KeyTrigger(KeyInput.KEY_E));
            inputManager.addMapping(Actions.PULL_IN.toString(), new KeyTrigger(KeyInput.KEY_SPACE));
            inputManager.addMapping(Actions.LOWER_HOOK.toString(), new KeyTrigger(KeyInput.KEY_R));
            inputManager.addMapping(Actions.HEIGHTEN_HOOK.toString(), new KeyTrigger(KeyInput.KEY_T));
            inputManager.addMapping(Actions.PHYSICS.toString(), new KeyTrigger(KeyInput.KEY_P));
            inputManager.addMapping(Actions.FIRST.toString(), new KeyTrigger(KeyInput.KEY_1));
            inputManager.addMapping(Actions.SECOND.toString(), new KeyTrigger(KeyInput.KEY_2));
        }
        if(o instanceof Controllable){
            InputListener listener = (InputListener)o;
            Actions[] names = ((Controllable)o).getAvailableActions();
            for(int i = 0; i < names.length; i++)
                inputManager.addListener(listener, names[i].toString());
        }else{
            //inputManager.addListener((BuildingSimulator)o, Actions.ACTION.toString());
            inputManager.addListener((BuildingSimulator)o, Actions.PHYSICS.toString());
            inputManager.addListener((BuildingSimulator)o, Actions.FIRST.toString());
            inputManager.addListener((BuildingSimulator)o, Actions.SECOND.toString());
        }
    }
    public static void removeListener(InputListener listener){
        InputManager inputManager = BuildingSimulator.getBuildingSimulator().getInputManager();
        inputManager.removeListener(listener);
    }
}
