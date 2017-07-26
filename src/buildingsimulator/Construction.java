package buildingsimulator;

import buildingmaterials.Wall;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.math.Quaternion;
import com.jme3.math.Transform;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import java.util.List;

/**
 * Obiekt klasy <code>Construction</code> reprezentuje budowaną konstrukcję. 
 * Jest to węzeł składajacy się z różnych rodzajów ścian połaczonych ze sobą. 
 * @author AleksanderSklorz 
 */
public class Construction extends Node{
    private static int counter = -1; 
    public Construction(){
        setName("Building" + (++counter));
    }
    
    /**
     * Dodaje kolejną ścianą do konstrukcji. 
     * @param wall dodawana ściana 
     */
    public void add(Wall wall){
        Spatial recentlyHitObject = wall.getRecentlyHitObject();
        if(recentlyHitObject != null){ 
            String recentlyHitObjectName = recentlyHitObject.getName(); 
            boolean collisionWithGround = recentlyHitObjectName.startsWith("New Scene");
            if(collisionWithGround || recentlyHitObjectName.startsWith("Wall")){
                if(merge(wall, collisionWithGround ? null : (Wall)recentlyHitObject)){
                    Spatial wallParent = wall.getParent(); 
                    if(wallParent.getName().startsWith("Building")){
                        deleteConstruction(wallParent);
                    }
                    wall.removeFromParent();
                    attachChild(wall);
                }
            }
            List<Spatial> c = BuildingSimulator.getBuildingSimulator().getRootNode().getChildren();
            for(Spatial s : c){
                //System.out.println(s); 
                //if(s instanceof Construction)
                //System.out.println(s + " " + ((Node)s).getChildren().size() + " " 
                //        + ((Node)s).getChild(0));
            } 
        }
    }
    
    private boolean merge(Wall wall1, Wall wall2){
        if(wall2 != null){
            RigidBodyControl control = wall1.getControl(RigidBodyControl.class);
            Transform location = calculateLocationProperly(wall1, wall2);
            if(location != null){
                control.setPhysicsLocation(location.getTranslation());
                control.setPhysicsRotation(location.getRotation());
            }else return false; 
        }
        return true;
    }
    
    private void deleteConstruction(Spatial construction){
        int constructionIndex = Integer.parseInt(construction.getName().substring(8));
        construction.removeFromParent();
        List<Spatial> objects = BuildingSimulator.getBuildingSimulator()
                .getRootNode().getChildren();
        for(int i = 0; i < objects.size(); i++){
            Spatial object = objects.get(i); 
            String nameObject = object.getName();
            int index; 
            if(nameObject.startsWith("Building") && (index = Integer.parseInt(nameObject
                    .substring(8))) > constructionIndex){
                object.setName("Building" + (--index));
            }
        }
        counter--; 
    }
    
    private Transform calculateLocationProperly(Wall wall1, Wall wall2){
        getDirection(wall1, wall2); 
        RigidBodyControl control1 = (RigidBodyControl)wall1.getControl(2),
                control2 = wall2.getControl(RigidBodyControl.class);
        Vector3f location1 = control1.getPhysicsLocation(), location2 = control2
                .getPhysicsLocation();
        //System.out.println(location1 + " " + location2);
        if(location1.z < location2.z && location1.x > location2.x - 2
                && location1.x < location2.x + 2){
            //System.out.println("BOTTOM");
            Quaternion rotation2 = control2.getPhysicsRotation();
            Vector3f bottomLocation = ((Node)wall2.getChild("Bottom")).getWorldTranslation();
            return new Transform(new Vector3f(bottomLocation.x, location1.y, bottomLocation.z),
                    rotation2.clone().multLocal(new Quaternion(-1.570796f, 0, 0, 1.570796f)));
        }else{
            if(location1.z > location2.z && location1.x > location2.x - 2 
                    && location1.x < location2.x + 2){
                //System.out.println("TOP");
                Quaternion rotation2 = control2.getPhysicsRotation();
                Vector3f bottomLocation = ((Node)wall2.getChild("Up")).getWorldTranslation();
                return new Transform(new Vector3f(bottomLocation.x, location1.y, bottomLocation.z),
                        rotation2.clone().multLocal(new Quaternion(-1.570796f, 0, 0, 1.570796f)));
            }else{
                if(location1.x > location2.x && location1.z > location2.z - 1 
                    && location1.z < location2.z + 1){
                    //System.out.println("LEFT");
                    Quaternion rotation2 = control2.getPhysicsRotation();
                    Vector3f bottomLocation = ((Node)wall2.getChild("Left")).getWorldTranslation();
                    return new Transform(new Vector3f(bottomLocation.x, location1.y, bottomLocation.z),
                            rotation2.clone().multLocal(new Quaternion(-1.570796f, 0, 0, 1.570796f)));
                }else{
                    if(location1.x < location2.x && location1.z > location2.z - 1 
                        && location1.z < location2.z + 1){
                        //System.out.println("RIGHT");
                        Quaternion rotation2 = control2.getPhysicsRotation();
                        Vector3f bottomLocation = ((Node)wall2.getChild("Right")).getWorldTranslation();
                        return new Transform(new Vector3f(bottomLocation.x, location1.y, bottomLocation.z),
                                rotation2.clone().multLocal(new Quaternion(-1.570796f, 0, 0, 1.570796f)));
                    }
                }
            }
        }
        return null; 
    }
    
    private void getDirection(Wall wall1, Wall wall2){
        RigidBodyControl control1 = (RigidBodyControl)wall1.getControl(2),
                control2 = wall2.getControl(RigidBodyControl.class);
        Vector3f location1 = control1.getPhysicsLocation(), location2 = control2
                .getPhysicsLocation();
        Node up = (Node)wall2.getChild("Up"), bottom = (Node)wall2.getChild("Bottom"),
                right = (Node)wall2.getChild("Right"), left = (Node)wall2.getChild("Left");
        System.out.println(location1 + " " + up.getWorldTranslation() + " " 
                + location2);
        float[] distances = {location1.distance(bottom.getWorldTranslation()), 
            location1.distance(up.getWorldTranslation()),
            location1.distance(right.getWorldTranslation()),
            location1.distance(left.getWorldTranslation())};
        int minDistance = getMin(distances); 
        if(minDistance == 0) System.out.println("Bottom");
        else if(minDistance == 1) System.out.println("Up");
        else if(minDistance == 2) System.out.println("Right");
        else System.out.println("Left");
        
        /*if(location1.z > bottom.getWorldTranslation().z && location1.z < location2.z){
            System.out.println("Bottom");
        }else if(location1.z < up.getWorldTranslation().z && location1.z > location2.z){
            System.out.println("Up");
        }*/
    }
    
    private int getMin(float... distances){
        int min = 0; 
        for(int i = 1; i < 4; i++){
            if(distances[min] > distances[i]){
                min = i; 
            }
        }
        return min; 
    }
}
