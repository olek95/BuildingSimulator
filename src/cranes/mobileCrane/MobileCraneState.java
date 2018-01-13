package cranes.mobileCrane;

import building.Wall;
import buildingsimulator.GameManager;
import com.jme3.export.InputCapsule;
import com.jme3.export.JmeExporter;
import com.jme3.export.JmeImporter;
import com.jme3.export.OutputCapsule;
import com.jme3.export.Savable;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import cranes.ArmControl;
import cranes.Hook;
import java.io.IOException;

public class MobileCraneState implements Savable{
    private Node craneNode;
    private Wall attachedObjectToHook;
    private Vector3f hookDisplacement, hookHandleDisplacement, propDisplacement; 
    private float hookActualLowering, armStretchingOut, propsLowering; 
    public MobileCraneState() {
        MobileCrane crane = GameManager.getMobileCrane(); 
        if(crane != null) {
            craneNode = crane.getCrane();
            propDisplacement = crane.getPropDisplacement();
            propsLowering = crane.getPropsLowering(); 
            ArmControl arm = crane.getArmControl();
            hookHandleDisplacement = ((MobileCraneArmControl)arm).getHookHandleDisplacement();
            armStretchingOut = ((MobileCraneArmControl)arm).getStrechingOut(); 
            Hook hook = arm.getHook();
            attachedObjectToHook = hook.getAttachedObject();
            hookDisplacement = hook.getHookDisplacement();
            hookActualLowering = hook.getActualLowering();
        }
    }
    
    @Override
    public void write(JmeExporter ex) throws IOException {
        OutputCapsule capsule = ex.getCapsule(this);
        capsule.write(craneNode, "CRANE", null);
        capsule.write(propDisplacement, "PROP_DISPLACEMENT", null);
        capsule.write(propsLowering, "PROPS_LOWERING", 0f);
        capsule.write(hookHandleDisplacement, "HOOK_HANDLE_DISPLACEMENT", null);
        capsule.write(armStretchingOut, "ARM_STRETCHING_OUT", 0f);
        capsule.write(attachedObjectToHook, "ATTACHED_OBJECT_TO_HOOK", null);
        capsule.write(hookDisplacement, "HOOK_DISPLACEMENT", null);
        capsule.write(hookActualLowering, "HOOK_ACTUAL_LOWERING", 0f);
    }
    
    @Override
    public void read(JmeImporter im) throws IOException {
        InputCapsule capsule = im.getCapsule(this);
        craneNode = (Node)capsule.readSavable("CRANE", null);
        propDisplacement = (Vector3f)capsule.readSavable("PROP_DISPLACEMENT", null);
        propsLowering = capsule.readFloat("PROPS_LOWERING", 0f);
        hookHandleDisplacement = (Vector3f)capsule.readSavable("HOOK_HANDLE_DISPLACEMENT", null);
        armStretchingOut = capsule.readFloat("ARM_STRETCHING_OUT", 0f);
        attachedObjectToHook = (Wall)capsule.readSavable("ATTACHED_OBJECT_TO_HOOK", null);
        hookDisplacement = (Vector3f)capsule.readSavable("HOOK_DISPLACEMENT", null);
        hookActualLowering = capsule.readFloat("HOOK_ACTUAL_LOWERING", 0f);
    }
    
    public Wall getAttachedObjectToHook() { 
        return attachedObjectToHook; 
    }
    
    public Node getCraneNode() { return craneNode; }
    
    public Vector3f getHookDisplacement() { return hookDisplacement; }
    
    public float getHookActualLowering() { return hookActualLowering; }
    
    public Vector3f getHookHandleDisplacement() { return hookHandleDisplacement; }
    
    public float getArmStretchingOut() { return armStretchingOut; }
    
    public Vector3f getPropDisplacement() { return propDisplacement; }
    
    public float getPropsLowering() { return propsLowering; }
}
