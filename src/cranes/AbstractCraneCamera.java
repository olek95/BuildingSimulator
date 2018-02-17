package cranes;

import buildingsimulator.GameManager;
import com.jme3.scene.CameraNode;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;

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
    
    public void setOff() {
        if(craneCamera != null) {
            craneCamera.removeFromParent();
            craneCamera = null;
        }
    }
    
    public void restore() {
        craneCamera = new CameraNode("Camera", GameManager.getCamera());
        System.out.println(cameraOwner);
        cameraOwner.attachChild(craneCamera);
        setPosition();
    }
    
    protected void setPosition() {
        Spatial start = crane.getChild(type.getStart());
        craneCamera.setLocalTranslation(start.getLocalTranslation());
        craneCamera.setLocalRotation(start.getLocalRotation());
    }
    
    protected void switchCameraOwner(Node newOwner) {
        if(!newOwner.equals(cameraOwner)) {
            System.out.println(craneCamera);
            craneCamera.removeFromParent();
            System.out.println(newOwner);
            newOwner.attachChild(craneCamera);
            cameraOwner = newOwner; 
        }
    }
    
    protected Node getCrane() { return crane; }
    
    protected CameraType getType() { return type; }
    
    protected void setType(CameraType type) { this.type = type; }
    
    protected CameraNode getCraneCamera() { return craneCamera; }
    
    protected void setCraneCamera(CameraNode craneCamera) {
        this.craneCamera = craneCamera; 
    }
    
    protected Node getCameraOwner() { return cameraOwner; }
    
    protected void setCameraOwner(Node cameraOwner) { this.cameraOwner = cameraOwner; }
}
