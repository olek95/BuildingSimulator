package buildingmaterials;

import buildingsimulator.BuildingSimulator;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.math.Quaternion;
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
     * @param wall1 dodawana ściana 
     * @param wallMode tryb fizyki dla dodawanego elementu w chwili dodawania 
     */
    public void add(Wall wall1, Wall wall2, int wallMode){
        Spatial recentlyHitObject = wall1.getRecentlyHitObject();
        if(recentlyHitObject != null && wallMode == 2){ 
            String recentlyHitObjectName = recentlyHitObject.getName(); 
            boolean collisionWithGround = recentlyHitObjectName.startsWith("New Scene");
            if(collisionWithGround || recentlyHitObjectName.startsWith("Wall")){
                    Spatial wallParent = wall1.getParent(); 
                    if(wallParent.getName().startsWith("Building")){
                        deleteConstruction(wallParent);
                    }
                    //wall.removeFromParent();
                    Node touchedWall = merge(wall1, collisionWithGround ? null 
                            : (Wall)recentlyHitObject, 1, wallMode);
                    if(touchedWall != null){
                        touchedWall.attachChild(wall1);
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
                    Spatial wallParent = wall1.getParent(); 
                    if(wallParent.getName().startsWith("Building")){
                        deleteConstruction(wallParent);
                    }
                    //wall.removeFromParent();
                    Node floor;  
                    if(wall2 != null) floor = mergeHorizontal(wall1, wall2, true, wallMode); 
                    else floor = mergeHorizontal(wall1, collisionWithGround ? null :
                                (Wall)recentlyHitObject, false, wallMode);
                    if(floor != null){
                        floor.attachChild(wall1);
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
    
    public static Wall getNearestBuildingWall(Wall wall){
        List<Spatial> gameObjects = BuildingSimulator.getBuildingSimulator()
                    .getRootNode().getChildren();
        float min = -1;
        Spatial minWall = null; 
        for(int i = 0; i < gameObjects.size(); i++){
            Spatial object = gameObjects.get(i); 
            if(object.getName().startsWith("Building")){
                List<Spatial> buildingElements = ((Node)object).getChildren();
                if(minWall == null){
                    minWall = buildingElements.get(0); 
                    min = wall.getWorldTranslation().distance(minWall
                            .getWorldTranslation());
                }
                for(int k = 1; k < buildingElements.size(); k++){
                    float distance = wall.getWorldTranslation()
                        .distance(buildingElements.get(k).getWorldTranslation());
                    if(distance < min){
                        min = distance;
                        minWall = buildingElements.get(k); 
                    } 
                }
            }
        }
        return min < 8 ? (Wall)minWall : null; 
    }
    
    private Node merge(Wall wall1, Wall wall2, int catchNodeIndex, int mode){
        if(wall2 != null){ 
            Vector3f location = ((RigidBodyControl)wall1.getControl(mode)).getPhysicsLocation();
            List<Spatial> wallChildren = wall2.getChildren(); 
            Node[] edges = new Node[4];
            Vector3f[] edgeLocations = new Vector3f[4];
            float[] distances = new float[4];
            for(int i = 0; i < 4; i++, catchNodeIndex++){
                edges[i] = (Node)wallChildren.get(catchNodeIndex);
                edgeLocations[i] = edges[i].getWorldTranslation(); 
                distances[i] = location.distance(edgeLocations[i]); 
            }
            int minDistance = getMin(distances); 
            if(edges[minDistance].getChildren().isEmpty()){
                RigidBodyControl control = wall1.getControl(RigidBodyControl.class);
                control.setPhysicsLocation(calculateProperLocation(location,
                        edgeLocations[minDistance], minDistance));
                System.out.println(catchNodeIndex); 
                control.setPhysicsRotation(calculateProperRotation(wall2
                        .getControl(RigidBodyControl.class).getPhysicsRotation(),
                        minDistance, catchNodeIndex <= 4));
                return edges[minDistance];
            }else return null; 
        }
        return this;
    }
    
    private Node mergeHorizontal(Wall wall1, Wall wall2, boolean isFoundations, int mode){
        if(!isFoundations){
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
        }else{
            merge(wall1, wall2, 6, mode); 
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
    
    private Vector3f calculateProperLocation(Vector3f wallLocation, Vector3f edgeLocation,
            int direction){
        Vector3f location; 
        if(direction < 2)
            location = edgeLocation.clone().add(0, 2.7f - 0.2f * 2, 0);  
        else location = new Vector3f(edgeLocation.x, wallLocation.y, edgeLocation.z);
        return location;
    }
    
    private Quaternion calculateProperRotation(Quaternion rotation, int direction,
            boolean reversal){
        Quaternion newRotation;
        if(reversal) 
            newRotation = rotation.clone().multLocal(-1.570796f, 0, 0, 1.570796f);
        else newRotation = rotation.clone(); 
        if(direction > 1) newRotation.multLocal(0, 0, -1.570796f, 1.570796f);
        return newRotation;
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
                int direction = i - 1; 
                control.setPhysicsLocation(calculateProperLocation(((RigidBodyControl)wall
                        .getControl(2)).getPhysicsLocation(),
                        edge.getWorldTranslation(), direction));
                control.setPhysicsRotation(calculateProperRotation(rotation,
                        direction, true));
            }
        }
    }
}
