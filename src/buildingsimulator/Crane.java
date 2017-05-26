package buildingsimulator;

import com.jme3.bounding.BoundingBox;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import java.util.List;

/**
 * Obiekt klasy <code>Crane</code> reprezentuje żuraw. 
 * @author AleksanderSklorz
 */
public class Crane extends CraneAbstract{
    private BuildingSimulator game = BuildingSimulator.getBuildingSimulator();
    private Node crane;
    private Spatial rack, entrancePlatform, mainElement;
    private Vector3f craneLocation;
    private int heightLevel = 5;
    public Crane(){
        craneLocation = new Vector3f(10f, -1f, 0f);
        initCraneElements((Node)game.getAssetManager().loadModel("Models/zuraw/zuraw.j3o"));
        GameManager.setCraneRack(rack);
        game.getRootNode().attachChild(crane);
    }
    public Crane(int heightLevel){
        this();
        this.heightLevel = heightLevel;
    }
    private RigidBodyControl setProperLocation(Spatial object, Vector3f displacement){
        RigidBodyControl control = object.getControl(RigidBodyControl.class);
        control.setPhysicsLocation(object.getLocalTranslation().add(displacement));
        return control;
    }
    private void initCraneElements(Node parent){
        crane = parent;
        crane.setLocalTranslation(craneLocation);
        PhysicsSpace physics = game.getBulletAppState().getPhysicsSpace();
        physics.add(setProperLocation(crane.getChild("prop0"), craneLocation));
        physics.add(setProperLocation(crane.getChild("prop1"), craneLocation));
        physics.add(setProperLocation(rack = crane.getChild("rack0"), craneLocation));
        Spatial rack1 = crane.getChild("rack1"),
                rack2 = crane.getChild("rack2");
        Vector3f firstRackLocation = rack1.getLocalTranslation(), 
                secondRackLocation = rack2.getLocalTranslation();
        physics.add(setProperLocation(rack1, craneLocation));
        physics.add(setProperLocation(rack2, craneLocation));
        entrancePlatform = crane.getChild("entrancePlatform");
        mainElement = crane.getChild("mainElement");
        physics.add(setProperLocation(entrancePlatform, craneLocation));
        setArmControl(new CraneArmControl(crane));
        raiseHeight(rack2, firstRackLocation, secondRackLocation, crane.getChild("ladder2"));
    }
    private void raiseHeight(Spatial copyingRack, Vector3f firstRackLocation,
            Vector3f secondRackLocation, Spatial copyingLadder){
        float distanceBetweenRacks = secondRackLocation.y - firstRackLocation.y,
                distanceRackLadder = copyingLadder.getLocalTranslation().y
                        - secondRackLocation.y;
        Spatial temp = copyingRack;
        PhysicsSpace physics = game.getBulletAppState().getPhysicsSpace();
        for(int i = 3; i < heightLevel; i++){
            copyingRack = copyingRack.clone();
            copyingRack.getLocalTranslation().addLocal(0, distanceBetweenRacks, 0);
            RigidBodyControl control = copyingRack.getControl(RigidBodyControl.class);
            control.setPhysicsLocation(copyingRack.getLocalTranslation().add(craneLocation));
            crane.attachChild(copyingRack);
            physics.add(control);
            copyingLadder = copyingLadder.clone();
            copyingLadder.getLocalTranslation().addLocal(0, distanceBetweenRacks, 0);
            crane.attachChild(copyingLadder);
        }
        addElementToEnd(temp, entrancePlatform, copyingRack);
        addElementToEnd(temp, getArmControl().getCraneControl(), copyingRack);
    }
    private void addElementToEnd(Spatial s1, Spatial s2, Spatial lastElement){
        float distance = s2.getLocalTranslation().y - s1.getLocalTranslation().y;
        Vector3f distanceBetweenTwoElements = lastElement.getLocalTranslation()
                .clone().addLocal(0, distance, 0);
        s2.setLocalTranslation(distanceBetweenTwoElements);
        if(s2.getControl(RigidBodyControl.class) != null) 
            setProperLocation(s2, craneLocation);
    }
}
