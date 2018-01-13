package cranes.mobileCrane;

import building.Wall;
import building.WallMode;
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
    private Vector3f hookDisplacement, hookLocalTranslation, hookScale, hookHandleDisplacement; 
    private float hookActualLowering, armStretchingOut; 
    public MobileCraneState() {
        MobileCrane crane = GameManager.getMobileCrane(); 
        if(crane != null) {
            ArmControl arm = crane.getArmControl();
            hookHandleDisplacement = ((MobileCraneArmControl)arm).getHookHandleDisplacement();
            armStretchingOut = ((MobileCraneArmControl)arm).getStrechingOut(); 
            Hook hook = arm.getHook();
            craneNode = crane.getCrane();
            attachedObjectToHook = hook.getAttachedObject();
            hookDisplacement = hook.getHookDisplacement();
            hookActualLowering = hook.getActualLowering();
            //hookLocalTranslation = hook.getRopes()[0].getLocalScale();
            hookScale = hook.getHook().getLocalScale();
        }
    }
    
    @Override
    public void write(JmeExporter ex) throws IOException {
        OutputCapsule capsule = ex.getCapsule(this);
        capsule.write(craneNode, "CRANE", null);
        capsule.write(hookHandleDisplacement, "HOOK_HANDLE_DISPLACEMENT", null);
        capsule.write(armStretchingOut, "ARM_STRETCHING_OUT", 0f);
        capsule.write(attachedObjectToHook, "ATTACHED_OBJECT_TO_HOOK", null);
        capsule.write(hookDisplacement, "HOOK_DISPLACEMENT", null);
        //capsule.write(hookLocalTranslation, "HOOK_LOCAL_TRANSLATION", null);
        capsule.write(hookActualLowering, "HOOK_ACTUAL_LOWERING", 0f);
        capsule.write(hookScale, "HOOK_SCALE", null);
    }
    
    @Override
    public void read(JmeImporter im) throws IOException {
        InputCapsule capsule = im.getCapsule(this);
        craneNode = (Node)capsule.readSavable("CRANE", null);
        hookHandleDisplacement = (Vector3f)capsule.readSavable("HOOK_HANDLE_DISPLACEMENT", null);
        armStretchingOut = capsule.readFloat("ARM_STRETCHING_OUT", 0f);
        attachedObjectToHook = (Wall)capsule.readSavable("ATTACHED_OBJECT_TO_HOOK", null);
        hookDisplacement = (Vector3f)capsule.readSavable("HOOK_DISPLACEMENT", null);
        //hookLocalTranslation = (Vector3f)capsule.readSavable("HOOK_LOCAL_TRANSLATION", null);
        hookActualLowering = capsule.readFloat("HOOK_ACTUAL_LOWERING", 0f);
        hookScale = (Vector3f)capsule.readSavable("HOOK_SCALE", null);
    }
    
    public Wall getAttachedObjectToHook() { 
        return attachedObjectToHook; 
    }
    
    public Node getCraneNode() { return craneNode; }
    
    public Vector3f getHookDisplacement() { return hookDisplacement; }
    
    public Vector3f getHookLocalTranslation() { return hookLocalTranslation; }
    
    public float getHookActualLowering() { return hookActualLowering; }
    
    public Vector3f getHookScale() { return hookScale; }
    
    public Vector3f getHookHandleDisplacement() { return hookHandleDisplacement; }
    
    public float getArmStretchingOut() { return armStretchingOut; }
}
