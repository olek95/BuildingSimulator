package building;

import listeners.BottomCollisionListener;
import buildingsimulator.BuildingSimulator;
import buildingsimulator.GameManager;
import buildingsimulator.RememberingRecentlyHitObject;
import com.jme3.bounding.BoundingBox;
import com.jme3.bullet.collision.shapes.CompoundCollisionShape;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Cylinder;
import java.util.List;
import net.wcomohundro.jme3.csg.CSGGeometry;
import net.wcomohundro.jme3.csg.CSGShape;
import building.CatchNode.*;
import buildingsimulator.ElementName;
import buildingsimulator.PhysicsManager;
import com.jme3.export.InputCapsule;
import com.jme3.export.JmeExporter;
import com.jme3.export.JmeImporter;
import com.jme3.export.OutputCapsule;
import com.jme3.export.Savable;
import com.jme3.renderer.queue.RenderQueue.ShadowMode;
import cranes.CraneAbstract;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Obiekt klasy <code>Wall</code> reprezentuje ścianę. Obiekt ten może 
 * służyć do budowy budynków. Każda ściana ma dodatkowe punkty, służące jako miejsca
 * połączeń z innymi ścianami i podłogami. Ściana może zawierać puste miejsca
 * przeznaczone na takie elementy jak np. okna. 
 * @author AleksanderSklorz
 */
final public class Wall extends AbstractWall implements RememberingRecentlyHitObject{
    private Spatial recentlyHitObject;
    private static BottomCollisionListener collisionListener = null; 
    private Geometry[] ropesHorizontal = new Geometry[4], ropesVertical = new Geometry[2];
    private float yExtend, zExtend, xExtend, distanceToHandle, distanceToHandleVertical; 
    private static int counter = 0; 
    private WallMode actualMode;
    private List<Spatial> hitObjects = new ArrayList();
    private boolean stale = false, protrudingCatched = false; 
    private Vector3f catchingLocation; 
    private Quaternion catchingRotation; 
    private WallType type;
    private ColorRGBA initialColor; 
    
    /*
     * Konstruktor bezparametrowy jest potrzebny podczas wczytywania ściany z
     * pliku zapisu gry. 
     */
    public Wall(){}
    
    public Wall(WallType type, CSGShape shape, Vector3f location, float mass, ColorRGBA color,
            CSGShape... differenceShapes){
        super(type, shape, location, mass, ElementName.WALL_BASE_NAME + counter, color,
                differenceShapes);
        this.type = type; 
        this.initialColor = color;
        BoundingBox bounding = (BoundingBox)shape.getWorldBound();
        yExtend = bounding.getYExtent(); 
        zExtend = bounding.getZExtent(); 
        xExtend = bounding.getXExtent(); 
        initCollisionListener(); 
        createWallNodes(); 
        createAttachingControl(location, false); 
        createAttachingControl(location, true); 
        swapControl(WallMode.LOOSE);
        counter++;
        setShadowMode(ShadowMode.Cast);
    }
    
    /**
     * Aktywuje aktualnie używaną fizykę obiektu, jeśli nie jest ona aktywna w 
     * danej chwili (czyli jeśli obiekt się nie porusza). 
     */
    public void activateIfInactive(){
        for(int i = 0; i < 3; i++){
            RigidBodyControl control = (RigidBodyControl)getControl(i); 
            if(control.isEnabled()) control.activate();
        }
    }
    
    /**
     * Zamienia aktualną fizykę. Jeśli LOOSE, to obiekt otrzymuje fizykę dla 
     * obiektu nieprzyczepionego (luźnego), jeśtli HORIZONTAL, to obiekt otrzymuje 
     * fizykę dla obiektu przyczepionego do haka poziomo, natomiast jeśli VERTICAL to 
     * obiekt otrzymuje fizykę dla obiektu przyczepionego do haka pionowo. 
     * @param mode typ fizyki: LOOSE - nieprzyczepiony, HORIZONTAL - przyczepiony
     * poziomo, VERTICAL - przyczepiony pionowo
     * @return fizyka dla aktualnego stanu obiektu (obiekt luźny, przyczepiony 
     * pionowo lub poziomo)
     */
    public RigidBodyControl swapControl(WallMode mode){
        RigidBodyControl selectedControl = null;
        actualMode = mode; 
        WallMode[] modes = WallMode.values();
        for(int i = 0; i < modes.length; i++){
            if(modes[i].equals(mode)){
                selectedControl = ((RigidBodyControl)getControl(i));
                selectedControl.setEnabled(true);
            }else ((RigidBodyControl)getControl(i)).setEnabled(false);
        }
        if(mode.equals(WallMode.LOOSE)) setRopesVisibility(ropesHorizontal[0].getCullHint()
                .equals(CullHint.Never) ? ropesHorizontal : ropesVertical ,false); 
        else if(mode.equals(WallMode.HORIZONTAL)) setRopesVisibility(ropesHorizontal, true); 
        else setRopesVisibility(ropesVertical, true);  
        return selectedControl; 
    }
    
    /**
     * Obraca ścianę w kierunku dźwigu do którego jest przyczepiona. 
     */
    public void rotateToCrane(){
        CraneAbstract crane = GameManager.getActualUnit();
        Quaternion turnToCrane = crane.getArmControl()
                        .getCraneControl().getWorldRotation().clone();
        if(actualMode.equals(WallMode.VERTICAL)) {
            turnToCrane.multLocal(new Quaternion(-1.570796f, 0, 0, 1.570796f));
            if(!crane.getCrane().getName().contains(ElementName.CRANE)) 
                    turnToCrane.multLocal(new Quaternion(0, 0, 0.2f, 0.0f));
        } else {
            if(!crane.getCrane().getName().contains(ElementName.CRANE)) 
                turnToCrane.multLocal(new Quaternion(0, 0.2f, 0, 0.0f));
        }
        ((RigidBodyControl)getControl(actualMode.ordinal())).setPhysicsRotation(turnToCrane);
    }
    
    /**
     * Włacza słuchacza dolnej kolizji dla obecnego obiektu. 
     */
    public void runCollisionListener(){
        collisionListener.setHittingObject(this);
        collisionListener.setHittingObjectName(name);
    }
    
    /**
     * Określa czy ściana jest obrócona wokół osi Y o ok. 90 stopni, czyli czy 
     * jest do niej prostopadła. 
     * @param wall ściana do której jest dodawany nowy element 
     * @return true jeśli ściana jest obrócona prostopadle, false w przeciwnym przypadku 
     */
    public boolean checkPerpendicularity(Wall wall){
        float yAngleOther = Math.abs(wall.getWorldRotation().toAngles(null)[1]),
                yAngle = Math.abs(getWorldRotation().toAngles(null)[1]);
        return yAngleOther - FastMath.QUARTER_PI > yAngle && yAngleOther - FastMath.PI
                + FastMath.QUARTER_PI < yAngle || yAngleOther + FastMath.QUARTER_PI 
                < yAngle && yAngleOther + FastMath.PI - FastMath.QUARTER_PI > yAngle;
    }
    
    /**
     * Tworzy słuchacza kolizji od dolnej krawędzi. 
     */
    public void initCollisionListener(){
        if(collisionListener == null){
            collisionListener = new BottomCollisionListener(this, name, 
                    ElementName.ROPE_HOOK);
            BuildingSimulator.getPhysicsSpace().addCollisionGroupListener(collisionListener, 5);
        }
    }
    
    @Override
    public void write(JmeExporter ex) throws IOException {
        super.write(ex);
        OutputCapsule capsule = ex.getCapsule(this);
        capsule.write(ropesHorizontal, "HORIZONTAL_ROPES", null);
        capsule.write(ropesVertical, "VERTICAL_ROPES", null);
        capsule.write(distanceToHandle, "DISTANCE_TO_HANDLE", 0f);
        capsule.write(distanceToHandleVertical, "DISTANCE_TO_HANDLE_VERTICAL", 0f);
        capsule.write(catchingLocation, "CATCHING_LOCATION", null);
        capsule.write(catchingRotation, "CATCHING_ROTATION", null);
        capsule.write(actualMode, "ACTUAL_MODE", null);
        capsule.write(xExtend, "LENGTH", 0f);
        capsule.write(zExtend, "HEIGHT", 0f);
        capsule.write(yExtend, "WIDTH", 0f);
        capsule.write(protrudingCatched, "PROTRUDING_CATCHED", false);
        capsule.write(type, "TYPE", null);
     }
     
    @Override
    public void read(JmeImporter im) throws IOException {
        super.read(im);
        InputCapsule capsule = im.getCapsule(this);
        Savable[] ropes1 = capsule.readSavableArray("HORIZONTAL_ROPES", null),
                ropes2 = capsule.readSavableArray("VERTICAL_ROPES", null);
        for(int i = 0; i < ropes1.length; i++) {
            ropesHorizontal[i] = (Geometry)ropes1[i];
        }
        for(int i = 0; i < ropes2.length; i++) {
            ropesVertical[i] = (Geometry)ropes2[i];
        }
        distanceToHandle = capsule.readFloat("DISTANCE_TO_HANDLE", 0f);
        distanceToHandleVertical = capsule.readFloat("DISTANCE_TO_HANDLE_VERTICAL", 0f);
        catchingLocation = (Vector3f)capsule.readSavable("CATCHING_LOCATION", null);
        catchingRotation = (Quaternion)capsule.readSavable("CATCHING_ROTATION", null);
        actualMode = capsule.readEnum("ACTUAL_MODE", WallMode.class, null);
        xExtend = capsule.readFloat("LENGTH", 0f);
        zExtend = capsule.readFloat("HEIGHT", 0f);
        yExtend = capsule.readFloat("WIDTH", 0f);
        protrudingCatched = capsule.readBoolean("PROTRUDING_CATCHED", false);
        type = capsule.readEnum("TYPE", WallType.class, null);
        initialColor = (ColorRGBA)((CSGGeometry)getChild(ElementName.WALL_GEOMETRY))
                .getMaterial().getParam("Color").getValue();
        counter++;
     }
    
    /**
     * Określa czy ściana jest ruchoma. Jeśli true to porusza się zawsze np. gdy 
     * spada po odczepieniu od haka, natomiast gdy jest false to porusza się tylko 
     * po uderzeniu, w każdym innym przypadku stoi nieruchomo np. w trakcie budowania 
     * budowli. 
     * @param notDamped true jeśli ruch nie ma być tłumiony, false w przeciwnym razie
     */
    public void setMovable(boolean notDamped){
        RigidBodyControl control = getControl(RigidBodyControl.class);
        int value = notDamped ? 0 : 1; 
        control.setAngularDamping(value);
        control.setLinearDamping(value);
    }
    
    /**
     * Przełącza stan ściany pomiędzy ścianą połączoną z budynkiem (oryginany kolor) 
     * i ścianą po odłączeniu z budynku, po zniszczeniu (czerwoną).
     * @param stale true jeśli ściana jest w stanie po zburzeniu budynku, false 
     * w przeciwnym przypadku 
     */
    public void setStale(boolean stale){
        ((CSGGeometry)getChild(ElementName.WALL_GEOMETRY)).getMaterial().setColor("Color", stale ?
                ColorRGBA.Red : initialColor);
        this.stale = stale; 
    }
    
    /**
     * Określa czy ściana jest zniszczona. 
     * @return false gdy ściana nie jest zniszczona, true w stanie po 
     * zniszczeniu budynku 
     */
    public boolean isStale(){ return stale; }
    
    /**
     * Zwraca największy wymiar (długość lub wysokość). 
     * @return największy wymiar 
     */
    public float getMaxSize(){ return xExtend > zExtend ? xExtend : zExtend; }
    
    /**
     * Zwraca szerokość ściany (mierzona od środka). 
     * @return szerokość 
     */
    public float getYExtend(){ return yExtend; }
    
    /**
     * Zwraca wysokość ściany (mierzona od środka). 
     * @return wysokość 
     */
    public float getZExtend(){ return zExtend; }
    
    /**
     * Zwraca długość ściany (mierzona od środka).
     * @return długość 
     */
    public float getXExtend() { return xExtend; }
    
    /**
     * Zwraca aktualny tryb obiektu (LOOSE - nieprzyczepiony, HORIZONTAL -
     * przyczepiony poziomo, VERTICAL - przyczepiony pionowo)
     * @return tryb obiektu 
     */
    public WallMode getActualMode(){ return actualMode; }
    
    /**
     * Zwraca listę dotkniętych obiektów. 
     * @return lista dotkniętych obiektów 
     */
    public List<Spatial> getHitObjects(){ return hitObjects; }
    
    /**
     * Zwraca odległość między środkiem ściany a przyszłym uchwytem na którym 
     * będzie powieszona ta ściana (za pomocą lin). 
     * @param vertical true jeśli ściana podnoszona pionowo, false jeśli poziomo 
     * @return odległość między środkiem ściany a uchwytem 
     */
    public float getDistanceToHandle(boolean vertical){ 
        return vertical ? distanceToHandleVertical : distanceToHandle;  
    }
    
    /**
     * Zwraca współrzędne miejsca do którego ściana jest przymocowana. 
     * @return współrzedne miejsca do którego ściana jest przymocowana
     */
    public Vector3f getCatchingLocation() { return catchingLocation; }
    
    /**
     * Ustawia kopię współrzędnych miejsca do którego ściana jest przymocowana. 
     * @param catchingLocation współrzedne miejsca do którego ściana jest przymocowana
     */
    public void setCatchingLocation(Vector3f catchingLocation) {
        this.catchingLocation = catchingLocation.clone();
    }
    
    /**
     * Zwraca rotację dla przymocowanej ściany. 
     * @return rotacja przymocowanej ściany 
     */
    public Quaternion getCatchingRotation() { return this.catchingRotation; }
    
    /**
     * Ustawia kopię rotacji dla przymocowanej ściany 
     * @param catchingRotation rotacja przymocowanej ściany 
     */
    public void setCatchingRotation(Quaternion catchingRotation) {
        this.catchingRotation = catchingRotation.clone(); 
    }
    
    /**
     * Określa czy ściana jest przyczepiona do podłogi budynku wystająco (łączy 
     * dwie podłogi ze sobą). 
     * @return true jeśli jest przyczepiona wystająco, false w przeciwnym przypadku 
     */
    public boolean isProtrudingCatched() { return protrudingCatched; }
    
    /**
     * Ustawia czy ściana jest przyczepiona do podłogi budynku wystająco (łączy 
     * dwie podłogi ze sobą). 
     * @param protrudingCatched true jeśli jest przyczepiona wystająco,
     * false w przeciwnym przypadku 
     */
    public void setProtrudingCatched(boolean protrudingCatched) { 
        this.protrudingCatched = protrudingCatched; 
    }
    
    /**
     * Ustawia kolor poczatkowy.
     * @param initialColor kolor 
     */
    public void setInitialColor(ColorRGBA initialColor) {
        ((CSGGeometry)getChild(ElementName.WALL_GEOMETRY)).getMaterial()
                .setColor("Color", initialColor);
        this.initialColor = initialColor;
    }
    
    @Override
    public void setCollision(Spatial b){
        if(recentlyHitObject != null && !hitObjects.contains(b)) hitObjects.add(b);
        /* zabezpiecza przypadek gdy hak dotyka jednocześnie elementu pionowego
        i poziomego */
        if(recentlyHitObject == null || recentlyHitObject.getWorldTranslation().y
                > b.getWorldTranslation().y)
            recentlyHitObject = b;
    }
    
    @Override
    public Spatial getRecentlyHitObject(){ return recentlyHitObject; }
    
    @Override
    public void setRecentlyHitObject(Spatial object){ recentlyHitObject = object; }
    
    /**
     * Zwraca słuchacza sprawdzającego kolizję od dołu. 
     * @return słuchacz sprawdzający kolizję od dołu 
     */
    public BottomCollisionListener getCollisionListener() {
        return Wall.collisionListener; 
    }
    
    /**
     * Zwraca typ ściany. 
     * @return typ 
     */
    public WallType getType() { return type; }
    
    /**
     * Ustawia stan licznika ścian. 
     * @param counter stan licznika ścian
     */
    public static void setCounter(int counter) { Wall.counter = counter; }
    
    /**
     * Zwraca współrzędną wybranego rogu tego obiektu. Możliwe punkty do pobrania, 
     * to tylko te tworzące górną podstawę. Numeracja: 0 - lewy dolny róg (lub 
     * lewy brzeg pionowo), 1 - lewy górny róg (lub prawy brzeg pionowo),
     * 2 - prawy dolny róg, 3 - prawy górny róg.
     * @param pointNumber numer punktu do pobrania (0 - 3)
     * @param vertical określa ilość i miejsce rysowanych lin. Jesli true to 
     * są rysowane dwie liny z boku, jeśli false to rysowane są 4 liny od góry
     * @return współrzędne wybranego rogu 
     */
    private Vector3f getProperPoint(int pointNumber, boolean vertical){
        Vector3f leftBottom = getWorldTranslation().subtract(-xExtend, -yExtend, -zExtend);
        switch(pointNumber){
            case 0:
                return vertical ? leftBottom.subtract(0f, leftBottom.y, 0f) : leftBottom;
            case 1: 
                return vertical ? leftBottom.subtract(xExtend * 2, leftBottom.y, 0f)
                        : leftBottom.subtract(xExtend * 2, 0f, 0);
            case 2:
                return leftBottom.subtract(0f, 0f, zExtend * 2);
            case 3: 
                return leftBottom.subtract(xExtend * 2, 0f, zExtend * 2); 
            default: return null;
        }
    }
    
    private void setRopesVisibility(Geometry[] ropes, boolean visibility){
        for(int i = 0; i < ropes.length; i++) 
            ropes[i].setCullHint(visibility ? CullHint.Never : CullHint.Always);
    }
    
    private void createAttachingControl(Vector3f location, boolean vertical){
        Geometry[] ropes = vertical ? ropesVertical : ropesHorizontal; 
        Vector3f end = getWorldTranslation().clone().add(vertical ? 
                new Vector3f(0f, 0f, zExtend + 1) : new Vector3f(0f, 1f, 0f)),
                start = getProperPoint(0, vertical);
        Vector3f[] ropesLocations = new Vector3f[ropes.length];
        BuildingSimulator game = BuildingSimulator.getBuildingSimulator();
        Material ropeMaterial = new Material(game.getAssetManager(),
                "Common/MatDefs/Misc/Unshaded.j3md");
        ropeMaterial.setColor("Color", ColorRGBA.Black); 
        float ropeLength = start.distance(end);
        if(!vertical) distanceToHandle = end.y - getWorldTranslation().y; 
        else distanceToHandleVertical = end.z - getWorldTranslation().z;
        CompoundCollisionShape wallRopesShape = PhysicsManager.createCompound(this, "Box");
        RigidBodyControl controlAttaching = new RigidBodyControl(wallRopesShape, 0.00001f);
        addControl(controlAttaching); 
        game.getBulletAppState().getPhysicsSpace().add(controlAttaching);
        controlAttaching.setAngularFactor(0);
        controlAttaching.setCollisionGroup(5);
        Vector3f physicsLocation = controlAttaching.getPhysicsLocation();
        for(int i = 0; i < ropes.length; i++){
            ropes[i] = new Geometry(ElementName.LINE + i, new Cylinder(4, 8, 0.02f, ropeLength));
            ropesLocations[i] = FastMath.interpolateLinear(0.5f, start, end);
            ropes[i].setMaterial(ropeMaterial); 
            attachChild(ropes[i]);
            start = getProperPoint(i + 1, vertical);
            ropes[i].setLocalTranslation(ropesLocations[i].subtract(physicsLocation));
            ropes[i].lookAt(end, Vector3f.UNIT_Y);
            ropes[i].setCullHint(CullHint.Always);
        }          
        controlAttaching.setPhysicsLocation(location);
    }
    
    private void createWallNodes(){
        CatchNode[] nodes = CatchNode.values();
        for(int i = 0; i < nodes.length; i++) {
            addNode(nodes[i], CatchNode.calculateTranslation(nodes[i], this, null,
                    false, false, 0, false)); 
        }
    }
    
    private Node addNode(CatchNode type, Vector3f location){
        Node node = new Node(type.toString()); 
        node.setLocalTranslation(location);
        attachChild(node); 
        return node; 
    }
}
