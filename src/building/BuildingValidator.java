package building;

import buildingsimulator.BuildingSimulator;
import buildingsimulator.ElementName;
import buildingsimulator.PhysicsManager;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.control.RigidBodyControl;
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
    private static int points = 0;
    /**
     * Oblicza punkty dla zbudowanych budynków. 
     * @return suma zebranych punktów
     */
    public static int validate(){
        points = 0;
        List<Spatial> gameObjects = BuildingSimulator.getBuildingSimulator().getRootNode()
                .getChildren();
        int objectsNumber = gameObjects.size(); 
        for(int i = 0; i < objectsNumber; i++){
            Spatial object = gameObjects.get(i); 
            if(object.getName().startsWith(ElementName.BUILDING_BASE_NAME)) {
                Construction building = (Construction)object; 
                if(!building.isSold()) {
                    building.breadthFirstTraversal(new SceneGraphVisitorAdapter() {
                        @Override
                        public void visit(Node object) {
                            if(object.getName().startsWith(ElementName.WALL_BASE_NAME)) {
                                Wall wall = (Wall)object; 
                                if(!wall.isStale()) {
                                    points += wall.getWorldTranslation().y + 
                                            (wall.getHeight() * wall.getLength() + 
                                            wall.getType().getPrice()) * 2;
                                }
                            }
                        }
                    });
                    building.setSold(true);
                }
            }
        }
        return points; 
    }
}
