package building;

import buildingsimulator.BuildingSimulator;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

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
    private boolean hit = false, resetWalls = false; 
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
            boolean collisionWithGround = recentlyHitObjectName.startsWith("terrain-gameMap");
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
                    wall1.setMovable(false);
                    wall1.setStale(false);
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
     * @param measurableObject obiekt dla którego szukamy najbliższy element z jakiegoś budynku. 
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
            return min < 2 * Math.ceil(wall.getMaxSize()) + Math.ceil(nearestWall.getMaxSize())
                    ? nearestWall : null; 
        }else return null; 
    }
    
    /**
     * Usuwa ścianę z budowli. Jeśli budowla już nie ma innych ścian, wtedy usuwa 
     * dodatkowo całą budowlę z drzewa gry. 
     * @param wall usuwana ściana 
     */
    public void removeWall(Wall wall){
        if(wall.getParent().equals(this)) removeFromParent();
        wall.removeFromParent();
        BuildingSimulator.getBuildingSimulator().getRootNode().attachChild(wall);
        lastAddedWall = null;
        wall.setMovable(true);
    }
    
    /**
     * Uaktualnia stan budynku. Jeśli jakieś obiekty zostały uderzony lub nie 
     * posiadają elementów trzymających je, zaczynają spadać powodując rozsypywanie 
     * się budynku.
     * @param element element którego dzieci są sprawdzane 
     * @return sprawdzany element 
     */
    public Spatial updateState(Node element){
//        List<Spatial> buildingWalls = element.getChildren(); 
//        for(int i = 1; i < 14; i++){ 
//            Node side = (Node)buildingWalls.get(i);
//            if(!side.getChildren().isEmpty()){
//                Spatial nextWall = updateState((Node)side.getChild(0));
//                if(nextWall != null){
//                    Wall wall = (Wall)nextWall;
//                    wall.setMovable(true);
//                    float distance = nextWall.getWorldTranslation()
//                            .distance(side.getWorldTranslation());
//                    if(wall.getHeight() + 0.01f < distance){ // umowna granica dozwolonego przesunięcia ściany
//                        //removeWall(wall);
//                        boolean ceilingStateChanged = false, wallStateChanged = false; 
//                        if(!wall.isStale()){
//                            wallStateChanged = true; 
//                            wall.setStale(true);
//                        }
//                        if(i <= 4){
//                            CatchNode[] ceilingPartsSides = {CatchNode.NORTH_0, 
//                                CatchNode.NORTH_1};
//                            for(int j = 0; j < 2; j++){
//                                List<Spatial> northChildren = ((Node)wall
//                                    .getChild(ceilingPartsSides[j].toString())).getChildren();
//                                int northChildrenAmount = northChildren.size();
//                                for(int k = 0; k < northChildrenAmount; k++){
//                                    Wall ceiling = (Wall)northChildren.get(k);
//                                    //removeWall(ceiling); 
//                                    ceiling.setMovable(true);
//                                    if(!ceiling.isStale()){
//                                        ceiling.setStale(true);
//                                        ceilingStateChanged = true; 
//                                    }
//                                }
//                            }
//                        }
//                        if(wallStateChanged || ceilingStateChanged) resetWalls = true; 
//                    }else  wall.setMovable(false);
//                }
//            }
//        }
        return element;
    }
    
    /**
     * Zwraca ostatnio dodaną ścianę. 
     * @return ostatnio dodana ściana
     */
    public Wall getLastAddedWall(){ return lastAddedWall; }
    
    /**
     * Sprawdza czy budynek został uderzony. 
     * @return true jeśli został uderzony, false w przeciwnym przypadku 
     */
    public boolean isHit() { return hit; }
    
    /**
     * Określa czy budynek został uderzony. 
     * @param hit true jeśli budynek został uderzony, false w przeciwnym przypadku 
     */
    public void setHit(boolean hit) { this.hit = hit; } 
    
    /** Zwraca informację czy stan ścian został zresetowany np. po zburzeniu 
     * budynków. 
     * @return true jeśli stan ścian został zresetowany, false w przeciwnym przypadku.  
     */
    public boolean isResetWalls() { return resetWalls; }
    
    /**
     * Ustawia informację o zresetowaniu stanu ścian. 
     * @param resetWalls true jeśli stan ścian został zresetowany, false w przeciwnym 
     * przypadku 
     */
    public void setResetWalls(boolean resetWalls) { this.resetWalls = resetWalls; }
    
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
            if(foundations) {
                catchNodes[i] = (Node)wallChildren.get(minDistance); 
            }
//            if(checkIfCanBeAdded(catchNodes[i], wall1)){
                boolean ceiling = mode == 1 && (int)(location.y - 
                        wall2.getWorldTranslation().y) > 0; 
//                if(foundations || ceiling){
//                    catchNodes[i] = wall2.changeCatchNodeLocation(wall1, 
//                            catchNodes[i], minDistance, perpendicularity, ceiling); 
//                    catchNodesLocations[i] = catchNodes[i].getWorldTranslation();
//                }
                if(foundations) {
                    catchNodesLocations[i] = catchNodes[i].getWorldTranslation();
                }
                
                RigidBodyControl control = wall1.getControl(RigidBodyControl.class);
                RigidBodyControl control2 = wall2.getControl(RigidBodyControl.class);
                Quaternion q2 = control2.getPhysicsRotation().clone(); 
                control2.setPhysicsRotation(new Quaternion(q2.getX(), 0, q2.getZ(),
                        q2.getW()));
                
                Node catchNode = createCatchNode(wall1, wall2, catchNodes[i],
                        perpendicularity, ceiling);
                wall2.attachChild(catchNode);
                control2.setPhysicsRotation(q2);
                
                Vector3f edgeLocation = catchNode.getWorldTranslation();
                control.setPhysicsLocation(edgeLocation);
                control.setPhysicsRotation(calculateProperRotation(wall2,
                        i, !foundations, perpendicularity, ceiling));
                wall2.detachChild(catchNode);
                return catchNodes[i];
                
//                wall1.getControl(RigidBodyControl.class)
//                        .setPhysicsRotation(new Quaternion(q1.getX(),0,0,q1.getW()));
//                wall1.getControl(RigidBodyControl.class)
//                        .setPhysicsLocation(new Vector3f(wall2.getWorldTranslation().x,
//                        wall1.getWidth() + wall2.getHeight(), wall2.getWorldTranslation().z
//                        - wall2.getHeight() + wall2.getWidth()));
//                control2.setPhysicsRotation(q2);
//                wall1.getControl(RigidBodyControl.class).setPhysicsRotation(q1);
//                control.setPhysicsLocation(calculateProperLocation(
//                        node.getWorldTranslation(), wall1, mode));
//            }else return null; 
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
            return min + 5; 
        }
        return min; 
    }
    
    private Node createCatchNode(Wall wall1, Wall wall2, Node parent, boolean perpendicularity,
            boolean ceiling) {
        String parentName = parent.getName();
        Node node = new Node(parentName + " - child");
        boolean bottom = false, up = false, right = false, left = false, south = false,
                north  = false, east = false;
        float coordinate, sum = 0; 
        if(parentName.equals(CatchNode.BOTTOM.toString())) bottom = true;
        else if(parentName.equals(CatchNode.UP.toString())) up = true;
        else if(parentName.equals(CatchNode.RIGHT.toString())) right = true;
        else if(parentName.equals(CatchNode.LEFT.toString())) left = true;
        else if(parentName.equals(CatchNode.SOUTH.toString())) south = true; 
        else if(parentName.equals(CatchNode.NORTH.toString())) north = true; 
        else if(parentName.equals(CatchNode.EAST.toString())) east = true; 
        List<Spatial> parentChildren = parent.getChildren(); 
        System.out.println(parentName);
        int childrenCount = parentChildren.size(); 
        if(bottom || up || left || right) {
            for(int i = 0; i < childrenCount; i++) {
                if(bottom || up) sum -= ((Wall)parentChildren.get(i)).getLength() * 2; 
                else sum -= ((Wall)parentChildren.get(i)).getHeight() * 2; 
            }
        } else {
            for(int i = 0; i < childrenCount; i++) {
                Wall wall = ((Wall)parentChildren.get(i));
                if(wall.checkPerpendicularity(wall2)) 
                    sum -= wall.getHeight() * 2;
                else sum -= wall.getLength() * 2;
            }
        }
        if(bottom || up) {
            coordinate = sum + wall2.getLength() - wall1.getLength();
        } else {
            if(left || right){
                coordinate = sum + wall2.getHeight() - wall1.getHeight();
            } else {
                if(south || north) {
                    coordinate = sum + wall2.getLength() - CatchNode
                            .getProperFoundationsDimension(wall1, perpendicularity, 
                            false, false);
                } else {
                    coordinate = sum + wall2.getHeight() - CatchNode
                            .getProperFoundationsDimension(wall1, perpendicularity, false, false);
                }
            }
        }
        node.setLocalTranslation(CatchNode.calculateTranslation(CatchNode
                .valueOf(parentName), wall2, wall1, perpendicularity, ceiling, coordinate));
        return node;
    }
    
    private Vector3f calculateProperLocation(Vector3f edgeLocation, Wall wall, int mode){
        return null; 
    }
//    private Vector3f calculateProperLocation(Vector3f edgeLocation, Wall wall, int mode){
//        return new Vector3f(edgeLocation.x, mode == 1 ?
//                ((RigidBodyControl)wall.getControl(mode)).getPhysicsLocation().y
//                : edgeLocation.y + wall.getHeight(), edgeLocation.z);
//    }
    
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
        int end = 5 + CatchNode.values().length - 4; 
        for(int i = 5; i < end; i++){ // przechodzi po wszystkich stronach ściany
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
    
    private boolean checkIfCanBeAdded(Node catchNode, Wall newWall){
        if(!newWall.isStale()) return catchNode.getChildren().isEmpty();
        else{
            List<Spatial> catchNodeChildren = catchNode.getChildren(); 
            if(catchNodeChildren.isEmpty()) return true; 
            else {
                Wall wall = (Wall)catchNodeChildren.get(0); 
                if(wall.equals(newWall)){
                    removeWall(wall); 
                    return true;
                }else{
                    float distance = wall.getWorldTranslation().distance(catchNode
                            .getWorldTranslation());
                    CatchNode[] catchNodes = CatchNode.values();
                    CatchNode node = CatchNode.valueOf(catchNode.getName());
                    int index = -1; 
                    for(int i = 0; i < catchNodes.length; i++)
                        if(node.equals(catchNodes[i])) index = i; 
                    if(index <= 3) {
                        if(distance > wall.getHeight()){
                            removeWall(wall); 
                            return true; 
                        }
                        return false; 
                    }else{
                        if(distance > wall.getWidth()){
                            removeWall(wall); 
                            return true; 
                        }
                        return false;
                    }
                }
            }
        }
    }
}