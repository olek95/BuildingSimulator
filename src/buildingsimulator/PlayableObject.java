package buildingsimulator;

public abstract class PlayableObject {
    private String[] availableActions; 
    protected String[] getAvailableActions(){
        return availableActions;
    }
    protected void setAvailableActions(String[] actions){
        availableActions = actions;
    }
}
