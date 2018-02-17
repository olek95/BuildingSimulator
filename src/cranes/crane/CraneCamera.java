package cranes.crane;

import buildingsimulator.ElementName;
import com.jme3.scene.Node;
import cranes.AbstractCraneCamera;
import cranes.CameraType;

/**
 * Klasa <code>CraneCamera</code> reprezentuje kamerę żurawia. 
 * @author AleksanderSklorz 
 */
public class CraneCamera extends AbstractCraneCamera {
    public CraneCamera(Node crane) {
        super(crane);
        switchCameraOwner((Node)crane.getChild(ElementName.ARM_CONTROL));
    }
    
    /**
     * Zmienia kamere. 
     */
    public void changeCamera() {
        CameraType type = getType(); 
        switch(type) {
            case CABIN:
                setType(CameraType.BEHIND);
                break;
            case BEHIND: 
                setType(CameraType.BEHIND_ARM);
                break;
            case BEHIND_ARM: 
                setType(CameraType.BIRDS_EYE_VIEW);
                break;
            case BIRDS_EYE_VIEW: 
                setType(CameraType.LOOSE);
                break;
            default: 
                if(type.equals(CameraType.LOOSE)) {
                   setOffLoose();
                }
                setType(CameraType.CABIN);
        }
        if(getType().equals(CameraType.LOOSE)) setOff();
        else setPosition();
    }
}
