package cranes;

import buildingsimulator.BuildingSimulator;
import com.jme3.renderer.Camera;
import com.jme3.scene.CameraNode;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;

/**
 * Klasa <code>AbstractCraneCamera</code> reprezentuje klasę abstrakcyjną 
 * kamer wszystkich rodzai dźwigów występujących w grze. 
 * @author AleksanderSklorz 
 */
public abstract class AbstractCraneCamera{
    private Node crane, cameraOwner;
    private CameraNode craneCamera; 
    private CameraType type;
    public AbstractCraneCamera(Node crane) {
        this.crane = crane; 
        this.type = CameraType.CABIN;
        craneCamera = new CameraNode("Camera", BuildingSimulator.getCam());
        setPosition();
    }
    
    /**
     * Wyłącza widok z kamery. 
     */
    public void setOff() {
        if(craneCamera != null) {
            craneCamera.removeFromParent();
            craneCamera = null;
        }
    }
    
    /**
     * Przywraca stan wcześniej wyłączonej kamery. 
     */
    public void restore() {
        if(!type.equals(CameraType.LOOSE)) {
            if(craneCamera == null) {
                craneCamera = new CameraNode("Camera", BuildingSimulator.getCam());
                cameraOwner.attachChild(craneCamera);
            }
            setPosition();
        } else {
            setOff();
            setLooseCameraPosition();
        }
    }
    
    /**
     * Zwraca typ kamery. 
     * @return typ kamery 
     */
    public CameraType getType() { return type; }
    
    /**
     * Ustawia typ kamery. 
     * @param type typ kamery 
     */
    public void setType(CameraType type) { this.type = type; }
    
    /**
     * Ustawia pozycję kamery.
     */
    protected final void setPosition() {
        Spatial start = crane.getChild(type.getStart());
        craneCamera.setLocalTranslation(start.getLocalTranslation());
        craneCamera.setLocalRotation(start.getLocalRotation());
    }
    
    /**
     * Przyczepia kamerę do określonego węzła. 
     * @param newOwner węzeł będący właścicielem kamery. 
     */
    protected void switchCameraOwner(Node newOwner) {
        if(!newOwner.equals(cameraOwner)) {
            if(craneCamera != null) {
                craneCamera.removeFromParent();
                newOwner.attachChild(craneCamera);
            }
            cameraOwner = newOwner; 
        }
    }
    
    /**
     * Wyłącza luźną kamerę. 
     */
    protected void setOffLoose() {
        craneCamera = new CameraNode("Camera", BuildingSimulator.getCam());
        cameraOwner.attachChild(craneCamera);
    }
    
    protected void setLooseCameraPosition() {
        Spatial behind = crane.getChild(CameraType.BEHIND_ARM.getStart());
        Camera cam = BuildingSimulator.getCam();
        cam.setLocation(behind.getLocalTranslation());
        cam.setRotation(behind.getLocalRotation());
    }
    
    /**
     * Zwraca dźwig do którego należy kamera. 
     * @return dźwig 
     */
    protected Node getCrane() { return crane; }
}
