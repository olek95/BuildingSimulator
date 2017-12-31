package cranes.crane;

import eyeview.BirdsEyeView;
import buildingsimulator.BuildingSimulator;
import buildingsimulator.ElementName;
import listeners.DummyCollisionListener;
import buildingsimulator.GameManager;
import eyeview.VisibleFromAbove;
import com.jme3.audio.AudioNode;
import com.jme3.bounding.BoundingBox;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import cranes.CraneAbstract;
import java.util.List;
import menu.HUD;

/**
 * Obiekt klasy <code>Crane</code> reprezentuje żuraw. 
 * @author AleksanderSklorz
 */
public class Crane extends CraneAbstract implements VisibleFromAbove{
    private BuildingSimulator game = BuildingSimulator.getBuildingSimulator();
    private Node crane;
    private Spatial rack, entrancePlatform, penultimateRack, lastRack;
    private Vector3f craneLocation, newLocation;
    private int heightLevel = 0;
    private DummyCollisionListener listener;
    private BirdsEyeView view; 
    public Crane(){
        initCrane();
    }
    
//    public Crane(int heightLevel){
//        this.heightLevel = heightLevel;
//        initCrane();
//    }
    
    /**
     * Zwiększa wysokość żurawia. 
     * @param height nowa wysokość 
     */
    public void raiseHeight(int height){
        boolean initial = false; 
        Node rootNode = game.getRootNode();
        if(rootNode.hasChild(crane)) {
            initial = true; 
            rootNode.detachChild(crane);
        }
        Spatial copyingLadder = crane.getChild(ElementName.LADDER);
        float y = lastRack.getLocalTranslation().y, distanceBetweenRacks = y 
                - penultimateRack.getLocalTranslation().y,
                distanceBetweenLadders = copyingLadder.getLocalTranslation().y - y;
        PhysicsSpace physics = game.getBulletAppState().getPhysicsSpace();
        for(int i = heightLevel; i < height; i++){
            Spatial newRack = lastRack.clone(), newLadder = copyingLadder.clone(); 
            moveElementToEnd(distanceBetweenRacks, newRack, lastRack);
            crane.attachChild(newRack);
            physics.add(newRack.getControl(0));
            moveElementToEnd(distanceBetweenLadders, newLadder, newRack);
            penultimateRack = lastRack;
            lastRack = newRack;
            crane.attachChild(newLadder);
        }
        heightLevel = height;
        moveElementToEnd(entrancePlatform.getLocalTranslation().y - y,
                entrancePlatform, lastRack);
        Node craneControl = getArmControl().getCraneControl();
        moveElementToEnd(craneControl.getLocalTranslation().y - y, craneControl,
                lastRack);
        if(initial) {
            rootNode.attachChild(crane);
        }
    }
    
    /**
     * Zmniejsza wysokość żurawia. 
     * @param height wysokość żurawia 
     */
    public void decreaseHeight(int height) {
        Node rootNode = game.getRootNode();
        rootNode.detachChild(crane);
        List<Spatial> rackElements = crane.getChildren();
        int lastIndex = rackElements.size() - 1, difference = heightLevel - height; 
        Vector3f secondRackLocation = lastRack.getLocalTranslation();
        float distance = secondRackLocation.y - penultimateRack.getLocalTranslation().y; 
        Spatial lastRackCopy = lastRack.clone();
        int i;
        for(i = 0; i < difference * 2; i += 2) {
            rackElements.get(lastIndex - i).removeFromParent(); // usunięcie drabinki
            rackElements.get(lastIndex - i - 1).removeFromParent(); // usunięcie kawałka stojaka
        }
        heightLevel = height;
        moveElementToEnd(entrancePlatform.getLocalTranslation().y - secondRackLocation.y
                - distance * difference, entrancePlatform, lastRackCopy);
        Node craneControl = getArmControl().getCraneControl();
        moveElementToEnd(craneControl.getLocalTranslation().y - secondRackLocation.y
                - distance * difference, craneControl, lastRackCopy);
        lastRack = rackElements.get(lastIndex - i - 1); 
        penultimateRack = rackElements.get(lastIndex - i - 3); 
        rootNode.attachChild(crane);
    }
    
    /**
     * Rozpoczyna proces przenoszenia żurawia.
     */
    public void startMoving() {
        HUD.changeShopButtonVisibility(false);
        view = new BirdsEyeView(this); 
    }
    
    @Override
    public void unload() {
        craneLocation = newLocation; 
        crane.setLocalTranslation(craneLocation);
        List<Spatial> craneElements = crane.getChildren(); 
        int elementsCount = craneElements.size();
        for(int i = 0; i < elementsCount; i++) {
            Spatial element = craneElements.get(i);
            String elementName = element.getName();
            if(elementName != null && elementName.matches("(" + ElementName.RACK
                    + "|" + ElementName.PROP + "|" + ElementName.ENTRANCE_PLATFORM
                    + ").*")) 
                setProperControlLocation(element, craneLocation);
        }
        removeView(); 
        BuildingSimulator.getBuildingSimulator().getBulletAppState()
                .getPhysicsSpace().removeCollisionGroupListener(6);
    }
    
    @Override
     public void setDischargingLocation(Vector3f location) {
         newLocation = location; 
     }
    
    @Override
     public void setListener(DummyCollisionListener listener) {
         this.listener = listener; 
         if(listener != null) {
            BoundingBox bounding = (BoundingBox)crane.getChild(ElementName.PROP0)
                    .getWorldBound();
            listener.createDummyWall(newLocation, bounding.getExtent(null));
         }
     }
    
    @Override
    public DummyCollisionListener getListener() { return listener; }
    
    /**
     * Zwraca widok z lotu ptaka. 
     * @return widok z lotu ptaka 
     */
    public BirdsEyeView getView() { return view; }
    
    /**
     * Usuwa widok z lotu ptaka. 
     */
    public void removeView() { 
        view.setOff();
        HUD.changeShopButtonVisibility(true);
        this.view = null; 
    }
     
    /**
     * Zwraca poziom wysokości żurawia. 
     * @return poziom wysokości 
     */
    public int getHeightLevel() { return heightLevel; }
    
    /**
     * Zwraca dźwig. 
     * @return dźwig
     */
    public Node getCrane() { return crane; }
    
    private void initCrane(){
        craneLocation = new Vector3f(10f, 0f, 0f);
        initCraneElements(GameManager.loadModel("Models/zuraw/zuraw.j3o"));
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
        physics.add(setProperControlLocation(crane.getChild(ElementName.PROP0), craneLocation));
        physics.add(setProperControlLocation(crane.getChild(ElementName.PROP1), craneLocation));
        physics.add(setProperControlLocation(rack = crane.getChild(ElementName.RACK0),
                craneLocation));
        Spatial rack1 = crane.getChild(ElementName.RACK1), 
                rack2 = crane.getChild(ElementName.RACK2);
        physics.add(setProperControlLocation(rack1, craneLocation));
        physics.add(setProperControlLocation(rack2, craneLocation));
        entrancePlatform = crane.getChild(ElementName.ENTRANCE_PLATFORM);
        physics.add(setProperControlLocation(entrancePlatform, craneLocation));
        setArmControl(new CraneArmControl(crane));
        penultimateRack = rack1;
        lastRack = rack2;
        raiseHeight(0);
    }
    
    private void moveElementToEnd(float yDistance, Spatial movingElement, Spatial lastElement){
        movingElement.getLocalTranslation().setY(lastElement
                .getLocalTranslation().y + yDistance);
        if(movingElement.getControl(RigidBodyControl.class) != null) 
            setProperControlLocation(movingElement, craneLocation);
    }
}
