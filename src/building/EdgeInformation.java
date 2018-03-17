package building;

import buildingsimulator.ElementName;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.scene.SceneGraphVisitorAdapter;
import com.jme3.scene.Spatial;
import java.util.ArrayList;
import java.util.List;

/**
 * Klasa <code>EdgeInformation</code> reprezentuje wszystkie najważniejsze 
 * informacje o wybranej krawędzi. Udostępnia takie informacje jak ściany położone 
 * przy tej krawędzi (lub na połączeniu tej z sąsiadującą krawędzią), sąsiadujące
 * podłogi czy ściany prostopadłe do tej krawędzi. 
 * @author AleksanderSklorz 
 */
public class EdgeInformation {
    private List<Spatial> edgeWalls, neighborFloorWalls;
    private Wall perpendicularToStart, perpendicularToEnd;
    private Construction building;
    private List<Spatial> temporaryEdgeChildren = null;
    private List<Node> neighborFloors;
    public EdgeInformation(Construction building, Wall floor, CatchNode edge) {
        this.building = building;
        Node floorEdge = (Node)floor.getChild(edge.toString());
        edgeWalls = floorEdge.getChildren();
        neighborFloors = findNeighborFloors((Wall)building.getChild(0), floor, edge);
        neighborFloorWalls = getNeighborFloorsWalls(neighborFloors, edge); 
        Vector3f edgeLocation = floorEdge.getWorldTranslation();
        boolean upLeft = edge.equals(CatchNode.LEFT) || edge.equals(CatchNode.UP);
        perpendicularToStart = findWallFromPerpendicularEnd(floor, edgeLocation,
                getPerpendicularEdge(edge, true), upLeft); 
        perpendicularToEnd = findWallFromPerpendicularEnd(floor, edgeLocation,
                getPerpendicularEdge(edge, false), upLeft);
    }
    
    /**
     * Zwraca listę ścian położonych przy tej krawędzi. 
     * @return lista ścian dla tej krawędzi 
     */
    public List<Spatial> getEdgeWalls() { return edgeWalls; }
    
    /**
     * Zwraca listę ścian położonych przy sąsiadującej krawędzi. 
     * @return lista ścian dla sąsiadującej krawędzi 
     */
    public List<Spatial> getNeighborFloorWalls() { return neighborFloorWalls; }
    
    /**
     * Zwraca ścianę prostopadłą do początku tej krawędzi. 
     * @return prostopadła ściana do początku krawędzi 
     */
    public Wall getPerpendicularToStart() { return perpendicularToStart; }
    
    /**
     * Zwraca ścianę prostopadłą do końca krawędzi 
     * @return ściana prostopadła do końca krawędzi 
     */
    public Wall getPerpendicularToEnd() { return perpendicularToEnd; }
    
    private Wall findWallFromPerpendicularEnd(Wall floor, Vector3f edgeLocation1, CatchNode edge2,
            boolean start) {
        Node edge2Node = (Node)floor.getChild(edge2.toString());
        List<Spatial> neighborFloorWallList = getChildrenForEdge(floor, edge2Node); 
        List<Spatial> perpendicularNeighborFloorWalls = getNeighborFloorsWalls(findNeighborFloors
                ((Wall)building.getChild(0), floor, edge2), edge2);
        if(!start && !checkIfAchievedEnd(neighborFloorWallList,
                perpendicularNeighborFloorWalls, edge2Node, floor)) {
            return null;
        }
        Spatial minWall = null,  perpendicularNeighborFloorWall = null;
        boolean neighborFloorWallsNotEmpty = !neighborFloorWallList.isEmpty(),
                perpendicularNeighborFloorWallsNotEmpty = !perpendicularNeighborFloorWalls.isEmpty();
        if(perpendicularNeighborFloorWallsNotEmpty) {
            perpendicularNeighborFloorWall = perpendicularNeighborFloorWalls
                .get(start ? 0 : perpendicularNeighborFloorWalls.size() - 1);
        }
        if(neighborFloorWallsNotEmpty) {
            minWall = neighborFloorWallList.get(start ? 0 :  neighborFloorWallList.size() - 1);
        } 
        temporaryEdgeChildren = null;
        if(perpendicularNeighborFloorWallsNotEmpty && neighborFloorWallsNotEmpty) {
            float minDistance = minWall.getWorldTranslation().distance(edgeLocation1),
                    distance = perpendicularNeighborFloorWall.getWorldTranslation()
                    .distance(edgeLocation1);
            if(minDistance > distance) minWall = perpendicularNeighborFloorWall; 
            return (Wall)minWall; 
        } else {
            if(perpendicularNeighborFloorWallsNotEmpty) {
                return (Wall)perpendicularNeighborFloorWall;
            }
            else {
                return neighborFloorWallsNotEmpty ? (Wall)minWall : null;
            } 
        }
    }
    
    private boolean checkIfAchievedEnd(List<Spatial> neighborFloorWallList, 
            List<Spatial> perpendicularNeighborFloorWalls, Node edge, Wall floor) {
        String edgeName = edge.getName(); 
        float sum = 0;
        int listSize = neighborFloorWallList.size(); 
        for(int i = 0; i < listSize; i++) 
            sum += ((Wall)neighborFloorWallList.get(i)).getXExtend();
        listSize = perpendicularNeighborFloorWalls.size();
        for(int i = 0; i < listSize; i++) 
            sum += ((Wall)perpendicularNeighborFloorWalls.get(i)).getXExtend();
        return sum >= (edgeName.equals(CatchNode.UP.toString()) || edgeName
                .equals(CatchNode.BOTTOM.toString()) ? floor.getXExtend()
                : floor.getZExtend()); 
    }
    
    private List<Node> findNeighborFloors(final Wall wall, final Wall floor, final CatchNode edge) {
        final List<Node> allFloors = new ArrayList(); 
        final Vector3f floorLocation = floor.getWorldTranslation(),
                edgeLocation = floor.getChild(edge.toString()).getWorldTranslation();
        final float width = floor.getYExtend();
        wall.breadthFirstTraversal(new SceneGraphVisitorAdapter() {
            @Override
            public void visit(Node object) {
                if(object.getName().startsWith(ElementName.WALL_BASE_NAME)) {
                    Wall checkingWall = (Wall)object;
                    if(!checkingWall.equals(floor)) {
                        Vector3f wallLocation = checkingWall.getWorldTranslation();
                        if(wallLocation.y < floorLocation.y + width && wallLocation.y > 
                                floorLocation.y - width && edgeLocation.distance(checkingWall
                                .getChild(getNeighborEdge(edge).toString()).getWorldTranslation())
                                <= 1) {
                            allFloors.add(checkingWall);
                        }
                    }
                }
            }
        });
        return allFloors; 
    }
    
    private CatchNode getPerpendicularEdge(CatchNode edge, boolean start) {
        if(start) {
            return edge.equals(CatchNode.UP) || edge.equals(CatchNode.BOTTOM) 
                    ? CatchNode.LEFT : CatchNode.UP;
        } else {
            return edge.equals(CatchNode.UP) || edge.equals(CatchNode.BOTTOM) 
                    ? CatchNode.RIGHT : CatchNode.BOTTOM;
        }
    }
    
    private List<Spatial> getNeighborFloorsWalls(List<Node> floors, CatchNode edge) {
        if(floors.isEmpty()) return new ArrayList(); 
        String neighborEdgeName = getNeighborEdge(edge).toString();
        List<Spatial> walls = new ArrayList();
        int floorsNumber = floors.size();
        for(int i = 0; i < floorsNumber; i++)
            walls.addAll(((Node)floors.get(i).getChild(neighborEdgeName)).getChildren());
        return walls;
    }
    
    private List<Spatial> getChildrenForEdge(final Node floor, final Node edge) {
        floor.breadthFirstTraversal(new SceneGraphVisitorAdapter() {
            @Override
            public void visit(Node object) {
                Node parent = object.getParent();
                if(parent.getName().equals(edge.getName()) && temporaryEdgeChildren == null) {
                    temporaryEdgeChildren = parent.getChildren(); 
                }
            }
        });
        return temporaryEdgeChildren != null ? temporaryEdgeChildren : new ArrayList();
    }
    
    private CatchNode getNeighborEdge(CatchNode edge) {
        return CatchNode.values()[edge.ordinal() ^ 1];
    }
}
