package building;

import buildingsimulator.BuildingSimulator;
import buildingsimulator.GameManager;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import java.util.List;

public class BuildingValidator {
    public static void validate(){
        List<Spatial> gameObjects = BuildingSimulator.getBuildingSimulator().getRootNode()
                .getChildren();
        int objectsAmount = gameObjects.size(), points = 0; 
        for(int i = 0; i < objectsAmount; i++){
            Spatial object = gameObjects.get(i); 
            if(object.getName().startsWith("Building"))
                points += calculatePointsForBuilding((Node)object);
        }
        GameManager.getUser().addPoints(points);
    }
    
    private static int calculatePoints(Node element) {
        List<Spatial> walls = element.getChildren();
        int points = 0;
        for(int i = 1; i < 14; i++){ // od 1 do 13 bo w tym przedziale są strony 
            Node catchNode = (Node)walls.get(i);
            if(!catchNode.getChildren().isEmpty()){
                Spatial wall = catchNode.getChild(0);
                if(wall.getName().startsWith("Wall")) {
                    points += calculatePoints((Node)wall);
                    points += wall.getWorldTranslation().y;
                }
            }
        }
        return points;
    }
    
    private static int calculatePointsForBuilding(Node building) {
        List<Spatial> parts = building.getChildren(); 
        int partsAmount = parts.size(), points = 0; 
        for(int i = 0; i < partsAmount; i++) { // sprawdza wszystkie początkowe węzły 
            Spatial part = parts.get(i); 
            points += calculatePoints((Node)part); 
            points += part.getWorldTranslation().y; 
        }
        return points; 
    }
}