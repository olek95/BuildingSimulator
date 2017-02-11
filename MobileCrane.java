package buildingsimulator;

import com.jme3.scene.Spatial;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.control.VehicleControl;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.AnalogListener;
import com.jme3.math.Vector3f;
public class MobileCrane implements ActionListener{
    private BuildingSimulator game = BuildingSimulator.getBuildingSimulator();
    private Spatial crane = game.getAssetManager().loadModel("Models/dzwig/dzwig.j3o");
    private VehicleControl control;
    private final float accelerationForce = 1000.0f, brakeForce = 50.0f, stopForceWithoutBrake = 10.0f;
    private float accelerationValue = 0;
    private boolean down = false, kierunek = false;
    private Thread stateVehicle;
    public MobileCrane(){
        control = crane.getControl(VehicleControl.class);
        System.out.println(control);
        crane.setLocalTranslation(0, 50, 0);
        game.getRootNode().attachChild(crane);
        PhysicsSpace physics = game.getBulletAppState().getPhysicsSpace();
        physics.add(control);
        stateVehicle = new Thread(new Runnable(){
           public void run(){
               while(true){
                   if(!kierunek){
                       if(control.getCurrentVehicleSpeedKmHour() <= 0){
                           control.brake(0f);
                           control.setLinearVelocity(Vector3f.ZERO);
                           control.setAngularVelocity(Vector3f.ZERO);
                       } 
                   }
               }
           } 
        });
        stateVehicle.start();
    }
    public void onAction(String name, boolean isPressed, float tpf) {
        if(name.equals("Up")){
            if(isPressed){
                accelerationValue += accelerationForce;
                kierunek = true;
            }else{
                accelerationValue -= accelerationForce;
                control.brake(stopForceWithoutBrake);
                kierunek = false;
            }
            control.accelerate(accelerationValue);
        }else{
            if(name.equals("Down")){
                if(isPressed){
                    control.brake(brakeForce);
                }else{
                    control.brake(0f);
                }
            }
        }
    }

    /*public void onAnalog(String name, float value, float tpf) {
        if(name.equals("Down")){
            if(control.getCurrentVehicleSpeedKmHour() > 0){
                System.out.println("Hamowanie");
                control.brake(brakeForce);
            }else{
                if(down){
                    control.accelerate(-brakeForce);
                }else{
                    control.accelerate(brakeForce);
                }
            }
        }
    }*/
}
