package buildingsimulator;

import com.jme3.input.FlyByCamera;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;

/**
 * Klasa <code>LimitedFlyByCamera</code> reprezentuje rozszerzoną, ograniczoną przez 
 * dolną granicę wersję poruszającej się kamery. 
 * @author AleksanderSklorz
 */
public class LimitedFlyByCamera extends FlyByCamera{
    public LimitedFlyByCamera(Camera cam) {
        super(cam);
        moveSpeed = 100;
    }
    
    @Override
    public void onAnalog(String name, float value, float tpf) {
        Vector3f location = cam.getLocation();
        if(location.y <= 0) cam.setLocation(location.setY(1));
        if(rotationSpeed == 0) {
            if(name.equals("FLYCAM_Forward")) {
                cam.setLocation(cam.getLocation().addLocal(0, 0, value * moveSpeed));
            } else {
                if(name.equals("FLYCAM_Backward")) {
                    cam.setLocation(cam.getLocation().subtractLocal(0, 0, value * moveSpeed));
                } else super.onAnalog(name, value, tpf);
            }
        } else super.onAnalog(name, value, tpf);
    }
}
