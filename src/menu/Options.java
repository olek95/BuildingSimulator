package menu;

import buildingsimulator.BuildingSimulator;
import com.jme3.app.state.AbstractAppState;
import java.awt.DisplayMode;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import tonegod.gui.controls.lists.SelectBox;
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
        fillResolutionsSelectBox();
        fillRefreshRateSelectBox();
    }
    
    public static void showOptions(){
        if(options == null) options = new Options();
        BuildingSimulator.getBuildingSimulator().getGuiNode().addControl(screen);
    }
    
    private void fillResolutionsSelectBox() {
         GraphicsDevice device = GraphicsEnvironment.getLocalGraphicsEnvironment()
                 .getDefaultScreenDevice();
         DisplayMode[] modes = device.getDisplayModes();
         SelectBox screenResolutions = (SelectBox)screen.getElementById("screen_resolution_select_box");
         for(int i = 0; i < modes.length; i++){
             String resolution = modes[i].getHeight() + "x" + modes[i].getWidth();
             screenResolutions.addListItem(resolution, resolution);
         }
    }
    
    private void fillRefreshRateSelectBox() {
        GraphicsDevice device = GraphicsEnvironment.getLocalGraphicsEnvironment()
                .getDefaultScreenDevice();
        DisplayMode[] modes = device.getDisplayModes();
        SelectBox refreshRates = (SelectBox)screen.getElementById("refresh_rate_select_box");
        for(int i = 0; i < modes.length; i++){
            refreshRates.addListItem(modes[i].getRefreshRate() + "", modes[i].getRefreshRate());
        }
   }
}
