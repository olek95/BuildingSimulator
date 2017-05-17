package buildingsimulator;

import com.jme3.input.InputManager;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.InputListener;
import com.jme3.input.controls.KeyTrigger;

public class Control {
    public static void setupKeys(Object o){
        InputManager inputManager = BuildingSimulator.getBuildingSimulator().getInputManager();
        if(!inputManager.hasMapping("Left")){
            inputManager.addMapping("Left", new KeyTrigger(KeyInput.KEY_H));
            inputManager.addMapping("Right", new KeyTrigger(KeyInput.KEY_K));
            inputManager.addMapping("Up", new KeyTrigger(KeyInput.KEY_U));
            inputManager.addMapping("Down", new KeyTrigger(KeyInput.KEY_J));
            inputManager.addMapping("Action", new KeyTrigger(KeyInput.KEY_F));
            inputManager.addMapping("Pull out", new KeyTrigger(KeyInput.KEY_E));
            inputManager.addMapping("Pull in", new KeyTrigger(KeyInput.KEY_SPACE));
            inputManager.addMapping("Lower hook", new KeyTrigger(KeyInput.KEY_R));
            inputManager.addMapping("Heighten hook", new KeyTrigger(KeyInput.KEY_T));
            inputManager.addMapping("Physics", new KeyTrigger(KeyInput.KEY_P));
            inputManager.addMapping("First", new KeyTrigger(KeyInput.KEY_1));
            inputManager.addMapping("Second", new KeyTrigger(KeyInput.KEY_2));
        }
        if(o instanceof PlayableObject){
            InputListener listener = (InputListener)o;
            inputManager.addListener(listener, ((PlayableObject)o).getAvailableActions());
        }else{
            inputManager.addListener((BuildingSimulator)o, "Action");
            inputManager.addListener((BuildingSimulator)o, "Physics");
            inputManager.addListener((BuildingSimulator)o, "First");
            inputManager.addListener((BuildingSimulator)o, "Second");
        }
    }
}
