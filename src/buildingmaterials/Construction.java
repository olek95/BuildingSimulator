package buildingmaterials;

import buildingsimulator.BuildingSimulator;
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
     * @param wallMode tryb fizyki dla dodawanego elementu w chwili dodawania 
     */
    public void add(Wall wall, int wallMode){
        Spatial recentlyHitObject = wall.getRecentlyHitObject();
        if(recentlyHitObject != null && wallMode == 2){ 
            String recentlyHitObjectName = recentlyHitObject.getName(); 
            boolean collisionWithGround = recentlyHitObjectName.startsWith("New Scene");
            if(collisionWithGround || recentlyHitObjectName.startsWith("Wall")){
                    Spatial wallParent = wall.getParent(); 
                    if(wallParent.getName().startsWith("Building")){
                        deleteConstruction(wallParent);
                    }
                    //wall.removeFromParent();
                    Node touchedWall = merge(wall, collisionWithGround ? null 
                            : (Wall)recentlyHitObject);
                    if(touchedWall != null){
                        touchedWall.attachChild(wall);
                        if(!collisionWithGround) correctLocations(touchedWall.getName()); 
                    }
            }
           /* List<Spatial> c = BuildingSimulator.getBuildingSimulator().getRootNode().getChildren();
            for(Spatial s : c){
                System.out.println(s); 
                if(s instanceof Construction)
                System.out.println(s + " " + ((Node)s).getChildren().size() + " " 
                        + ((Node)s).getChild(0));
            } */
        }else{
            if(recentlyHitObject != null && wallMode == 1){
                String recentlyHitObjectName = recentlyHitObject.getName(); 
                boolean collisionWithGround = recentlyHitObjectName.startsWith("New Scene");
                if(collisionWithGround || recentlyHitObjectName.startsWith("Wall")){
                    Spatial wallParent = wall.getParent(); 
                    if(wallParent.getName().startsWith("Building")){
                        deleteConstruction(wallParent);
                    }
                    //wall.removeFromParent();
                    Node floor = mergeHorizontal(wall, collisionWithGround ? null 
                            : (Wall)recentlyHitObject);
                    if(floor != null){
                        floor.attachChild(wall);
                        if(!collisionWithGround) correctLocations(null); 
                    }
                }
            }
        }
    }
    
    /**
     * Zwraca całą budowlę do której należy obiekt. 
     * @param wall sprawdzany obiekt 
     * @return budowlę do której nalezy obiekt lub null jeśli nie należy do budowli  
     */
    public static Construction getWholeConstruction(Spatial wall){
        Node wallParent = wall.getParent(); 
        while(wallParent != null){  
            if(wallParent.getName().startsWith("Building")) return (Construction)wallParent; 
            wallParent = wallParent.getParent();
        }
        return null; 
    }
    
    private Node merge(Wall wall1, Wall wall2){
        if(wall2 != null){ 
            Vector3f location = ((RigidBodyControl)wall1.getControl(2)).getPhysicsLocation();
            List<Spatial> wallChildren = wall2.getChildren(); 
            Node[] edges = new Node[4];
            Vector3f[] edgeLocations = new Vector3f[4];
            float[] distances = new float[4];
            for(int i = 0; i < 4; i++){
                edges[i] = (Node)wallChildren.get(i + 1);
                edgeLocations[i] = edges[i].getWorldTranslation(); 
                distances[i] = location.distance(edgeLocations[i]); 
            }
            int minDistance = getMin(distances); 
            if(edges[minDistance].getChildren().isEmpty()){
                RigidBodyControl control = wall1.getControl(RigidBodyControl.class);
                Transform transform = calculateProperLocation(location, edgeLocations[minDistance], 
                    wall2.getControl(RigidBodyControl.class).getPhysicsRotation(), minDistance);
                if(transform != null){
                    control.setPhysicsLocation(transform.getTranslation());
                    control.setPhysicsRotation(transform.getRotation());
                }
                return edges[minDistance];
            }else return null; 
        }
        return this;
    }
    
    private Node mergeHorizontal(Wall wall1, Wall wall2){
        if(wall2 != null){
            Node edge = wall2.getParent(), floor = edge.getParent();
            int index = floor.getChildIndex(edge);
            index += index % 2 == 0 ? -1 : 1;
            // sprawdza czy na przeciwko jest druga ściana 
            if((Node)((Node)floor.getChild(index)).getChild(0) != null){
                Vector3f center = floor.getWorldTranslation();
                RigidBodyControl control = wall1.getControl(RigidBodyControl.class); 
                control.setPhysicsLocation(new Vector3f(center.x, wall1
                        .getWorldTranslation().y, center.z));
                control.setPhysicsRotation(floor.getWorldRotation());
                return (Node)floor.getChild(5);
            }
            return null; 
        }
        return this; 
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
    
    private int getMin(float[] distances){
        int min = 0; 
        for(int i = 1; i < 4; i++){
            if(distances[min] > distances[i]){
                min = i; 
            }
        }
        return min; 
    }
    
    private Transform calculateProperLocation(Vector3f wallLocation, Vector3f edgeLocation,
            Quaternion rotation, int direction){
        Vector3f location; 
        if(direction < 2)
            location = edgeLocation.clone().add(0, 2.7f - 0.2f * 2, 0);  
        else location = new Vector3f(edgeLocation.x, wallLocation.y, edgeLocation.z);
        Quaternion newRotation = rotation.clone().multLocal(-1.570796f, 0, 0, 1.570796f);
        if(direction > 1) newRotation.multLocal(0, 0, -1.570796f, 1.570796f);
        return new Transform(location,
                newRotation);
    }
    
    private void correctLocations(String edgeName){
        Node floor = (Node)getChild(0); 
        Quaternion rotation = floor.getWorldRotation(); 
        for(int i = 1; i <= 4; i++){
            Node edge = (Node)floor.getChild(i); 
            // dodatkowo sprawdza czy nie jest równy ostatnio ustawionemu obiektowi 
            if(!edge.getChildren().isEmpty() && !edge.getName().equals(edgeName)){
                Spatial wall = edge.getChild(0); 
                RigidBodyControl control = wall.getControl(RigidBodyControl.class); 
                Transform transform = calculateProperLocation(((RigidBodyControl)wall
                        .getControl(2)).getPhysicsLocation(), edge.getWorldTranslation(),
                        rotation, i - 1);
                control.setPhysicsLocation(transform.getTranslation());
                control.setPhysicsRotation(transform.getRotation());
            }
        }
    }
}
