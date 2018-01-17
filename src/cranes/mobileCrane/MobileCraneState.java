package cranes.mobileCrane;

import buildingsimulator.GameManager;
import com.jme3.export.InputCapsule;
import com.jme3.export.JmeExporter;
import com.jme3.export.JmeImporter;
import com.jme3.export.OutputCapsule;
import com.jme3.math.Vector3f;
import cranes.AbstractCraneState;
import java.io.IOException;

/**
 * Klasa <code>MobileCraneState</code> umożliwia zapisanie aktualnego stanu dźwigu 
 * mobilnego. 
 * @author AleksanderSklorz
 */
public class MobileCraneState extends AbstractCraneState{
    private Vector3f hookHandleDisplacement, propDisplacement; 
    private float armStretchingOut, propsLowering, cranePropsProtrusion,
            yCraneOffset; 
    
    @Override
    public void write(JmeExporter ex) throws IOException {
        MobileCrane crane = GameManager.getMobileCrane(); 
        setCrane(crane);
        super.write(ex);
        OutputCapsule capsule = ex.getCapsule(this);
        capsule.write(crane.getPropDisplacement(), "PROP_DISPLACEMENT", null);
        capsule.write(crane.getPropsLowering(), "PROPS_LOWERING", 0f);
        MobileCraneArmControl arm = (MobileCraneArmControl)crane.getArmControl();
        capsule.write(arm.getHookHandleDisplacement(), "HOOK_HANDLE_DISPLACEMENT", null);
        capsule.write(arm.getStrechingOut(), "ARM_STRETCHING_OUT", 0f);
        capsule.write(arm.getCranePropsProtrusion(), "CRANE_PROPS_PROTRUSION", 0f);
        capsule.write(arm.getYCraneOffset(), "Y_CRANE_OFFSET", 0f); 
    }
    
    @Override
    public void read(JmeImporter im) throws IOException {
        super.read(im);
        InputCapsule capsule = im.getCapsule(this);
        propDisplacement = (Vector3f)capsule.readSavable("PROP_DISPLACEMENT", null);
        propsLowering = capsule.readFloat("PROPS_LOWERING", 0f);
        hookHandleDisplacement = (Vector3f)capsule.readSavable("HOOK_HANDLE_DISPLACEMENT", null);
        armStretchingOut = capsule.readFloat("ARM_STRETCHING_OUT", 0f);
        cranePropsProtrusion = capsule.readFloat("CRANE_PROPS_PROTRUSION", cranePropsProtrusion);
        yCraneOffset = capsule.readFloat("Y_CRANE_OFFSET", yCraneOffset);
    }
    
    /**
     * Zwraca wektor o jaki przesuwa się część uchwytu na hak podczas wysuwania/wsuwania
     * ramienia. 
     * @return wektor przesuwania się części uchwytu na hak
     */
    public Vector3f getHookHandleDisplacement() { return hookHandleDisplacement; }
    
    /**
     * Zwraca aktualne wysunięcie ramienia. 
     * @return aktualne wysunięcie remienia 
     */
    public float getArmStretchingOut() { return armStretchingOut; }
    
    /**
     * Zwraca wetor o jaki przesuwa się podpora.
     * @return wektor o jaki przesuwa się podpora  
     */
    public Vector3f getPropDisplacement() { return propDisplacement; }
    
    /**
     * Zwraca wetor o jaki przesuwa się podpora.
     * @return wektor o jaki przesuwa się podpora  
     */
    public float getPropsLowering() { return propsLowering; }
    
    /**
     * Zwraca wartość wysunięcia podpór ramienia dźwigu. 
     * @return wysunięcie podpór ramienia dźwigu 
     */
    public float getCranePropsProtrusion() { return cranePropsProtrusion; }
    
    /**
     * Zwraca wysokość na jakiej znajduje się ramie dźwigu. 
     * @return wysokość na jakiej znajduje się ramie dźwigu 
     */
    public float getYCraneOffset() { return yCraneOffset; }
}
