package buildingsimulator;

import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.collision.shapes.CompoundCollisionShape;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.bullet.control.VehicleControl;
import com.jme3.bullet.joints.HingeJoint;
import com.jme3.input.controls.AnalogListener;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import static buildingsimulator.GameManager.*;
import com.jme3.bounding.BoundingBox;
import com.jme3.bullet.collision.PhysicsCollisionGroupListener;
import com.jme3.bullet.collision.PhysicsCollisionObject;
import com.jme3.collision.CollisionResults;
import com.jme3.math.Ray;

/**
 * Obiekt klasy <code>CraneCabin</code> reprezentuje kabinę operatora ramienia 
 * dźwigu. Posiada on umiejętność obracania ramienia w pionie i poziomie, 
 * wysuwania go i wsuwania, a także opuszczania i podnoszenia liny z hakiem. 
 * @author AleksanderSklorz
 */
public class CraneCabin implements AnalogListener{
    private Node craneCabin, lift, rectractableCranePart, mobileCrane, rope;
    private Spatial hookHandle, hook, ropeHook;
    private float yCraneOffset = 0f, stretchingOut = 1f, hookLowering = 1f;
    private Vector3f hookHandleDisplacement, hookDisplacement;
    private static final float MAX_PROTRUSION = 9.5f, MIN_PROTRUSION = 1f,
            LIFTING_SPEED = 0.005f, STRETCHING_OUT_SPEED = 0.05f, 
            HOOK_LOWERING_SPEED = 0.05f;
    private HingeJoint lineAndHookHandleJoint = null;
    private Spatial recentlyHitObject;
    public CraneCabin(Spatial craneSpatial){
        mobileCrane = (Node)craneSpatial;
        craneCabin = (Node)mobileCrane.getChild("crane");
        lift = (Node)craneCabin.getChild("lift");
        rectractableCranePart = (Node)lift.getChild("retractableCranePart");
        hookHandle = lift.getChild("hookHandle");
        // do aktualnej kolizji dołącza kolizję z grupą 1
        hookHandle.getControl(RigidBodyControl.class).addCollideWithGroup(1);
        ropeHook = mobileCrane.getChild("ropeHook");
        hook = ((Node)ropeHook).getChild("hook");
        rope = (Node)((Node)ropeHook).getChild("rope");
        createCranePhysics();
        hookHandleDisplacement = calculateDisplacementAfterScaling(rectractableCranePart, 
                new Vector3f(1f, 1f, stretchingOut + STRETCHING_OUT_SPEED), false,
                true, true);
        hookDisplacement = calculateDisplacementAfterScaling((Node)mobileCrane
                .getChild("rope"), new Vector3f(1f, hookLowering + HOOK_LOWERING_SPEED, 1f),
                false, true, false);
        hookDisplacement.y *= 2; // wyrównuje poruszanie się haka wraz z liną 
    }
    public void onAnalog(String name, float value, float tpf) {
        CompoundCollisionShape ropeHookCompound;
        switch(name){
            case "Right":
                craneCabin.rotate(0, -tpf / 4, 0);
                break;
            case "Left": 
                craneCabin.rotate(0, tpf / 4, 0);
                break;
            case "Up":
                if(yCraneOffset + LIFTING_SPEED < 0.6f){
                    yCraneOffset += LIFTING_SPEED;
                    lift.rotate(-LIFTING_SPEED, 0, 0);
                }
                break;
            case "Down":
                if(yCraneOffset - LIFTING_SPEED >= 0f){
                    yCraneOffset -= LIFTING_SPEED;
                    lift.rotate(LIFTING_SPEED, 0, 0);
                }
                break;
            case "Pull out":
                if(stretchingOut <= MAX_PROTRUSION){
                    ((Geometry)rectractableCranePart.getChild(0)).setLocalScale(1f, 1f,
                            stretchingOut += STRETCHING_OUT_SPEED);
                    movingDuringStretchingOut(true, hookHandle, hookHandleDisplacement);
                    createObjectPhysics(rectractableCranePart, rectractableCranePart,
                            1f, true, rectractableCranePart.getChild(0).getName());
                    rectractableCranePart.getControl(RigidBodyControl.class)
                            .setCollisionGroup(3);
                }
                break;
            case "Pull in":
                if(stretchingOut > MIN_PROTRUSION){
                    ((Geometry)rectractableCranePart.getChild(0)).setLocalScale(1f, 1f,
                            stretchingOut -= STRETCHING_OUT_SPEED);
                    movingDuringStretchingOut(false, hookHandle, hookHandleDisplacement);
                    createObjectPhysics(rectractableCranePart, rectractableCranePart, 1f,
                            true, rectractableCranePart.getChild(0).getName());
                    rectractableCranePart.getControl(RigidBodyControl.class)
                            .setCollisionGroup(3);
                }
                break;
            case "Lower hook":
                CollisionResults results = new CollisionResults();
                /* jeśli nie dotknęło żadnego obiektu, to zbędne jest sprawdzanie 
                kolizji w dół*/
                if(recentlyHitObject != null){ 
                    // tworzy pomocniczy promień sprawdzający kolizję w dół
                    Ray r = new Ray(hook.getWorldTranslation(), new Vector3f(0,-0.5f,0));
                    System.out.println(recentlyHitObject);
                    System.out.println(recentlyHitObject.getWorldBound());
                    r.collideWith((BoundingBox)recentlyHitObject.getWorldBound(),
                            results);
                    System.out.println(results.size());}
                // obniża hak, jeśli w żadnym punkcie z dołu nie dotyka jakiegoś obiektu
                if(results.size() == 0){ 
                    ((Geometry)mobileCrane.getChild("Cylinder.0021")).setLocalScale(1f,
                            hookLowering += HOOK_LOWERING_SPEED, 1f);
                    movingDuringStretchingOut(false, hook, hookDisplacement);
                    ropeHookCompound = createCompound(rope, rope.getChild(0).getName());
                    addNewCollisionShapeToComponent(ropeHookCompound, (Node)hook, "Mesh1",
                            hook.getLocalTranslation(), hook.getLocalRotation());
                    createPhysics(ropeHookCompound, ropeHook, 4f, false, (Geometry)rope
                            .getChild(0));
                    RigidBodyControl ropeHookControl = ropeHook
                            .getControl(RigidBodyControl.class);
                    ropeHookControl.setCollisionGroup(2); 
                    ropeHookControl.setCollideWithGroups(3);
                    lineAndHookHandleJoint = joinsElementToOtherElement(lineAndHookHandleJoint,
                            hookHandle, ropeHook, Vector3f.ZERO, new Vector3f(0, 0.06f,0));
                    recentlyHitObject = null;
                }
                break;
            case "Highten hook":
                ((Geometry)mobileCrane.getChild("Cylinder.0021")).setLocalScale(1f,
                        hookLowering -= HOOK_LOWERING_SPEED, 1f);
                movingDuringStretchingOut(true, hook, hookDisplacement);
                ropeHookCompound = createCompound(rope, rope.getChild(0).getName());
                addNewCollisionShapeToComponent(ropeHookCompound, (Node)hook, "Mesh1",
                        hook.getLocalTranslation(), hook.getLocalRotation());
                createPhysics(ropeHookCompound, ropeHook, 4f, false, (Geometry)rope
                        .getChild(0));
                RigidBodyControl ropeHookControl = ropeHook
                            .getControl(RigidBodyControl.class);
                ropeHookControl.setCollisionGroup(2); 
                ropeHookControl.setCollideWithGroups(3);
                lineAndHookHandleJoint = joinsElementToOtherElement(lineAndHookHandleJoint,
                        hookHandle, ropeHook, Vector3f.ZERO, new Vector3f(0, 0.06f,0));
        }
        recentlyHitObject = null;
    }
    private void createCranePhysics(){
        PhysicsSpace physics = BuildingSimulator.getBuildingSimulator()
                .getBulletAppState().getPhysicsSpace();
        createObjectPhysics(craneCabin, craneCabin, 1f, true, "outsideCabin", "turntable");
        createObjectPhysics(rectractableCranePart, rectractableCranePart, 1f, true,
                rectractableCranePart.getChild(0).getName());
        rectractableCranePart.getControl(RigidBodyControl.class).setCollisionGroup(3);
        CompoundCollisionShape ropeHookCompound = createCompound(rope, rope.getChild(0)
                .getName());
        addNewCollisionShapeToComponent(ropeHookCompound, (Node)hook, "Mesh1", hook
                .getLocalTranslation(), hook.getLocalRotation());
        createPhysics(ropeHookCompound, ropeHook, 4f, false, (Geometry)rope.getChild(0));
        RigidBodyControl ropeHookControl = ropeHook
                            .getControl(RigidBodyControl.class);
        ropeHookControl.setCollisionGroup(2); 
        ropeHookControl.setCollideWithGroups(3);
        lineAndHookHandleJoint = joinsElementToOtherElement(lineAndHookHandleJoint,
                hookHandle, ropeHook, Vector3f.ZERO, new Vector3f(0, 0.06f,0));
        HingeJoint cabinAndMobilecraneJoin = new HingeJoint(mobileCrane
                .getControl(VehicleControl.class),(RigidBodyControl)craneCabin
                .getControl(0), craneCabin.getLocalTranslation(), Vector3f.ZERO,
                Vector3f.ZERO, Vector3f.ZERO);
        physics.add(cabinAndMobilecraneJoin);
        physics.add(lift.getChild("longCraneElement").getControl(0));
        physics.add(hookHandle.getControl(0));
        physics.addCollisionGroupListener(new PhysicsCollisionGroupListener(){
            public boolean collide(PhysicsCollisionObject nodeA, PhysicsCollisionObject nodeB){
                if(nodeA.getUserObject().equals(ropeHook)
                        && !nodeB.getUserObject().equals(hookHandle)){
                    System.out.println(nodeB + "DOTKNAL");
                    recentlyHitObject = (Spatial)nodeB.getUserObject();
                }
                return true;
            }
        }, 2);
    }
}
