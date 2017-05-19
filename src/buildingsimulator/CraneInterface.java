package buildingsimulator;

public interface CraneInterface {
    public Hook getHook();
    
    public Cabin getCabin();
    
    public boolean isUsing();
    
    public void setUsing(boolean using);
}
