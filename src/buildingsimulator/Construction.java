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
            RigidBodyControl control1 = wall1.getControl(RigidBodyControl.class), 
                    control2 = wall2.getControl(RigidBodyControl.class);
            Transform location = calculateLocationProperly(wall1, wall2);
            if(location != null){
                control1.setPhysicsLocation(location.getTranslation());
                //System.out.println(((Node)wall2.getChild("Bottom")).getWorldTranslation());
                //((Node)wall2.getChild("Bottom")).attachChild(wall1);
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
        System.out.println(location1 + " " + location2); 
        if(location1.x > location2.x && location1.z > location2.z - 1 && 
                location1.z < location2.z + 1){
            //float height = wall2.getHeight(); 
            //float zOffset = (float)Math.sin(1.0516) * height,
            //        xOffset = (float)Math.cos(1.0516) * height;
            //System.out.println(zOffset + " " + xOffset); 
            //float offset = (float)(wall2.getHeight() / Math.sqrt(2));
            //location1.setX(location2.x + xOffset);
            //location1.setZ(location2.z - zOffset);
            /*Vector3f prostopadly = new Vector3f(0,0, location2.z);
            Vector3f prostopadlyZnormalizowany = prostopadly.clone().setX(0).setY(0).normalize(); 
            Vector3f znormalizowany = location1.clone().normalize(); 
            float kat = prostopadlyZnormalizowany.angleBetween(znormalizowany); 
            float height = wall2.getHeight();
            float zOffset = (float)Math.sin(kat) * height,
                    xOffset = (float)Math.cos(kat) * height;
            System.out.println(zOffset + " " + xOffset + " " + kat); 
           // float offset = (float)(wall2.getHeight() / Math.sqrt(2));
            location1.setX(location2.x + xOffset);
            location1.setZ(location2.z - zOffset);*/
            /*RigidBodyControl cp = wall2.clone().getControl(RigidBodyControl.class);
            Quaternion old = cp.getPhysicsRotation().clone(); 
            cp.setPhysicsRotation(Quaternion.IDENTITY);
            Vector3f l = cp.getPhysicsLocation(); 
            Vector3f l2 = new Vector3f(l.x, l.y, l.z + wall2.getHeight());
            cp.setPhysicsLocation(l2);
            cp.setPhysicsRotation(old);*/
            Quaternion rotation2 = control2.getPhysicsRotation();
            //Vector3f newL = cp.getPhysicsLocation();
            //System.out.println(newL);
            Vector3f l = ((Node)wall2.getChild("Bottom")).getWorldTranslation();
            return new Transform(new Vector3f(l.x, location1.y, l.z), rotation2.clone()
                    .multLocal(new Quaternion(-1.570796f, 0, 0, 1.570796f)));
        }
        return null; 
    }
}
