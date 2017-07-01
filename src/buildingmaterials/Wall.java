package buildingmaterials;

import buildingsimulator.BottomCollisionListener;
import buildingsimulator.BuildingSimulator;
import buildingsimulator.GameManager;
import buildingsimulator.RememberingRecentlyHitObject;
import com.jme3.bounding.BoundingBox;
import com.jme3.bullet.PhysicsSpace;
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
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Cylinder;
import com.jme3.texture.Texture;
import java.util.List;

/**
 * Obiekt klasy <code>Wall</code> reprezentuje kawałek ściany. Obiekt ten może 
 * służyć do budowy budynków. 
 * @author AleksanderSklorz
 */
final public class Wall extends Node implements RememberingRecentlyHitObject{
    private Spatial recentlyHitObject;
    private static BottomCollisionListener collisionListener = null; 
    private Geometry[] ropesHorizontal = new Geometry[4], ropesVertical = new Geometry[2];
    private static int counter = 0; 
    private Quaternion verticalRotation; 
    @SuppressWarnings("LeakingThisInConstructor")
    public Wall(Box shape, Vector3f location){
        BuildingSimulator game = BuildingSimulator.getBuildingSimulator();
        Geometry wall = new Geometry("Box", shape); 
        Material mat = new Material(game.getAssetManager(), 
                "Common/MatDefs/Misc/Unshaded.j3md");
        Texture gypsumTexture = game.getAssetManager().loadTexture("Textures/gips.jpg");
        mat.setTexture("ColorMap", gypsumTexture);
        wall.setMaterial(mat);                               
        setName("Wall" + counter);
        attachChild(wall);
        game.getRootNode().attachChild(this);
        //lookAt(new Vector3f(0, 1, 0), Vector3f.UNIT_Y);
        //System.out.println(this.getLocalRotation());
        if(collisionListener == null){
            collisionListener = new BottomCollisionListener(this, getName(), "ropeHook");
            game.getBulletAppState().getPhysicsSpace()
                    .addCollisionGroupListener(collisionListener, 5);
        }
        verticalRotation = new Quaternion(-1.570796f, 0, 0, 1.570796f);
        createAttachingControl(location, false); 
        createAttachingControl(location, true); 
        createLooseControl(location); 
        swapControl(2); 
        //((RigidBodyControl)getControl(1)).setPhysicsRotation(
          //      new Quaternion(-0.70710677f, 0.0f, 0.0f, 0.70710677f));
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
    
    @Override
    public void setCollision(Spatial b){
        /* zabezpiecza przypadek gdy hak dotyka jednocześnie elementu pionowego
        i poziomego */
        if((recentlyHitObject == null || ((BoundingBox)recentlyHitObject
                .getWorldBound()).getMax(null).y > ((BoundingBox)b.getWorldBound())
                .getMax(null).y))
            recentlyHitObject = b;
    }
    
    /**
     * Zamienia aktualną fizykę. Jeśli true, to obiekt otrzymuje fizykę dla 
     * obiektu przyczepionego do haka (z linami), natomiast jeśli false to 
     * obiekt otrzymuje fizykę dla obiektu nieprzyczepionego (luźnego). 
     * @param shouldBeAttached true dla fizyki dla obiektu przyczepionego, 
     * false dla nieprzyczepionego 
     */
    public void swapControl(int type){
        for(int i = 0; i < 3; i++){
            if(i == type) ((RigidBodyControl)getControl(i)).setEnabled(true);
            else ((RigidBodyControl)getControl(i)).setEnabled(false);
        }
        if(type == 0) setRopesVisibility(ropesHorizontal, true); 
        else if(type == 1) setRopesVisibility(ropesVertical, true); 
        else setRopesVisibility(ropesHorizontal[0].getCullHint()
                .equals(CullHint.Never) ? ropesHorizontal : ropesVertical ,false); 
    }
    
    @Override
    public Spatial getRecentlyHitObject(){
        return recentlyHitObject; 
    }
    
    @Override
    public void setRecentlyHitObject(Spatial object){
        recentlyHitObject = object; 
    }
    
    public Quaternion getVerticalRotation(){
        return verticalRotation; 
    }
    
    /**
     * Zwraca współrzędną wybranego rogu tego obiektu. Możliwe punkty do pobrania, 
     * to tylko te tworzące górną podstawę. Numeracja: 0 - lewy dolny róg, 
     * 1 - prawy dolny róg, 2 - lewy górny róg, 3 - prawy górny róg.
     * @param pointNumber numer punktu do pobrania 
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
        Vector3f end = getWorldTranslation().clone().add(vertical ? 
                new Vector3f(0f, 0f, 3f) : new Vector3f(0f, 1f, 0f)),
                start = getProperPoint(0, vertical);
        Geometry[] ropes = vertical ? ropesVertical : ropesHorizontal; 
        Vector3f[] ropesLocations = new Vector3f[ropes.length];
        Material ropeMaterial = new Material(BuildingSimulator.getBuildingSimulator()
                .getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
        ropeMaterial.setColor("Color", ColorRGBA.Black); 
        float length = start.distance(end);
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
        PhysicsSpace physics = BuildingSimulator.getBuildingSimulator()
                .getBulletAppState().getPhysicsSpace();
        RigidBodyControl controlAttaching = new RigidBodyControl(wallRopesShape, 0.00001f);
        addControl(controlAttaching); 
        physics.add(controlAttaching);
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
    
    private void createLooseControl(Vector3f location){
        CompoundCollisionShape compound = new CompoundCollisionShape(); 
        CollisionShape wallCollisionShape = CollisionShapeFactory
                .createDynamicMeshShape(getChild("Box"));
        compound.addChildShape(wallCollisionShape, Vector3f.ZERO);
        RigidBodyControl control = new RigidBodyControl(compound, 0.00001f);
        addControl(control); 
        control.setPhysicsLocation(location);
        BuildingSimulator.getBuildingSimulator().getBulletAppState()
                .getPhysicsSpace().add(control);
    }
    
    private void createAttachingVerticalControl(Vector3f location){
        Vector3f end = getWorldTranslation().clone().add(0f, 0f, 3f),
                start = getProperPoint(0, true);
        ropesHorizontal = new Geometry[2];
        Vector3f[] ropesLocations = new Vector3f[2];
        Material ropeMaterial = new Material(BuildingSimulator.getBuildingSimulator()
                .getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
        ropeMaterial.setColor("Color", ColorRGBA.Black); 
        float length = start.distance(end);
        for(int i = 0; i < ropesHorizontal.length; i++){
            ropesHorizontal[i] = new Geometry("Cylinder" + i, new Cylinder(4, 8, 0.02f, length));
            ropesLocations[i] = FastMath.interpolateLinear(0.5f, start, end);
            ropesHorizontal[i].setMaterial(ropeMaterial); 
            attachChild(ropesHorizontal[i]);
            start = getProperPoint(i + 1, true);
            ropesHorizontal[i].setCullHint(CullHint.Always);
        }   
        CompoundCollisionShape wallRopesShape = GameManager
                .createCompound(this, new String[] {"Box", "Cylinder0", "Cylinder1"});
        PhysicsSpace physics = BuildingSimulator.getBuildingSimulator()
                .getBulletAppState().getPhysicsSpace();
        RigidBodyControl controlAttaching = new RigidBodyControl(wallRopesShape, 0.00001f);
        addControl(controlAttaching); 
        physics.add(controlAttaching);
        controlAttaching.setAngularFactor(0);
        controlAttaching.setCollisionGroup(5);
        Vector3f physicsLocation = controlAttaching.getPhysicsLocation();
        List<ChildCollisionShape> collisionShapeChildren = wallRopesShape.getChildren();
        for(int i = 0; i < ropesHorizontal.length; i++){
            ropesHorizontal[i].setLocalTranslation(ropesLocations[i].subtract(physicsLocation));
            ropesHorizontal[i].lookAt(end, Vector3f.UNIT_Y);
            ChildCollisionShape gotShape = collisionShapeChildren.get(1 + i);
            gotShape.location = ropesHorizontal[i].getLocalTranslation();
            gotShape.rotation = ropesHorizontal[i].getLocalRotation().toRotationMatrix();
        }
        controlAttaching.setPhysicsLocation(location);
    }
}
