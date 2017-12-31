package cranes;

import building.Wall;
import listeners.BottomCollisionListener;
import buildingsimulator.BuildingSimulator;
import building.Construction;
import building.WallMode;
import buildingsimulator.ElementName;
import buildingsimulator.GameManager;
import buildingsimulator.PhysicsManager;
import buildingsimulator.RememberingRecentlyHitObject;
import com.jme3.bounding.BoundingBox;
import com.jme3.bullet.collision.shapes.CompoundCollisionShape;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.bullet.joints.HingeJoint;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import menu.HUD;
import texts.Translator;

/**
 * Klasa <code>Hook</code> jest klasą abstrakcji dla wszystkich haków w grze. 
 * @author AleksanderSklorz
 */
public abstract class Hook implements RememberingRecentlyHitObject{
    private Node ropeHook;
    private Spatial hook, hookHandle, recentlyHitObject;
    private Wall attachedObject = null; 
    private float actualLowering = 1f, speed;
    private Vector3f hookDisplacement;
    private HingeJoint lineAndHookHandleJoint = null, buildingMaterialJoint;
    private static BottomCollisionListener collisionListener = null; 
    public Hook(Node ropeHook, Spatial hookHandle, float speed){
        this.ropeHook = ropeHook;
        hook = ropeHook.getChild(ElementName.HOOK);
        this.hookHandle = hookHandle;
        this.speed = speed;
        if(collisionListener == null){
            collisionListener = new BottomCollisionListener(this, ElementName.ROPE_HOOK,
                    ElementName.HOOK_HANDLE);
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
        //if(b.getName().startsWith(Wall.BASE_NAME)) ((Wall)b).setMovable(false);
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
            attachedObject = (Wall)recentlyHitObject;
            boolean wallStale = attachedObject.isStale(), lastAddedWall = true; 
            if(!wallStale){
                Construction building = Construction.getWholeConstruction(attachedObject); 
                if(building != null){
                    lastAddedWall = attachedObject.equals(building.getLastAddedWall()); 
                    if(lastAddedWall && !attachedObject.isStale()) 
                        building.removeWall(attachedObject); 
                }
            }
            if(wallStale || lastAddedWall){
                if(!vertical) 
                    joinObject(false, WallMode.HORIZONTAL, attachedObject.getWidth());
                else joinObject(true, WallMode.VERTICAL, attachedObject.getHeight());
            }else attachedObject = null; 
        }
    }
    
    /**
     * Odłącza od haka przyczepiony obiekt. 
     */
    public void detach(boolean merging, boolean protruding){
        if(attachedObject != null){
            WallMode oldMode = attachedObject.getActualMode(); 
            attachedObject.swapControl(WallMode.LOOSE);
            attachedObject.activateIfInactive();
            if(merging){
                Spatial wallRecentlyHitObject = attachedObject.getRecentlyHitObject(); 
                if(wallRecentlyHitObject != null){
                    Wall nearestBuildingWall = null; 
                    if(oldMode.equals(WallMode.VERTICAL)) {
                        if(!wallRecentlyHitObject.getName().startsWith(ElementName.WALL_BASE_NAME)) { 
                            HUD.setMessage(Translator.NO_FOUNDATIONS.getValue());
                            confirmDetaching();
                            return;
                        }
                    } else {
                        if(wallRecentlyHitObject.getName().equals(ElementName.MAP_FIELD_PART_NAME)) {
                            nearestBuildingWall = Construction
                                    .getNearestBuildingWall(attachedObject);
                        }
                    }
                    Construction construction = Construction
                            .getWholeConstruction(nearestBuildingWall == null ? 
                            wallRecentlyHitObject : nearestBuildingWall); 
                    if(construction == null){
                        construction = new Construction();
                        GameManager.addToGame(construction);
                    } 
                    if(construction.add(attachedObject, nearestBuildingWall, oldMode, protruding)) {
                        confirmDetaching();
                    } else attachedObject.swapControl(oldMode);
                } else confirmDetaching();
            } else confirmDetaching();
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
        PhysicsManager.addNewCollisionShapeToCompound(ropeHookCompound, (Node)hook, ((Node)hook).getChild(0).getName(),
                hook.getLocalTranslation(), hook.getLocalRotation());
        PhysicsManager.createPhysics(ropeHookCompound, ropeHook, 4f, false);
        RigidBodyControl ropeHookControl = ropeHook.getControl(RigidBodyControl.class);
        ropeHookControl.setCollisionGroup(2); 
        lineAndHookHandleJoint = PhysicsManager.joinsElementToOtherElement(lineAndHookHandleJoint,
                hookHandle, ropeHook, Vector3f.ZERO, distanceHookHandleAndRopeHook);
    }
    
    /**
     * Podnosi lub opuszcza hak. 
     * @param scallingVector wektor o jaki ma być przesunięty hak 
     * @param heightening true jeśli podnosimy hak, false w przeciwnym przypadku 
     */
    protected void changeHookPosition(Vector3f scallingVector, boolean heightening){
        if(attachedObject != null){
            PhysicsManager.moveWithScallingObject(heightening, hookDisplacement, scallingVector, 
                    getRopes(), hook, attachedObject);
        }else{
            PhysicsManager.moveWithScallingObject(heightening, hookDisplacement, scallingVector, 
                    getRopes(), hook);
        }
        createRopeHookPhysics();
    }
    
    /**
     * Opuszcza hak do momentu wykrycia przeszkody. 
     */
    public void lower(){
        boolean hookNoCollision = collisionListener.isNothingBelow(null);
        boolean attachedObjectNoCollision = true;
        if(recentlyHitObject == null){
            if(attachedObject != null){
                attachedObjectNoCollision = ((Wall)attachedObject).getCollisionListener()
                        .isNothingBelow(new Vector3f(0, -((BoundingBox)attachedObject
                        .getWorldBound()).getYExtent() - 0.1f, 0));
            }
        }
        if(hookNoCollision && attachedObjectNoCollision) {
            changeHookPosition(new Vector3f(1f, actualLowering += speed, 1f),
                    false);
            recentlyHitObject = null;
        }
    }
    
    /**
     * Tworzy fizykę dla całego haka wraz z linami go trzymającymi. 
     */
    protected abstract void createRopeHookPhysics();
    
    /**
     * Zwraca tablicę wszystkich lin trzymających hak. 
     * @return tablica lin 
     */
    protected abstract Node[] getRopes();
    
    private void joinObject(boolean vertical, WallMode mode, float y){
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
            if(parent.getName().contains(ElementName.CRANE))
                distanceBetweenHookAndObject += ((BoundingBox)hook.getWorldBound())
                        .getYExtent(); 
            parent = parent.getParent();
        }while(parent != null);
        return distanceBetweenHookAndObject; 
    }
    
    private void confirmDetaching() {
        BuildingSimulator.getBuildingSimulator().getBulletAppState().getPhysicsSpace()
                .remove(buildingMaterialJoint);
        attachedObject = null;
        buildingMaterialJoint = null;
    }
}
