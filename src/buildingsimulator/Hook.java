package buildingsimulator;

import static buildingsimulator.GameManager.moveByVector;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;

public abstract class Hook {
    protected Node ropeHook;
    protected Spatial hook, hookHandle, recentlyHitObject;
    protected Vector3f hookDisplacement;
    protected float hookLowering = 1f;
    protected static final float HOOK_LOWERING_SPEED = 0.05f;
    public Hook(Node ropeHook, Spatial hookHandle){
        this.ropeHook = ropeHook;
        hook = ropeHook.getChild("hook");
        this.hookHandle = hookHandle;
    }
    /**
     * Podnosi hak. 
     */
    public void highten(){
        changeHookPosition(new Vector3f(1f, hookLowering -= HOOK_LOWERING_SPEED, 1f),
                true);
    }
    /**
     * Ustawia ostatnio dotknięty przez hak obiekt.
     * @param object dotknięty obiekt 
     */
    public void setRecentlyHitObject(Spatial object){
        recentlyHitObject = object;
    }
    protected abstract void createRopeHookPhysics();
    protected void changeHookPosition(Vector3f scallingVector, boolean heightening){
        moveByVector(heightening, hook, hookDisplacement);
        createRopeHookPhysics();
    }
}
