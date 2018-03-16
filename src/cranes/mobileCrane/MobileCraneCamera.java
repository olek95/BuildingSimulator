package cranes.mobileCrane;

import buildingsimulator.ElementName;
import buildingsimulator.GameManager;
import com.jme3.scene.Node;
import cranes.AbstractCraneCamera;
import cranes.CameraType;

/**
 * Klasa <code>MobileCraneCamera</code> reprezentuje kamerę dźwigu mobilnego. 
 * @author AleksanderSklorz 
 */
public class MobileCraneCamera extends AbstractCraneCamera{
    public MobileCraneCamera(Node crane) {
        super(crane);
        switchCameraOwner(crane);
    }
    
    /**
     * Zmienia kamerę. 
     * @param armControlMode true jeśli aktualnie sterowane jest ramię dźwigu, 
     * false w przeciwnym przypadku 
     */
    public void changeCamera(boolean armControlMode) {
        CameraType type = getType(); 
        if(!armControlMode) {
            switchCameraOwner(getCrane());
            if(type.equals(CameraType.CABIN)) setType(CameraType.BEHIND);
            else if(type.equals(CameraType.BEHIND)) setType(CameraType.BIRDS_EYE_VIEW);
            else if(type.equals(CameraType.BIRDS_EYE_VIEW)) setType(CameraType.LOOSE);
            else {
                if(type.equals(CameraType.LOOSE)) {
                   setOffLoose();
                }
                setType(CameraType.CABIN);
                GameManager.displayProperMobileCraneHUD();
            }
        } else {
            switchCameraOwner(((Node)getCrane().getChild(ElementName.ARM_CONTROL)));
            if(type.equals(CameraType.ARM_CABIN)) setType(CameraType.BEHIND_ARM);
            else if(type.equals(CameraType.BEHIND_ARM)) setType(CameraType.BIRDS_EYE_VIEW);
            else if(type.equals(CameraType.BIRDS_EYE_VIEW)) setType(CameraType.LOOSE);
            else {
                if(type.equals(CameraType.LOOSE)) {
                   setOffLoose();
                }
                setType(CameraType.ARM_CABIN);
                GameManager.displayProperMobileCraneHUD();
            }
        }
        if(getType().equals(CameraType.LOOSE)) {
            setOff();
            setLooseCameraPosition();
            GameManager.displayProperMobileCraneHUD();
        } else setPosition();
    }
}
