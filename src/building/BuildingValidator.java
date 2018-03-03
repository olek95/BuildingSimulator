package building;

import authorization.User;
import buildingsimulator.BuildingSimulator;
import buildingsimulator.ElementName;
import buildingsimulator.GameManager;
import com.jme3.math.ColorRGBA;
import com.jme3.scene.Node;
import com.jme3.scene.SceneGraphVisitorAdapter;
import com.jme3.scene.Spatial;
import java.util.List;
import menu.Shop;
import net.wcomohundro.jme3.csg.CSGGeometry;

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
        List<Spatial> gameObjects = GameManager.getGameObjects();
        int objectsNumber = gameObjects.size(); 
        User user = GameManager.getUser();
        for(int i = 0; i < objectsNumber; i++){
            Spatial object = gameObjects.get(i); 
            String objectName = object.getName(); 
            if(objectName.startsWith(ElementName.BUILDING_BASE_NAME)) {
                Construction building = (Construction)object; 
                if(!building.isSold()) {
                    building.breadthFirstTraversal(new SceneGraphVisitorAdapter() {
                        @Override
                        public void visit(Node object) {
                            if(object.getName().startsWith(ElementName.WALL_BASE_NAME)) {
                                Wall wall = (Wall)object; 
                                if(!wall.isStale()) {
                                    points += wall.getWorldTranslation().y +
                                            Shop.calculateWallCost(wall.getLength(),
                                            wall.getHeight(), wall.getType()) * 2;
                                    ((CSGGeometry)wall.getChild(ElementName.WALL_GEOMETRY))
                                            .getMaterial().setColor("Color", ColorRGBA.Gray);
                                }
                            }
                        }
                    });
                    building.setSold(true);
                    user.setBuildingsNumber(user.getBuildingsNumber() + 1);
                }
            }
        }
        return points; 
    }
}
