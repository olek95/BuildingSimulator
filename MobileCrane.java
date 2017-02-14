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
import java.util.List;
public class MobileCrane implements ActionListener{
    private BuildingSimulator game = BuildingSimulator.getBuildingSimulator();
    private Spatial crane = game.getAssetManager().loadModel("Models/dzwig/dzwig.j3o");
    private VehicleControl control;
    private final float accelerationForce = 100.0f, brakeForce = 20.0f, frictionForce = 10.0f;
    private float steeringValue = 0f;
    private boolean pressed = true, forward = true;
    public MobileCrane(){
        control = crane.getControl(VehicleControl.class);
        crane.setLocalTranslation(0, 100, 0);
        game.getRootNode().attachChild(crane);
        scaleTiresTexture();
        PhysicsSpace physics = game.getBulletAppState().getPhysicsSpace();
        physics.add(control);
    }
    public void onAction(String name, boolean isPressed, float tpf) {
        switch(name){
            case "Up":
                if(isPressed){
                    forward = true;
                    pressed = true;
                    control.accelerate(accelerationForce);
                }else{
                    pressed = false;
                    control.accelerate(-frictionForce);
                }
                break;
            case "Down":
                if(isPressed){
                    forward = false;
                    pressed = true;
                    control.accelerate(0f);
                    control.brake(brakeForce);
                }else{
                    pressed = false;
                    control.brake(0f);
                    if(control.getCurrentVehicleSpeedKmHour() < 0) control.accelerate(frictionForce);
                    else control.accelerate(-frictionForce);
                }
                break;
            case "Left":
                if(isPressed){
                    steeringValue += 0.5f;
                }else{
                    steeringValue -= 0.5f;
                }
                control.steer(steeringValue);
                break;
            case "Right":
                if(isPressed){
                    steeringValue -= 0.5f;
                }else{
                    steeringValue += 0.5f;
                }
                control.steer(steeringValue);
        }
    }
    public void updateState(){
        if(!forward && pressed && control.getCurrentVehicleSpeedKmHour() <= 0){
            control.brake(0f);
            control.accelerate(-brakeForce);
        }else if(!pressed && control.getCurrentVehicleSpeedKmHour() < 1 && control.getCurrentVehicleSpeedKmHour() > -1)
                stop();
    }
    private void scaleTiresTexture(){
        List<Spatial> craneElements = ((Node)crane).getChildren();
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
    private void stop(){
        forward = true;
        pressed = true;
        control.accelerate(0f);
        control.brake(0f);
        control.setLinearVelocity(Vector3f.ZERO);
        control.setAngularVelocity(Vector3f.ZERO);
    }
}

