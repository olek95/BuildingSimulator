package cranes.mobileCrane;

import buildingsimulator.ElementName;
import buildingsimulator.GameManager;
import com.jme3.scene.CameraNode;
import com.jme3.scene.Node;
import cranes.AbstractCraneCamera;
import cranes.CameraType;

public class MobileCraneCamera extends AbstractCraneCamera{
    private Node cameraOwner;
    public MobileCraneCamera(Node crane) {
        super(crane);
        switchCameraOwner(crane);
    }
    
    public void changeCamera(boolean armControlMode) {
        CameraType type = getType(); 
        if(!armControlMode) {
            switchCameraOwner(getCrane());
            if(type.equals(CameraType.CABIN)) setType(CameraType.BEHIND);
            else setType(type.equals(CameraType.BEHIND) ? CameraType.BIRDS_EYE_VIEW : CameraType.CABIN);
        } else {
            switchCameraOwner(((Node)getCrane().getChild(ElementName.ARM_CONTROL)));
            if(type.equals(CameraType.ARM_CABIN)) setType(CameraType.BEHIND_ARM);
            else setType(type.equals(CameraType.BEHIND_ARM) ? CameraType.BIRDS_EYE_VIEW : CameraType.ARM_CABIN);
        }
        setPosition();
    }
    
    @Override
    public void restore() {
        CameraNode craneCamera = new CameraNode("Camera", GameManager.getCamera());
        setCraneCamera(craneCamera);
        cameraOwner.attachChild(craneCamera);
        setPosition();
    }
    
    private void switchCameraOwner(Node newOwner) {
        if(!newOwner.equals(cameraOwner)) {
            CameraNode camera = getCraneCamera(); 
            camera.removeFromParent();
            newOwner.attachChild(camera);
            cameraOwner = newOwner; 
        }
    }
}
