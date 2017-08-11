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
     * @param wall2 najbliższy element do którego można dołaczyć nową ścianę.
     * Jeśli null, to element jest łaczony z ostatnio dotkniętym elementem. 
     * @param wallMode tryb fizyki dla dodawanego elementu w chwili dodawania 
     */
    public void add(Wall wall1, Wall wall2, int wallMode){
        Spatial recentlyHitObject = wall1.getRecentlyHitObject();
        if(recentlyHitObject != null){ 
            String recentlyHitObjectName = recentlyHitObject.getName(); 
            boolean collisionWithGround = recentlyHitObjectName.startsWith("New Scene");
            if(collisionWithGround || recentlyHitObjectName.startsWith("Wall")){
                    Spatial wallParent = wall1.getParent(); 
                    if(wallParent.getName().startsWith("Building")){
                        deleteConstruction(wallParent);
                    }
                    //wall.removeFromParent();
                    Node touchedWall; 
                    if(wallMode == 2){ 
                        touchedWall = merge(wall1, collisionWithGround ? null 
                            : (Wall)recentlyHitObject, 1, wallMode);
                    }else{
                        if(wall2 != null) 
                            touchedWall = mergeHorizontal(wall1, wall2, true, wallMode); 
                        else touchedWall = mergeHorizontal(wall1, collisionWithGround ? null :
                                (Wall)recentlyHitObject, false, wallMode);
                    }
                    if(touchedWall != null){
                        touchedWall.attachChild(wall1);
                        if(!collisionWithGround) correctLocations(touchedWall.getName()); 
                        RigidBodyControl control = wall1.getControl(RigidBodyControl.class);
                        control.setAngularDamping(1);
                        control.setLinearDamping(1);
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
    
    /**
     * Zwraca najbliższą ścianę z jakiegokolwiek budynku. 
     * @param wall ściana dla której szukamy najbliższy element z jakiegoś budynku. 
     * @return najbliższy element z jakiegokolwiek budynku 
     */
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
        if(minWall != null){
            Wall nearestWall = (Wall)minWall; 
            return min < Math.ceil(wall.getMaxSize()) + Math.ceil(nearestWall.getMaxSize())
                    ? nearestWall : null; 
        }else return null; 
    }
    
    private Node merge(Wall wall1, Wall wall2, int catchNodeIndex, int mode){
        if(wall2 != null){ 
            Vector3f location = ((RigidBodyControl)wall1.getControl(mode)).getPhysicsLocation();
            List<Spatial> wallChildren = wall2.getChildren(); 
            //int nodeCount = catchNodeIndex == 1 ? 4 : 6;
            Node[] catchNodes = new Node[4];
            Vector3f[] catchNodesLocations = new Vector3f[4];
            float[] distances = new float[4];
            //for(int i = 0; i < nodeCount; i++, catchNodeIndex++){
            for(int i = 0; i < 4; i++){
                catchNodes[i] = (Node)wallChildren.get(catchNodeIndex);
                catchNodesLocations[i] = catchNodes[i].getWorldTranslation(); 
                distances[i] = location.distance(catchNodesLocations[i]); 
            }
            //for(int i = 0; i < catchNodes.length;i++){
            //    System.out.println(catchNodes[i]);
            //}
            boolean perpendicularity = wall1.checkPerpendicularity(wall2),
                    foundations = catchNodeIndex != 5; 
              int minDistance = getMin(distances, foundations, wall1), 
                      i = minDistance > 3 ? 0 : minDistance; 
              if(foundations) {
                  catchNodes[i] = (Node)wallChildren.get(minDistance); 
                  for(Spatial n : wallChildren) System.out.println(n); 
                  System.out.println(i + " " + catchNodes[i] + " " + minDistance);
              }
//            if(hasEmptySpace(catchNodes[minDistance], perpendicularity)){
              if(catchNodes[i].getChildren().isEmpty()){
                if(foundations){
//                    if(perpendicularity){
//                       // catchNodes[minDistance] = getNearestChild(catchNodes[minDistance],
//                         //   distances, minDistance);
//                        if(catchNodes[minDistance] != null)
//                            catchNodesLocations[minDistance] = catchNodes[minDistance]
//                                    .getWorldTranslation();
//                        else return null; 
//                    }
                    catchNodes[i] = wall2.changeCatchNodeLocation(wall1, 
                            catchNodes[i], minDistance, perpendicularity); 
                    catchNodesLocations[i] = catchNodes[i].getWorldTranslation();
                }
                RigidBodyControl control = wall1.getControl(RigidBodyControl.class);
                control.setPhysicsLocation(calculateProperLocation(
                        catchNodesLocations[i], foundations, wall1, mode));
                control.setPhysicsRotation(calculateProperRotation(wall2
                        .getControl(RigidBodyControl.class).getPhysicsRotation(),
                        i, !foundations, perpendicularity));
                return catchNodes[i];
            }else return null; 
        }
        return this;
    }
    
    private Node mergeHorizontal(Wall wall1, Wall wall2, boolean foundations, int mode){
        if(!foundations){
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
            return merge(wall1, wall2, 6, mode); 
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
    
    private int getMin(float[] distances, boolean foundations, Wall wall){
        int min = 0; 
        for(int i = 1; i < 4; i++){
            if(distances[min] > distances[i]){
                min = i; 
            }
        }
        if(foundations){
            Vector3f wallLocation = wall.getWorldTranslation();
            int offset = min % 2, i1 = min + 6 + offset, i2 = min + 7 + offset; 
            System.out.println(i1 + " " + i2 +  " "  + wall); 
            return wall.getChild(i1).getWorldTranslation().distance(wallLocation) 
                    < wall.getChild(i2).getWorldTranslation().distance(wallLocation) ?
                    i1 : i2; 
        }
        return min; 
    }
    
    private Vector3f calculateProperLocation(Vector3f edgeLocation,
            boolean foundations, Wall wall, int mode){
        Vector3f location = ((RigidBodyControl)wall.getControl(mode)).getPhysicsLocation();
        return new Vector3f(edgeLocation.x, foundations || mode == 1 ? location.y
                : edgeLocation.y + wall.getHeight(), edgeLocation.z);
    }
    
    private Quaternion calculateProperRotation(Quaternion rotation, int direction,
            boolean notFoundations, boolean perpendicular){
        Quaternion newRotation;
        if(notFoundations){
            newRotation = rotation.clone().multLocal(-1.570796f, 0, 0, 1.570796f);
            if(direction > 1) newRotation.multLocal(0, 0, -1.570796f, 1.570796f);
        }else{
            newRotation = rotation.clone();
            if(perpendicular) newRotation.multLocal(0, -1.570796f, 0, 1.570796f);
        } 
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
                //control.setPhysicsLocation(calculateProperLocation(((RigidBodyControl)wall
                //        .getControl(2)).getPhysicsLocation(),
                 //       edge.getWorldTranslation()));
                //control.setPhysicsRotation(calculateProperRotation(rotation,
                //        direction, true, ((Wall)wall).checkPerpendicularity(null)));
            }
        }
    }
    
//    private boolean hasEmptySpace(Node catchNode, boolean perpendicularity){
//        String catchNodeName = catchNode.getName(); 
//        CatchNode[] directions = {CatchNode.NORTH, CatchNode.SOUTH, 
//            CatchNode.EAST, CatchNode.WEST};
//        boolean found = false;
//        for(int i = 0; i < directions.length && !found; i++){
//            if(catchNodeName.equals(directions[i].toString())) found = true; 
//        }
//        if(found){
//            if(perpendicularity) return true; 
//            List<Spatial> catchNodeChildren = catchNode.getChildren(); 
//            if(catchNodeChildren.size() > 2) return false; 
//            boolean empty = true;
//            for(int i = 0; i < 2; i++){
//                empty = ((Node)catchNodeChildren.get(i)).getChildren().isEmpty(); 
//                if(!empty) return empty; 
//            }
//            return empty; 
//        }else return catchNode.getChildren().isEmpty();
//    }
    
    private Node getNearestChild(Node parent, float[] distances, int directionIndex){
        Node nearestChild;
        if(directionIndex < 2)
            nearestChild = distances[3] < distances[2] ? (Node)parent.getChild(0) :
                    (Node)parent.getChild(1);
        else
            nearestChild = distances[1] < distances[0] ? (Node)parent.getChild(0) :
                    (Node)parent.getChild(1);
        if(nearestChild.getChildren().isEmpty()) return nearestChild; 
        return null; 
    }
}