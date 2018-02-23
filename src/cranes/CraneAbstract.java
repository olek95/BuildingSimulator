package cranes;

import com.jme3.scene.Node;
import cranes.mobileCrane.MobileCraneCamera;

/**
 * Klasa abstrakcyjna <code>CraneAbstract</code>jest klasą abstrakcyjną dla 
 * wszystkich rodzai dźwigów. 
 * @author AleksanderSklorz
 */
public abstract class CraneAbstract {
    private Node crane;
    private ArmControl armControl;
    private boolean using; 
    private AbstractCraneCamera camera; 
    /**
     * Zwraca hak dźwigu. 
     * @return hak 
     */
    public Hook getHook(){
        return armControl.getHook();
    }
    
    /**
     * Zwraca obiekt reprezentujący kontrolę ramienia dźwigu. 
     * @return obiekt kontroli ramienia dźwigu 
     */
    public ArmControl getArmControl(){
        return armControl;
    }
    
    /**
     * Ustawia obiekt reprezentujący kontrolę ramienia dźwigu. 
     * @param control obiekt kontroli ramienia dźwigu 
     */
    public void setArmControl(ArmControl control){
        armControl = control;
    }
    
    /**
     * Zwraca informację czy dany dźwig jest aktualnie używany. 
     * @return true jeśli dźwig jest używany, false w przeciwnym razie 
     */
    public boolean isUsing(){
        return using;
    }
    
    /**
     * Określa czy dany dźwig jest aktualnie używany. Jeśli dźwig jest używany, 
     * to dodatkowo ustawia dla jego haka słuchacza kolizji od dołu. 
     * @param using true jeśli dźwig jest używany, false w przeciwnym razie 
     */
    public void setUsing(boolean using){
        this.using = using;
        Hook hook = armControl.getHook();
        if(using) hook.getCollisionListener().setHittingObject(hook);
    }
    
    /**
     * Zwraca model dźwigu. 
     * @return model dźwigu 
     */
    public Node getCrane() { return crane; }
    
    /**
     * Ustawia model dźwigu. 
     * @param crane model dźwigu 
     */
    public void setCrane(Node crane) { this.crane = crane; }
    
    /**
     * Zwraca kamerę dźwigu. 
     * @return kamera dźwigu 
     */
    public AbstractCraneCamera getCamera() { return camera; }
    
    /**
     * Ustawia kamerę dźwigu. 
     * @param camera kamera dźwigu 
     */
    public void setCamera(AbstractCraneCamera camera) { this.camera = camera; }
    
    public boolean hasLooseCamera() {
        return camera.getType().equals(CameraType.LOOSE);
    }
}
