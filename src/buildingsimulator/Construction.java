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
                Spatial wallParent = wall.getParent(); 
                if(wallParent.getName().startsWith("Building")){
                    deleteConstruction(wallParent);
                }
                wall.removeFromParent();
                merge(wall, collisionWithGround ? null : (Wall)recentlyHitObject);
                attachChild(wall);
            }
            List<Spatial> c = BuildingSimulator.getBuildingSimulator().getRootNode().getChildren();
            //for(Spatial s : c){
             //   System.out.println(s); 
                //if(s instanceof Construction)
                //System.out.println(s + " " + ((Node)s).getChildren().size() + " " 
                //        + ((Node)s).getChild(0));
            //} 
        }
    }
    
    private void merge(Wall wall1, Wall wall2){
        if(wall2 != null){
            RigidBodyControl control1 = wall1.getControl(RigidBodyControl.class);
            Transform location = calculateLocationProperly(wall1, wall2);
            if(location != null){
                control1.setPhysicsLocation(location.getTranslation());
                control1.setPhysicsRotation(location.getRotation());
            }
        }
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
        RigidBodyControl control1 = (RigidBodyControl)wall1.getControl(2),
                control2 = wall2.getControl(RigidBodyControl.class);
        Vector3f location1 = control1.getPhysicsLocation(), location2 = control2
                .getPhysicsLocation();
        if(location1.x > location2.x && location1.z > location2.z - 1 && 
                location1.z < location2.z + 1){
            Quaternion rotation2 = control2.getPhysicsRotation();
            Vector3f bottomLocation = ((Node)wall2.getChild("Bottom")).getWorldTranslation();
            return new Transform(new Vector3f(bottomLocation.x, location1.y, bottomLocation.z),
                    rotation2.clone().multLocal(new Quaternion(-1.570796f, 0, 0, 1.570796f)));
        }
        return null; 
    }
}
