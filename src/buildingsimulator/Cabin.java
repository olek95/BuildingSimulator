package buildingsimulator;

import com.jme3.input.controls.AnalogListener;
import com.jme3.scene.Node;

public abstract class Cabin implements AnalogListener{
    protected Hook hook;
    private final float maxProtrusion = 9.5f, minProtrusion = 1f;
    protected final float maxArmHeight = 0.6f, minArmHeight = 0f;
    @Override
    public void onAnalog(String name, float value, float tpf) {
        switch(name){
            case "Right":
                rotate(-tpf / 5);
                break;
            case "Left":
                rotate(tpf / 5);
                break;
            case "Pull out":
                moveHandleHook(maxProtrusion, true);
                break;
            case "Pull in":
                moveHandleHook(maxProtrusion, false);
                break;
            case "Lower hook":
                hook.lower();
                break;
            case "Heighten hook":
                if(hook.getHookLowering() > 1f)
                    hook.heighten();
                break;
            case "Up":
                changeArmHeight(maxArmHeight, false);
                break;
            case "Down":
                changeArmHeight(minArmHeight, true); 
        }
        if(!name.equals("Lower hook")) hook.setRecentlyHitObject(null);
    }
    
    protected abstract void rotate(float yAngle);
    
    protected abstract void moveHandleHook(float limit, boolean movingForward);
    
    protected abstract void changeArmHeight(float limit, boolean lowering); 
}
