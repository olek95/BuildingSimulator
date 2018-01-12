package cranes.mobileCrane;

import building.Wall;
import buildingsimulator.GameManager;
import com.jme3.export.InputCapsule;
import com.jme3.export.JmeExporter;
import com.jme3.export.JmeImporter;
import com.jme3.export.OutputCapsule;
import com.jme3.export.Savable;
import com.jme3.scene.Node;
import java.io.IOException;

public class MobileCraneState implements Savable{
    private Node craneNode;
    private Wall attachedObjectToHook;
    public MobileCraneState() {
        System.out.println(13);
        MobileCrane crane = GameManager.getMobileCrane(); 
        if(crane != null) {
            craneNode = crane.getCrane();
            attachedObjectToHook = crane.getArmControl().getHook().getAttachedObject();
        }
    }
    
    @Override
    public void write(JmeExporter ex) throws IOException {
        OutputCapsule capsule = ex.getCapsule(this);
        capsule.write(craneNode, "CRANE", null);
        capsule.write(attachedObjectToHook, "ATTACHED_OBJECT_TO_HOOK", null);
    }
    
    @Override
    public void read(JmeImporter im) throws IOException {
        System.out.println(1111);
        InputCapsule capsule = im.getCapsule(this);
        craneNode = (Node)capsule.readSavable("CRANE", null);
        attachedObjectToHook = (Wall)capsule.readSavable("ATTACHED_OBJECT_TO_HOOK", null);
    }
    
    public Wall getAttachedObjectToHook() { return attachedObjectToHook; }
    
    public Node getCraneNode() { return craneNode; }
}
