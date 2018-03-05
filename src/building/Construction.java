package building;

import buildingsimulator.ElementName;
import buildingsimulator.GameManager;
import buildingsimulator.PhysicsManager;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.export.InputCapsule;
import com.jme3.export.JmeExporter;
import com.jme3.export.JmeImporter;
import com.jme3.export.OutputCapsule;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.scene.SceneGraphVisitorAdapter;
import com.jme3.scene.Spatial;
import cranes.Hook;
import java.io.IOException;
import java.util.List;
import menu.HUD;
import texts.Translator;

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
    private boolean hit = false, resetWalls = false, sold = false; 
    public Construction(){
        setName(ElementName.BUILDING_BASE_NAME + (++counter));
    }
    
    /**
     * Dodaje kolejną ścianą do konstrukcji. 
     * @param wall1 dodawana ściana 
     * @param wall2 najbliższy element do którego można dołaczyć nową ścianę.
     * Jeśli null, to element jest łaczony z ostatnio dotkniętym elementem. 
     * @param wallMode tryb fizyki dla dodawanego elementu w chwili dodawania 
     */
    public boolean add(Wall wall1, Wall wall2, WallMode wallMode, boolean protruding){
        Spatial recentlyHitObject = wall1.getRecentlyHitObject();
        if(recentlyHitObject != null){ 
            String recentlyHitObjectName = recentlyHitObject.getName(); 
            boolean collisionWithGround = recentlyHitObjectName.equals(ElementName.MAP_FIELD_PART_NAME);
            if(collisionWithGround || recentlyHitObjectName.startsWith(ElementName.WALL_BASE_NAME)){
                Node touchedWall; 
                if(wallMode.equals(WallMode.VERTICAL)){ 
                    touchedWall = merge(wall1, collisionWithGround ? null 
                        : (Wall)recentlyHitObject, false, wallMode, protruding, 0, 4);
                }else{
                    if(wall2 != null) 
                        touchedWall = mergeHorizontal(wall1, wall2, true, wallMode); 
                    else touchedWall = mergeHorizontal(wall1, collisionWithGround ? null :
                            (Wall)recentlyHitObject, false, wallMode);
                }
                if(!getChildren().isEmpty()) renovateBuilding();
                if(touchedWall != null){
                    wall1.setMovable(false);
                    touchedWall.attachChild(wall1);
                    lastAddedWall = wall1; 
                    wall1.setStale(false);
                    wall1.setCatchingLocation(wall1.getLocalTranslation());
                    wall1.setCatchingRotation(wall1.getLocalRotation());
                    wall1.setProtrudingCatched(protruding);
                    return true;
                }
            }
        }
        return false;
    }
    
    /**
     * Zwraca całą budowlę do której należy obiekt. 
     * @param wall sprawdzany obiekt 
     * @return budowlę do której nalezy obiekt lub null jeśli nie należy do budowli  
     */
    public static Construction getWholeConstruction(Spatial wall){
        Node wallParent = wall.getParent(); 
        while(wallParent != null){  
            if(wallParent.getName().startsWith(ElementName.BUILDING_BASE_NAME))
                return (Construction)wallParent; 
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
        List<Spatial> gameObjects = GameManager.getGameObjects();
        float min = -1;
        int objectsNumber = gameObjects.size(); 
        Spatial minWall = null; 
        Vector3f wallLocation = wall.getWorldTranslation();
        for(int i = 0; i < objectsNumber; i++){
            Spatial object = gameObjects.get(i);
            if(object.getName().startsWith(ElementName.BUILDING_BASE_NAME)){
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
        GameManager.addToScene(wall);
        lastAddedWall = null;
        wall.setMovable(true);
    }
    
    /**
     * Uaktualnia stan budynku. Jeśli jakieś obiekty zostały uderzony lub nie 
     * posiadają elementów trzymających je, zaczynają spadać powodując rozsypywanie 
     * się budynku.
     */
    public void updateState() {
        final boolean isSample = name.contains("sample");
        breadthFirstTraversal(new SceneGraphVisitorAdapter() {
            @Override
            public void visit(Node object) {
                if(object.getName().startsWith("Wall") && object.getWorldTranslation().y >= 0.4f) {
                    Wall wall = (Wall)object;
                    wall.setMovable(true);
                    if((isSample ? 5 : 3) < (isSample ? wall.getLocalTranslation() : wall.getWorldTranslation())
                            .distance(wall.getCatchingLocation())){ 
                        boolean wallStateChanged = false; 
                        if(!wall.isStale()){
                            wallStateChanged = true; 
                            detachWall(wall);
                        }
                        if(wallStateChanged) resetWalls = true; 
                    } else {
                        if(!wall.isStale())
                        wall.setMovable(false);
                    }
                }
            }
        });
    }
    
    public void detachWall(Wall wall) {
        wall.depthFirstTraversal(new SceneGraphVisitorAdapter() {
            @Override
            public void visit(Node object) {
                if(object.getName().startsWith("Wall")) {
                    Wall wall = (Wall)object; 
                    wall.removeFromParent();
                    GameManager.addToScene(wall);
                    wall.setStale(true);
                    wall.setMovable(true);
                }
            }
        });
    }
    
    /**
     * Odnawia budynek. 
     */
    public void renovateBuilding() {
        depthFirstTraversal(new SceneGraphVisitorAdapter() {
            @Override
            public void visit(Node object) {
                if(object.getName().startsWith("Wall")) {
                    Wall wall = (Wall)object; 
                    if(!wall.isStale()) {
                        wall.setLocalRotation(wall.getCatchingRotation().clone());
                        wall.setLocalTranslation(wall.getCatchingLocation().clone());
                    }
                }
            }
        });
    }
    
    /**
     * Przywraca zapisany budynek. 
     * @param wall pierwszy element budynku 
     */
    public static void restoreConstruction(Construction building) {
        building.breadthFirstTraversal(new SceneGraphVisitorAdapter() {
            @Override
            public void visit(Node object) {
                if(object.getName().startsWith(ElementName.WALL_BASE_NAME)) {
                    PhysicsManager.addPhysicsToGame(object);
                }
            }}
        );
    }
    
    @Override
    public void write(JmeExporter ex) throws IOException {
        super.write(ex);
        OutputCapsule capsule = ex.getCapsule(this);
        capsule.write(sold, "SOLD", false);
    }
    
    @Override
    public void read(JmeImporter im) throws IOException {
        super.read(im);
        InputCapsule capsule = im.getCapsule(this);
        sold = capsule.readBoolean("SOLD", false);
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
    
    /**
     * Określa czy budynek został już oceniony i sprzedany. 
     * @return true jeśli budynek jest już sprzedany, false jeśli jest w trakcie budowy 
     */
    public boolean isSold() { return sold; }
    
    /**
     * Ustawia czy budynek został już oceniony i sprzedany. 
     * @param sold true jeśli budynek jest już sprzedany, false jeśli jest w trakcie budowy 
     */
    public void setSold(boolean sold) { this.sold = sold; }
    
    /**
     * Zwraca licznik budynków. 
     * @return ilość budynków 
     */
    public static int getCounter() { return Construction.counter; }
    
    /**
     * Ustawia licznik budynków. 
     * @param counter nowa wartość
     */
    public static void setCounter(int counter) { Construction.counter = counter; }
    
    protected Node merge(Wall wall1, Wall wall2, boolean ceiling, WallMode mode,
            boolean protruding, int start, int end){
        if(wall2 != null){ 
            Vector3f location = ((RigidBodyControl)wall1.getControl(mode.ordinal())).getPhysicsLocation();
            List<Spatial> wallChildren = wall2.getChildren(); 
            Node[] catchNodes = new Node[4];
            Vector3f[] catchNodesLocations = new Vector3f[4];
            float[] distances = new float[4];
            for(int i = start; i < end; i++){ 
                int index = i % 4;
                catchNodes[index] = (Node)wallChildren.get(i + 1);
                catchNodesLocations[index] = catchNodes[index].getWorldTranslation(); 
                distances[index] = location.distance(catchNodesLocations[index]); 
            }
            boolean perpendicularity = wall1.checkPerpendicularity(wall2); 
            int minDistance = getMin(distances); 
            if(setWallInProperPosition(wall1, wall2, catchNodes[minDistance], perpendicularity, 
                    ceiling, mode.equals(WallMode.HORIZONTAL), protruding))
                return catchNodes[minDistance];
            return null;
        }
        return this;
    }
    
    protected Node mergeHorizontal(Wall wall1, Wall wall2, boolean foundations,
            WallMode mode){
        if(!foundations){
            if(wall2 != null){
                // sprawdza czy na przeciwko jest druga ściana 
                boolean oppositeWall = getWallFromOpposite(wall1, wall2) != null;
                if(oppositeWall) {
                    boolean ceiling = (int)(wall1.getWorldTranslation().y 
                            - wall2.getWorldTranslation().y) > 0;
                    Node catchNode = merge(wall1, wall2, ceiling, mode, false,
                            4, 8);
                    if(ceiling) {
                        Hook hook = GameManager.getActualUnit().getHook(); 
                        hook.heighten();
                        hook.heighten(); 
                    }
                    return catchNode;
                }
                return oppositeWall ? 
                        merge(wall1, wall2, (int)(wall1.getWorldTranslation()
                        .y - wall2.getWorldTranslation().y) > 0, mode, false, 4, 8)
                        : null;
            }
        }else{
            return merge(wall1, wall2, false, mode, false, 4, 8); 
        }
        return this; 
    }
    
    protected boolean setWallInProperPosition(Wall wall1, Wall wall2, Node catchNode,
            boolean perpendicularity, boolean ceiling, boolean horizontal, 
            boolean protruding) {
        RigidBodyControl control = wall1.getControl(RigidBodyControl.class);
        RigidBodyControl control2 = wall2.getControl(RigidBodyControl.class);
        Node newCatchNode = createCatchNode(wall1, wall2, catchNode,
                perpendicularity, ceiling, protruding);
        if(newCatchNode != null) {
            Quaternion q2 = control2.getPhysicsRotation().clone(); 
            control2.setPhysicsRotation(new Quaternion(q2.getX(), 0, q2.getZ(),
                    q2.getW()));
            wall2.attachChild(newCatchNode);
            control2.setPhysicsRotation(q2);
            Vector3f edgeLocation = newCatchNode.getWorldTranslation();
            control.setPhysicsLocation(edgeLocation);
            control.setPhysicsRotation(calculateProperRotation(wall2,
                    catchNode, !horizontal, perpendicularity, ceiling));
            wall2.detachChild(newCatchNode);
            return true;
        }
        HUD.setMessage(Translator.NO_ENOUGH_PLACE.getValue());
        return false;
    }
    
    private int getMin(float[] distances){
        int min = 0; 
        for(int i = 1; i < 4; i++)
            if(distances[min] > distances[i]) min = i; 
        return min; 
    }
    
    private Node createCatchNode(Wall wall1, Wall wall2, Node parent, boolean perpendicularity,
            boolean ceiling, boolean protruding) {
        String parentName = parent.getName();
        Node node = new Node(parentName + " - child");
        boolean bottom = false, up = false, right = false, left = false, south = false,
                north  = false;
        float coordinate; 
        if(parentName.equals(CatchNode.BOTTOM.toString())) bottom = true;
        else if(parentName.equals(CatchNode.UP.toString())) up = true;
        else if(parentName.equals(CatchNode.RIGHT.toString())) right = true;
        else if(parentName.equals(CatchNode.LEFT.toString())) left = true;
        else if(parentName.equals(CatchNode.SOUTH.toString())) south = true; 
        else if(parentName.equals(CatchNode.NORTH.toString())) north = true; 
        List<Spatial> parentChildren = parent.getChildren(); 
        int childrenCount = parentChildren.size(); 
        float sum = 0;
        boolean bottomUp = bottom || up; 
        if(bottomUp || left || right) {
            EdgeInformation information = new EdgeInformation(this, wall2,
                    CatchNode.valueOf(parentName)); 
            sum -= getBusyPlace(information);
            float busyPlaceAfterMerged = -sum + wall1.getLength() * 2,
                    checkingDimension = (bottomUp ? wall2.getLength() : wall2.getHeight()) * 2,
                    busyPlaceOfPerpendicularToEnd = 0;
            Wall perpendicularToEnd = information.getPerpendicularToEnd();
            if(perpendicularToEnd != null) {
                busyPlaceOfPerpendicularToEnd = perpendicularToEnd.getWidth();
                if(!perpendicularToEnd.isProtrudingCatched()) 
                    busyPlaceOfPerpendicularToEnd *= 2; 
            }
            if(busyPlaceAfterMerged > checkingDimension - busyPlaceOfPerpendicularToEnd)
                return null;
        } else {
            boolean southOrNorth = south || north;
            for(int i = 0; i < childrenCount; i++) {
                Wall wall = ((Wall)parentChildren.get(i));
                if(wall.checkPerpendicularity(wall2)) 
                    sum -= (southOrNorth ? wall.getHeight() : wall.getLength()) * 2;
                else sum -= (southOrNorth ? wall.getLength() : wall.getHeight()) * 2;
            }
        }
        if(bottom || up) {
            coordinate = sum + wall2.getLength() - wall1.getLength();
        } else {
            if(left || right){
                coordinate = sum + wall2.getHeight() - wall1.getLength();
            } else {
                boolean southOrNorth = south || north; 
                coordinate = sum - CatchNode.getProperFoundationsDimension(wall1, 
                        perpendicularity, !southOrNorth, false) + (southOrNorth 
                        ? wall2.getLength() : wall2.getHeight());
            }
        }
        node.setLocalTranslation(CatchNode.calculateTranslation(CatchNode
                .valueOf(parentName), wall2, wall1, perpendicularity, ceiling,
                coordinate, protruding));
        return node;
    }
    
    private Quaternion calculateProperRotation(Wall ownerRotation, Node catchNode,
            boolean vertical, boolean perpendicular, boolean ceiling){
        if(ceiling) return ownerRotation.getParent().getParent().getWorldRotation();
        Quaternion newRotation = ownerRotation.getControl(RigidBodyControl.class)
                .getPhysicsRotation();
        if(vertical){
            newRotation = newRotation.clone().multLocal(-1.570796f, 0, 0, 1.570796f);
            String catchNodeName = catchNode.getName(); 
            if(catchNodeName.equals(CatchNode.LEFT.toString()) 
                    || catchNodeName.equals(CatchNode.RIGHT.toString()))
                newRotation.multLocal(0, 0, -1.570796f, 1.570796f);
        }else{
            newRotation = newRotation.clone();
            if(perpendicular) newRotation.multLocal(0, -1.570796f, 0, 1.570796f);
        } 
        return newRotation;
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
    
    protected Node getWallFromOpposite(Wall wall, Wall recentlyHitWall){
        List<Spatial> hitObjects = wall.getHitObjects();
        int numberHitObjects = hitObjects.size(); 
        for(int i = 0; i < numberHitObjects; i++){
            Spatial hitObject = hitObjects.get(i); 
            if(hitObject.getName().startsWith(ElementName.WALL_BASE_NAME) && !recentlyHitWall
                    .checkPerpendicularity((Wall)hitObject) && (int)(recentlyHitWall
                    .getWorldTranslation().y - hitObject.getWorldTranslation().y) == 0)
                return (Node)hitObject; 
        }
        return null; 
    }
    
    private float getBusyPlace(EdgeInformation information) {
        List<Spatial> edgeWalls = information.getEdgeWalls(), 
                neighborFloorWalls = information.getNeighborFloorWalls(); 
        int edgeWallsNumber = edgeWalls.size(), 
                neighborFloorWallsNumber = neighborFloorWalls.size();
        float sum = 0; 
        for(int i = 0; i < edgeWallsNumber; i++) 
            sum += ((Wall)edgeWalls.get(i)).getLength() * 2; 
        for(int i = 0; i < neighborFloorWallsNumber; i++)
            sum += ((Wall)neighborFloorWalls.get(i)).getLength() * 2; 
        if(sum == 0) {
            Wall wall = information.getPerpendicularToStart(); 
            if(wall != null) {
                sum += wall.getWidth();
                if(!wall.isProtrudingCatched()) sum *= 2; 
            } 
        }
        return sum;
    }
    
    private float getBusyPlace(Wall wall2, Wall floor, CatchNode edge,
            boolean firstWall) {
        float innerSum = 0;
        // sprawdzanie dla dotknietego catch node 
        if(firstWall) {
            List<Spatial> floorEdgeChildren = ((Node)floor.getChild(edge.toString()))
                    .getChildren();
            int floorEdgeChildrenCount = floorEdgeChildren.size();
            for(int i = 0; i < floorEdgeChildrenCount; i++) {
                innerSum += ((Wall)floorEdgeChildren.get(i)).getLength() * 2;
            } 
            edge = getEdgeFromOpposite(edge);
        }
        List<Spatial> wallElements = wall2.getChildren(); 
        int end = CatchNode.values().length;
        // poruszanie się po wszystkich ścianach 
        for(int i = 1; i <= end; i++) {
            List<Spatial> catchNodeChildren = ((Node)wallElements.get(i)).getChildren(); 
            int childrenCount = catchNodeChildren.size(); 
            for(int k = 0; k < childrenCount; k++) { 
                float busyPlace = getBusyPlace((Wall)catchNodeChildren.get(k), floor,
                        edge, false);
                if(busyPlace != -1) return innerSum + busyPlace;
            }
        }
        Vector3f floorLocation = floor.getWorldTranslation(), 
                wall2Location = wall2.getWorldTranslation();
        float width = floor.getWidth();
        if(wall2.equals(floor)) return innerSum;
        boolean soughtFloor = wall2Location.y < floorLocation.y + width && wall2Location.y >
                floorLocation.y - width && floorLocation.distance(wall2Location)
                <= floor.getHeight() + wall2.getHeight() + 0.1f; 
        /* znalezienie podłogi sąsiadującej z dotkniętą krawędzią i sprawdzenie 
        zajętego miejsca po przeciwnej stronie */
        if(soughtFloor) {
            float sum = innerSum;
            List<Spatial> edgeChildren = ((Node)wall2.getChild(edge.toString()))
                    .getChildren();
            int edgeChildrenCount = edgeChildren.size();
            for(int i = 0; i < edgeChildrenCount; i++) {
                sum += ((Wall)edgeChildren.get(i)).getLength() * 2;
            }
            return sum; 
        }
        return -1;
    }
    
    private CatchNode getEdgeFromOpposite(CatchNode edge) {
        CatchNode[] edges = {CatchNode.BOTTOM, CatchNode.UP, CatchNode.RIGHT, 
            CatchNode.LEFT};
        for(int i = 0; i < edges.length; i++)
            if(edges[i].equals(edge)) return edges[i + (i % 2 == 0 ? 1 : -1)];
        return null;
    }
    
    private boolean isEmptyPerpendicularEdge(String catchNodeName, Wall floor, boolean start) {
        CatchNode catchNodePerpendicular;
        float maxBusy = 0;
        if(catchNodeName.equals(CatchNode.UP.toString())) {
            catchNodePerpendicular = start ? CatchNode.LEFT : CatchNode.RIGHT;
        } else {
            if(catchNodeName.equals(CatchNode.RIGHT.toString())) {
                catchNodePerpendicular =  start ? CatchNode.UP : CatchNode.BOTTOM;
                maxBusy = (floor.getLength() - floor.getWidth()) * 2;
            } else {
                if(catchNodeName.equals(CatchNode.BOTTOM.toString())) {
                    catchNodePerpendicular = start ? CatchNode.LEFT : CatchNode.RIGHT;
                    maxBusy = (floor.getHeight() - floor.getWidth()) * 2;
                } else {
                    catchNodePerpendicular = start ? CatchNode.UP : CatchNode.BOTTOM;
                }
            }
        }
        return getBusyPlace((Wall)getChildren().get(0), floor, 
                catchNodePerpendicular, true) <= maxBusy;
    }
}