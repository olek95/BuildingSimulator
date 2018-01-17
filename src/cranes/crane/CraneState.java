package cranes.crane;

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

public class CraneState implements Savable{
    private Node craneNode; 
    private Wall attachedObjectToHook;
    private Vector3f hookDisplacement;
    private float hookActualLowering, minHandleHookDisplacement;
    private int heightLevel;
    public CraneState() {
        Crane crane = GameManager.getCrane();
        if(crane != null) {
            craneNode = crane.getCrane();
            heightLevel = crane.getHeightLevel();
            ArmControl arm = crane.getArmControl();
            minHandleHookDisplacement = arm.getMinHandleHookDisplacement();
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
        capsule.write(heightLevel, "HEIGHT", 0);
        capsule.write(minHandleHookDisplacement, "MIN_HANDLE_HOOK_DISPLACEMENT", 0f);
        capsule.write(attachedObjectToHook, "ATTACHED_OBJECT_TO_HOOK", null);
        capsule.write(hookDisplacement, "HOOK_DISPLACEMENT", null);
        capsule.write(hookActualLowering, "HOOK_ACTUAL_LOWERING", 0f);
    }
    
    @Override
    public void read(JmeImporter im) throws IOException {
        InputCapsule capsule = im.getCapsule(this);
        craneNode = (Node)capsule.readSavable("CRANE", null);
        heightLevel = capsule.readInt("HEIGHT", 0);
        minHandleHookDisplacement = capsule.readFloat("MIN_HANDLE_HOOK_DISPLACEMENT", 0f);
        attachedObjectToHook = (Wall)capsule.readSavable("ATTACHED_OBJECT_TO_HOOK", null);
        hookDisplacement = (Vector3f)capsule.readSavable("HOOK_DISPLACEMENT", null);
        hookActualLowering = capsule.readFloat("HOOK_ACTUAL_LOWERING", 0f);
    }
    
    public Node getCraneNode() { return craneNode; }
    
    public Vector3f getHookDisplacement() { return hookDisplacement; }
    
    public float getHookActualLowering() { return hookActualLowering; }
    
    public float getMinHandleHookDisplacement() { return minHandleHookDisplacement; }
    
    public Wall getAttachedObjectToHook() { return attachedObjectToHook; }
    
    public int getHeightLevel() { return heightLevel; }
}
