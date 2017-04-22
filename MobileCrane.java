package buildingsimulator;

import com.jme3.scene.Spatial;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.collision.PhysicsCollisionEvent;
import com.jme3.bullet.collision.PhysicsCollisionListener;
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
import java.util.List;
/**
 * Obiekt klasy <code>MobileCrane</code> reprezentuje mobilny dźwig. Dźwig
 * ten posiada umiejętność poruszania się w przód i w tył, skręciania oraz 
 * obsługi ramienia dźwigu. 
 * @author AleksanderSklorz
 */
public class MobileCrane implements ActionListener{
    private BuildingSimulator game = BuildingSimulator.getBuildingSimulator();
    private Node crane = (Node)game.getAssetManager().loadModel("Models/dzwig/dzwig.j3o");
    private VehicleControl craneControl = crane.getControl(VehicleControl.class);
    private CraneCabin cabin;
    private static final float ACCELERATION_FORCE = 100.0f, BRAKE_FORCE = 20.0f,
            FRICTION_FORCE = 10.0f;
    private float steeringValue = 0f;
    private String key = "";
    boolean using = true;
    public MobileCrane(){
        crane.setLocalTranslation(0, 1.15f, 0);
        createMobileCranePhysics();
        scaleTiresTexture();
        createMirrors();
        game.getRootNode().attachChild(crane);
        PhysicsSpace physics = game.getBulletAppState().getPhysicsSpace();
        physics.add(craneControl);
        cabin = new CraneCabin(crane);
    }
    @Override
    public void onAction(String name, boolean isPressed, float tpf) {
        switch(name){
            case "Up":
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
            case "Down":
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
            case "Left":
                craneControl.steer(steeringValue += isPressed ? 0.5f : -0.5f);
                break;
            case "Right": craneControl.steer(steeringValue += isPressed ? -0.5f : 0.5f);
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
        }else if(!key.equals(""))
            if(key.equals("Down") && craneControl.getCurrentVehicleSpeedKmHour() < 0){
                craneControl.brake(0f);
                craneControl.accelerate(-ACCELERATION_FORCE * 0.5f); // prędkość w tył jest mniejsza 
            }else{
                if(key.equals("Up") && Math.ceil(craneControl.getCurrentVehicleSpeedKmHour()) >= 0){
                    craneControl.brake(0f);
                    craneControl.accelerate(ACCELERATION_FORCE);
                }
            }
    }
    /**
     * Zwraca kabinę operatora ramienia dźwigu. 
     * @return kabina operatora ramienia dźwigu. 
     */
    public CraneCabin getCabin(){
        return cabin;
    }
    /**
     * Zwraca aktualną prędkość dźwigu. 
     * @return prędkość
     */
    public float getSpeed(){
        return craneControl.getCurrentVehicleSpeedKmHour();
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
        GameManager.addNewCollisionShapeToComponent((CompoundCollisionShape)craneControl
                .getCollisionShape(),crane, "outsideMobileCraneCabin", Vector3f.ZERO, null);
        GameManager.addNewCollisionShapeToComponent((CompoundCollisionShape)craneControl
                .getCollisionShape(),crane, "bollardsShape", Vector3f.ZERO, null);
    }
}
