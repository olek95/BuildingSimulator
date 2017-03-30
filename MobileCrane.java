package buildingsimulator;

import com.jme3.scene.Spatial;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.collision.shapes.CompoundCollisionShape;
import com.jme3.bullet.control.VehicleControl;
import com.jme3.bullet.util.CollisionShapeFactory;
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
    private Node craneSpatial = (Node)game.getAssetManager().loadModel("Models/dzwig/dzwig.j3o");
    private VehicleControl crane = craneSpatial.getControl(VehicleControl.class);
    private CraneCabin cabin;
    private final float accelerationForce = 100.0f, brakeForce = 20.0f, frictionForce = 10.0f;
    private float steeringValue = 0f;
    private String key = "";
    boolean using = true;
    public MobileCrane(){
        craneSpatial.setLocalTranslation(0, 1.15f, 0); // 100
        createMobileCranePhysics();
        scaleTiresTexture();
        createMirrors();
        game.getRootNode().attachChild(craneSpatial);
        PhysicsSpace physics = game.getBulletAppState().getPhysicsSpace();
        physics.add(crane);
        cabin = new CraneCabin(craneSpatial);
    }
    public void onAction(String name, boolean isPressed, float tpf) {
        switch(name){
            case "Up":
                if(isPressed){
                    key = name;
                    crane.accelerate(0f);
                    crane.brake(brakeForce);
                }else{
                    key = null;
                    if(crane.getCurrentVehicleSpeedKmHour() > 0) crane.accelerate(-frictionForce);
                    else crane.accelerate(frictionForce);
                }
                break;
            case "Down":
                if(isPressed){
                    key = name;
                    crane.accelerate(0f);
                    crane.brake(brakeForce);
                }else{
                    key = null;
                    crane.brake(0f);
                    if(crane.getCurrentVehicleSpeedKmHour() < 0) crane.accelerate(frictionForce);
                    else crane.accelerate(-frictionForce);
                }
                break;
            case "Left":
                if(isPressed){
                    steeringValue += 0.5f;
                }else{
                    steeringValue -= 0.5f;
                }
                crane.steer(steeringValue);
                break;
            case "Right":
                if(isPressed){
                    steeringValue -= 0.5f;
                }else{
                    steeringValue += 0.5f;
                }
                crane.steer(steeringValue);
        }
    }
    /**
     * Aktualizuje stan pojazdu. Możliwe stany to: stop, jazda w przód i jazda
     * w tył. 
     */
    public void updateState(){
        if(key == null){
            if(crane.getCurrentVehicleSpeedKmHour() < 1 && crane.getCurrentVehicleSpeedKmHour() > -1)
                stop();
        }else if(!key.equals(""))
            if(key.equals("Down") && crane.getCurrentVehicleSpeedKmHour() < 0){
                crane.brake(0f);
                crane.accelerate(-accelerationForce * 0.5f); // prędkość w tył jest mniejsza 
            }else{
                if(key.equals("Up") && Math.ceil(crane.getCurrentVehicleSpeedKmHour()) >= 0){
                    crane.brake(0f);
                    crane.accelerate(accelerationForce);
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
    private void stop(){
        key = "";
        crane.accelerate(0f);
        crane.brake(0f);
        crane.setLinearVelocity(Vector3f.ZERO); // jeśli jeszcze jest jakaś mała prędkość, to zeruje
    }
    private void scaleTiresTexture(){
        List<Spatial> craneElements = craneSpatial.getChildren();
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
            if(i == 0) mirror = (Geometry)craneSpatial.getChild("leftMirror");
            else mirror = (Geometry)craneSpatial.getChild("rightMirror");
            SimpleWaterProcessor mirrorProcessor = new SimpleWaterProcessor(game.getAssetManager());
            mirrorProcessor.setReflectionScene(game.getRootNode());
            mirrorProcessor.setDistortionScale(0.0f);
            mirrorProcessor.setWaveSpeed(0.0f);
            mirrorProcessor.setWaterDepth(0f);
            if(i == 1) x = -x;
            mirrorProcessor.setPlane(new Vector3f(0f, 0f, 0f),new Vector3f(x, 0f, 1f));
            game.getViewPort().addProcessor(mirrorProcessor);
            Material mirrorMaterial = mirrorProcessor.getMaterial();
            mirror.setMaterial(mirrorMaterial);
        }
    }
    private void createMobileCranePhysics(){
        String[] mobileCraneElements = {"outsideMobileCraneCabin", "bollardsShape"};
        CompoundCollisionShape mobileCraneCollision = (CompoundCollisionShape)crane.getCollisionShape();
        for(int i = 0; i < mobileCraneElements.length; i++){
            Geometry elementGeometry = (Geometry)craneSpatial.getChild(mobileCraneElements[i]);
            CollisionShape elementCollision = CollisionShapeFactory
                    .createDynamicMeshShape(elementGeometry);
            elementCollision.setScale(elementGeometry.getWorldScale());
            mobileCraneCollision.addChildShape(elementCollision, Vector3f.ZERO);
        }
    }
}

