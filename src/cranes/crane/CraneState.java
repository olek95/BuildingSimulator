package cranes.crane;

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

public class CraneState implements Savable{
    private Node craneNode; 
    private Vector3f hookDisplacement;
    private float hookActualLowering, minHandleHookDisplacement;
    public CraneState() {
        Crane crane = GameManager.getCrane();
        if(crane != null) {
            craneNode = crane.getCrane();
            ArmControl arm = crane.getArmControl();
            minHandleHookDisplacement = arm.getMinHandleHookDisplacement();
            Hook hook = arm.getHook();
            hookDisplacement = hook.getHookDisplacement();
            hookActualLowering = hook.getActualLowering();
        }
    }
    
    @Override
    public void write(JmeExporter ex) throws IOException {
        OutputCapsule capsule = ex.getCapsule(this);
        capsule.write(craneNode, "CRANE", null);
        capsule.write(minHandleHookDisplacement, "MIN_HANDLE_HOOK_DISPLACEMENT", 0f);
        capsule.write(hookDisplacement, "HOOK_DISPLACEMENT", null);
        capsule.write(hookActualLowering, "HOOK_ACTUAL_LOWERING", 0f);
    }
    
    @Override
    public void read(JmeImporter im) throws IOException {
        InputCapsule capsule = im.getCapsule(this);
        craneNode = (Node)capsule.readSavable("CRANE", null);
        minHandleHookDisplacement = capsule.readFloat("MIN_HANDLE_HOOK_DISPLACEMENT", 0f);
        hookDisplacement = (Vector3f)capsule.readSavable("HOOK_DISPLACEMENT", null);
        hookActualLowering = capsule.readFloat("HOOK_ACTUAL_LOWERING", 0f);
    }
    
    public Node getCraneNode() { return craneNode; }
    
    public Vector3f getHookDisplacement() { return hookDisplacement; }
    
    public float getHookActualLowering() { return hookActualLowering; }
    
    public float getMinHandleHookDisplacement() { return minHandleHookDisplacement; }
}
