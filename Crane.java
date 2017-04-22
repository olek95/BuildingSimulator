package buildingsimulator;

import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;

/**
 * Obiekt klasy <code>Crane</code> reprezentuje żuraw. 
 * @author AleksanderSklorz
 */
public class Crane {
    private BuildingSimulator game = BuildingSimulator.getBuildingSimulator();
    private Node crane = (Node)game.getAssetManager().loadModel("Models/zuraw/zuraw.j3o");
    public Crane(){
        PhysicsSpace physics = game.getBulletAppState().getPhysicsSpace();
        Vector3f craneLocation = new Vector3f(10f, -1f, 0f);
        crane.setLocalTranslation(craneLocation);
        physics.add(setProperLocation(crane.getChild("prop"), craneLocation));
        Spatial rack = crane.getChild("rack");
        physics.add(setProperLocation(rack, craneLocation));
        GameManager.setCrane(rack);
        game.getRootNode().attachChild(crane);
    }
    private RigidBodyControl setProperLocation(Spatial object, Vector3f displacement){
        RigidBodyControl control = object.getControl(RigidBodyControl.class);
        control.setPhysicsLocation(object.getLocalTranslation().add(displacement));
        return control;
    }
}
