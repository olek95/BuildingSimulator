package buildingsimulator;

import com.jme3.scene.Spatial;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.control.VehicleControl;
import com.jme3.input.controls.ActionListener;
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
    public VehicleControl getControl(){
        return control;
    }
    public boolean getForward(){
        return forward;
    }
    public boolean getPressed(){
        return pressed;
    }
    public void setForward(boolean forward){
        this.forward = forward;
    }
    public void setPressed(boolean pressed){
        this.pressed = pressed;
    }
    public float getBrakeForce(){
        return brakeForce;
    }
}
