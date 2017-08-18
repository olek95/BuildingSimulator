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
 * Konstrukcja budynku jest stworzona na zasadzie drzewa, tzn. każda ściana 
 * posiada połączone z nią dzieci. 
 * @author AleksanderSklorz 
 */
public class Construction extends Node{
    private static int counter = -1; 
    private Wall lastAddedWall; 
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
                    Node touchedWall; 
                    if(wallMode == 2){ 
                        touchedWall = merge(wall1, collisionWithGround ? null 
                            : (Wall)recentlyHitObject, false, wallMode);
                    }else{
                        if(wall2 != null) 
                            touchedWall = mergeHorizontal(wall1, wall2, true, wallMode); 
                        else touchedWall = mergeHorizontal(wall1, collisionWithGround ? null :
                                (Wall)recentlyHitObject, false, wallMode);
                    }
                    if(touchedWall != null){
                        touchedWall.attachChild(wall1);
                        lastAddedWall = wall1; 
                        //if(!collisionWithGround) correctLocations(touchedWall.getName()); 
                        RigidBodyControl control = wall1.getControl(RigidBodyControl.class);
                        control.setAngularDamping(1);
                        control.setLinearDamping(1);
                        System.out.println("DODAD");
                        for(Spatial s : BuildingSimulator.getBuildingSimulator().getRootNode().getChildren()){
                            System.out.println(s); 
                            if(s.getName().startsWith("Building")){
                                for(int i = 0; i < ((Node)s).getChildren().size(); i++)
                                    System.out.println("A+" + ((Node)s).getChild(i));
                            }
                        }
                        System.out.println("______");
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
        int objectsNumber = gameObjects.size(); 
        Spatial minWall = null; 
        Vector3f wallLocation = wall.getWorldTranslation();
        for(int i = 0; i < objectsNumber; i++){
            Spatial object = gameObjects.get(i); 
            if(object.getName().startsWith("Building")){
                Node building = (Node)object; 
                if(minWall == null){
                    minWall = building.getChild(0);
                    min = wallLocation.distance(minWall.getWorldTranslation());
                }
                Spatial suspectMinWall = getNearestChildFromWall(wallLocation, building
                        .getChild(0), wall.getWorldTranslation().distance(minWall
                        .getWorldTranslation())); 
                if(suspectMinWall != null){
                    float suspectMin = wall.getWorldTranslation().distance(suspectMinWall
                            .getWorldTranslation());
                    if(min > suspectMin){
                        min = suspectMin;
                        minWall = suspectMinWall; 
                    }
                }
            }
        }
        if(minWall != null){
            Wall nearestWall = (Wall)minWall; 
            // założony możliwy błąd dla odległości 
            return min < Math.ceil(wall.getMaxSize()) + Math.ceil(nearestWall.getMaxSize())
                    ? nearestWall : null; 
        }else return null; 
    }
    
    /**
     * Usuwa ścianę z budowli. Jeśli budowla już nie ma innych ścian, wtedy usuwa 
     * dodatkowo całą budowlę z drzewa gry. 
     * @param wall usuwana ściana 
     */
    public void removeWall(Wall wall){
        if(wall.getParent().equals(this)) this.removeFromParent();
        wall.removeFromParent();
    }
    
    /**
     * Zwraca ostatnio dodaną ścianę. 
     * @return ostatnio dodana ściana
     */
    public Wall getLastAddedWall(){ return lastAddedWall; }
    
    private Node merge(Wall wall1, Wall wall2, boolean foundations, int mode){
        if(wall2 != null){ 
            Vector3f location = ((RigidBodyControl)wall1.getControl(mode)).getPhysicsLocation();
            List<Spatial> wallChildren = wall2.getChildren(); 
            Node[] catchNodes = new Node[4];
            Vector3f[] catchNodesLocations = new Vector3f[4];
            float[] distances = new float[4];
            for(int i = 0; i < 4; i++){
                catchNodes[i] = (Node)wallChildren.get(i + 1);
                catchNodesLocations[i] = catchNodes[i].getWorldTranslation(); 
                distances[i] = location.distance(catchNodesLocations[i]); 
            }
            boolean perpendicularity = wall1.checkPerpendicularity(wall2); 
            int minDistance = getMin(distances, foundations, location, wall2), 
                    i = minDistance > 3 ? 0 : minDistance; 
            if(foundations) 
                catchNodes[i] = (Node)wallChildren.get(minDistance); 
            if(catchNodes[i].getChildren().isEmpty()){
                boolean ceiling = mode == 1 && (int)(location.y - 
                        wall2.getWorldTranslation().y) != 0; 
                if(foundations || ceiling){
                    catchNodes[i] = wall2.changeCatchNodeLocation(wall1, 
                            catchNodes[i], minDistance, perpendicularity, ceiling); 
                    catchNodesLocations[i] = catchNodes[i].getWorldTranslation();
                }
                RigidBodyControl control = wall1.getControl(RigidBodyControl.class);
                control.setPhysicsLocation(calculateProperLocation(
                        catchNodesLocations[i], foundations, wall1, mode));
                control.setPhysicsRotation(calculateProperRotation(wall2,
                        i, !foundations, perpendicularity, ceiling));
                return catchNodes[i];
            }else return null; 
        }
        return this;
    }
    
    private Node mergeHorizontal(Wall wall1, Wall wall2, boolean foundations, int mode){
        if(!foundations){
            if(wall2 != null){
                // sprawdza czy na przeciwko jest druga ściana 
                return getWallFromOpposite(wall1, wall2) != null ? 
                        merge(wall1, wall2, true, mode) : null;
            }
        }else{
            return merge(wall1, wall2, true, mode); 
        }
        return this; 
    }
    
    private int getMin(float[] distances, boolean foundations, Vector3f wallLocation,
            Wall wall){
        int min = 0; 
        for(int i = 1; i < 4; i++)
            if(distances[min] > distances[i]) min = i; 
        if(foundations){
            int offset = min * 2, i1 = 6 + offset, i2 = 7 + offset;  
            return wall.getChild(i1).getWorldTranslation().distance(wallLocation) 
                    < wall.getChild(i2).getWorldTranslation().distance(wallLocation) ?
                    i1 : i2; 
        }
        return min; 
    }
    
    private Vector3f calculateProperLocation(Vector3f edgeLocation,
            boolean foundations, Wall wall, int mode){
        return new Vector3f(edgeLocation.x, foundations || mode == 1 ?
                ((RigidBodyControl)wall.getControl(mode)).getPhysicsLocation().y
                : edgeLocation.y + wall.getHeight(), edgeLocation.z);
    }
    
    private Quaternion calculateProperRotation(Wall ownerRotation, int direction,
            boolean notFoundations, boolean perpendicular, boolean ceiling){
        if(ceiling) return ownerRotation.getParent().getParent().getWorldRotation();
        Quaternion newRotation = ownerRotation.getControl(RigidBodyControl.class)
                .getPhysicsRotation();
        if(notFoundations){
            newRotation = newRotation.clone().multLocal(-1.570796f, 0, 0, 1.570796f);
            if(direction > 1) newRotation.multLocal(0, 0, -1.570796f, 1.570796f);
        }else{
            newRotation = newRotation.clone();
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
    
    private static Spatial getNearestChildFromWall(Vector3f wallLocation,
            Spatial wallFromTree, float min){
        List<Spatial> wallFromTreeChildren = ((Node)wallFromTree).getChildren(); 
        Spatial nearestWall = wallFromTree;
        for(int i = 6; i < 14; i++){ // przechodzi po wszystkich stronach ściany
            Node side = (Node)wallFromTreeChildren.get(i);
            if(!side.getChildren().isEmpty()){
                Spatial nextWall = getNearestChildFromWall(wallLocation,
                        (Node)side.getChild(0), min);
                if(nextWall != null){
                    float distance = nextWall.getWorldTranslation().distance(wallLocation);
                    if(min > distance){
                        nearestWall = nextWall; 
                        min = distance;
                    }
                }
            }
        }
        return wallLocation.distance(nearestWall.getWorldTranslation()) <= min 
                ? nearestWall : null;
    }
    
    private Node getWallFromOpposite(Wall wall, Wall recentlyHitWall){
        List<Spatial> hitObjects = wall.getHitObjects();
        int numberHitObjects = hitObjects.size(); 
        for(int i = 0; i < numberHitObjects; i++){
            Spatial hitObject = hitObjects.get(i); 
            if(hitObject.getName().startsWith("Wall") && !recentlyHitWall
                    .checkPerpendicularity((Wall)hitObject) && (int)(recentlyHitWall
                    .getWorldTranslation().y - hitObject.getWorldTranslation().y) == 0)
                return (Node)hitObject; 
        }
        return null; 
    }
}