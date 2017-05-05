package buildingsimulator;

import static buildingsimulator.GameManager.*;
import com.jme3.bounding.BoundingBox;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.collision.shapes.CompoundCollisionShape;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.bullet.util.CollisionShapeFactory;
import com.jme3.collision.CollisionResults;
import com.jme3.input.controls.AnalogListener;
import com.jme3.math.Ray;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import java.util.ArrayList;
import java.util.List;

/**
 * Obiekt klasy <code>Crane</code> reprezentuje żuraw. 
 * @author AleksanderSklorz
 */
public class Crane implements AnalogListener{
    private BuildingSimulator game = BuildingSimulator.getBuildingSimulator();
    private Node crane, craneControl, hookHandleControl, ropeHook, littleHookHandle;
    private Node[] ropes = new Node[4];
    private Spatial rack, craneArm, hookHandle;
    private static final float MIN_HANDLE_HOOK_DISPLACEMENT = -63f,
            HOOK_LOWERING_SPEED = 0.05f;
    private float maxHandleHookDisplacement, hookLowering = 1f;
    private Vector3f hookDisplacement;
    private FourRopesHook hook;
    public Crane(){
        initCraneElements((Node)game.getAssetManager().loadModel("Models/zuraw/zuraw.j3o"));
        GameManager.setCraneRack(rack);
        maxHandleHookDisplacement = hookHandleControl.getLocalTranslation().z;
        hook = new FourRopesHook((Node)crane.getChild("ropeHook"),
                hookHandle);
        
        game.getBulletAppState().getPhysicsSpace()
                .addCollisionGroupListener(hook.createCollisionListener(), 2);
        game.getRootNode().attachChild(crane);
    }
    @Override
    public void onAnalog(String name, float value, float tpf) {
        Vector3f hookHandleTranslation;
        switch(name){
            case "Right":
                craneControl.rotate(0f, -tpf / 5, 0f);
                hook.get().getControl(RigidBodyControl.class).setPhysicsRotation(
                        craneControl.getLocalRotation());
                break;
            case "Left": 
                craneControl.rotate(0f, tpf / 5, 0f);
                hook.get().getControl(RigidBodyControl.class).setPhysicsRotation(
                        craneControl.getLocalRotation());
                break;
            case "Up":
                hookHandleTranslation = hookHandleControl.getLocalTranslation();
                if(hookHandleTranslation.z >= MIN_HANDLE_HOOK_DISPLACEMENT)
                    hookHandleControl.setLocalTranslation(hookHandleTranslation
                            .addLocal(0 , 0, -tpf));
                break;
            case "Down":
                hookHandleTranslation = hookHandleControl.getLocalTranslation();
                if(hookHandleTranslation.z < maxHandleHookDisplacement)
                    hookHandleControl.setLocalTranslation(hookHandleTranslation
                            .addLocal(0 , 0, tpf));
                break;
            case "Lower hook":
                hook.lower();
                break;
            case "Highten hook":
                hook.highten();
        }
        if(!name.equals("Lower hook")) hook.setRecentlyHitObject(null);
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
        //crane.getChild("prop").removeControl(RigidBodyControl.class);
        /*CompoundCollisionShape shape = new CompoundCollisionShape(); 
        CollisionShape sh = CollisionShapeFactory.createDynamicMeshShape(crane.getChild("prop0"));
        shape.addChildShape(sh, Vector3f.ZERO);
        CollisionShape sh2 = CollisionShapeFactory.createDynamicMeshShape(crane.getChild("prop1"));
        shape.addChildShape(sh2, Vector3f.ZERO);
        RigidBodyControl rbc = new RigidBodyControl(sh, 0);*/
       //BoundingBox box = ((BoundingBox)crane.getChild("prop").getWorldBound());
        //box.setXExtent(6);
       // box.setZExtent(10);
        //crane.getChild("prop").setModelBound(box);
        //crane.getChild("prop").addControl(rbc);
        //physics.add(rbc);
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
}
