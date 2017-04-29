package buildingsimulator;

import static buildingsimulator.GameManager.*;
import com.jme3.bounding.BoundingBox;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.collision.shapes.CompoundCollisionShape;
import com.jme3.bullet.control.RigidBodyControl;
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
    private Node crane, craneControl, hookHandleControl, ropeHook;
    private Node[] ropes = new Node[4];
    private Spatial rack, craneArm, hook;
    private static final float MIN_HANDLE_HOOK_DISPLACEMENT = -63f,
            HOOK_LOWERING_SPEED = 0.05f;
    private float maxHandleHookDisplacement, hookLowering = 1f;
    private Vector3f hookDisplacement;
    public Crane(){
        initCraneElements((Node)game.getAssetManager().loadModel("Models/zuraw/zuraw.j3o"));
        GameManager.setCraneRack(rack);
        maxHandleHookDisplacement = hookHandleControl.getLocalTranslation().z;
        
        
        PhysicsSpace physics = game.getBulletAppState().getPhysicsSpace();
        ropeHook = (Node)hookHandleControl.getChild("ropeHook");
        hook = ropeHook.getChild("hook");
        List<Spatial> ropeHookChildren = ropeHook.getChildren();
        int index = 0;
        for(int i = 0; i < ropeHookChildren.size(); i++){
            Spatial children = ropeHookChildren.get(i);
            if(children.getName().startsWith("rope")){
                ropes[index] = (Node)children;
                index++;
            }
        }
        //hookHandle.getControl(RigidBodyControl.class).addCollideWithGroup(1);
        hookDisplacement = calculateDisplacementAfterScaling(ropes[0], 
                new Vector3f(1f, hookLowering + HOOK_LOWERING_SPEED, 1f),
                false, true, false);
        //hookDisplacement.y *= 2;
        createRopeHookPhysics();
        
        
        
        
        game.getRootNode().attachChild(crane);
    }
    @Override
    public void onAnalog(String name, float value, float tpf) {
        Vector3f hookHandleTranslation;
        switch(name){
            case "Right":
                craneControl.rotate(0f, -tpf / 5, 0f);
                break;
            case "Left": 
                craneControl.rotate(0f, tpf / 5, 0f);
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
                lower();
                break;
            case "Highten hook":
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
    
    
    
    
    
    public void lower(){
        //CollisionResults results = new CollisionResults();
        /* jeśli nie dotknęło żadnego obiektu, to zbędne jest sprawdzanie 
        kolizji w dół*/
        //if(recentlyHitObject != null)
            // tworzy pomocniczy promień sprawdzający kolizję w dół
            //new Ray(hook.getWorldTranslation(), new Vector3f(0,-0.5f,0))
                    //.collideWith((BoundingBox)recentlyHitObject.getWorldBound(), results);
        // obniża hak, jeśli w żadnym punkcie z dołu nie dotyka jakiegoś obiektu
        //if(results.size() == 0)
            changeHookPosition(new Vector3f(1f,hookLowering += HOOK_LOWERING_SPEED, 1f),
                    false);
    }
    private void createRopeHookPhysics(){
        CompoundCollisionShape ropeHookCompound = createCompound(ropes[0], ropes[0].getChild(0)
                .getName());
        ropeHookCompound.getChildren().get(0).location = ropes[0].getLocalTranslation();
        addNewCollisionShapeToCompound(ropeHookCompound, ropes[1], ropes[1].getChild(0).getName(),
                ropes[1].getLocalTranslation(), null);
        addNewCollisionShapeToCompound(ropeHookCompound, ropes[2], ropes[2].getChild(0).getName(),
                ropes[2].getLocalTranslation(), null);
        addNewCollisionShapeToCompound(ropeHookCompound, ropes[3], ropes[3].getChild(0).getName(),
                ropes[3].getLocalTranslation(), null);
                
                
        addNewCollisionShapeToCompound(ropeHookCompound, (Node)hook, ((Node)hook).getChild(0).getName(),
                hook.getLocalTranslation(), hook.getLocalRotation());
        createPhysics(ropeHookCompound, ropeHook, 4f, true);
        /*RigidBodyControl ropeHookControl = ropeHook.getControl(RigidBodyControl.class);
        ropeHookControl.setCollisionGroup(2); 
        ropeHookControl.addCollideWithGroup(1);
        ropeHookControl.setCollideWithGroups(3);*/
        //lineAndHookHandleJoint = joinsElementToOtherElement(lineAndHookHandleJoint,
        //        hookHandle, ropeHook, Vector3f.ZERO, new Vector3f(0, 0.06f,0));
    }
    private void changeHookPosition(Vector3f scallingVector,
            boolean heightening){
        for(int i = 0; i < ropes.length; i++)
            movingDuringStretchingOut((Geometry)ropes[i].getChild(0), 
                    scallingVector, heightening, hook, hookDisplacement);
        createRopeHookPhysics();
    }
}
