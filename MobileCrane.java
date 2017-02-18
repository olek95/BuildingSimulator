package buildingsimulator;

import com.jme3.scene.Spatial;
import com.jme3.bullet.PhysicsSpace;
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
public class MobileCrane implements ActionListener{
    private BuildingSimulator game = BuildingSimulator.getBuildingSimulator();
    private Spatial craneSpatial = game.getAssetManager().loadModel("Models/dzwig9/dzwig9.j3o");
    private VehicleControl crane;
    private CraneCabin cabin;
    private final float accelerationForce = 100.0f, brakeForce = 20.0f, frictionForce = 10.0f;
    private float steeringValue = 0f;
    private String key = "";
    boolean using = true;
    public MobileCrane(){
        crane = craneSpatial.getControl(VehicleControl.class);
        craneSpatial.setLocalTranslation(0, 10, 0); // 100
        game.getRootNode().attachChild(craneSpatial);
        //scaleTiresTexture();
        createMirrors();
        //PhysicsSpace physics = game.getBulletAppState().getPhysicsSpace();
        //physics.add(crane);
        //cabin = new CraneCabin(craneSpatial);
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
    public CraneCabin getCabin(){
        return cabin;
    }
    private void scaleTiresTexture(){
        List<Spatial> craneElements = ((Node)craneSpatial).getChildren();
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
        Geometry g;
        float x = 0.2f;
        Node craneNode = (Node)craneSpatial;
        for(int i = 0; i < 2; i++){
            if(i == 0) g = (Geometry)craneNode.getChild("Circle.0024");
            else g = (Geometry)craneNode.getChild("Circle.0003");
            SimpleWaterProcessor waterProcessor = new SimpleWaterProcessor(game.getAssetManager());
            waterProcessor.setReflectionScene(game.getRootNode());
            waterProcessor.setDistortionScale(0.0f);
            waterProcessor.setWaveSpeed(0.0f);
            waterProcessor.setWaterDepth(0f);
            if(i == 1) x = -x;
            waterProcessor.setPlane(new Vector3f(0f, 0f, 0f),new Vector3f(x, 0f, 1f));
            game.getViewPort().addProcessor(waterProcessor);
            Material mat = waterProcessor.getMaterial();
            g.setMaterial(mat);
        }
    }
    private void stop(){
        key = "";
        crane.accelerate(0f);
        crane.brake(0f);
        crane.setLinearVelocity(Vector3f.ZERO); // jeśli jeszcze jest jakaś mała prędkość, to zeruje
    }
}

