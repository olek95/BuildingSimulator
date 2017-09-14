package cranes.crane;

import buildingsimulator.BuildingSimulator;
import buildingsimulator.GameManager;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import cranes.CraneAbstract;

/**
 * Obiekt klasy <code>Crane</code> reprezentuje żuraw. 
 * @author AleksanderSklorz
 */
public class Crane extends CraneAbstract{
    private BuildingSimulator game = BuildingSimulator.getBuildingSimulator();
    private Node crane;
    private Spatial rack, entrancePlatform;
    private Vector3f craneLocation;
    public int heightLevel = 3;
    public Spatial penultimateRack, lastRack; 
    public Crane(){
        initCrane();
    }
    
    public Crane(int heightLevel){
        this.heightLevel = heightLevel;
        initCrane();
    }
    
    /**
     * Zwraca poziom wysokości żurawia. 
     * @return poziom wysokości 
     */
    public int getHeightLevel() { return heightLevel; }
    
    private void initCrane(){
        craneLocation = new Vector3f(10f, 0f, 0f);
        initCraneElements((Node)game.getAssetManager().loadModel("Models/zuraw/zuraw.j3o"));
        game.getRootNode().attachChild(crane);
    }
    
    private RigidBodyControl setProperControlLocation(Spatial object, Vector3f displacement){
        RigidBodyControl control = object.getControl(RigidBodyControl.class);
        control.setPhysicsLocation(object.getLocalTranslation().add(displacement));
        return control;
    }
    
    private void initCraneElements(Node parent){
        crane = parent;
        crane.setLocalTranslation(craneLocation);
        PhysicsSpace physics = game.getBulletAppState().getPhysicsSpace();
        physics.add(setProperControlLocation(crane.getChild("prop0"), craneLocation));
        physics.add(setProperControlLocation(crane.getChild("prop1"), craneLocation));
        physics.add(setProperControlLocation(rack = crane.getChild("rack0"), craneLocation));
        Spatial rack1 = crane.getChild("rack1"), rack2 = crane.getChild("rack2");
        physics.add(setProperControlLocation(rack1, craneLocation));
        physics.add(setProperControlLocation(rack2, craneLocation));
        entrancePlatform = crane.getChild("entrancePlatform");
        physics.add(setProperControlLocation(entrancePlatform, craneLocation));
        setArmControl(new CraneArmControl(crane));
        penultimateRack = rack1;
        lastRack = rack2;
        raiseHeight(heightLevel);
    }
    
    public void raiseHeight(int height){
        boolean initial = false; 
        heightLevel = height; 
        Node rootNode = game.getRootNode();
        if(game.getRootNode().hasChild(crane)) {
            initial = true; 
            rootNode.detachChild(crane);
        }
        Vector3f firstRackLocation = penultimateRack.getLocalTranslation(),
                secondRackLocation = lastRack.getLocalTranslation();
        Spatial copyingLadder = crane.getChild("ladder2");
        float y = secondRackLocation.y, distanceBetweenRacks = y - firstRackLocation.y,
                distanceBetweenLadders = copyingLadder.getLocalTranslation().y - y;
        PhysicsSpace physics = game.getBulletAppState().getPhysicsSpace();
        for(int i = 3; i < height; i++){
            Spatial newRack = lastRack.clone(), newLadder = copyingLadder.clone(); 
            moveElementToEnd(distanceBetweenRacks, newRack, lastRack);
            crane.attachChild(newRack);
            physics.add(newRack.getControl(0));
            moveElementToEnd(distanceBetweenLadders, newLadder, newRack);
            lastRack = newRack;
            crane.attachChild(newLadder);
        }
        moveElementToEnd(entrancePlatform.getLocalTranslation().y - y,
                entrancePlatform, lastRack);
        Node craneControl = getArmControl().getCraneControl();
        moveElementToEnd(craneControl.getLocalTranslation().y - y, craneControl, lastRack);
        if(initial) {
            rootNode.attachChild(crane);
        }
    }
    
    private void moveElementToEnd(float yDistance, Spatial movingElement, Spatial lastElement){
        movingElement.getLocalTranslation().setY(lastElement
                .getLocalTranslation().y + yDistance);
        if(movingElement.getControl(RigidBodyControl.class) != null) 
            setProperControlLocation(movingElement, craneLocation);
    }
}
