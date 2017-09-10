package building;

import buildingsimulator.BuildingSimulator;
import buildingsimulator.GameManager;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import java.util.List;

public class BuildingValidation {
    public static void validate(){
        List<Spatial> gameObjects = BuildingSimulator.getBuildingSimulator().getRootNode()
                .getChildren();
        int objectsAmount = gameObjects.size(), points = 0; 
        for(int i = 0; i < objectsAmount; i++){
            Spatial object = gameObjects.get(i); 
//            System.out.println(object.getName());
            if(object.getName().startsWith("Building"))
                points += checkBuildingPart((Node)object);
        }
        GameManager.getUser().addPoints(points);
    }
    
    private static int calculatePoints(Node element) {
        List<Spatial> walls = element.getChildren();
        int points = 0;
        for(int i = 1; i < 14; i++){ // od 1 do 13 bo w tym przedziale są strony 
            Spatial wall = ((Node)walls.get(i)).getChild(0);
            System.out.println(wall);
            if(wall.getName().startsWith("Wall")) {
                points += calculatePoints((Node)wall);
                System.out.println("P " + wall.getWorldTranslation().y);
                points += wall.getWorldTranslation().y;
            }
        }
        return points;
    }
    
    private static int checkBuildingPart(Node building) {
        List<Spatial> parts = building.getChildren(); 
        int partsAmount = parts.size(), points = 0; 
        for(int i = 0; i < partsAmount; i++) {
            Spatial part = parts.get(i); 
            System.out.println("PART: " + part); 
            points += calculatePoints((Node)part); 
            points += part.getWorldTranslation().y; 
        }
        return points; 
    }
}