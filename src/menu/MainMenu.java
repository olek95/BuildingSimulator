package menu;

import buildingsimulator.BuildingSimulator;
import com.jme3.app.Application;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.input.event.MouseButtonEvent;
import tonegod.gui.controls.windows.Window;
import tonegod.gui.core.Screen;

public class MainMenu extends AbstractAppState {
    private static Window mainMenu;
    private static Screen screen = new Screen(BuildingSimulator.getBuildingSimulator());
    private static MainMenu menu;
    private MainMenu(){
        screen.parseLayout("Interface/main_menu.gui.xml", this);
        //mainMenu = (Window)screen.getElementById("main_menu");
        //inventory.hide();
        screen.setUseCustomCursors(true);
    }
    
    public static void showMenu(){
        if(menu == null) menu = new MainMenu(); 
        BuildingSimulator.getBuildingSimulator().getGuiNode().addControl(screen);
    }
    
    public void startGame(MouseButtonEvent evt, boolean isToggled) {
        
    }
    
//    @Override
//    public void initialize(AppStateManager stateManager, Application app) {
//        super.initialize(stateManager, app);
//
//        // Useful place for running load effects
//        inventory.showWithEffect();
//    }
}
