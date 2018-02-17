package cranes;

import buildingsimulator.GameManager;
import com.jme3.scene.CameraNode;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;

/**
 * Klasa <code>AbstractCraneCamera</code> reprezentuje klasę abstrakcyjną 
 * kamer wszystkich rodzai dźwigów występujących w grze. 
 * @author AleksanderSklorz 
 */
public abstract class AbstractCraneCamera {
    private Node crane, cameraOwner;
    private CameraNode craneCamera; 
    private CameraType type;
    public AbstractCraneCamera(Node crane) {
        this.crane = crane; 
        this.type = CameraType.CABIN;
        craneCamera = new CameraNode("Camera", GameManager.getCamera());
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
            craneCamera = new CameraNode("Camera", GameManager.getCamera());
            cameraOwner.attachChild(craneCamera);
            setPosition();
        }
    }
    
    /**
     * Ustawia pozycję kamery.
     */
    protected void setPosition() {
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
            System.out.println(craneCamera);
            craneCamera.removeFromParent();
            System.out.println(newOwner);
            newOwner.attachChild(craneCamera);
            cameraOwner = newOwner; 
        }
    }
    
    /**
     * Wyłącza luźną kamerę. 
     */
    protected void setOffLoose() {
        craneCamera = new CameraNode("Camera", GameManager.getCamera());
        cameraOwner.attachChild(craneCamera);
    }
    
    /**
     * Zwraca dźwig do którego należy kamera. 
     * @return dźwig 
     */
    protected Node getCrane() { return crane; }
    
    /**
     * Zwraca typ kamery. 
     * @return typ kamery 
     */
    protected CameraType getType() { return type; }
    
    /**
     * Ustawia typ kamery. 
     * @param type typ kamery 
     */
    protected void setType(CameraType type) { this.type = type; }
}
