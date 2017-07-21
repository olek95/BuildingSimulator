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
            String recentlyHitObjectName = recentlyHitObject.getName(); 
            boolean collisionWithGround = recentlyHitObjectName.startsWith("New Scene");
            if(collisionWithGround || recentlyHitObject.getParent().getName()
                    .startsWith("Building") || recentlyHitObjectName.startsWith("Wall")){
                wall.removeFromParent();
                merge(wall, collisionWithGround ? null : (Wall)recentlyHitObject);
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
        if(wall2 != null){
            RigidBodyControl control1 = wall1.getControl(RigidBodyControl.class), 
                    control2 = wall2.getControl(RigidBodyControl.class); 
            control1.setPhysicsLocation(control2.getPhysicsLocation()
                    .setY(control1.getPhysicsLocation().y));
            control1.setPhysicsRotation(control2.getPhysicsRotation());
        }
    }
}
