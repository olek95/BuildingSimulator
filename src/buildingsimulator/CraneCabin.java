package buildingsimulator;

import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;

public class CraneCabin extends Cabin{
    private Node hookHandleControl;
    public CraneCabin(Node crane){
        super(crane);
        initCraneCabinElements(crane);
        maxHandleHookDisplacement = -63f;
        minHandleHookDisplacement = hookHandleControl.getLocalTranslation().z;
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
    
    @Override
    protected void initCraneCabinElements(Node crane){
        super.initCraneCabinElements(crane);
        PhysicsSpace physics = BuildingSimulator.getBuildingSimulator()
                .getBulletAppState().getPhysicsSpace();
        Vector3f craneLocation = crane.getLocalTranslation();
        physics.add(setProperLocation(crane.getChild("entrancePlatform"), craneLocation));
        physics.add(setProperLocation(craneControl.getChild("turntable"), craneLocation));
        physics.add(setProperLocation(craneControl.getChild("mainElement"), craneLocation));
        physics.add(setProperLocation(craneControl.getChild("craneArm"), craneLocation));
        physics.add(setProperLocation(craneControl.getChild("cabin"), craneLocation));
        hookHandleControl = (Node)craneControl.getChild("hookHandleControl");
        hookHandle = hookHandleControl.getChild("hookHandle");
        physics.add(setProperLocation(hookHandle, craneLocation));
        hook = new FourRopesHook((Node)crane.getChild("ropeHook"), hookHandle);
    }
    
    private RigidBodyControl setProperLocation(Spatial object, Vector3f displacement){
        RigidBodyControl control = object.getControl(RigidBodyControl.class);
        control.setPhysicsLocation(object.getLocalTranslation().add(displacement));
        return control;
    }
    
    
}
