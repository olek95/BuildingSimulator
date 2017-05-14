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

/**
 * Klasa <code>Hook</code> jest klasą abstrakcji dla wszystkich haków w grze. 
 * @author AleksanderSklorz
 */
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
    
    /**
     * Opuszcza hak, jeśli nic nie znajduje się pod nim. W przeciwnym razie 
     * hak nie zmienia pozycji. 
     * @param results wyniki kolizji, dzięki którym sprawdza się w ilu punktach 
     * jest kolizja z danym obiektem 
     */
    protected void lower(CollisionResults results){
        // obniża hak, jeśli w żadnym punkcie z dołu nie dotyka jakiegoś obiektu
        if(results.size() == 0){
            changeHookPosition(new Vector3f(1f,hookLowering += HOOK_LOWERING_SPEED, 1f),
                    false);
            recentlyHitObject = null;
        }
    }
    
    /**
     * Podnosi hak. 
     */
    public void heighten(){
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
    
    /**
     * Dołącza do podanego złożonego kształtu kolizji kształty kolizji innych 
     * elementów haka, wspólnych dla wszystkich haków. Ponadto dołącza fizykę 
     * dla tego obiektu do gry, ustawia odpowiednie grupy kolizji, a także 
     * łączy hak z uchwytem na hak. 
     * @param ropeHookCompound złożony kształt kolizji do którego dołącza się 
     * kształty kolizji dla haka. 
     * @param distanceHookHandleAndRopeHook wektor decydujący w jakiej odległości 
     * ma być zawieszony hak od uchwytu na hak. 
     */
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
    
    /**
     * Tworzy obiekt kolizji dla haków. Ustawia on ostatnio dotkniety przez hak 
     * obiekt. 
     * @return obiekt kolizji dla haków 
     */
    public static PhysicsCollisionGroupListener createCollisionListener(){
        return new PhysicsCollisionGroupListener(){
            @Override
            public boolean collide(PhysicsCollisionObject nodeA, PhysicsCollisionObject nodeB){
                Object a = nodeA.getUserObject(), b = nodeB.getUserObject();
                Spatial aSpatial = (Spatial)a, bSpatial = (Spatial)b;
                String aName = aSpatial.getName(), bName = bSpatial.getName();
                if(aName.equals("ropeHook") && !bName.equals("hookHandle")){
                    return setCollision(aSpatial, bSpatial);
                }
                else if(bName.equals("ropeHook") && !aName.equals("hookHandle")){
                    return setCollision(bSpatial, aSpatial);
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
     * Zwraca węzeł z hakiem z liniami. 
     * @return węzeł z hakiem z liniami 
     */
    public Node getRopeHook(){
        return ropeHook;
    }
    
    /**
     * Zmienia pozycję haka w górę lub w dół równocześnie skalując liny haka. 
     * @param scallingVector wektor skalowania lin 
     * @param heightening true jeśli podnosimy hak, false w przeciwnym razie 
     */
    protected abstract void changeHookPosition(Vector3f scallingVector, boolean heightening);
    
    protected abstract void lower();
    
    private static boolean setCollision(Spatial a, Spatial b){
        float y1 = ((BoundingBox)a.getWorldBound()).getMin(null).y,
                y2 = ((BoundingBox)b.getWorldBound()).getMax(null).y;
        /* zabezpiecza przypadek gdy hak dotyka jednocześnie elementu pionowego
        i poziomego*/
        if(Math.abs(y1 - y2) >= 0 && Math.abs(y1 - y2) < 0.1f)
            GameManager.getActualUnit().getHook().recentlyHitObject = b;
        // PhysicsCollisionObject bo control nie musi być tylko typu RigidBodyControl 
        int collisionGroup = ((PhysicsCollisionObject)b.getControl(0)).getCollisionGroup();
        return collisionGroup == 3 || collisionGroup == 1;
    }
}