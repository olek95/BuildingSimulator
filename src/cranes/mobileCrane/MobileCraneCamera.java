package cranes.mobileCrane;

import buildingsimulator.ElementName;
import buildingsimulator.GameManager;
import com.jme3.input.FlyByCamera;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.scene.CameraNode;
import com.jme3.scene.Node;

public class MobileCraneCamera {
    public enum CameraType {
        CABIN("cabinCamStart", "cabinCamEnd"),
        BEHIND("behindCamStart", "behindCamEnd"),
        ARM_CABIN("armCabinCamStart", "armCabinCamEnd"),
        BEHIND_ARM("behindArmCamStart", "behindArmCamEnd"),
        HOOK("hookCamStart", "hookCamEnd");
        
        private String start; 
        private String end;
        
        private CameraType(String start, String end) {
            this.start = start;
            this.end = end;
        }
    }
    
    private Camera camera = GameManager.getCamera();
    private FlyByCamera flyByCamera = GameManager.getFlyByCamera();
    private Node crane, cameraOwner;
    private CameraNode craneCamera; 
    private CameraType type;
    public MobileCraneCamera(Node crane) {
        this.crane = crane; 
        this.type = CameraType.CABIN;
        craneCamera = new CameraNode("Camera", camera);
        switchCameraOwner(crane);
        setPosition();
    }
    
    public void changeCamera(boolean armControlMode) {
        if(!armControlMode) {
            type = type.equals(CameraType.CABIN) ? CameraType.BEHIND : CameraType.CABIN;
        } else {
            switchCameraOwner(((Node)crane.getChild(ElementName.ARM_CONTROL)));
            if(type.equals(CameraType.ARM_CABIN)) {
                type = CameraType.BEHIND;
            } else {
                type = CameraType.ARM_CABIN;
            }
        }
        setPosition();
    }
    
    private void setPosition() {
        camera.lookAt(crane.getChild(type.end).getWorldTranslation(), Vector3f.UNIT_XYZ);
        craneCamera.setLocalTranslation(crane.getChild(type.start).getLocalTranslation());
    }
    
    private void switchCameraOwner(Node newOwner) {
        if(!newOwner.equals(cameraOwner)) {
            crane.detachChild(craneCamera);
            newOwner.attachChild(craneCamera);
            cameraOwner = newOwner; 
        }
    }
}
