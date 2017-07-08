package cranes;

import buildingmaterials.Wall;
import buildingsimulator.BottomCollisionListener;
import buildingsimulator.BuildingSimulator;
import buildingsimulator.GameManager;
import static buildingsimulator.GameManager.*;
import buildingsimulator.RememberingRecentlyHitObject;
import com.jme3.bounding.BoundingBox;
import com.jme3.bullet.collision.shapes.CompoundCollisionShape;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.bullet.joints.HingeJoint;
import com.jme3.collision.CollisionResults;
import com.jme3.math.Quaternion;
import com.jme3.math.Ray;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;

/**
 * Klasa <code>Hook</code> jest klasą abstrakcji dla wszystkich haków w grze. 
 * @author AleksanderSklorz
 */
public abstract class Hook implements RememberingRecentlyHitObject{
    private Node ropeHook;
    private Spatial hook, hookHandle, recentlyHitObject, attachedObject = null;
    private float actualLowering = 1f, speed, gapBetweenHookAndAttachedObject;
    private Vector3f hookDisplacement;
    private HingeJoint lineAndHookHandleJoint = null, buildingMaterialJoint;
    private static BottomCollisionListener collisionListener = null; 
    public Hook(Node ropeHook, Spatial hookHandle, float speed, 
            float gapBetweenHookAndAttachedObject){
        this.ropeHook = ropeHook;
        hook = ropeHook.getChild("hook");
        this.hookHandle = hookHandle;
        this.speed = speed;
        this.gapBetweenHookAndAttachedObject = gapBetweenHookAndAttachedObject; 
        if(collisionListener == null){
            collisionListener = new BottomCollisionListener(this, "ropeHook", "hookHandle");
            BuildingSimulator.getBuildingSimulator().getBulletAppState().getPhysicsSpace()
                    .addCollisionGroupListener(collisionListener, 2);
        }
    } 
    
    @Override
    public void setCollision(Spatial b){
        /* zabezpiecza przypadek gdy hak dotyka jednocześnie elementu pionowego
        i poziomego*/
        if((recentlyHitObject == null || ((BoundingBox)recentlyHitObject
                .getWorldBound()).getMax(null).y > ((BoundingBox)b.getWorldBound())
                .getMax(null).y) && attachedObject == null)
            recentlyHitObject = b;
    }
    
    /**
     * Podnosi hak. 
     */
    public void heighten(){
        changeHookPosition(new Vector3f(1f, actualLowering -= speed, 1f),
                true);
    }
    
    /**
     * Łączy hak z dotknieym obiektem. Może połączyć hak z linami pionowo lub 
     * poziomo. 
     * @param vertical true łączy pionowo, false poziomo 
     */
    public void attach(boolean vertical){
        if(buildingMaterialJoint == null){
            attachedObject = recentlyHitObject;
            if(!vertical) joinObject(false, 1, null, 0.2f);
            else joinObject(true, 2, new Quaternion(-1.570796f, 0, 0, 1.570796f), 2.5f);
        }
    }
    
    /**
     * Odłącza od haka przyczepiony obiekt. 
     */
    public void detach(){
        if(attachedObject != null){
            BuildingSimulator.getBuildingSimulator().getBulletAppState().getPhysicsSpace()
                    .remove(buildingMaterialJoint);
            Wall wall = (Wall)attachedObject;
            wall.swapControl(0);
            wall.activateIfInactive();
            attachedObject = null;
            buildingMaterialJoint = null; 
        }
    }
    
    @Override
    public Spatial getRecentlyHitObject(){ return recentlyHitObject; }
    
    @Override
    public void setRecentlyHitObject(Spatial object){ recentlyHitObject = object; }
    
    /**
     * Zwraca wartość określającą jak bardzo opuszczony jest hak. 
     * @return wartość opuszczenia haka 
     */
    public float getActualLowering(){ return actualLowering; }
    
    /**
     * Zwraca uchwyt do jakiego przyczepiona jest lina. 
     * @return uchwyt liny
     */
    public Spatial getHookHandle(){ return hookHandle; }
    
    /**
     * Zwraca węzeł z hakiem z liniami. 
     * @return węzeł z hakiem z liniami 
     */
    public Node getRopeHook(){ return ropeHook; }
    
    /**
     * Zwraca zaczepiony obiekt. 
     * @return zaczepiony obiekt 
     */
    public Spatial getAttachedObject() { return attachedObject; }
    
    /**
     * Zwraca przesunięcie haka po opuszczeniu liny. 
     * @return przesunięcie haka 
     */
    public Vector3f getHookDisplacement() { return hookDisplacement; }
    
    /**
     * Ustawia przesunięcie haka po opuszczeniu liny. 
     * @param displacement przesunięcie haka 
     */
    public void setHookDisplacement(Vector3f displacement){
        this.hookDisplacement = displacement;
    }
    
    /**
     * Zwraca hak. 
     * @return hak 
     */
    public Spatial getHook() { return hook; }
    
    /**
     * Zwraca słuchacza dla sprawdzający kolizje z hakiem od dołu. 
     * @return słuchacz dla kolizji od dołu 
     */
    public BottomCollisionListener getCollisionListener(){ return collisionListener; }
    
    /**
     * Opuszcza hak, jeśli nic nie znajduje się pod nim. W przeciwnym razie 
     * hak nie zmienia pozycji. 
     * @param results wyniki kolizji, dzięki którym sprawdza się w ilu punktach 
     * jest kolizja z danym obiektem 
     */
    protected void lower(CollisionResults results){
        if(recentlyHitObject == null){
            if(attachedObject != null){
                Spatial hitObjectByAttachedObject = ((Wall)attachedObject)
                        .getRecentlyHitObject(); 
                if(hitObjectByAttachedObject != null){
                    new Ray(attachedObject.getWorldTranslation(), new Vector3f(0, 
                            -((BoundingBox)attachedObject.getWorldBound()).getYExtent()
                            - 0.1f, 0)).collideWith((BoundingBox)hitObjectByAttachedObject
                            .getWorldBound(), results); // -0.1 aby zmniejszyć przerwę
                }
            }
        }
        // obniża hak, jeśli w żadnym punkcie z dołu nie dotyka jakiegoś obiektu
        if(results.size() == 0){
            changeHookPosition(new Vector3f(1f, actualLowering += speed, 1f),
                    false);
            recentlyHitObject = null;
        }
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
    protected  void addHookPhysics(CompoundCollisionShape ropeHookCompound,
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
     * Podnosi lub opuszcza hak. 
     * @param scallingVector wektor o jaki ma być przesunięty hak 
     * @param heightening true jeśli podnosimy hak, false w przeciwnym przypadku 
     */
    protected void changeHookPosition(Vector3f scallingVector, boolean heightening){
        if(attachedObject != null){
            moveWithScallingObject(heightening, hookDisplacement, scallingVector, 
                    getRopes(), hook, attachedObject);
        }else{
            moveWithScallingObject(heightening, hookDisplacement, scallingVector, 
                    getRopes(), hook);
        }
        createRopeHookPhysics();
    }
    
    /**
     * Metoda abstrakcyjna którą musi nadpisać każda klasa haka, aby określić 
     * dodatkowe zasady sprawdzania kolizji podczas opuszczania haka. 
     */
    protected abstract void lower();
    
    /**
     * Tworzy fizykę dla całego haka wraz z linami go trzymającymi. 
     */
    protected abstract void createRopeHookPhysics();
    
    /**
     * Zwraca tablicę wszystkich lin trzymających hak. 
     * @return tablica lin 
     */
    protected abstract Node[] getRopes();
    
    private void joinObject(boolean vertical, int mode, Quaternion rotation, float y){
        float distanceBetweenHookAndObject = + ((BoundingBox)hook.getWorldBound())
                    .getYExtent();
        Quaternion turnToCrane = GameManager.getUnit(0).getArmControl()
                        .getCraneControl().getWorldRotation();
        if(rotation != null) turnToCrane.multLocal(rotation);
        RigidBodyControl selectedControl = ((Wall)attachedObject).swapControl(mode);
        selectedControl.setPhysicsRotation(rotation);
        BoundingBox objectBounding = (BoundingBox)attachedObject.getWorldBound();
        Vector3f distanceBetweenHookAndObjectCenter;
        if(objectBounding.getYExtent() < objectBounding.getZExtent()){
            distanceBetweenHookAndObjectCenter = vertical ? 
                    new Vector3f(0,0,objectBounding.getZExtent() 
                    + distanceBetweenHookAndObject) : new Vector3f(0,objectBounding
                    .getYExtent() + distanceBetweenHookAndObject, 0);
        } else {
            distanceBetweenHookAndObjectCenter = vertical ? 
                    new Vector3f(0,0,objectBounding.getYExtent() 
                    + distanceBetweenHookAndObject) : new Vector3f(0,objectBounding
                    .getZExtent() + distanceBetweenHookAndObject, 0);
        }
        buildingMaterialJoint = new HingeJoint(hook.getControl(RigidBodyControl.class),
                selectedControl, Vector3f.ZERO, distanceBetweenHookAndObjectCenter,
                Vector3f.ZERO, Vector3f.ZERO);
        BuildingSimulator.getBuildingSimulator().getBulletAppState()
                .getPhysicsSpace().add(buildingMaterialJoint);
        float height = gapBetweenHookAndAttachedObject + (vertical ? 
                objectBounding.getZExtent() * 2 : objectBounding.getYExtent() * 2); 
        selectedControl.setPhysicsLocation(selectedControl.getPhysicsLocation()
                .clone().setY(y));
        float yHook = hook.getWorldTranslation().y;
        while(yHook <= height){
            heighten();
            yHook += hookDisplacement.y;
        }
    }
}
