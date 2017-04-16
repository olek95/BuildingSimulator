package buildingsimulator;

import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;

public class Crane {
    private BuildingSimulator game = BuildingSimulator.getBuildingSimulator();
    private Node crane = (Node)game.getAssetManager().loadModel("Models/zuraw/zuraw.j3o");
    private Spatial prop;
    public Crane(){
        PhysicsSpace physics = game.getBulletAppState().getPhysicsSpace();
        crane.setLocalTranslation(10f, -3f, 0f);
        prop = crane.getChild("prop");
        RigidBodyControl propControl = prop.getControl(RigidBodyControl.class);
        propControl.setPhysicsLocation(prop.getLocalTranslation().add(10f, -3f, 0f));
        physics.add(propControl);
        game.getRootNode().attachChild(crane);
    }
}
