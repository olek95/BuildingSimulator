package cranes;

/**
 * Klasa abstrakcyjna <code>CraneAbstract</code>jest klasą abstrakcyjną dla 
 * wszystkich rodzai dźwigów. 
 * @author AleksanderSklorz
 */
public abstract class CraneAbstract {
    private ArmControl armControl;
    private boolean using; 
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
    };
    
    /**
     * Określa czy dany dźwig jest aktualnie używany. 
     * @param using true jeśli dźwig jest używany, false w przeciwnym razie 
     */
    public void setUsing(boolean using){
        this.using = using;
    };
}
