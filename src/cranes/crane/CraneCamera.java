package cranes.crane;

import buildingsimulator.ElementName;
import com.jme3.scene.Node;
import cranes.AbstractCraneCamera;
import cranes.CameraType;

public class CraneCamera extends AbstractCraneCamera {
    public CraneCamera(Node crane) {
        super(crane);
        switchCameraOwner((Node)crane.getChild(ElementName.ARM_CONTROL));
    }
    
    public void changeCamera() {
        CameraType type = getType(); 
        switch(type) {
            case CABIN:
                setType(CameraType.BEHIND);
                break;
            case BEHIND: 
                setType(CameraType.BEHIND_ARM);
                break;
            default: 
                setType(CameraType.CABIN);
        }
        setPosition();
    }
}
