package buildingsimulator;

import buildingmaterials.Wall;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;

public class Construction extends Node{
    private static int counter = -1; 
    public Construction(){
        setName("Building" + (++counter));
    }
    
    public void add(Wall wall){
        Spatial recentlyHitObject = wall.getRecentlyHitObject();
        if(recentlyHitObject != null){ 
            Spatial recentlyHitObjectParent = recentlyHitObject.getParent();
            if(recentlyHitObject.getName().startsWith("New Scene") 
                    || recentlyHitObjectParent.getName().startsWith("Building")){
                wall.removeFromParent();
                attachChild(wall);
            }
        }
    }
}
