package buildingsimulator;

import buildingmaterials.Wall;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import java.util.List;

public class Construction extends Node{
    private static int counter = -1; 
    public Construction(){
        setName("Building" + (++counter));
    }
    
    public void add(Wall wall){
        Spatial recentlyHitObject = wall.getRecentlyHitObject();
        if(recentlyHitObject != null){ 
            //System.out.println("WSZEDL");
            String recentlyHitObjectName = recentlyHitObject.getName(); 
            boolean collisionWithOtherWall = recentlyHitObjectName.startsWith("Wall");
            if(recentlyHitObjectName.startsWith("New Scene") 
                    || recentlyHitObject.getParent().getName().startsWith("Building")
                    || collisionWithOtherWall){
                //System.out.println("if");
                wall.removeFromParent();
                attachChild(wall);
            }
            List<Spatial> c = BuildingSimulator.getBuildingSimulator().getRootNode().getChildren();
            for(Spatial s : c){
                if(s instanceof Construction)
                System.out.println(s + " " + ((Node)s).getChildren().size() + " " 
                        + ((Node)s).getChild(0));
            } 
        }
    }
    
    private void merge(Wall wall1, Wall wall2){
        Vector3f location = wall2.getControl(RigidBodyControl.class).getPhysicsLocation();
        Vector3f newLocation = new Vector3f(location.x, wall1
                .getControl(RigidBodyControl.class).getPhysicsLocation().y, location.z);
        wall1.getControl(RigidBodyControl.class).setPhysicsLocation(newLocation);
    }
}
