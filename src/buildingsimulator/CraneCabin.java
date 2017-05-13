package buildingsimulator;

import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.input.controls.AnalogListener;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;

public class CraneCabin extends Cabin{
    private Node crane, craneControl, hookHandleControl;
    public CraneCabin(Node crane){
        initCraneElements(crane);
        
    }
    @Override
    protected void rotate(float yAngle) {
        craneControl.rotate(0f, yAngle, 0f);
        hook.getRopeHook().getControl(RigidBodyControl.class).setPhysicsRotation(
                craneControl.getLocalRotation());
    }

    @Override
    protected void moveHandleHook(float limit, boolean movingForward, float speed) {
        Vector3f hookHandleTranslation = hookHandleControl.getLocalTranslation();
        if(movingForward && hookHandleTranslation.z >= limit 
                || !movingForward && hookHandleTranslation.z < limit)
            hookHandleControl.setLocalTranslation(hookHandleTranslation
                    .addLocal(0 , 0, speed));
    }
    
    private void initCraneElements(Node crane){
        this.crane = crane;
        craneControl = (Node)crane.getChild("craneControl");
        hookHandleControl = (Node)craneControl.getChild("hookHandleControl");
    }
}
