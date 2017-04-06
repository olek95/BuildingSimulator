package buildingsimulator;

import static buildingsimulator.GameManager.*;
import com.jme3.bounding.BoundingBox;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.collision.PhysicsCollisionGroupListener;
import com.jme3.bullet.collision.PhysicsCollisionObject;
import com.jme3.bullet.collision.shapes.CompoundCollisionShape;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.bullet.joints.HingeJoint;
import com.jme3.collision.CollisionResults;
import com.jme3.math.Ray;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;

public class Hook {
    private Spatial hook, hookHandle, recentlyHitObject;
    private Node rope, ropeHook;
    private Vector3f hookDisplacement;
    private HingeJoint lineAndHookHandleJoint = null;
    private float hookLowering = 1f;
    private static final float HOOK_LOWERING_SPEED = 0.05f;
    public Hook(Node ropeHook, Spatial hookHandle){
        this.ropeHook = ropeHook;
        hook = ropeHook.getChild("hook");
        rope = (Node)ropeHook.getChild("rope");
        this.hookHandle = hookHandle;
        hookHandle.getControl(RigidBodyControl.class).addCollideWithGroup(1);
        hookDisplacement = calculateDisplacementAfterScaling(rope, 
                new Vector3f(1f, hookLowering + HOOK_LOWERING_SPEED, 1f),
                false, true, false);
        hookDisplacement.y *= 2; // wyrównuje poruszanie się haka wraz z liną 
        createRopeHookPhysics();
        PhysicsSpace physics = BuildingSimulator.getBuildingSimulator()
                .getBulletAppState().getPhysicsSpace();
        physics.add(hookHandle.getControl(0));
    }
    public void lower(){
        CollisionResults results = new CollisionResults();
        /* jeśli nie dotknęło żadnego obiektu, to zbędne jest sprawdzanie 
        kolizji w dół*/
        if(recentlyHitObject != null)
            // tworzy pomocniczy promień sprawdzający kolizję w dół
            new Ray(hook.getWorldTranslation(), new Vector3f(0,-0.5f,0))
                    .collideWith((BoundingBox)recentlyHitObject.getWorldBound(),results);
        // obniża hak, jeśli w żadnym punkcie z dołu nie dotyka jakiegoś obiektu
        if(results.size() == 0)
            changeHookPosition(rope, new Vector3f(1f,hookLowering += HOOK_LOWERING_SPEED, 1f),
                    false);
    }
    public void highten(){
        changeHookPosition(rope, new Vector3f(1f, hookLowering -= HOOK_LOWERING_SPEED, 1f),
                true);
    }
    public float getHookLowering(){
        return hookLowering;
    }
    public PhysicsCollisionGroupListener createCollisionListener(){
        return new PhysicsCollisionGroupListener(){
            public boolean collide(PhysicsCollisionObject nodeA, PhysicsCollisionObject nodeB){
                if(nodeA.getUserObject().equals(ropeHook)
                        && !nodeB.getUserObject().equals(hookHandle)){
                    recentlyHitObject = (Spatial)nodeB.getUserObject();
                }
                return true;
            }
        };
    }
    public Spatial getHookHandle(){
        return hookHandle;
    }
    public void setRecentlyHitObject(Spatial object){
        recentlyHitObject = object;
    }
    private void changeHookPosition(Node scallingGeometryParent, Vector3f scallingVector,
            boolean heightening){
        movingDuringStretchingOut((Geometry)scallingGeometryParent.getChild(0), 
                scallingVector, heightening, hook, hookDisplacement);
        createRopeHookPhysics();
    }
    private void createRopeHookPhysics(){
        Geometry ropeGeometry = (Geometry)rope.getChild(0);
        CompoundCollisionShape ropeHookCompound = createCompound(rope, ropeGeometry.getName());
        addNewCollisionShapeToComponent(ropeHookCompound, (Node)hook, "hookGeometry",
                hook.getLocalTranslation(), hook.getLocalRotation());
        createPhysics(ropeHookCompound, ropeHook, 4f, false, ropeGeometry);
        RigidBodyControl ropeHookControl = ropeHook.getControl(RigidBodyControl.class);
        ropeHookControl.setCollisionGroup(2); 
        ropeHookControl.setCollideWithGroups(3);
        lineAndHookHandleJoint = joinsElementToOtherElement(lineAndHookHandleJoint,
                hookHandle, ropeHook, Vector3f.ZERO, new Vector3f(0, 0.06f,0));
    }
}
