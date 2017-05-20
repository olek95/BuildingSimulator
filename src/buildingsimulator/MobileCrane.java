package buildingsimulator;

import static buildingsimulator.GameManager.calculateDisplacementAfterScaling;
import static buildingsimulator.GameManager.moveWithScallingObject;
import buildingsimulator.Control.Actions;
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
/**
 * Obiekt klasy <code>MobileCrane</code> reprezentuje mobilny dźwig. Dźwig
 * ten posiada umiejętność poruszania się w przód i w tył, skręciania oraz 
 * obsługi ramienia dźwigu. 
 * @author AleksanderSklorz
 */
public class MobileCrane extends CraneAbstract implements ActionListener, Controllable{
    private BuildingSimulator game = BuildingSimulator.getBuildingSimulator();
    private Node crane = (Node)game.getAssetManager().loadModel("Models/dzwig/dzwig.j3o");
    private VehicleControl craneControl = crane.getControl(VehicleControl.class);
    private static final float ACCELERATION_FORCE = 100.0f, BRAKE_FORCE = 20.0f,
            FRICTION_FORCE = 10.0f, PROP_LOWERING_SPEED = 0.05f;
    public static final float MAX_PROP_PROTRUSION = 6.35f, MIN_PROP_PROTRUSION = 1f;
    private float steeringValue = 0f, propsLowering = 1f;
    private String key = "";
    private Vector3f propDisplacement;
    public static final boolean WEAK = true;
    private Actions[] availableActions = {Actions.UP, Actions.DOWN, Actions.LEFT,
        Actions.RIGHT, Actions.ACTION};
    public MobileCrane(){
        crane.setLocalTranslation(0, 1.15f, 0);
        createMobileCranePhysics();
        scaleTiresTexture();
        createMirrors();
        game.getRootNode().attachChild(crane);
        PhysicsSpace physics = game.getBulletAppState().getPhysicsSpace();
        physics.add(craneControl);
        setArmControl(new MobileCraneArmControl(crane));
        propDisplacement =  calculateDisplacementAfterScaling((Node)crane
                .getChild("protractileProp1"), new Vector3f(1f, propsLowering + PROP_LOWERING_SPEED,
                1f), false, true, false);
    }
    
    @Override
    public void onAction(String name, boolean isPressed, float tpf) {
        switch(Actions.valueOf(name)){
            case UP:
                if(isPressed){
                    key = name;
                    craneControl.accelerate(0f);
                    craneControl.brake(BRAKE_FORCE);
                }else{
                    key = null;
                    craneControl.accelerate(craneControl.getCurrentVehicleSpeedKmHour() > 0 ?
                            -FRICTION_FORCE : FRICTION_FORCE);
                }
                break;
            case DOWN:
                if(isPressed){
                    key = name;
                    craneControl.accelerate(0f);
                    craneControl.brake(BRAKE_FORCE);
                }else{
                    key = null;
                    craneControl.brake(0f);
                    craneControl.accelerate(craneControl.getCurrentVehicleSpeedKmHour() < 0 ?
                            FRICTION_FORCE : -FRICTION_FORCE);
                }
                break;
            case LEFT:
                craneControl.steer(steeringValue += isPressed ? 0.5f : -0.5f);
                break;
            case RIGHT: 
                craneControl.steer(steeringValue += isPressed ? -0.5f : 0.5f);
                break;
            case ACTION: if(isPressed) getOff(name);
        }
    }
    
    /**
     * Aktualizuje stan pojazdu. Możliwe stany to: stop, jazda w przód i jazda
     * w tył. 
     */
    public void updateState(){
        if(key == null){
            if(craneControl.getCurrentVehicleSpeedKmHour() < 1 
                    && craneControl.getCurrentVehicleSpeedKmHour() > -1)
                stop();
        }else{ 
            if(!key.equals("")){
                if(key.equals(Actions.DOWN.toString()) && craneControl
                        .getCurrentVehicleSpeedKmHour() < 0){
                    craneControl.brake(0f);
                    craneControl.accelerate(-ACCELERATION_FORCE * 0.5f); // prędkość w tył jest mniejsza 
                }else{
                    if(key.equals(Actions.UP.toString()) && Math.ceil(craneControl
                            .getCurrentVehicleSpeedKmHour()) >= 0){
                        craneControl.brake(0f);
                        craneControl.accelerate(ACCELERATION_FORCE);
                    }
                }
            }else{
                if(((MobileCraneArmControl)getArmControl()).isUsing()) getInCabin();
                else getInMobileCrane();
            }
        }
    }
    
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
            if(element.getName().startsWith("wheel")){
                Geometry tire = ((Geometry)((Node)((Node)element).getChild(0)).getChild(0));
                Material tireMaterial = tire.getMaterial();
                if(tireTexture == null){
                    tireTexture = (Texture)tireMaterial.getTextureParam("DiffuseMap").getValue();
                    tireTexture.setWrap(Texture.WrapMode.Repeat);
                    tire.getMesh().scaleTextureCoordinates(new Vector2f(1,6f));
                }else tireMaterial.setTexture("DiffuseMap", tireTexture);
            }
    }
    
    private void createMirrors(){
        Geometry mirror;
        float x = 0.2f;
        for(int i = 0; i < 2; i++){
            if(i == 0) mirror = (Geometry)crane.getChild("leftMirror");
            else mirror = (Geometry)crane.getChild("rightMirror");
            SimpleWaterProcessor mirrorProcessor = new SimpleWaterProcessor(game.getAssetManager());
            mirrorProcessor.setReflectionScene(game.getRootNode());
            mirrorProcessor.setDistortionScale(0.0f);
            mirrorProcessor.setWaveSpeed(0.0f);
            mirrorProcessor.setWaterDepth(0f);
            if(i == 1) x = -x;
            mirrorProcessor.setPlane(new Vector3f(0f, 0f, 0f),new Vector3f(x, 0f, 1f));
            game.getViewPort().addProcessor(mirrorProcessor);
            mirror.setMaterial(mirrorProcessor.getMaterial());
        }
    }
    
    private void createMobileCranePhysics(){
        GameManager.addNewCollisionShapeToCompound((CompoundCollisionShape)craneControl
                .getCollisionShape(),crane, "outsideMobileCraneCabin", Vector3f.ZERO, null);
        GameManager.addNewCollisionShapeToCompound((CompoundCollisionShape)craneControl
                .getCollisionShape(),crane, "bollardsShape", Vector3f.ZERO, null);
    }
    
    /**
     * Pozwala na kontrolowanie podporami (na opuszczanie i podnioszenie ich). 
     * @param lowering true jeśli podpory mają być opuszczane, false jeśli podnioszone
     */
    public void controlProps(boolean lowering){
        List<Spatial> mobileCraneChildren = crane.getChildren();
        int i = 0, changed = 0;
        String[] props = {"propParts1", "propParts2", "propParts3",
            "propParts4"};
        Vector3f scallingVector = new Vector3f(1f, propsLowering += lowering ? 
                PROP_LOWERING_SPEED : -PROP_LOWERING_SPEED, 1f);
        do{
            Node prop = (Node)mobileCraneChildren.get(i);
            if(Arrays.binarySearch(props, prop.getName()) >= 0){
                    changed++;
                    moveWithScallingObject(!lowering, propDisplacement, scallingVector, (Node)prop
                            .getChild(0), prop.getChild(1));
            }
            i++;
        }while(changed < 4);
    }
    
    /**
     * Zwraca wartość określającą jak bardzo opuszczone są podpory. 
     * @return wartość określającą opuszczenie podpór 
     */
    public float getPropsLowering(){
        return propsLowering;
    }
    
    private void getOff(String name){
        // zezwala na opuszczenie podpór tylko gdy dźwig nie porusza się
        if("".equals(key)){
            Control.removeListener((MobileCrane)GameManager.getUnit(0));
            MobileCraneArmControl control = (MobileCraneArmControl)getArmControl();
            control.setUsing(!control.isUsing());
            GameManager.setLastAction(name);
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
                Control.setupKeys(getArmControl());
                GameManager.setLastAction(null);
            }
        }
    }
    
    private void getInMobileCrane(){
        if(propsLowering > MIN_PROP_PROTRUSION)
            controlProps(false);
        else{
            if(Control.Actions.ACTION.toString().equals(GameManager.getLastAction())){
                Control.setupKeys(this);
                GameManager.setLastAction(null);
            }
        }
    }
    
    public Control.Actions[] getAvailableActions(){
        return availableActions;
    }
}
