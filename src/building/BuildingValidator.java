package building;

import authorization.User;
import buildingsimulator.ElementName;
import buildingsimulator.GameManager;
import com.jme3.scene.Node;
import com.jme3.scene.SceneGraphVisitorAdapter;
import com.jme3.scene.Spatial;
import java.util.List;
import menu.Shop;

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
                                            Shop.calculateWallCost(wall.getXExtend(),
                                            wall.getZExtend(), wall.getType()) * 2;
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
