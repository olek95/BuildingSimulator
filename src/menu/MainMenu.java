package menu;

import buildingsimulator.BuildingSimulator;
import com.jme3.app.Application;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import tonegod.gui.controls.windows.Window;
import tonegod.gui.core.Screen;

public class MainMenu extends AbstractAppState {
    private static Window inventory;
    private static Screen screen = new Screen(BuildingSimulator.getBuildingSimulator());
    private static MainMenu menu;
    private MainMenu(){
        screen.parseLayout("Interface/main_menu.gui.xml", this);
        inventory = (Window)screen.getElementById("InventoryWindowID");
        //inventory.hide();
        BuildingSimulator.getBuildingSimulator().getGuiNode().addControl(screen);
    }
    public static void showMenu(){
        if(menu == null) menu = new MainMenu(); 
    }
    
//    @Override
//    public void initialize(AppStateManager stateManager, Application app) {
//        super.initialize(stateManager, app);
//
//        // Useful place for running load effects
//        inventory.showWithEffect();
//    }
}
