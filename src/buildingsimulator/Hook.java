package buildingsimulator;

import static buildingsimulator.GameManager.addNewCollisionShapeToCompound;
import static buildingsimulator.GameManager.createPhysics;
import static buildingsimulator.GameManager.joinsElementToOtherElement;
import com.jme3.bounding.BoundingBox;
import com.jme3.bullet.collision.PhysicsCollisionGroupListener;
import com.jme3.bullet.collision.PhysicsCollisionObject;
import com.jme3.bullet.collision.shapes.CompoundCollisionShape;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.bullet.joints.HingeJoint;
import com.jme3.collision.CollisionResults;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.Control;

public abstract class Hook {
    protected Node ropeHook;
    protected Spatial hook, hookHandle, recentlyHitObject;
    protected Vector3f hookDisplacement;
    protected float hookLowering = 1f;
    protected static final float HOOK_LOWERING_SPEED = 0.05f;
    private HingeJoint lineAndHookHandleJoint = null;
    public Hook(Node ropeHook, Spatial hookHandle){
        this.ropeHook = ropeHook;
        hook = ropeHook.getChild("hook");
        this.hookHandle = hookHandle;
    }
    protected void lower(CollisionResults results){
        // obniża hak, jeśli w żadnym punkcie z dołu nie dotyka jakiegoś obiektu
        //System.out.println(results.size());
        if(results.size() == 0){
            changeHookPosition(new Vector3f(1f,hookLowering += HOOK_LOWERING_SPEED, 1f),
                    false);
            recentlyHitObject = null;
        }
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
    /**
     * Zwraca wartość określającą jak bardzo opuszczony jest hak. 
     * @return wartość opuszczenia haka 
     */
    public float getHookLowering(){
        return hookLowering;
    }
    protected  void createRopeHookPhysics(CompoundCollisionShape ropeHookCompound,
            Vector3f distanceHookHandleAndRopeHook){
        addNewCollisionShapeToCompound(ropeHookCompound, (Node)hook, ((Node)hook).getChild(0).getName(),
                hook.getLocalTranslation(), hook.getLocalRotation());
        createPhysics(ropeHookCompound, ropeHook, 4f, false);
        RigidBodyControl ropeHookControl = ropeHook.getControl(RigidBodyControl.class);
        ropeHookControl.setCollisionGroup(2); 
        ropeHookControl.addCollideWithGroup(1); // tylko mobilny???
        ropeHookControl.setCollideWithGroups(3); // tylko mobilny???
        lineAndHookHandleJoint = joinsElementToOtherElement(lineAndHookHandleJoint,
                hookHandle, ropeHook, Vector3f.ZERO, distanceHookHandleAndRopeHook);
    }
    protected abstract void changeHookPosition(Vector3f scallingVector, boolean heightening);
    public static PhysicsCollisionGroupListener createCollisionListener(){
        return new PhysicsCollisionGroupListener(){
            @Override
            public boolean collide(PhysicsCollisionObject nodeA, PhysicsCollisionObject nodeB){
                Object a = nodeA.getUserObject(), b = nodeB.getUserObject();
                String aName = ((Spatial)a).getName(), bName = ((Spatial)b).getName();
                boolean weak;
                if(aName.equals("ropeHook") && !bName.equals("hookHandle")){
                    //System.out.println(((Spatial)a).getParent());
                    Playable playerUnit = BuildingSimulator.getActualUnit();
                    weak = playerUnit instanceof MobileCrane;
                    return setCollision(playerUnit, (Spatial)a, (Spatial)b, weak);
                }
                else if(bName.equals("ropeHook") && !aName.equals("hookHandle")){
                    //System.out.println(((Spatial)b).getParent());
                    Playable playerUnit = BuildingSimulator.getActualUnit();
                    weak = playerUnit instanceof MobileCrane;
                    return setCollision(playerUnit, (Spatial)b, (Spatial)a, weak);
                }
                return true;
            }
        };
    }
    private static boolean setCollision(Playable playerUnit, Spatial a, Spatial b, boolean weak){
        float y1 = 0, y2 = 0;
        if(!weak){
            y1 = ((BoundingBox)a.getWorldBound()).getMin(null).y;
            y2 = ((BoundingBox)b.getWorldBound()).getMax(null).y;
        }
        if(!weak){
            if(Math.abs(y1-y2) >= 0 && Math.abs(y1-y2) < 0.1f)
                playerUnit.getHook().recentlyHitObject = b;
        }else playerUnit.getHook().recentlyHitObject = b;
        // PhysicsCollisionObject bo control nie musi być tylko typu RigidBodyControl 
        int collisionGroup = ((PhysicsCollisionObject)b.getControl(0)).getCollisionGroup();
        return collisionGroup == 3 || collisionGroup == 1;
    }
}
