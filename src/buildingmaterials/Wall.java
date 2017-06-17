package buildingmaterials;

import buildingsimulator.BuildingSimulator;
import com.jme3.bounding.BoundingBox;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Box;

public class Wall extends Node{
    public Wall(Box shape, Vector3f location){
        BuildingSimulator game = BuildingSimulator.getBuildingSimulator();
        Geometry wall = new Geometry("Box", shape); 
        Material mat = new Material(game.getAssetManager(), 
                "Common/MatDefs/Misc/Unshaded.j3md");  
        mat.setColor("Color", ColorRGBA.Blue);   
        wall.setMaterial(mat);                               
        setName("Wall0");
        attachChild(wall);
        initPhysics(location);
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
    
    /**
     * Inicjuje fizykę dla tego obiektu. 
     * @param location położenie fizyki 
     */
    public void initPhysics(Vector3f location){
        PhysicsSpace physics = BuildingSimulator.getBuildingSimulator()
                .getBulletAppState().getPhysicsSpace();
        RigidBodyControl control = getControl(RigidBodyControl.class);
        if(control != null){
            removeControl(control);
            physics.remove(control);
        }
        RigidBodyControl wallControl = new RigidBodyControl(0.00001f);
        addControl(wallControl);
        wallControl.setPhysicsLocation(location);
        physics.add(wallControl);
    }
}
