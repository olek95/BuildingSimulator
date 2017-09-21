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
                        PhysicsSpace p = BuildingSimulator.getBuildingSimulator().getBulletAppState()
                        .getPhysicsSpace();
                        Control c0 = wall.getControl(0);
                        wall.removeControl(c0);
                        p.remove(c0);
                        Control c1 = wall.getControl(0);
                        wall.removeControl(c1);
                        p.remove(c1);
                        Control c2 = wall.getControl(0);
                        wall.removeControl(c2);
                        p.remove(c2);
                        List<Spatial> children = wall.getChildren();
                        for(int k = 0; k < children.size(); k++) {
                            Spatial child = children.get(k);
                            if(child.getName().startsWith("Cylinder")) {
                                child.removeFromParent();
                            }
                        }
                        RigidBodyControl cn = new RigidBodyControl(0);
                        wall.addControl(cn);
                        BuildingSimulator.getBuildingSimulator().getBulletAppState()
                                .getPhysicsSpace().add(cn);
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
                PhysicsSpace p = BuildingSimulator.getBuildingSimulator().getBulletAppState()
                        .getPhysicsSpace();
                Control c0 = part.getControl(0);
                part.removeControl(c0);
                p.remove(c0);
                Control c1 = part.getControl(0);
                part.removeControl(c1);
                p.remove(c1);
                Control c2 = part.getControl(0);
                part.removeControl(c2);
                p.remove(c2);
                List<Spatial> children = part.getChildren();
                        for(int k = 0; k < children.size(); k++) {
                            Spatial child = children.get(k);
                            if(child.getName().startsWith("Cylinder")) {
                                child.removeFromParent();
                            }
                        }
                RigidBodyControl cn = new RigidBodyControl(0);
                part.addControl(cn);
                BuildingSimulator.getBuildingSimulator().getBulletAppState()
                        .getPhysicsSpace().add(cn);
            } 
        }
        return points; 
    }
}