package buildingsimulator;

import com.jme3.scene.Spatial;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.control.VehicleControl;
import com.jme3.input.controls.ActionListener;
import com.jme3.math.Vector3f;
public class MobileCrane implements ActionListener{
    private BuildingSimulator game = BuildingSimulator.getBuildingSimulator();
    private Spatial crane = game.getAssetManager().loadModel("Models/dzwig/dzwig.j3o");
    private VehicleControl control;
    private final float accelerationForce = 200.0f, brakeForce = 20.0f, 
            stopForceWithoutBrake = 10.0f;
    private float accelerationValue = 0, steeringValue = 0f;
    private boolean down = false, kierunek = false, puszczone = false;
    private Thread stateVehicle;
    private String key;
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
                   System.out.println(control.getCurrentVehicleSpeedKmHour());
                   if(key != null && key.equals("Down") && !puszczone && control.getCurrentVehicleSpeedKmHour() <= 0){
                           //accelerationValue += accelerationForce;
                       control.brake(0f);
                       control.accelerate(-brakeForce);
                   }else{
                       if(key != null && (key.equals("Down") || key.equals("Up")) 
                               && puszczone && control.getCurrentVehicleSpeedKmHour() < 1 && control.getCurrentVehicleSpeedKmHour() > -1){
                           System.out.println("D");
                           key = null;
                           puszczone = false;
                           control.accelerate(0f);
                           control.brake(0f);
                           control.setLinearVelocity(Vector3f.ZERO);
                           control.setAngularVelocity(Vector3f.ZERO);
                       }/*else{
                            if(key != null && key.equals("Up") && puszczone && control.getCurrentVehicleSpeedKmHour() <= 0){
                                System.out.println("U");
                                key = null;
                                puszczone = false;
                                control.brake(0f);
                                control.setLinearVelocity(Vector3f.ZERO);
                                control.setAngularVelocity(Vector3f.ZERO);
                            }
                       }*/
                   }
                   if(!kierunek){
                       /*if(control.getCurrentVehicleSpeedKmHour() <= 0){
                           control.brake(0f);
                           control.setLinearVelocity(Vector3f.ZERO);
                           control.setAngularVelocity(Vector3f.ZERO);
                       }*/
                   }
               }
           } 
        });
        stateVehicle.start();
    }
    public void onAction(String name, boolean isPressed, float tpf) {
        if(name.equals("Up")){
            if(isPressed){
                kierunek = true;
                key = name;
                //accelerationValue += accelerationForce;
                control.accelerate(accelerationValue);
            }else{
                kierunek = false;
                puszczone = true;
                //accelerationValue -= accelerationForce;
                control.accelerate(-stopForceWithoutBrake);
                //control.brake(stopForceWithoutBrake);
                //key = null;
            }
            //control.accelerate(accelerationValue);
        }else{
            if(name.equals("Down")){
                if(isPressed){
                    System.out.println("asas");
                    key = name;
                    puszczone = false;
                    control.accelerate(0f);
                    control.brake(brakeForce);
                }else{
                    //key = null;
                    puszczone = true;
                    control.brake(0f);
                    if(control.getCurrentVehicleSpeedKmHour() < 0) control.accelerate(stopForceWithoutBrake);
                    else control.accelerate(-stopForceWithoutBrake);
                }
            }else{
                if(name.equals("Left")){
                    if(isPressed){
                        steeringValue += 0.5f;
                    }else{
                        steeringValue -= 0.5f;
                    }
                    control.steer(steeringValue);
                }else{
                    if(name.equals("Right")){
                        if(isPressed){
                            steeringValue -= 0.5f;
                        }else{
                            steeringValue += 0.5f;
                        }
                        control.steer(steeringValue);
                    }
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
