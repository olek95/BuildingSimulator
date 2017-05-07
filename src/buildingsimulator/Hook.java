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
    protected Spatial lower(CollisionResults results){
        // obniża hak, jeśli w żadnym punkcie z dołu nie dotyka jakiegoś obiektu
        if(results.size() == 0){
            changeHookPosition(new Vector3f(1f,hookLowering += HOOK_LOWERING_SPEED, 1f),
                    false);
            return null;
        }
        return recentlyHitObject;
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
    protected  void createRopeHookPhysics(CompoundCollisionShape ropeHookCompound){
        addNewCollisionShapeToCompound(ropeHookCompound, (Node)hook, ((Node)hook).getChild(0).getName(),
                hook.getLocalTranslation(), hook.getLocalRotation());
        createPhysics(ropeHookCompound, ropeHook, 4f, false);
        RigidBodyControl ropeHookControl = ropeHook.getControl(RigidBodyControl.class);
        ropeHookControl.setCollisionGroup(2); 
        lineAndHookHandleJoint = joinsElementToOtherElement(lineAndHookHandleJoint,
                hookHandle, ropeHook, Vector3f.ZERO, new Vector3f(0, 0.6f,0));
    }
    protected abstract void changeHookPosition(Vector3f scallingVector, boolean heightening);
    public PhysicsCollisionGroupListener createCollisionListener(final boolean weak){
        return new PhysicsCollisionGroupListener(){
            @Override
            public boolean collide(PhysicsCollisionObject nodeA, PhysicsCollisionObject nodeB){
                Object a = nodeA.getUserObject(), b = nodeB.getUserObject();
                if(a.equals(ropeHook) && !b.equals(hookHandle))
                    return setCollision(ropeHook, (Spatial)b, weak);
                else if(b.equals(ropeHook) && !a.equals(hookHandle))
                    return setCollision(ropeHook, (Spatial)a, weak);
                return true;
            }
        };
    }
    private boolean setCollision(Spatial a, Spatial b, boolean weak){
        float y1 = 0, y2 = 0;
        if(!weak){
            y1 = ((BoundingBox)a.getWorldBound()).getMin(null).y;
            y2 = ((BoundingBox)b.getWorldBound()).getMax(null).y;
        }
        if(!weak && Math.abs(y1-y2) >= 0 && Math.abs(y1-y2) < 0.1f)
            recentlyHitObject = b;
        int collisionGroup = b.getControl(RigidBodyControl.class).getCollisionGroup();
        return collisionGroup == 3 || collisionGroup == 1;
    }
}
