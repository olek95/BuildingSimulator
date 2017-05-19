package buildingsimulator;

import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.input.controls.AnalogListener;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;

/**
 * Obiekt klasy <code>Crane</code> reprezentuje żuraw. 
 * @author AleksanderSklorz
 */
public class Crane implements CraneInterface{
    private BuildingSimulator game = BuildingSimulator.getBuildingSimulator();
    private Node crane, hookHandleControl;
    private Spatial rack, craneArm, hookHandle;
    private Cabin cabin;
    public static final boolean WEAK = false;
    private boolean using;
    public Crane(){
        initCraneElements((Node)game.getAssetManager().loadModel("Models/zuraw/zuraw.j3o"));
        GameManager.setCraneRack(rack);
        cabin = new CraneCabin(crane);
        game.getRootNode().attachChild(crane);
    }
    private RigidBodyControl setProperLocation(Spatial object, Vector3f displacement){
        RigidBodyControl control = object.getControl(RigidBodyControl.class);
        control.setPhysicsLocation(object.getLocalTranslation().add(displacement));
        return control;
    }
    private void initCraneElements(Node parent){
        crane = parent;
        Vector3f craneLocation = new Vector3f(10f, -1f, 0f);
        crane.setLocalTranslation(craneLocation);
        PhysicsSpace physics = game.getBulletAppState().getPhysicsSpace();
        physics.add(setProperLocation(crane.getChild("prop0"), craneLocation));
        physics.add(setProperLocation(crane.getChild("prop1"), craneLocation));
        physics.add(setProperLocation(rack = crane.getChild("rack"), craneLocation));
    }
    
    @Override
    public Hook getHook() {
        return cabin.getHook();
    }
    
    public Cabin getCabin(){
        return cabin;
    }
    
    public boolean isUsing(){
        return using;
    }
    
    public void setUsing(boolean using){
        this.using = using;
    }
}
