package buildingmaterials;

import buildingsimulator.BottomCollisionListener;
import buildingsimulator.BuildingSimulator;
import buildingsimulator.GameManager;
import buildingsimulator.RememberingRecentlyHitObject;
import com.jme3.bounding.BoundingBox;
import com.jme3.bullet.collision.PhysicsCollisionGroupListener;
import com.jme3.bullet.collision.PhysicsCollisionObject;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.collision.shapes.CompoundCollisionShape;
import com.jme3.bullet.collision.shapes.infos.ChildCollisionShape;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.bullet.util.CollisionShapeFactory;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Cylinder;
import java.util.List;

/**
 * Obiekt klasy <code>Wall</code> reprezentuje kawałek ściany. Obiekt ten może 
 * służyć do budowy budynków. 
 * @author AleksanderSklorz
 */
public class Wall extends Node implements RememberingRecentlyHitObject{
    private Spatial recentlyHitObject;
    private boolean attached = false; 
    private static BottomCollisionListener collisionListener = null; 
    private Geometry[] ropes = new Geometry[4];
    public Wall(Box shape, Vector3f location){
        BuildingSimulator game = BuildingSimulator.getBuildingSimulator();
        Geometry wall = new Geometry("Box", shape); 
        Material mat = new Material(game.getAssetManager(), 
                "Common/MatDefs/Misc/Unshaded.j3md");  
        mat.setColor("Color", ColorRGBA.Blue);   
        wall.setMaterial(mat);                               
        setName("Wall0");
        attachChild(wall);
        //GameManager.changeControl(null, this, 0.00001f, false);
        //getControl(RigidBodyControl.class).setPhysicsLocation(location);
        game.getRootNode().attachChild(this);
        /*if(collisionListener == null){
            collisionListener = new BottomCollisionListener(this, "Wall0", "ropeHook");
            game.getBulletAppState().getPhysicsSpace()
                    .addCollisionGroupListener(collisionListener, 5);
        }*/
        
        
        
        
        Vector3f end = getWorldTranslation().clone().setY(1.3f),
                start = getProperPoint(0);
        ropes = new Geometry[4];
        Vector3f[] ropesLocations = new Vector3f[4];
        Material ropeMaterial = new Material(game.getAssetManager(),
                "Common/MatDefs/Misc/Unshaded.j3md");
        ropeMaterial.setColor("Color", ColorRGBA.Black); 
        float length = start.distance(end);
        for(int i = 0; i < ropes.length; i++){
            ropes[i] = new Geometry("Cylinder" + i, new Cylinder(4, 8, 0.02f, length));
            ropesLocations[i] = FastMath.interpolateLinear(0.5f, start, end);
            ropes[i].setMaterial(ropeMaterial); 
            attachChild(ropes[i]);
            start = getProperPoint(i + 1);
            ropes[i].setCullHint(CullHint.Always);
        }          
        CompoundCollisionShape wallRopesShape = GameManager
                .createCompound(this, new String[] {"Box", "Cylinder0",
                "Cylinder1", "Cylinder2", "Cylinder3"});
        GameManager.createPhysics(wallRopesShape, this, 0.00001f, false);
        RigidBodyControl controlAttaching = this.getControl(RigidBodyControl.class); 
        controlAttaching.setCollisionGroup(5);
        Vector3f physicsLocation = controlAttaching.getPhysicsLocation();
        List<ChildCollisionShape> children = wallRopesShape.getChildren();
        for(int i = 0; i < ropes.length; i++){
            ropes[i].setLocalTranslation(ropesLocations[i].subtract(physicsLocation));
            ropes[i].lookAt(end, Vector3f.UNIT_Y);
            ChildCollisionShape gotShape = children.get(1 + i);
            gotShape.location = ropes[i].getLocalTranslation();
            gotShape.rotation = ropes[i].getLocalRotation().toRotationMatrix();
        }
        getControl(RigidBodyControl.class).setPhysicsLocation(location);
        
        CompoundCollisionShape compound = new CompoundCollisionShape(); 
        CollisionShape wallCollisionShape = CollisionShapeFactory
                .createDynamicMeshShape(wall);
        compound.addChildShape(wallCollisionShape, Vector3f.ZERO);
        RigidBodyControl control = new RigidBodyControl(compound, 0.00001f);
        addControl(control); 
        control.setPhysicsLocation(location);
        game.getBulletAppState().getPhysicsSpace().add(control);
        changeControl(false); 
    }
    
    /**
     * Zwraca współrzędną wybranego rogu tego obiektu. Możliwe punkty do pobrania, 
     * to tylko te tworzące górną podstawę. Numeracja: 0 - lewy dolny róg, 
     * 1 - prawy dolny róg, 2 - lewy górny róg, 3 - prawy górny róg.
     * @param pointNumber numer punktu do pobrania 
     * @return współrzędne wybranego rogu 
     */
    private Vector3f getProperPoint(int pointNumber){
        BoundingBox bounding = (BoundingBox)getWorldBound();
        Vector3f max = bounding.getMax(null);
        switch(pointNumber){
            case 0:
                return max;
            case 1:
                return max.subtract(0, 0, bounding.getZExtent() * 2);
            case 2: 
                return max.subtract(bounding.getXExtent() * 2, 0, 0);
            case 3: 
                return max.subtract(bounding.getXExtent() * 2, 0, bounding
                        .getZExtent() * 2); 
            default: return null;
        }
    }
    
    /**
     * Aktywuje fizykę obiektu, jeśli nie jest ona aktywna w danej chwili (czyli 
     * jeśli obiekt się nie porusza). 
     */
    public void activateIfInactive(){
        ((RigidBodyControl)getControl(attached ? 0 : 1)).activate();
    }
    
    @Override
    public void setCollision(Spatial b){
        /* zabezpiecza przypadek gdy hak dotyka jednocześnie elementu pionowego
        i poziomego*/
        if((recentlyHitObject == null || ((BoundingBox)recentlyHitObject
                .getWorldBound()).getMax(null).y > ((BoundingBox)b.getWorldBound())
                .getMax(null).y))
            recentlyHitObject = b;
    }
    
    public void setRopesVisibility(boolean visibility){
        for(int i = 0; i < ropes.length; i++)
            ropes[i].setCullHint(visibility ? CullHint.Never : CullHint.Always);
    }
    
    public void changeControl(boolean shouldBeAttached){
        getControl(RigidBodyControl.class).setEnabled(shouldBeAttached);
        ((RigidBodyControl)getControl(1)).setEnabled(!shouldBeAttached);
    }
    
    @Override
    public Spatial getRecentlyHitObject(){
        return recentlyHitObject; 
    }
    
    @Override
    public void setRecentlyHitObject(Spatial object){
        recentlyHitObject = object; 
    }
    
    public boolean isAttached(){
        return attached; 
    }
    
    public void setAttached(boolean attached){
        this.attached = attached; 
    }
}
