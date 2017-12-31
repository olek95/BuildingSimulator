package cranes.mobileCrane;

import buildingsimulator.BuildingSimulator;
import settings.Control;
import cranes.CraneAbstract;
import static buildingsimulator.PhysicsManager.calculateDisplacementAfterScaling;
import static buildingsimulator.PhysicsManager.moveWithScallingObject;
import settings.Control.Actions;
import buildingsimulator.Controllable;
import buildingsimulator.ElementName;
import buildingsimulator.GameManager;
import buildingsimulator.PhysicsManager;
import com.jme3.audio.AudioNode;
import com.jme3.scene.Spatial;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.collision.shapes.CompoundCollisionShape;
import com.jme3.bullet.control.VehicleControl;
import com.jme3.input.controls.ActionListener;
import com.jme3.material.Material;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.texture.Texture;
import com.jme3.water.SimpleWaterProcessor;
import java.util.Arrays;
import java.util.List;
import menu.HUD;
import texts.Translator;
/**
 * Obiekt klasy <code>MobileCrane</code> reprezentuje mobilny dźwig. Dźwig
 * ten posiada umiejętność poruszania się w przód i w tył, skręciania oraz 
 * obsługi ramienia dźwigu. 
 * @author AleksanderSklorz
 */
public class MobileCrane extends CraneAbstract implements ActionListener, Controllable{
    private Node crane = GameManager.loadModel("Models/dzwig/dzwig.j3o");
    private VehicleControl craneControl = crane.getControl(VehicleControl.class);
    private static final float ACCELERATION_FORCE = 100.0f, BRAKE_FORCE = 20.0f,
            FRICTION_FORCE = 10.0f, PROP_LOWERING_SPEED = 0.05f;
    public static final float MAX_PROP_PROTRUSION = 6.35f, MIN_PROP_PROTRUSION = 1f;
    private float propsLowering = 1f;
    private String key = "";
    private Vector3f propDisplacement;
    private Actions[] availableActions = {Actions.UP, Actions.DOWN, Actions.LEFT,
        Actions.RIGHT, Actions.ACTION};
    private AudioNode craneStartEngineSound, craneDrivingSound, craneDrivingBackwardsSound;
    public MobileCrane(){
        createMobileCranePhysics();
        scaleTiresTexture();
//        createMirrors();
        PhysicsSpace physics = BuildingSimulator.getBuildingSimulator()
                .getBulletAppState().getPhysicsSpace();
        physics.add(craneControl);
        setArmControl(new MobileCraneArmControl(crane));
        propDisplacement =  PhysicsManager.calculateDisplacementAfterScaling((Node)crane
                .getChild("protractileProp1"), new Vector3f(1f, propsLowering + PROP_LOWERING_SPEED,
                1f), false, true, false);
        craneStartEngineSound = GameManager.createSound("Sounds/crane_engine_start.wav", 
                GameManager.getGameSoundVolume(), false, crane);
        craneDrivingSound = GameManager.createSound("Sounds/crane_engine_driving.wav",
                GameManager.getGameSoundVolume(), true, crane);
        craneDrivingBackwardsSound = GameManager.createSound("Sounds/crane_driving_backwards.wav",
                GameManager.getGameSoundVolume(), true, crane);
    }
    
    /**
     * Metoda określająca sterowanie dźwigu. Dźwig może jeździć w dowolnym 
     * kierunku, a także opuszczać podpory. 
     * @param name nazwa akcji 
     * @param isPressed true jeśli klawisz został naciśnięty, false w przeciwnym razie  
     * @param tpf 
     */
    @Override
    public void onAction(String name, boolean isPressed, float tpf) {
        switch(Actions.valueOf(name)){
            case UP:
                if(isPressed){
                    if(key != null && key.equals("")) craneStartEngineSound.play();
                    GameManager.stopSound(craneDrivingBackwardsSound, false);
                    key = name;
                    craneControl.accelerate(0f);
                    craneControl.brake(BRAKE_FORCE);
                }else{
                    key = null;
                    craneControl.accelerate(craneControl.getCurrentVehicleSpeedKmHour() > 0 ?
                            -FRICTION_FORCE : FRICTION_FORCE);
                    GameManager.stopSound(craneDrivingSound, false);
                }
                break;
            case DOWN:
                if(isPressed){
                    if(key != null && key.equals("")) craneStartEngineSound.play();
                    GameManager.stopSound(craneDrivingSound, false);
                    key = name;
                    craneControl.accelerate(0f);
                    craneControl.brake(BRAKE_FORCE);
                }else{
                    key = null;
                    craneControl.brake(0f);
                    craneControl.accelerate(craneControl.getCurrentVehicleSpeedKmHour() < 0 ?
                            FRICTION_FORCE : -FRICTION_FORCE);
                     GameManager.stopSound(craneDrivingBackwardsSound, false);
                }
                break;
            case LEFT:
                craneControl.steer(isPressed ? 0.5f : 0);
                break;
            case RIGHT: 
                craneControl.steer(isPressed ? -0.5f : 0);
                break;
            case ACTION: if(isPressed) getOff(name);
        }
    }
    
    /**
     * Aktualizuje stan pojazdu. Możliwe stany to: stop, jazda w przód i jazda
     * w tył. Oprócz tego decyduje czy gracz działa w trybie sterowania pojazdem 
     * żurawia czy w trybie sterowania jego ramieniem z kabiny sterowania 
     * remieniem żurawia. 
     */
    public void updateState(){
        if(key == null){
            if(craneControl.getCurrentVehicleSpeedKmHour() < 1 
                    && craneControl.getCurrentVehicleSpeedKmHour() > -1)
                stop();
        }else{ 
            if(!key.equals("")){
                if(key.equals(Actions.DOWN.toString())) {
                    if(GameManager.isSoundStopped(craneStartEngineSound))
                        craneDrivingBackwardsSound.play();
                    if(craneControl.getCurrentVehicleSpeedKmHour() < 0) {
                        craneControl.brake(0f);
                        craneControl.accelerate(-ACCELERATION_FORCE * 0.5f); // prędkość w tył jest mniejsza
                    }
                }else{
                    if(key.equals(Actions.UP.toString())) {
                        if(GameManager.isSoundStopped(craneStartEngineSound))
                            craneDrivingSound.play();
                        if(Math.ceil(craneControl.getCurrentVehicleSpeedKmHour()) >= 0) {
                            craneControl.brake(0f);
                            craneControl.accelerate(ACCELERATION_FORCE);
                        }
                    }
                }
            }else{
                if(((MobileCraneArmControl)getArmControl()).isUsing()) getInCabin();
                else getInMobileCrane();
            }
        }
    }
    
    /**
     * Zwraca dostępne akcje dla dźwigu, czyli - jazda w przód, do tyłu, skręcanie 
     * w lewo i w prawo, a także akcja opuszczania podpór.
     * @return tablicę dostępnych akcji dla dźwigu mobilnego 
     */
    @Override
    public Control.Actions[] getAvailableActions(){
        return availableActions;
    }
    
    /**
     * Ustawia kąt skrętu kół pojazdu. 
     * @param angle kąt 
     */
    public void setSteeringAngle(float angle){ craneControl.steer(angle); }
    
    /**
     * Zwraca model dźwigu mobilnego. 
     * @return model dźwigu mobilnego 
     */
    public Node getCrane() { return crane; }
    
    private void stop(){
        key = "";
        craneControl.accelerate(0f);
        craneControl.brake(0f);
        // jeśli jeszcze jest jakaś mała prędkość, to zeruje
        craneControl.setLinearVelocity(Vector3f.ZERO); 
    }
    
    private void scaleTiresTexture(){
        List<Spatial> craneElements = crane.getChildren();
        Texture tireTexture = null;
        for(Spatial element : craneElements)
            if(element.getName().startsWith(ElementName.WHEEL)){
                Geometry tire = ((Geometry)((Node)((Node)element).getChild(0)).getChild(0));
                Material tireMaterial = tire.getMaterial();
                if(tireTexture == null){
                    tireTexture = (Texture)tireMaterial.getTextureParam("DiffuseMap").getValue();
                    tireTexture.setWrap(Texture.WrapMode.Repeat);
                    tire.getMesh().scaleTextureCoordinates(new Vector2f(1,1f));
                }else tireMaterial.setTexture("DiffuseMap", tireTexture);
            }
    }
    
    private void createMobileCranePhysics(){
        PhysicsManager.addNewCollisionShapeToCompound((CompoundCollisionShape)craneControl
                .getCollisionShape(),crane, ElementName.OUTSIDE_MOBILE_CRANE_CABIN,
                Vector3f.ZERO, null);
        PhysicsManager.addNewCollisionShapeToCompound((CompoundCollisionShape)craneControl
                .getCollisionShape(),crane, ElementName.BOLLARDS, Vector3f.ZERO, null);
    }
    
    private void controlProps(boolean lowering){
        List<Spatial> mobileCraneChildren = crane.getChildren();
        int i = 0, changed = 0;
        String[] props = {ElementName.PROP_PARTS1, ElementName.PROP_PARTS2,
            ElementName.PROP_PARTS3, ElementName.PROP_PARTS4};
        Vector3f scallingVector = new Vector3f(1f, propsLowering += lowering ? 
                PROP_LOWERING_SPEED : -PROP_LOWERING_SPEED, 1f);
        do{
            Node prop = (Node)mobileCraneChildren.get(i);
            if(Arrays.binarySearch(props, prop.getName()) >= 0){
                    changed++;
                    PhysicsManager.moveWithScallingObject(!lowering, propDisplacement, scallingVector,
                            new Node[] { (Node)prop.getChild(0) }, prop.getChild(1));
            }
            i++;
        }while(changed < 4);
    }
    
    private void getOff(String name){
        // zezwala na opuszczenie podpór tylko gdy dźwig nie porusza się
        if("".equals(key)){
            Control.removeListener(this);
            MobileCraneArmControl control = (MobileCraneArmControl)getArmControl();
            control.setUsing(!control.isUsing());
            GameManager.setLastAction(name);
            setSteeringAngle(0f); 
        }
    }
    
    private void getInCabin(){
        if(propsLowering <= MAX_PROP_PROTRUSION){
            controlProps(true);
        }else{
            /* metoda wywołana na łańcuchu "Action", gdyż ostatnia akcja może być nullem.
            Może być bez tego ifa, ale dodany w celu optymalizacji aby nie powtarzać
            dodawania listenerów klawiszy*/
            if(Control.Actions.ACTION.toString().equals(GameManager.getLastAction())){
                Control.addListener(getArmControl());
                GameManager.setLastAction(null);
                HUD.setMessage(Translator.LOWERED_PROPS.getValue());
            }
        }
    }
    
    private void getInMobileCrane(){
        if(propsLowering > MIN_PROP_PROTRUSION)
            controlProps(false);
        else{
            if(Control.Actions.ACTION.toString().equals(GameManager.getLastAction())){
                Control.addListener(this);
                GameManager.setLastAction(null);
                HUD.setMessage(Translator.HEIGHTENED_PROPS.getValue());
            }
        }
    }
}
