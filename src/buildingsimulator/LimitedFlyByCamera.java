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
        GameManager.getInputManager().addListener(this, "FLYCAM_Left", "FLYCAM_Right",
                "FLYCAM_Up", "FLYCAM_Down", "FLYCAM_StrafeLeft", "FLYCAM_StrafeRight", 
                "FLYCAM_Forward", "FLYCAM_Backward", "FLYCAM_ZoomIn", "FLYCAM_ZoomOut",
                "FLYCAM_RotateDrag", "FLYCAM_Rise", "FLYCAM_Lower",  "FLYCAM_InvertY");
    }
    
    @Override
    public void onAnalog(String name, float value, float tpf) {
        super.onAnalog(name, value, tpf);
        Vector3f location = cam.getLocation();
        if(location.y <= 0) cam.setLocation(location.setY(1));
    }
}
