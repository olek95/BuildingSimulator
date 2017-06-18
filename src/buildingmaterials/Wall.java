package buildingmaterials;

import buildingsimulator.BuildingSimulator;
import buildingsimulator.GameManager;
import com.jme3.bounding.BoundingBox;
import com.jme3.bullet.collision.PhysicsCollisionGroupListener;
import com.jme3.bullet.collision.PhysicsCollisionObject;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Box;
import cranes.Hook;

/**
 * Obiekt klasy <code>Wall</code> reprezentuje kawałek ściany. Obiekt ten może 
 * służyć do budowy budynków. 
 * @author AleksanderSklorz
 */
public class Wall extends Node{
    private Spatial recentlyHitObject; 
    public Wall(Box shape, Vector3f location){
        BuildingSimulator game = BuildingSimulator.getBuildingSimulator();
        Geometry wall = new Geometry("Box", shape); 
        Material mat = new Material(game.getAssetManager(), 
                "Common/MatDefs/Misc/Unshaded.j3md");  
        mat.setColor("Color", ColorRGBA.Blue);   
        wall.setMaterial(mat);                               
        setName("Wall0");
        attachChild(wall);
        GameManager.createPhysics(null, this, 0.00001f, false);
        getControl(RigidBodyControl.class).setPhysicsLocation(location);
        game.getRootNode().attachChild(this);
    }
    
    /**
     * Zwraca współrzędną wybranego rogu tego obiektu. Możliwe punkty do pobrania, 
     * to tylko te tworzące górną podstawę. Numeracja: 0 - lewy dolny róg, 
     * 1 - prawy dolny róg, 2 - lewy górny róg, 3 - prawy górny róg.
     * @param pointNumber numer punktu do pobrania 
     * @return współrzędne wybranego rogu 
     */
    public Vector3f getProperPoint(int pointNumber){
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
        getControl(RigidBodyControl.class).activate();
    }
    
    public PhysicsCollisionGroupListener createCollisionListener(){
        return new PhysicsCollisionGroupListener(){
            @Override
            public boolean collide(PhysicsCollisionObject nodeA, PhysicsCollisionObject nodeB){
                Object a = nodeA.getUserObject(), b = nodeB.getUserObject();
                Spatial aSpatial = (Spatial)a, bSpatial = (Spatial)b;
                String aName = aSpatial.getName(), bName = bSpatial.getName();
                if(!isProperCollisionGroup(bSpatial)) return false;
                if(aName.equals("Wall0")){
                    setCollision(bSpatial);
                }
                else if(bName.equals("Wall0")){
                    setCollision(aSpatial);
                }
                return true;
            }
        };
    }
    
    public Spatial getRecentlyHitObject(){
        return recentlyHitObject; 
    }
    
    public void setRecentlyHitObject(Spatial object){
        recentlyHitObject = object; 
    }
    
    private void setCollision(Spatial b){
        /* zabezpiecza przypadek gdy hak dotyka jednocześnie elementu pionowego
        i poziomego*/
        if((recentlyHitObject == null || ((BoundingBox)recentlyHitObject
                .getWorldBound()).getMax(null).y > ((BoundingBox)b.getWorldBound())
                .getMax(null).y))
            recentlyHitObject = b;
    }
    
    private static boolean isProperCollisionGroup(Spatial b){
        // PhysicsCollisionObject bo control nie musi być tylko typu RigidBodyControl
        int collisionGroup = ((PhysicsCollisionObject)b.getControl(0)).getCollisionGroup();
        return collisionGroup != 4;
    }
}
