package buildingsimulator;

import static buildingsimulator.GameManager.*;
import com.jme3.bounding.BoundingBox;
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

/**
 * Obiekt klasy <code>OneRopeHook</code> reprezentuje hak zawieszony na linie. 
 * Może być zarówno opuszczany jak i podnoszony. 
 * @author AleksanderSklorz 
 */
public class OneRopeHook {
    private Spatial hook, hookHandle, recentlyHitObject;
    private Node rope, ropeHook;
    private Vector3f hookDisplacement;
    private HingeJoint lineAndHookHandleJoint = null;
    private float hookLowering = 1f;
    private static final float HOOK_LOWERING_SPEED = 0.05f;
    public OneRopeHook(Node ropeHook, Spatial hookHandle){
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
        BuildingSimulator.getBuildingSimulator().getBulletAppState().getPhysicsSpace()
                .add(hookHandle.getControl(0));
    }
    /**
     * Opuszcza hak do momentu wykrycia przeszkody. 
     */
    public void lower(){
        CollisionResults results = new CollisionResults();
        /* jeśli nie dotknęło żadnego obiektu, to zbędne jest sprawdzanie 
        kolizji w dół*/
        if(recentlyHitObject != null)
            // tworzy pomocniczy promień sprawdzający kolizję w dół
            new Ray(hook.getWorldTranslation(), new Vector3f(0,-0.5f,0))
                    .collideWith((BoundingBox)recentlyHitObject.getWorldBound(), results);
        // obniża hak, jeśli w żadnym punkcie z dołu nie dotyka jakiegoś obiektu
        if(results.size() == 0)
            changeHookPosition(rope, new Vector3f(1f,hookLowering += HOOK_LOWERING_SPEED, 1f),
                    false);
    }
    /**
     * Podnosi hak. 
     */
    public void highten(){
        changeHookPosition(rope, new Vector3f(1f, hookLowering -= HOOK_LOWERING_SPEED, 1f),
                true);
    }
    /**
     * Zwraca wartość określajacą jak bardzo opuszczony jest hak. 
     * @return wartość opuszczenia haka 
     */
    public float getHookLowering(){
        return hookLowering;
    }
    /**
     * Tworzy słuchacza dla haka, który sprawdza kolizję haka z obiektami otoczenia.
     * Dodatkowo sprawdza czy hak ma kolizję z żurawiem, jeśli tak - nie widzi przeszkody.
     * @return słuchacz kolizji
     */
    public PhysicsCollisionGroupListener createCollisionListener(){
        return new PhysicsCollisionGroupListener(){
            @Override
            public boolean collide(PhysicsCollisionObject nodeA, PhysicsCollisionObject nodeB){
                Object a = nodeA.getUserObject(), b = nodeB.getUserObject();
                if(a.equals(ropeHook) && !b.equals(hookHandle)){
                    recentlyHitObject = (Spatial)nodeB.getUserObject();
                    int collisionGroup = nodeB.getCollisionGroup();
                    if(collisionGroup != 3 && collisionGroup != 1) return false;
                }else{
                    if(b.equals(ropeHook) && !a.equals(hookHandle)){
                        recentlyHitObject = (Spatial)nodeA.getUserObject();
                        int collisionGroup = nodeA.getCollisionGroup();
                        if(collisionGroup != 3 && collisionGroup != 1) return false;
                    }
                }
                return true;
            }
        };
    }
    /**
     * Zwraca uchwyt do jakiego przyczepiona jest lina. 
     * @return uchwyt liny
     */
    public Spatial getHookHandle(){
        return hookHandle;
    }
    /**
     * Ustawia ostatnio dotknięty przez hak obiekt.
     * @param object dotknięty obiekt 
     */
    public void setRecentlyHitObject(Spatial object){
        recentlyHitObject = object;
    }
    private void changeHookPosition(Node scallingGeometryParent, Vector3f scallingVector,
            boolean heightening){
        ((Geometry)scallingGeometryParent.getChild(0)).setLocalScale(scallingVector);
        moveByVector(heightening, hook, hookDisplacement);
        //movingDuringStretchingOut((Geometry)scallingGeometryParent.getChild(0), 
        //        scallingVector, heightening, hook, hookDisplacement);
        createRopeHookPhysics();
    }
    private void createRopeHookPhysics(){
        CompoundCollisionShape ropeHookCompound = createCompound(rope, rope.getChild(0)
                .getName());
        addNewCollisionShapeToCompound(ropeHookCompound, (Node)hook, "hookGeometry",
                hook.getLocalTranslation(), hook.getLocalRotation());
        createPhysics(ropeHookCompound, ropeHook, 4f, false);
        RigidBodyControl ropeHookControl = ropeHook.getControl(RigidBodyControl.class);
        ropeHookControl.setCollisionGroup(2); 
        ropeHookControl.addCollideWithGroup(1);
        ropeHookControl.setCollideWithGroups(3);
        lineAndHookHandleJoint = joinsElementToOtherElement(lineAndHookHandleJoint,
                hookHandle, ropeHook, Vector3f.ZERO, new Vector3f(0, 0.06f,0));
    }
}
