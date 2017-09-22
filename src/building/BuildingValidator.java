package building;

import buildingsimulator.BuildingSimulator;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.Control;
import java.util.List;

/**
 * Klasa <code>BuildingValidator</code> sprawdza i ocenia zbudowany budynek. 
 * @author AleksanderSklorz
 */
public class BuildingValidator {
    /**
     * Oblicza punkty dla zbudowanych budynków. 
     * @return suma zebranych punktów
     */
    public static int validate(){
        List<Spatial> gameObjects = BuildingSimulator.getBuildingSimulator().getRootNode()
                .getChildren();
        int objectsAmount = gameObjects.size(), points = 0; 
        for(int i = 0; i < objectsAmount; i++){
            Spatial object = gameObjects.get(i); 
            if(object.getName().startsWith("Building"))
                points += calculatePointsForBuilding((Node)object);
        }
        return points; 
    }
    
    private static int calculatePoints(Wall element) {
        List<Spatial> walls = element.getChildren();
        int points = 0;
        for(int i = 1; i < 14; i++){ // od 1 do 13 bo w tym przedziale są strony 
            Node catchNode = (Node)walls.get(i);
            if(!catchNode.getChildren().isEmpty()){
                Spatial spatialWall = catchNode.getChild(0);
                if(spatialWall.getName().startsWith("Wall")) {
                    Wall wall = (Wall)spatialWall; 
                    points += calculatePoints(wall);
                    if(!wall.isStale()) {
                        points += wall.getWorldTranslation().y;
                        blockWall(wall);
                    }
                }
            }
        }
        return points;
    }
    
    private static int calculatePointsForBuilding(Node building) {
        List<Spatial> parts = building.getChildren(); 
        int partsAmount = parts.size(), points = 0; 
        for(int i = 0; i < partsAmount; i++) { // sprawdza wszystkie początkowe węzły 
            Wall part = (Wall)parts.get(i); 
            points += calculatePoints(part); 
            if(!part.isStale()) {
                points += part.getWorldTranslation().y;
                blockWall(part);
            } 
        }
        return points; 
    }
    
    private static void blockWall(Wall wall) {
        PhysicsSpace physics = BuildingSimulator.getBuildingSimulator().getBulletAppState()
                        .getPhysicsSpace();
        for(int i = 0; i < 3; i++) {
            Control control = wall.getControl(0);
            wall.removeControl(control);
            physics.remove(control);
        }
        List<Spatial> children = wall.getChildren();
        for(int i = 0; i < children.size(); i++) {
            Spatial child = children.get(i);
            if(child.getName().startsWith("Line")) {
                child.removeFromParent();
                i--;
            }
        }
        RigidBodyControl blockControl = new RigidBodyControl(0f);
        wall.addControl(blockControl);
        physics.add(blockControl);
    }
}