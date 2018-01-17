package cranes.crane;

import buildingsimulator.GameManager;
import com.jme3.export.InputCapsule;
import com.jme3.export.JmeExporter;
import com.jme3.export.JmeImporter;
import com.jme3.export.OutputCapsule;
import cranes.AbstractCraneState;
import java.io.IOException;

/**
 * Klasa <code>CraneState</code> umożliwia zapisanie aktualnego stanu żurawia.
 * @author AleksanderSklorz
 */
public class CraneState extends AbstractCraneState{
    private float minHandleHookDisplacement;
    private int heightLevel;
    
    @Override
    public void write(JmeExporter ex) throws IOException {
        Crane crane = GameManager.getCrane();
        setCrane(crane);
        super.write(ex);
        OutputCapsule capsule = ex.getCapsule(this);
        capsule.write(crane.getHeightLevel(), "HEIGHT", 0);
        capsule.write(crane.getArmControl().getMinHandleHookDisplacement(), "MIN_HANDLE_HOOK_DISPLACEMENT", 0f);
    }
    
    @Override
    public void read(JmeImporter im) throws IOException {
        super.read(im);
        InputCapsule capsule = im.getCapsule(this);
        heightLevel = capsule.readInt("HEIGHT", 0);
        minHandleHookDisplacement = capsule.readFloat("MIN_HANDLE_HOOK_DISPLACEMENT", 0f);
    }
    
    /**
     * Zwraca minimalną odległość na jaką można przesunąć uchwyt haka. 
     * @return minimalna odległość przesunięcia uchwytu haka 
     */
    public float getMinHandleHookDisplacement() { return minHandleHookDisplacement; }
    
    /**
     * Zwraca poziom wysokości żurawia. 
     * @return poziom wysokości 
     */
    public int getHeightLevel() { return heightLevel; }
}
