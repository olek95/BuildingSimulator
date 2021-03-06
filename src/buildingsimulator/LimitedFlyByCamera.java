package buildingsimulator;

import com.jme3.input.FlyByCamera;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import settings.Control.Actions;

/**
 * Klasa <code>LimitedFlyByCamera</code> reprezentuje rozszerzoną, ograniczoną przez 
 * granicę mapy oraz podłogę wersję poruszającej się kamery. 
 * @author AleksanderSklorz
 */
public class LimitedFlyByCamera extends FlyByCamera implements Controllable{
    private Actions[] availableActions = new Actions[]{Actions.FLYCAM_Backward, 
        Actions.FLYCAM_Forward, Actions.FLYCAM_StrafeLeft, Actions.FLYCAM_StrafeRight,
        Actions.FLYCAM_Rise, Actions.FLYCAM_Lower}; 
    
    public LimitedFlyByCamera(Camera cam) {
        super(cam);
        moveSpeed = 100;
    }
    
    @Override
    public void onAnalog(String name, float value, float tpf) {
        /* zmienia zachowanie sterowania - gdy widok z lotu ptaka, W i S sterują
           w górę/w dół ekranu, zamiast w górę/dół mapy */
        if(rotationSpeed == 0) {
            if(name.equals("FLYCAM_Forward")) {
                cam.setLocation(cam.getLocation().addLocal(0, 0, value * moveSpeed));
            } else {
                if(name.equals("FLYCAM_Backward")) {
                    cam.setLocation(cam.getLocation().subtractLocal(0, 0, value * moveSpeed));
                } else super.onAnalog(name, value, tpf);
            }
        } else super.onAnalog(name, value, tpf);
        Vector3f location = cam.getLocation();
        if(location.y <= 0) location.y = 1;
        else {
            float positiveLocation = Map.calculateBorderLocation(false);
            if(positiveLocation < location.x) location.x = positiveLocation;
            else if(-positiveLocation > location.x) location.x = -positiveLocation;
            if(positiveLocation < location.z) location.z = positiveLocation;
            else if(-positiveLocation > location.z) location.z = -positiveLocation;
        }
    }
    
    @Override
    public Actions[] getAvailableActions() { 
        for(int i = 0; i < availableActions.length; i++) 
            inputManager.deleteMapping(availableActions[i].toString());
        return availableActions;
    }
}
