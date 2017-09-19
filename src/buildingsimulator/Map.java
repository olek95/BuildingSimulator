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
        int x = 0, z = 254, end = 8;
        PhysicsSpace physics = game.getBulletAppState().getPhysicsSpace();
        Spatial firstPart = scene.getChild("terrain-gameMap");
        float offset = -z * 4; // przesunięcie planszy o połowę 
        for(int i = 0; i < 9; i++){
            for(int k = 0; k < end; k++){
                Spatial scenePart = firstPart.clone(true);
                scene.attachChild(scenePart);
                if(x + offset < 254 * 2.5f && x + offset > -254 * 2.5f 
                        && z + offset < 254 * 2.5f && z + offset > -254 * 2.5f) {
                    RigidBodyControl rgc = new RigidBodyControl(0.0f);
                    scenePart.addControl(rgc);
                    physics.add(rgc);
                    rgc.setPhysicsLocation(new Vector3f(x + offset, 0, z + offset));
                } else {
                    scenePart.setLocalTranslation(new Vector3f(x + offset, 0, z + offset));
                }
                z += 254;
            }
            x += 254;
            z = 0;
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
        new DummyWall(new Vector3f(0, 2.2f, 254 * 2.5f), 
                new Vector3f(254 * 2.5f, 1, 0.2f)); 
        new DummyWall(new Vector3f(0, 2.2f, -254 * 2.5f), 
                new Vector3f(254 * 2.5f, 1, 0.2f)); 
        new DummyWall(new Vector3f(254 * 2.5f, 2.2f, 0), 
                new Vector3f(0.2f, 1, 254 * 2.5f)); 
        new DummyWall(new Vector3f(-254 * 2.5f, 2.2f, 0), 
                new Vector3f(0.2f, 1, 254 * 2.5f)); 
    }
}
