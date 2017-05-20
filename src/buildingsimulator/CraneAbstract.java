package buildingsimulator;

public abstract class CraneAbstract {
    private ArmControl armControl;
    private boolean using;
    public Hook getHook(){
        return armControl.getHook();
    }
    
    public ArmControl getArmControl(){
        return armControl;
    }
    
    public void setArmControl(ArmControl control){
        armControl = control;
    }
    
    public boolean isUsing(){
        return using;
    };
    
    public void setUsing(boolean using){
        this.using = using;
    };
}
