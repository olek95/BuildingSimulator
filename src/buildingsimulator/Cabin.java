package buildingsimulator;

import com.jme3.input.controls.AnalogListener;
import com.jme3.scene.Node;

public abstract class Cabin implements AnalogListener{
    protected Hook hook;
    protected float maxHandleHookDisplacement, minHandleHookDisplacement;
    protected final float maxArmHeight = 0.6f, minArmHeight = 0f;
    public Cabin(){}
    public Cabin(float maxHandleHookDisplacement, float minHandleHookDisplacement){
        this.maxHandleHookDisplacement = maxHandleHookDisplacement; 
        this.minHandleHookDisplacement = minHandleHookDisplacement;
    }
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
    
    public Hook getHook(){
        return hook;
    }
}
