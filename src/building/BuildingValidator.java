package building;

import buildingsimulator.BuildingSimulator;
import buildingsimulator.ElementName;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.scene.SceneGraphVisitorAdapter;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.Control;
import java.util.ArrayList;
import java.util.List;

/**
 * Klasa <code>BuildingValidator</code> sprawdza i ocenia zbudowany budynek. 
 * Po sprawdzeniu budynku blokuje dalszą możliwośc jego modyfikowania. 
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
            if(object.getName().startsWith(ElementName.BUILDING_BASE_NAME))
//                points += calculatePointsForBuilding((Node)object);
                points += calculatePoints((Wall)((Node)object).getChild(0));
        }
        return points; 
    }
    
    private static int calculatePoints(Wall element) {
        List<Spatial> wallElements = element.getChildren();
        int points = 0, end = CatchNode.values().length;
        for(int i = 1; i <= end; i++){ 
            List<Spatial> sideChildren  = ((Node)wallElements.get(i)).getChildren();
            int sideChildrenCount = sideChildren.size();
            for(int k = 0; k < sideChildrenCount; k++) {
                points += calculatePoints((Wall)sideChildren.get(k));
            }
        }
        if(!element.isStale()) {
            points += element.getWorldTranslation().y;
            blockWall(element);
        }
        return points;
    }
    
//    private static int calculatePointsForBuilding(Node building) {
//        List<Spatial> parts = building.getChildren(); 
//        int partsAmount = parts.size(), points = 0; 
//        for(int i = 0; i < partsAmount; i++) { // sprawdza wszystkie początkowe węzły 
//            Wall part = (Wall)parts.get(i); 
//            points += calculatePoints(part); 
//            if(!part.isStale()) {
//                points += part.getWorldTranslation().y;
//                blockWall(part);
//            } 
//        }
//        return points; 
//    }
    
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
            if(child.getName().startsWith(ElementName.LINE)) {
                child.removeFromParent();
                i--;
            }
        }
        RigidBodyControl blockControl = new RigidBodyControl(0f);
        wall.addControl(blockControl);
        physics.add(blockControl);
    }
}
