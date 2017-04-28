package buildingsimulator;

import com.jme3.bounding.BoundingBox;
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
public class Crane implements AnalogListener{
    private BuildingSimulator game = BuildingSimulator.getBuildingSimulator();
    private Node crane, craneControl, hookHandleControl;
    private Spatial rack, craneArm;
    private static final float MIN_HANDLE_HOOK_DISPLACEMENT = -63f;
    public Crane(){
        initCraneElements((Node)game.getAssetManager().loadModel("Models/zuraw/zuraw.j3o"));
        GameManager.setCraneRack(rack);
        game.getRootNode().attachChild(crane);
    }
    @Override
    public void onAnalog(String name, float value, float tpf) {
        switch(name){
            case "Right":
                craneControl.rotate(0f, -tpf / 5, 0f);
                break;
            case "Left": 
                craneControl.rotate(0f, tpf / 5, 0f);
                break;
            case "Up":
                Vector3f hookHandleTranslation = hookHandleControl.getLocalTranslation();
                if(hookHandleTranslation.z >= MIN_HANDLE_HOOK_DISPLACEMENT)
                hookHandleControl.setLocalTranslation(hookHandleTranslation
                        .addLocal(0 , 0, -tpf));
                break;
            case "Down":
                if(((BoundingBox)((Node)craneArm).getChild(0).getWorldBound()).getMax(null).z 
                        > hookHandleControl.getLocalTranslation().z)
                hookHandleControl.setLocalTranslation(hookHandleControl
                        .getLocalTranslation().addLocal(0 , 0, 1));
                break;
        }
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
        physics.add(setProperLocation(crane.getChild("prop"), craneLocation));
        physics.add(setProperLocation(rack = crane.getChild("rack"), craneLocation));
        craneControl = (Node)crane.getChild("craneControl");
        physics.add(setProperLocation(crane.getChild("entrancePlatform"), craneLocation));
        physics.add(setProperLocation(craneControl.getChild("turntable"), craneLocation));
        physics.add(setProperLocation(craneControl.getChild("mainElement"), craneLocation));
        craneArm = craneControl.getChild("craneArm");
        physics.add(setProperLocation(craneArm, craneLocation));
        physics.add(setProperLocation(craneControl.getChild("cabin"), craneLocation));
        hookHandleControl = (Node)craneControl.getChild("hookHandleControl");
        physics.add(setProperLocation(hookHandleControl.getChild("hookHandle"), craneLocation));
    }
}
