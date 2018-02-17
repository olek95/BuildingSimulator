package cranes;

import buildingsimulator.GameManager;
import com.jme3.scene.CameraNode;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;

public abstract class AbstractCraneCamera {
    private Node crane;
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
    
    public abstract void restore();
    
    protected void setPosition() {
        Spatial start = crane.getChild(type.getStart());
        craneCamera.setLocalTranslation(start.getLocalTranslation());
        craneCamera.setLocalRotation(start.getLocalRotation());
    }
    
    protected Node getCrane() { return crane; }
    
    protected CameraType getType() { return type; }
    
    protected void setType(CameraType type) { this.type = type; }
    
    protected CameraNode getCraneCamera() { return craneCamera; }
    
    protected void setCraneCamera(CameraNode craneCamera) {
        this.craneCamera = craneCamera; 
    }
}
