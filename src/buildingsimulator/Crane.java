package buildingsimulator;

import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.input.controls.AnalogListener;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;

/**
 * Obiekt klasy <code>Crane</code> reprezentuje żuraw. 
 * @author AleksanderSklorz
 */
public class Crane implements Playable{
    private BuildingSimulator game = BuildingSimulator.getBuildingSimulator();
    private Node crane, craneControl, hookHandleControl;
    private Spatial rack, craneArm, hookHandle;
    private static final float MIN_HANDLE_HOOK_DISPLACEMENT = -63f;
    private float maxHandleHookDisplacement;
    private FourRopesHook hook;
    public static final boolean WEAK = false;
    public Crane(){
        initCraneElements((Node)game.getAssetManager().loadModel("Models/zuraw/zuraw.j3o"));
        GameManager.setCraneRack(rack);
        maxHandleHookDisplacement = hookHandleControl.getLocalTranslation().z;
        hook = new FourRopesHook((Node)crane.getChild("ropeHook"),
                hookHandle);
        
        /*game.getBulletAppState().getPhysicsSpace()
                .addCollisionGroupListener(hook.createCollisionListener(false), 2);*/
        game.getRootNode().attachChild(crane);
    }
    private RigidBodyControl setProperLocation(Spatial object, Vector3f displacement){
        RigidBodyControl control = object.getControl(RigidBodyControl.class);
        control.setPhysicsLocation(object.getLocalTranslation().add(displacement));
        return control;
    }
    private void initCraneElements(Node parent){
        crane = parent;
        Vector3f craneLocation = new Vector3f(10f, -1f, 0f);
        crane.setLocalTranslation(craneLocation);
        PhysicsSpace physics = game.getBulletAppState().getPhysicsSpace();
        physics.add(setProperLocation(crane.getChild("prop0"), craneLocation));
        physics.add(setProperLocation(crane.getChild("prop1"), craneLocation));
        physics.add(setProperLocation(rack = crane.getChild("rack"), craneLocation));
        craneControl = (Node)crane.getChild("craneControl");
        physics.add(setProperLocation(crane.getChild("entrancePlatform"), craneLocation));
        physics.add(setProperLocation(craneControl.getChild("turntable"), craneLocation));
        physics.add(setProperLocation(craneControl.getChild("mainElement"), craneLocation));
        craneArm = craneControl.getChild("craneArm");
        physics.add(setProperLocation(craneArm, craneLocation));
        physics.add(setProperLocation(craneControl.getChild("cabin"), craneLocation));
        hookHandleControl = (Node)craneControl.getChild("hookHandleControl");
        hookHandle = hookHandleControl.getChild("hookHandle");
        physics.add(setProperLocation(hookHandle, craneLocation));
    }
    @Override
    public Hook getHook() {
        return hook;
    }
}
