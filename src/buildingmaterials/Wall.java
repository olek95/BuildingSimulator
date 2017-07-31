package buildingmaterials;

import buildingsimulator.BottomCollisionListener;
import buildingsimulator.BuildingSimulator;
import buildingsimulator.GameManager;
import buildingsimulator.RememberingRecentlyHitObject;
import com.jme3.bounding.BoundingBox;
import com.jme3.bullet.collision.shapes.CompoundCollisionShape;
import com.jme3.bullet.collision.shapes.infos.ChildCollisionShape;
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
import com.jme3.texture.Texture;
import java.util.List;
import net.wcomohundro.jme3.csg.CSGGeometry;
import net.wcomohundro.jme3.csg.CSGShape;

/**
 * Obiekt klasy <code>Wall</code> reprezentuje kawałek ściany. Obiekt ten może 
 * służyć do budowy budynków. Każda ściana ma dodatkowe punkty, służące jako miejsca
 * połączeń z innymi ścianami i podłogami. 
 * @author AleksanderSklorz
 */
final public class Wall extends Node implements RememberingRecentlyHitObject{
    private Spatial recentlyHitObject;
    private static BottomCollisionListener collisionListener = null; 
    private Geometry[] ropesHorizontal = new Geometry[4], ropesVertical = new Geometry[2];
    private float width, height, length, distanceToHandle, distanceToHandleVertical; 
    private static int counter = 0; 
    private int actualMode;
    @SuppressWarnings("LeakingThisInConstructor")
    public Wall(CSGShape shape, Vector3f location, CSGShape... differenceShapes){
        BoundingBox bounding = (BoundingBox)shape.getWorldBound();
        width = bounding.getYExtent(); 
        height = bounding.getZExtent(); 
        length = bounding.getXExtent(); 
        initShape(shape, differenceShapes);
        initCollisionListener(); 
        ((CSGGeometry)getChild(0)).regenerate();
        createWallNodes(); 
        createLooseControl(location); 
        createAttachingControl(location, false); 
        createAttachingControl(location, true); 
        swapControl(0);
        counter++;
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
     * Zamienia aktualną fizykę. Jeśli 0, to obiekt otrzymuje fizykę dla 
     * obiektu nieprzyczepionego (luźnego), jeśtli 1, to obiekt otrzymuje 
     * fizykę dla obiektu przyczepionego do haka poziomo, natomiast jeśli 2 to 
     * obiekt otrzymuje fizykę dla obiektu przyczepionego do haka pionowo. 
     * @param mode typ fizyki: 0 - nieprzyczepiony, 1 - przyczepiony poziomo,
     * 2 - przyczepiony pionowo
     * @return fizyka dla aktualnego stanu obiektu (obiekt luźny, przyczepiony 
     * pionowo lub poziomo)
     */
    public RigidBodyControl swapControl(int mode){
        RigidBodyControl selectedControl = null;
        actualMode = mode; 
        for(int i = 0; i < 3; i++){
            if(i == mode){
                selectedControl = ((RigidBodyControl)getControl(i));
                selectedControl.setEnabled(true);
            }else ((RigidBodyControl)getControl(i)).setEnabled(false);
        }
        if(mode == 0) setRopesVisibility(ropesHorizontal[0].getCullHint()
                .equals(CullHint.Never) ? ropesHorizontal : ropesVertical ,false); 
        else if(mode == 1) setRopesVisibility(ropesHorizontal, true); 
        else setRopesVisibility(ropesVertical, true);  
        return selectedControl; 
    }
    
    /**
     * Obraca ścianę w kierunku dźwigu do którego jest przyczepiona. 
     */
    public void rotateToCrane(){
        RigidBodyControl control;
        int i = -1; 
        do{
            control = (RigidBodyControl)getControl(++i); 
        }while(!control.isEnabled());
        Quaternion turnToCrane = GameManager.findActualUnit().getArmControl()
                        .getCraneControl().getWorldRotation().clone();
        if(i == 2) turnToCrane.multLocal(new Quaternion(-1.570796f, 0, 0, 1.570796f));
        control.setPhysicsRotation(turnToCrane);
    }
    
    /**
     * Włacza słuchacza dolnej kolizji dla obecnego obiektu. 
     */
    public void runCollisionListener(){
        collisionListener.setHittingObject(this);
        collisionListener.setHittingObjectName(name);
    }
    
    /**
     * Zwraca szerokość ściany (mierzona od środka). 
     * @return szerokość 
     */
    public float getWidth(){ return width; }
    
    /**
     * Zwraca wysokość ściany (mierzona od środka). 
     * @return wysokość 
     */
    public float getHeight(){ return height; }
    
    /**
     * Zwraca długość ściany (mierzona od środka).
     * @return długość 
     */
    public float getLength() { return length; }
    
    /**
     * Zwraca aktualny tryb obiektu (0 - nieprzyczepiony, 1 - przyczepiony poziomo,
     * 2 - przyczepiony pionowo)
     * @return tryb obiektu 
     */
    public int getActualMode(){ return actualMode; }
    
    /**
     * Zwraca odległość między środkiem ściany a przyszłym uchwytem na którym 
     * będzie powieszona ta ściana (za pomocą lin). 
     * @param vertical true jeśli ściana podnoszona pionowo, false jeśli poziomo 
     * @return odległość między środkiem ściany a uchwytem 
     */
    public float getDistanceToHandle(boolean vertical){ 
        return vertical ? distanceToHandleVertical : distanceToHandle;  
    }
    
    @Override
    public void setCollision(Spatial b){
        /* zabezpiecza przypadek gdy hak dotyka jednocześnie elementu pionowego
        i poziomego */
        if((recentlyHitObject == null || recentlyHitObject.getWorldTranslation().y
                > b.getWorldTranslation().y))
            recentlyHitObject = b;
    }
    
    @Override
    public Spatial getRecentlyHitObject(){
        return recentlyHitObject; 
    }
    
    @Override
    public void setRecentlyHitObject(Spatial object){
        recentlyHitObject = object; 
    }
    
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
        BoundingBox bounding = (BoundingBox)getWorldBound();
        Vector3f max = bounding.getMax(null);
        switch(pointNumber){
            case 0:
                return vertical ? max.subtract(0f, max.y, 0f) : max;
            case 1: 
                return vertical ? max.subtract(bounding.getXExtent() * 2, max.y, 0f)
                        : max.subtract(bounding.getXExtent() * 2, 0f, 0f);
            case 2:
                return max.subtract(0, 0, bounding.getZExtent() * 2);
            case 3: 
                return max.subtract(bounding.getXExtent() * 2, 0, bounding
                        .getZExtent() * 2); 
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
                new Vector3f(0f, 0f, 3f) : new Vector3f(0f, 1f, 0f)),
                start = getProperPoint(0, vertical);
        Vector3f[] ropesLocations = new Vector3f[ropes.length];
        Material ropeMaterial = new Material(BuildingSimulator.getBuildingSimulator()
                .getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
        ropeMaterial.setColor("Color", ColorRGBA.Black); 
        float length = start.distance(end);
        if(!vertical) distanceToHandle = end.y - getWorldTranslation().y; 
        else distanceToHandleVertical = end.z - getWorldTranslation().z;
        String[] elementsName = new String[ropes.length + 1];
        elementsName[0] = "Box";
        for(int i = 0; i < ropes.length; i++){
            elementsName[i + 1] = "Cylinder" + i;
            ropes[i] = new Geometry(elementsName[i + 1], new Cylinder(4, 8, 0.02f, length));
            ropesLocations[i] = FastMath.interpolateLinear(0.5f, start, end);
            ropes[i].setMaterial(ropeMaterial); 
            attachChild(ropes[i]);
            start = getProperPoint(i + 1, vertical);
            ropes[i].setCullHint(CullHint.Always);
        }          
        CompoundCollisionShape wallRopesShape = GameManager.createCompound(this, elementsName);
        RigidBodyControl controlAttaching = new RigidBodyControl(wallRopesShape, 0.00001f);
        addControl(controlAttaching); 
        BuildingSimulator.getBuildingSimulator().getBulletAppState()
                .getPhysicsSpace().add(controlAttaching);
        controlAttaching.setAngularFactor(0);
        controlAttaching.setCollisionGroup(5);
        Vector3f physicsLocation = controlAttaching.getPhysicsLocation();
        List<ChildCollisionShape> collisionShapeChildren = wallRopesShape.getChildren();
        for(int i = 0; i < ropes.length; i++){
            ropes[i].setLocalTranslation(ropesLocations[i].subtract(physicsLocation));
            ropes[i].lookAt(end, Vector3f.UNIT_Y);
            ChildCollisionShape gotShape = collisionShapeChildren.get(1 + i);
            gotShape.location = ropes[i].getLocalTranslation();
            gotShape.rotation = ropes[i].getLocalRotation().toRotationMatrix();
        }
        controlAttaching.setPhysicsLocation(location);
    }
    
    private void initShape(CSGShape shape, CSGShape... differenceShapes){
        BuildingSimulator game = BuildingSimulator.getBuildingSimulator();
        CSGGeometry wall = new CSGGeometry("Box"); 
        wall.addShape(shape);
        Material mat = new Material(game.getAssetManager(), 
                "Common/MatDefs/Misc/Unshaded.j3md");
        Texture gypsumTexture = game.getAssetManager().loadTexture("Textures/gips.jpg");
        mat.setTexture("ColorMap", gypsumTexture);
        wall.setMaterial(mat);     
        for(int i = 0; i < differenceShapes.length; i++)
            wall.subtractShape(differenceShapes[i]);
        setName("Wall" + counter);
        attachChild(wall);
    }
    
    private void initCollisionListener(){
        if(collisionListener == null){
            collisionListener = new BottomCollisionListener(this, name, "ropeHook");
            BuildingSimulator.getBuildingSimulator().getBulletAppState().getPhysicsSpace()
                    .addCollisionGroupListener(collisionListener, 5);
        }
    }
    
    private void createLooseControl(Vector3f location){
        GameManager.createObjectPhysics(this, 0.00001f, false, new String[] {"Box"});
        RigidBodyControl control = getControl(RigidBodyControl.class); 
        control.setPhysicsLocation(location);
        control.setAngularDamping(1);
        control.setLinearDamping(1);
    }
    
    private void createWallNodes(){
        addNode("Bottom", new Vector3f(0, width, -height - width)); 
        addNode("Up", new Vector3f(0, width, height + width)); 
        addNode("Right", new Vector3f(-length + width, width, 0)); 
        addNode("Left", new Vector3f(length - width, width, 0f)); 
        addNode("Center", new Vector3f(0, 0, 0)); 
        addNode("South", new Vector3f(0, width, -height * 2));
        addNode("North", new Vector3f(0, width, height * 2)); 
        addNode("East", new Vector3f(-length * 2, width, 0)); 
        addNode("West", new Vector3f(length * 2, width, 0)); 
    }
    
    private void addNode(String name, Vector3f location){
        Node node = new Node(name); 
        node.setLocalTranslation(location);
        attachChild(node); 
    }
}
