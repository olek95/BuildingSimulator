package cranes;

import building.Wall;
import com.jme3.export.InputCapsule;
import com.jme3.export.JmeExporter;
import com.jme3.export.JmeImporter;
import com.jme3.export.OutputCapsule;
import com.jme3.export.Savable;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import java.io.IOException;

/**
 * Klasa <code>AbstractCraneState</code> umożliwia zapisanie ogólnych (wspólnych)
 * danych o dźwigach. 
 * @author AleksanderSklorz
 */
public class AbstractCraneState implements Savable{
    private Node craneNode; 
    private Wall attachedObjectToHook;
    private Vector3f hookDisplacement;
    private float hookActualLowering;
    private CraneAbstract crane; 
    @Override
    public void write(JmeExporter ex) throws IOException {
        OutputCapsule capsule = ex.getCapsule(this);
        capsule.write(crane.getCrane(), "CRANE", null);
        ArmControl arm = crane.getArmControl();
        Hook hook = arm.getHook();
        capsule.write(hook.getAttachedObject(), "ATTACHED_OBJECT_TO_HOOK", null);
        capsule.write(hook.getHookDisplacement(), "HOOK_DISPLACEMENT", null);
        capsule.write(hook.getActualLowering(), "HOOK_ACTUAL_LOWERING", 0f);
    }
    
    @Override
    public void read(JmeImporter im) throws IOException {
        InputCapsule capsule = im.getCapsule(this);
        craneNode = (Node)capsule.readSavable("CRANE", null);
        attachedObjectToHook = (Wall)capsule.readSavable("ATTACHED_OBJECT_TO_HOOK", null);
        hookDisplacement = (Vector3f)capsule.readSavable("HOOK_DISPLACEMENT", null);
        hookActualLowering = capsule.readFloat("HOOK_ACTUAL_LOWERING", 0f);
    }
    
    /**
     * Zwraca model dźwigu. 
     * @return model dźwigu 
     */
    public Node getCraneNode() { return craneNode; }
    
    /**
     * Zwraca przesunięcie haka po opuszczeniu liny. 
     * @return przesunięcie haka 
     */
    public Vector3f getHookDisplacement() { return hookDisplacement; }
    
    /**
     * Zwraca wartość określającą jak bardzo opuszczony jest hak. 
     * @return wartość opuszczenia haka 
     */
    public float getHookActualLowering() { return hookActualLowering; }
    
    /**
     * Zwraca zaczepiony obiekt. 
     * @return zaczepiony obiekt 
     */
    public Wall getAttachedObjectToHook() { return attachedObjectToHook; }
    
    /**
     * Określa którego dźwigu są to dane. 
     * @param crane dźwig będący właścicielem danych 
     */
    public void setCrane(CraneAbstract crane) { this.crane = crane; }
}
