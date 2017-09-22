package buildingsimulator;

import building.DummyWall;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;

/**
 * Obiekt klasy <code>Map</code> reprezentuje planszę do gry w tą grę. Składa 
 * się z podłoża oraz nieba. Zawiera także granicę, która ogranicza zasięg 
 * przemieszczania się. 
 * @author AleksanderSklorz
 */
public class Map {
    private Node scene;
    private static final int TERRAIN_PART_SIZE = 254;
    public Map(int size) {
        createTerrain(size);
        createMapBorder();
    }
    
    /**
     * Zwraca węzeł przechowujący całą mapę gry (podłoże, granice, niebo). 
     * @return węzeł mapy gry 
     */
    public Node getScene() {
        return scene;
    }
    
    private void createTerrain(int size) {
        BuildingSimulator game = BuildingSimulator.getBuildingSimulator();
        scene = (Node)game.getAssetManager().loadModel("Scenes/gameMap.j3o");
        // -z * 4 to przesunięcie planszy 
        int z = TERRAIN_PART_SIZE, end = size - 1, offset = -z * 4, x = offset; 
        PhysicsSpace physics = game.getBulletAppState().getPhysicsSpace();
        Spatial firstPart = scene.getChild("terrain-gameMap");
        float positiveBorder = calculateBorderLocation(false),
                negativeBorder = calculateBorderLocation(true);  
        z += offset; 
        for(int i = 0; i < size; i++){
            for(int k = 0; k < end; k++){
                Spatial scenePart = firstPart.clone(true);
                scene.attachChild(scenePart);
                if(x < positiveBorder && x > negativeBorder && z < positiveBorder 
                        && z > negativeBorder) {
                    RigidBodyControl rgc = new RigidBodyControl(0.0f);
                    scenePart.addControl(rgc);
                    physics.add(rgc);
                    rgc.setPhysicsLocation(new Vector3f(x, 0, z));
                } else {
                    scenePart.setLocalTranslation(x, 0, z);
                }
                z += TERRAIN_PART_SIZE;
            }
            x += TERRAIN_PART_SIZE;
            z = 0;
            z += offset;
            end = size;
        }
        RigidBodyControl firstPartControl = new RigidBodyControl(0.0f); 
        firstPart.addControl(firstPartControl);
        physics.add(firstPartControl); 
        firstPartControl.setPhysicsLocation(new Vector3f(offset, 0, offset));
    }
    
    private void createMapBorder() {
        float positiveLocation = calculateBorderLocation(false),
                negativeLocation = calculateBorderLocation(true);
        Vector3f xDimensions = new Vector3f(254 * 2.5f, 1, 0.2f),
                zDimensions = new Vector3f(0.2f, 1, 254 * 2.5f);
        DummyWall.createDummyWall(new Vector3f(0, 2.2f, positiveLocation), xDimensions, 0); 
        DummyWall.createDummyWall(new Vector3f(0, 2.2f, negativeLocation), xDimensions, 0); 
        DummyWall.createDummyWall(new Vector3f(positiveLocation, 2.2f, 0), zDimensions, 0); 
        DummyWall.createDummyWall(new Vector3f(negativeLocation, 2.2f, 0), zDimensions, 0); 
    }
    
    public static float calculateBorderLocation(boolean negative) {
        return (negative ? -254 : 254) * 2.5f;
    }
}
