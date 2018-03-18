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
 * Umożliwia podnoszenie ścian. 
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
            BuildingSimulator.getPhysicsSpace().addCollisionGroupListener(collisionListener, 2);
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
     * @param tpf umożliwia uzależnienie prędkości podnoszenia haka od ilości klatek 
     */
    public void heighten(float tpf){
        changeHookPosition(new Vector3f(1f, actualLowering -= speed * tpf, 1f),  true, tpf);
    }
    
    /**
     * Łączy hak z dotknieym obiektem. Może połączyć hak z linami pionowo lub 
     * poziomo. 
     * @param vertical true łączy pionowo, false poziomo 
     */
    public void attach(boolean vertical){
        if(buildingMaterialJoint == null){
            attachedObject = (Wall)recentlyHitObject;
            if(!attachedObject.isStale()) {
                boolean  mayBeAttached = true; 
                Construction building = Construction.getWholeConstruction(attachedObject); 
                if(building != null){
                    mayBeAttached = attachedObject.equals(building.getLastAddedWall())
                            && !building.isSold(); 
                    if(mayBeAttached) 
                        building.removeWall(attachedObject); 
                } 
                if(mayBeAttached){
                    if(!vertical) 
                        joinObject(WallMode.HORIZONTAL);
                    else joinObject(WallMode.VERTICAL);
                }else attachedObject = null; 
            } else attachedObject = null;
        }
    }
    
    /**
     * Odłącza od haka przyczepiony obiekt. 
     * @param merging true jeśli ma nastąpić połaczenie z inną ścianą, false 
     * gdy ma spaść swobodnie 
     * @param protruding true jeśli ściana ma połączyć dwie sąsiadujące ze sobą 
     * podłogi, false w przeciwnym przypadku 
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
                        GameManager.addToScene(construction);
                    } 
                    if(!construction.isSold()) {
                        if(construction.add(attachedObject, nearestBuildingWall, oldMode, protruding)) {
                            confirmDetaching();
                        } else attachedObject.swapControl(oldMode);
                    }
                } else confirmDetaching();
            } else confirmDetaching();
        }
    }
    
    /**
     * Opuszcza hak do momentu wykrycia przeszkody. 
     * @param tpf umożiwia uzależnienie opuszczania haka od ilości klatek 
     */
    public void lower(float tpf){
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
            changeHookPosition(new Vector3f(1f, actualLowering += speed * tpf, 1f),
                    false, tpf);
            recentlyHitObject = null;
        }
    }
    
    /**
     * Dodaje do gry połączenie pomiędzy hakiem a przyczepionym do niego obiektem. 
     * @param mode pozycja przyczepianej ściany 
     */
    public void addAttachingJoint(WallMode mode) {
        attachedObject = (Wall)recentlyHitObject;
        boolean vertical = mode.equals(WallMode.VERTICAL);
        float distanceBetweenHookAndObject = calculateDistanceBetweenHookAndObject(vertical);
        RigidBodyControl selectedControl = attachedObject.swapControl(mode);
        if(GameManager.getActualUnit() != null) attachedObject.rotateToCrane();
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
        BuildingSimulator.getPhysicsSpace().add(buildingMaterialJoint);
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
     * Ustawia wartość aktualnego opuszczenia haka. 
     * @param actualLowering wartość aktualnego opuszczenia haka
     */
    public void setActualLowering(float actualLowering) { 
        this.actualLowering = actualLowering; 
    }
    
    /**
     * Zwraca uchwyt do jakiego przyczepiony jest hak. 
     * @return uchwyt haka
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
    public Wall getAttachedObject() { return attachedObject; }
    
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
        ropeHook.getControl(RigidBodyControl.class).setCollisionGroup(2); 
        lineAndHookHandleJoint = PhysicsManager.joinsElementToOtherElement(lineAndHookHandleJoint,
                hookHandle, ropeHook, Vector3f.ZERO, distanceHookHandleAndRopeHook);
    }
    
    /**
     * Przywraca fizykę haka oraz połączenie między hakiem a jego uchwytem do świata gry. 
     * @param distanceHookHandleAndRopeHook odległość między hakiem a jego uchwytem
     */
    protected void restoreHookPhysics(Vector3f distanceHookHandleAndRopeHook) {
        PhysicsManager.addPhysicsToGame(ropeHook);
        lineAndHookHandleJoint = PhysicsManager.joinsElementToOtherElement(lineAndHookHandleJoint,
                hookHandle, ropeHook, Vector3f.ZERO, distanceHookHandleAndRopeHook);
    }
    
    /**
     * Podnosi lub opuszcza hak. 
     * @param scallingVector wektor o jaki ma być przesunięty hak 
     * @param heightening true jeśli podnosimy hak, false w przeciwnym przypadku
     * @param tpf umożliwia uzależnienie prędkości zmiany pozycji haka od ilości klatek 
     */
    protected void changeHookPosition(Vector3f scallingVector, boolean heightening,
            float tpf){
        System.out.println(tpf);
        if(attachedObject != null){
            PhysicsManager.moveWithScallingObject(heightening, hookDisplacement.clone()
                    .mult(new Vector3f(1, tpf, 1)), scallingVector,  getRopes(), hook, attachedObject);
        }else{
            PhysicsManager.moveWithScallingObject(heightening, hookDisplacement.clone()
                    .mult(new Vector3f(1, tpf, 1)), scallingVector, getRopes(), hook);
        }
        createRopeHookPhysics();
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
    
    private void joinObject(WallMode mode){
        boolean vertical = mode.equals(WallMode.VERTICAL);
        // +0.1 w przypadku żurawia, aby nie było kolizji z obiektami pod tym obiektem
        float height = attachedObject.getDistanceToHandle(vertical) + attachedObject.getWorldTranslation().y
                + (vertical ? attachedObject.getZExtend() + 0.1f : attachedObject.getYExtend()),
                yHook = hook.getWorldTranslation().y;
        int i = 0;
        while(yHook <= height){
            heighten(1);
            yHook += hookDisplacement.y;
            i++;
        }
        if(actualLowering < 1) {
            for(; i > 0; i--)  {
                changeHookPosition(new Vector3f(1f, actualLowering += speed, 1f), 
                        false, 1);
            }
            attachedObject = null;
            HUD.setMessage(Translator.NO_ENOUGH_PLACE.getValue());
        } else {
            addAttachingJoint(mode);
            attachedObject.runCollisionListener();
        }
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
        BuildingSimulator.getPhysicsSpace().remove(buildingMaterialJoint);
        attachedObject = null;
        buildingMaterialJoint = null;
    }
}
