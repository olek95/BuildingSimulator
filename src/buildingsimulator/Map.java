package buildingsimulator;

import building.DummyWall;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;

public class Map {
    private Node scene; 
    public Map() {
        BuildingSimulator game = BuildingSimulator.getBuildingSimulator();
        scene = (Node)game.getAssetManager().loadModel("Scenes/gameMap.j3o");
        // -z * 4 to przesunięcie planszy 
        int z = 254, end = 8, offset = -z * 4, x = offset; 
        PhysicsSpace physics = game.getBulletAppState().getPhysicsSpace();
        Spatial firstPart = scene.getChild("terrain-gameMap");
        float positiveBorder = calculateBorderLocation(false),
                negativeBorder = calculateBorderLocation(true);  
        z += offset; 
        for(int i = 0; i < 9; i++){
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
                z += 254;
            }
            x += 254;
            z = 0;
            z += offset;
            end = 97;
        }
        RigidBodyControl firstPartControl = new RigidBodyControl(0.0f); 
        firstPart.addControl(firstPartControl);
        physics.add(firstPartControl); 
        firstPartControl.setPhysicsLocation(new Vector3f(offset, 0, offset));
        createMapBorder();
    }
    
    public Node getScene() {
        return scene;
    }
    
    private void createMapBorder() {
        float positiveLocation = calculateBorderLocation(false),
                negativeLocation = calculateBorderLocation(true);
        new DummyWall(new Vector3f(0, 2.2f, positiveLocation), 
                new Vector3f(254 * 2.5f, 1, 0.2f), 0); 
        new DummyWall(new Vector3f(0, 2.2f, negativeLocation), 
                new Vector3f(254 * 2.5f, 1, 0.2f), 0); 
        new DummyWall(new Vector3f(positiveLocation, 2.2f, 0), 
                new Vector3f(0.2f, 1, 254 * 2.5f), 0); 
        new DummyWall(new Vector3f(negativeLocation, 2.2f, 0), 
                new Vector3f(0.2f, 1, 254 * 2.5f), 0); 
    }
    
    private float calculateBorderLocation(boolean negative) {
        return (negative ? -254 : 254) * 2.5f;
    }
}
