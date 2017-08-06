package buildingmaterials;

import buildingsimulator.BottomCollisionListener;
import buildingsimulator.BuildingSimulator;
import buildingsimulator.GameManager;
import static buildingsimulator.GameManager.addNewCollisionShapeToCompound;
import buildingsimulator.RememberingRecentlyHitObject;
import com.jme3.bounding.BoundingBox;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.collision.shapes.CompoundCollisionShape;
import com.jme3.bullet.collision.shapes.infos.ChildCollisionShape;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.bullet.util.CollisionShapeFactory;
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
     * Określa czy ściana jest obrócona wokół osi Y o ok. 90 stopni. 
     * @return true jeśli ściana jest obrócona, false w przeciwnym przypadku 
     */
    public boolean checkPerpendicularity(Wall wall){
        float yAngleOther = Math.abs(wall.getWorldRotation().toAngles(null)[1]),
                yAngle = Math.abs(getWorldRotation().toAngles(null)[1]);
        return yAngleOther - FastMath.QUARTER_PI > yAngle 
                && FastMath.QUARTER_PI + yAngleOther < FastMath.TWO_PI - yAngle;
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
        if(recentlyHitObject == null || recentlyHitObject.getWorldTranslation().y
                > b.getWorldTranslation().y)
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
        Vector3f leftBottom = getWorldTranslation().subtract(-length, -width, -height);
        switch(pointNumber){
            case 0:
                return vertical ? leftBottom.subtract(0f, leftBottom.y, 0f) : leftBottom;
            case 1: 
                return vertical ? leftBottom.subtract(length * 2, leftBottom.y, 0f)
                        : leftBottom.subtract(length * 2, 0f, 0);
            case 2:
                return leftBottom.subtract(0f, 0f, height * 2);
            case 3: 
                return leftBottom.subtract(length * 2, 0f, height * 2); 
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
        float ropeLenght = start.distance(end);
        if(!vertical) distanceToHandle = end.y - getWorldTranslation().y; 
        else distanceToHandleVertical = end.z - getWorldTranslation().z;
        for(int i = 0; i < ropes.length; i++){
            ropes[i] = new Geometry("Cylinder" + i, new Cylinder(4, 8, 0.02f, ropeLenght));
            ropesLocations[i] = FastMath.interpolateLinear(0.5f, start, end);
            ropes[i].setMaterial(ropeMaterial); 
            attachChild(ropes[i]);
            start = getProperPoint(i + 1, vertical);
            ropes[i].setCullHint(CullHint.Always);
        }          
        CompoundCollisionShape wallRopesShape = GameManager.createCompound(this, "Box");
        
        //CompoundCollisionShape wallRopesShape = new CompoundCollisionShape(); 
       // for(int i = 0; i < 1; i++){
            //addNewCollisionShapeToCompound(compound, parent, children[i], 
              //      Vector3f.ZERO, null);
            
         /*   Geometry parentGeometry = (Geometry)this.getChild(elementsName[i]);
            CollisionShape elementCollisionShape;
            System.out.println(parentGeometry); 
            if(!(parentGeometry instanceof CSGGeometry))
        elementCollisionShape = CollisionShapeFactory.createBoxShape(parentGeometry);
        else
            elementCollisionShape = CollisionShapeFactory
                    .createDynamicMeshShape(((CSGGeometry)parentGeometry).asSpatial());
            elementCollisionShape.setScale(parentGeometry.getWorldScale()); 
            wallRopesShape.addChildShape(elementCollisionShape, Vector3f.ZERO);
        }*/
        
        
        RigidBodyControl controlAttaching = new RigidBodyControl(wallRopesShape, 0.00001f);
        addControl(controlAttaching); 
        BuildingSimulator.getBuildingSimulator().getBulletAppState()
                .getPhysicsSpace().add(controlAttaching);
        controlAttaching.setAngularFactor(0);
        controlAttaching.setCollisionGroup(5);
        Vector3f physicsLocation = controlAttaching.getPhysicsLocation();
        for(int i = 0; i < ropes.length; i++){
            ropes[i].setLocalTranslation(ropesLocations[i].subtract(physicsLocation));
            ropes[i].lookAt(end, Vector3f.UNIT_Y);
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
        float x = length * 2, z = height * 2, zSouthParts = -length + height,
                zNorthParts = length - height; 
        addNode("Bottom", new Vector3f(0, width, -height + width), this); 
        addNode("Up", new Vector3f(0, width, height - width), this); 
        addNode("Right", new Vector3f(-length + width, width, 0), this); 
        addNode("Left", new Vector3f(length - width, width, 0f), this); 
        addNode("Center", new Vector3f(0, 0, 0), this); 
        Node south = addNode("South", new Vector3f(0, width, -z), this),
                north = addNode("North", new Vector3f(0, width, z), this); 
        addNode("East", new Vector3f(-x, width, 0), this); 
        addNode("West", new Vector3f(x, width, 0), this);
        addNode("South0", new Vector3f(height, width, zSouthParts), south); 
        addNode("South1", new Vector3f(-height, width, zSouthParts), south); 
        addNode("North0", new Vector3f(height, width, zNorthParts), north);
        addNode("North1", new Vector3f(-height, width, zNorthParts), north); 
    }
    
    private Node addNode(String name, Vector3f location, Node parent){
        Node node = new Node(name); 
        node.setLocalTranslation(location);
        parent.attachChild(node); 
        return node; 
    }
}
