package buildingsimulator;

import com.jme3.input.controls.AnalogListener;
import com.jme3.scene.Node;

public abstract class Cabin implements AnalogListener{
    protected Hook hook;
    protected final float maxHandleHookDisplacement = 9.5f, minHandleHookDisplacement = 1f;
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
                moveHandleHook(maxHandleHookDisplacement, true, -tpf);
                break;
            case "Pull in":
                moveHandleHook(minHandleHookDisplacement, false, tpf);
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
    
    protected abstract void moveHandleHook(float limit, boolean movingForward, float speed);
    
    protected void changeArmHeight(float limit, boolean lowering){} 
}
