package cranes;

import buildingmaterials.Wall;
import buildingsimulator.BottomCollisionListener;
import buildingsimulator.BuildingSimulator;
import buildingmaterials.Construction;
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
import java.util.List;

/**
 * Klasa <code>Hook</code> jest klasą abstrakcji dla wszystkich haków w grze. 
 * @author AleksanderSklorz
 */
public abstract class Hook implements RememberingRecentlyHitObject{
    private Node ropeHook;
    private Spatial hook, hookHandle, recentlyHitObject, attachedObject = null;
    private float actualLowering = 1f, speed;
    private Vector3f hookDisplacement;
    private HingeJoint lineAndHookHandleJoint = null, buildingMaterialJoint;
    private static BottomCollisionListener collisionListener = null; 
    public Hook(Node ropeHook, Spatial hookHandle, float speed){
        this.ropeHook = ropeHook;
        hook = ropeHook.getChild("hook");
        this.hookHandle = hookHandle;
        this.speed = speed;
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
            Wall wall = (Wall)attachedObject;
            Construction building = Construction.getWholeConstruction(attachedObject); 
            boolean lastAddedWall = true; 
            if(building != null){
                lastAddedWall = wall.equals(building.getLastAddedWall()); 
                if(lastAddedWall) building.removeWall(wall); 
            }
            if(lastAddedWall){
                if(!vertical) joinObject(false, 1, wall.getWidth());
                else joinObject(true, 2, wall.getHeight());
            }else attachedObject = null; 
        }
    }
    
    /**
     * Odłącza od haka przyczepiony obiekt. 
     */
    public void detach(boolean merging){
        if(attachedObject != null){
            BuildingSimulator.getBuildingSimulator().getBulletAppState().getPhysicsSpace()
                    .remove(buildingMaterialJoint);
            Wall wall = (Wall)attachedObject;
            int oldMode = wall.getActualMode(); 
            wall.swapControl(0);
            wall.activateIfInactive();
            if(merging){
                Spatial wallRecentlyHitObject = wall.getRecentlyHitObject(); 
                if(wallRecentlyHitObject != null){
                    Wall nearestBuildingWall = null; 
                    if(oldMode == 1 && wallRecentlyHitObject.getName().equals("New Scene"))
                        nearestBuildingWall = Construction.getNearestBuildingWall(wall);
                    Construction construction = Construction
                            .getWholeConstruction(nearestBuildingWall == null ? 
                            wallRecentlyHitObject : nearestBuildingWall); 
                    if(construction == null){
                        construction = new Construction();
                        GameManager.addToGame(construction);
                    } 
                    construction.add(wall, nearestBuildingWall, oldMode);
                }
            }
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
    
    @Override
    public Vector3f getWorldTranslation(){ return hook.getWorldTranslation(); }
    
    /**
     * Opuszcza hak, jeśli nic nie znajduje się pod nim. W przeciwnym razie 
     * hak nie zmienia pozycji. 
     * @param results wyniki kolizji, dzięki którym sprawdza się w ilu punktach 
     * jest kolizja z danym obiektem 
     */
    protected void lower(CollisionResults results){
        boolean hitOtherWall = false; 
        if(recentlyHitObject == null){
            if(attachedObject != null){
                Wall wall = (Wall)attachedObject; 
                Spatial hitObjectByAttachedObject = wall.getRecentlyHitObject(); 
                if(hitObjectByAttachedObject != null){
                    new Ray(attachedObject.getWorldTranslation(), new Vector3f(0, 
                            -((BoundingBox)attachedObject.getWorldBound()).getYExtent()
                            - 0.1f, 0)).collideWith((BoundingBox)hitObjectByAttachedObject
                            .getWorldBound(), results); // -0.1 aby zmniejszyć przerwę
                    List<Spatial> hitObjects = wall.getHitObjects();
                    int hitObjectsNumber = hitObjects.size(); 
                    for(int i = 0; i < hitObjectsNumber && !hitOtherWall; i++)
                        if(hitObjects.get(i).getName().startsWith("Wall"))
                            hitOtherWall = true; 
                }
            }
        }
        // obniża hak, jeśli w żadnym punkcie z dołu nie dotyka jakiegoś obiektu
        if(results.size() == 0 && !hitOtherWall){
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
    
    private void joinObject(boolean vertical, int mode, float y){
        Wall wall = (Wall)attachedObject; 
        float distanceBetweenHookAndObject = calculateDistanceBetweenHookAndObject(vertical);
        RigidBodyControl selectedControl = wall.swapControl(mode);
        wall.rotateToCrane();
        BoundingBox objectBounding = (BoundingBox)attachedObject.getWorldBound();
        Vector3f distanceBetweenHookAndObjectCenter;
        if(objectBounding.getYExtent() < objectBounding.getZExtent()){
            distanceBetweenHookAndObjectCenter = vertical ? 
                    new Vector3f(0,0, distanceBetweenHookAndObject) : new Vector3f(0, 
                    distanceBetweenHookAndObject, 0);
        } else {
            distanceBetweenHookAndObjectCenter = vertical ? 
                    new Vector3f(0,0, distanceBetweenHookAndObject) : new Vector3f(0, 
                    distanceBetweenHookAndObject, 0);
        }
        buildingMaterialJoint = new HingeJoint(hook.getControl(RigidBodyControl.class),
                selectedControl, Vector3f.ZERO, distanceBetweenHookAndObjectCenter,
                Vector3f.ZERO, Vector3f.ZERO);
        BuildingSimulator.getBuildingSimulator().getBulletAppState()
                .getPhysicsSpace().add(buildingMaterialJoint);
        // +0.1 w przypadku żurawia, aby nie było kolizji z obiektami pod tym obiektem
        float height = wall.getDistanceToHandle(vertical) + wall.getWorldTranslation().y
                + (vertical ? wall.getHeight() + 0.1f : wall.getWidth()),
                yHook = hook.getWorldTranslation().y;
        while(yHook <= height){
            heighten();
            yHook += hookDisplacement.y;
        }
        wall.runCollisionListener();
    }
    
    private float calculateDistanceBetweenHookAndObject(boolean vertical){
        Wall wall = (Wall)attachedObject; 
        float distanceBetweenHookAndObject = wall.getDistanceToHandle(vertical);
        Node parent = hook.getParent();
        do{
            if(parent.getName().contains("dzwig"))
                distanceBetweenHookAndObject += ((BoundingBox)hook.getWorldBound())
                        .getYExtent(); 
            parent = parent.getParent();
        }while(parent != null);
        return distanceBetweenHookAndObject; 
    }
}
