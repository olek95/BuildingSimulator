package building;

import buildingsimulator.ElementName;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.scene.SceneGraphVisitorAdapter;
import com.jme3.scene.Spatial;
import java.util.ArrayList;
import java.util.List;

public class EdgeInformation {
    private List<Spatial> edgeWalls, neighborFloorWalls;
    private Wall neighborFloor, perpendicularToStart, perpendicularToEnd;
    private Construction building;
    public EdgeInformation(Construction building, Wall floor, CatchNode edge) {
        this.building = building;
        edgeWalls = ((Node)floor.getChild(edge.toString())).getChildren();
        System.out.println(building);
        neighborFloor = findNeighborFloor((Wall)building.getChild(0), floor, edge);
        neighborFloorWalls = getNeighborFloorWalls(neighborFloor, edge); 
        Vector3f edgeLocation = floor.getChild(edge.toString()).getWorldTranslation();
        boolean upLeft = edge.equals(CatchNode.LEFT) || edge.equals(CatchNode.UP);
        perpendicularToStart = findWallFromPerpendicularEnd(floor, edgeLocation,
                getPerpendicularEdge(edge, true), upLeft); 
        perpendicularToEnd = findWallFromPerpendicularEnd(floor, edgeLocation,
                getPerpendicularEdge(edge, false), upLeft);
    }
    
    public List<Spatial> getEdgeWalls() { return edgeWalls; }
    
    public List<Spatial> getNeighborFloorWalls() { return neighborFloorWalls; }
    
    public Wall getPerpendicularToStart() { return perpendicularToStart; }
    
    public Wall getPerpendicularToEnd() { return perpendicularToEnd; }
    
    private Wall findWallFromPerpendicularEnd(Wall floor, Vector3f edgeLocation1, CatchNode edge2,
            boolean start) {
        Node edge2Node = (Node)floor.getChild(edge2.toString());
        List<Spatial> neighborFloorWallList = edge2Node.getChildren(), 
                perpendicularNeighborFloorWalls = getNeighborFloorWalls(findNeighborFloor
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
        if(perpendicularNeighborFloorWallsNotEmpty && neighborFloorWallsNotEmpty) {
            float minDistance = minWall.getWorldTranslation().distance(edgeLocation1),
                    distance = perpendicularNeighborFloorWall.getWorldTranslation()
                    .distance(edgeLocation1);
            if(minDistance > distance) minWall = perpendicularNeighborFloorWall; 
            return (Wall)minWall; 
        } else {
            if(perpendicularNeighborFloorWallsNotEmpty) return (Wall)perpendicularNeighborFloorWall;
            else return neighborFloorWallsNotEmpty ? (Wall)minWall : null; 
        }
    }
    
    private boolean checkIfAchievedEnd(List<Spatial> neighborFloorWallList, 
            List<Spatial> perpendicularNeighborFloorWalls, Node edge, Wall floor) {
        String edgeName = edge.getName(); 
        float max = edgeName.equals(CatchNode.UP.toString()) || edgeName
                .equals(CatchNode.BOTTOM.toString()) ? floor.getLength()
                : floor.getHeight(), sum = 0;
        int listSize = neighborFloorWallList.size(); 
        for(int i = 0; i < listSize; i++) 
            sum += ((Wall)neighborFloorWallList.get(i)).getLength();
        listSize = perpendicularNeighborFloorWalls.size();
        for(int i = 0; i < listSize; i++) 
            sum += ((Wall)perpendicularNeighborFloorWalls.get(i)).getLength();
        return sum >= max; 
    }
    
    private Wall findNeighborFloor(final Wall wall, final Wall floor, CatchNode edge) {
        final List<Node> allFloors = new ArrayList(); 
        final Vector3f floorLocation = floor.getWorldTranslation();
        final float width = floor.getWidth();
        wall.breadthFirstTraversal(new SceneGraphVisitorAdapter() {
            @Override
            public void visit(Node object) {
                if(object.getName().startsWith(ElementName.WALL_BASE_NAME)) {
                    Wall checkingWall = (Wall)object;
                    if(!checkingWall.equals(floor)) {
                        Vector3f wallLocation = checkingWall.getWorldTranslation();
                        if(wallLocation.y < floorLocation.y + width && wallLocation.y > 
                                floorLocation.y - width && floorLocation.distance(wallLocation)
                                <= floor.getHeight() + checkingWall.getHeight() + 0.1f) {
                            allFloors.add(checkingWall);
                        }
                    }
                }
            }
        });
        Vector3f edgeLocation = floor.getChild(edge.toString()).getWorldTranslation();
        int allFloorsNumber = allFloors.size();
        float minDistance = Float.MAX_VALUE;
        Node minFloor = null;
        for(int i = 0; i < allFloorsNumber; i++) {
            Node neighborOfFloor = allFloors.get(i); 
            float distance = neighborOfFloor.getWorldTranslation().distance(edgeLocation);
            if(minDistance > distance) {
                minFloor = neighborOfFloor; 
                minDistance = distance; 
            }
        }
        return (Wall)minFloor; 
    }
    
    private CatchNode getPerpendicularEdge(CatchNode edge, boolean start) {
        boolean upBottom = edge.equals(CatchNode.UP) || edge.equals(CatchNode.BOTTOM);
        if(start) {
            return upBottom ? CatchNode.LEFT : CatchNode.UP;
        } else {
            return upBottom ? CatchNode.RIGHT : CatchNode.BOTTOM;
        }
    }
    
    private List<Spatial> getNeighborFloorWalls(Wall floor, CatchNode edge) {
        return floor != null ? ((Node)floor.getChild(CatchNode.values()[edge.ordinal() ^ 1]
                .toString())).getChildren() : new ArrayList();
    }
}