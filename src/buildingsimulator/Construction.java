package buildingsimulator;

import buildingmaterials.Wall;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import java.util.List;

public class Construction extends Node{
    private static int counter = -1; 
    public Construction(){
        setName("Building" + (++counter));
    }
    
    public void add(Wall wall){
        wall.removeFromParent();
        attachChild(wall);
    }
}
