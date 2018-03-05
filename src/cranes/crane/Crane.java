package cranes.crane;

import building.Wall;
import eyeview.BirdsEyeView;
import buildingsimulator.BuildingSimulator;
import buildingsimulator.ElementName;
import listeners.DummyCollisionListener;
import buildingsimulator.GameManager;
import buildingsimulator.PhysicsManager;
import eyeview.VisibleFromAbove;
import com.jme3.bounding.BoundingBox;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import cranes.ArmControl;
import cranes.CraneAbstract;
import cranes.Hook;
import java.util.List;
import menu.HUD;

/**
 * Obiekt klasy <code>Crane</code> reprezentuje żuraw. 
 * @author AleksanderSklorz
 */
public class Crane extends CraneAbstract implements VisibleFromAbove{
    private Spatial rack, entrancePlatform, penultimateRack, lastRack;
    private Vector3f craneLocation, newLocation;
    private int heightLevel = 0;
    private DummyCollisionListener listener;
    private BirdsEyeView view; 
    public Crane(){
        setCrane(GameManager.loadModel("Models/zuraw/zuraw.j3o"));
        craneLocation = new Vector3f(10f, 0f, 0f);
        initCraneElements();
        initCranePhysics();
    }
    
    public Crane(CraneState state) {
        Node crane = state.getCraneNode(); 
        setCrane(crane);
        craneLocation = crane.getLocalTranslation();
        initCraneElements();
        getCamera().setType(state.getCameraType());
        PhysicsManager.addPhysicsToGame(crane.getChild(ElementName.PROP0), 
                crane.getChild(ElementName.PROP1), rack, penultimateRack,
                lastRack, entrancePlatform);
        ArmControl arm = getArmControl(); 
        arm.setMinHandleHookDisplacement(state.getMinHandleHookDisplacement());
        Hook hook = arm.getHook(); 
        hook.setHookDisplacement(state.getHookDisplacement());
        hook.setActualLowering(state.getHookActualLowering());
        Wall wall = state.getAttachedObjectToHook();
        if(wall != null) {
            hook.setRecentlyHitObject(wall);
            hook.addAttachingJoint(wall.getActualMode());
        }
        List<Spatial> craneElements = crane.getChildren(); 
        int elementsNumber = craneElements.size(); 
        for(int i = 0; i < elementsNumber; i++) {
            Spatial element = craneElements.get(i);
            if(element.getName().startsWith(ElementName.RACK)){
                penultimateRack = lastRack;
                lastRack = element;
                if(i > 2) {
                    BuildingSimulator.getPhysicsSpace().add(setProperControlLocation(element,
                            craneLocation));
                }
            }
        }
        heightLevel = state.getHeightLevel();
    }
    
    /**
     * Zwiększa wysokość żurawia. 
     * @param height nowa wysokość 
     */
    public void raiseHeight(int height){
        boolean initial = false; 
        BuildingSimulator game = BuildingSimulator.getBuildingSimulator();
        Node rootNode = game.getRootNode(), crane = getCrane();
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
        Node rootNode = BuildingSimulator.getGameRootNode(), crane = getCrane();
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
        view = new BirdsEyeView(this, false); 
        BirdsEyeView.displayNotMovingModeHUD();
    }
    
    @Override
    public void unload() {
        craneLocation = newLocation; 
        Node crane = getCrane();
        crane.setLocalTranslation(craneLocation);
        List<Spatial> craneElements = crane.getChildren(); 
        int elementsCount = craneElements.size();
        for(int i = 0; i < elementsCount; i++) {
            Spatial element = craneElements.get(i);
            String elementName = element.getName();
            if(elementName != null && elementName.matches(createRegexCraneTower())) 
                setProperControlLocation(element, craneLocation);
        }
        removeView(); 
        BuildingSimulator.getPhysicsSpace().removeCollisionGroupListener(6);
    }
    
    @Override
     public void setDischargingLocation(Vector3f location) {
         newLocation = location; 
     }
    
    @Override
     public void setListener(DummyCollisionListener listener) {
         this.listener = listener; 
         if(listener != null) {
            BoundingBox bounding = (BoundingBox)getCrane().getChild(ElementName.PROP0)
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
     * Ustawia widok z lotu ptaka.
     * @param view widok z lotu ptaka 
     */
    public void setView(BirdsEyeView view) { this.view = view; }
    
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
    
    private RigidBodyControl setProperControlLocation(Spatial object, Vector3f displacement){
        RigidBodyControl control = object.getControl(RigidBodyControl.class);
        control.setPhysicsLocation(object.getLocalTranslation().add(displacement));
        return control;
    }
    
    private void initCraneElements() {
        Node crane = getCrane(); 
        setCamera(new CraneCamera(crane));
        rack = crane.getChild(ElementName.RACK0);
        entrancePlatform = crane.getChild(ElementName.ENTRANCE_PLATFORM);
        penultimateRack = crane.getChild(ElementName.RACK1); 
        lastRack = crane.getChild(ElementName.RACK2);
        setArmControl(new CraneArmControl(crane, (CraneCamera)getCamera()));
    }
    
    private void initCranePhysics(){
        Node crane = getCrane();
        crane.setLocalTranslation(craneLocation);
        PhysicsSpace physics = BuildingSimulator.getPhysicsSpace();
        physics.add(setProperControlLocation(crane.getChild(ElementName.PROP0), craneLocation));
        physics.add(setProperControlLocation(crane.getChild(ElementName.PROP1), craneLocation));
        physics.add(setProperControlLocation(rack, craneLocation));
        physics.add(setProperControlLocation(penultimateRack, craneLocation));
        physics.add(setProperControlLocation(lastRack, craneLocation));
        physics.add(setProperControlLocation(entrancePlatform, craneLocation));
        raiseHeight(0);
    }
    
    private void moveElementToEnd(float yDistance, Spatial movingElement, Spatial lastElement){
        movingElement.getLocalTranslation().setY(lastElement
                .getLocalTranslation().y + yDistance);
        if(movingElement.getControl(RigidBodyControl.class) != null) 
            setProperControlLocation(movingElement, craneLocation);
    }
    
    private String createRegexCraneTower() {
        return "(" + ElementName.RACK + "|" + ElementName.PROP + "|" +
                ElementName.ENTRANCE_PLATFORM + ").*";
    }
}
