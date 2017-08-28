package menu;

import buildingsimulator.BuildingSimulator;
import com.jme3.app.state.AbstractAppState;
import tonegod.gui.controls.windows.Window;
import tonegod.gui.core.Screen;

public class Options extends AbstractAppState  {
    private static Window optionsWindow;
    private static Screen screen = new Screen(BuildingSimulator.getBuildingSimulator());
    private static Options options;
    private Options(){
        screen.parseLayout("Interface/options.gui.xml", this);
        optionsWindow = (Window)screen.getElementById("options");
        optionsWindow.getDragBar().setIsMovable(false);
    }
    
    public static void showOptions(){
        if(options == null) options = new Options();
        BuildingSimulator.getBuildingSimulator().getGuiNode().addControl(screen);
    }
}
