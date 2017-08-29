package menu;

import buildingsimulator.BuildingSimulator;
import com.jme3.app.state.AbstractAppState;
import com.jme3.input.event.MouseButtonEvent;
import com.jme3.system.AppSettings;
import java.awt.DisplayMode;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import tonegod.gui.controls.buttons.Button;
import tonegod.gui.controls.buttons.CheckBox;
import tonegod.gui.controls.buttons.RadioButton;
import tonegod.gui.controls.buttons.RadioButtonGroup;
import tonegod.gui.controls.lists.SelectBox;
import tonegod.gui.controls.windows.Window;
import tonegod.gui.core.Screen;

public class Options extends AbstractAppState  {
    private static Window optionsWindow;
    private static Screen screen = new Screen(BuildingSimulator.getBuildingSimulator());
    private static Options options;
    private static RadioButtonGroup colorDepthButtonsGroup;
    private Options(){
        screen.parseLayout("Interface/options.gui.xml", this);
        optionsWindow = (Window)screen.getElementById("options");
        optionsWindow.getDragBar().setIsMovable(false);
        fillResolutionsSelectBox();
        fillRefreshRateSelectBox();
        addColorDepthButtonsToGroup(); 
    }
    
    public static void showOptions(){
        if(options == null) options = new Options();
        BuildingSimulator.getBuildingSimulator().getGuiNode().addControl(screen);
    }
    
    public void accept(MouseButtonEvent evt, boolean isToggled) {
        AppSettings settings = new AppSettings(true);
        String[] selectedResolution = ((String)((SelectBox)screen
                .getElementById("screen_resolution_select_box")).getSelectedListItem()
                .getValue()).split("x");
        settings.setResolution(Integer.parseInt(selectedResolution[0]),
                Integer.parseInt(selectedResolution[1]));
        settings.setFrequency((int)((SelectBox)screen
                .getElementById("refresh_rate_select_box")).getSelectedListItem()
                .getValue());
        settings.setSamples(Integer.parseInt((String)((SelectBox)screen
                .getElementById("antialiasing_select_box")).getSelectedListItem()
                .getValue()));
        settings.setFullscreen(((CheckBox)screen.getElementById("fullscreen_checkbox"))
                .getIsChecked());
        settings.setBitsPerPixel(Integer.parseInt(colorDepthButtonsGroup
                .getSelected().getText().substring(0, 2)));
        BuildingSimulator game = BuildingSimulator.getBuildingSimulator(); 
        game.setSettings(settings);
        game.restart();
    }
    
    private void fillResolutionsSelectBox() {
         GraphicsDevice device = GraphicsEnvironment.getLocalGraphicsEnvironment()
                 .getDefaultScreenDevice();
         DisplayMode[] modes = device.getDisplayModes();
         SelectBox screenResolutions = (SelectBox)screen.getElementById("screen_resolution_select_box");
         for(int i = 0; i < modes.length; i++){
             String resolution = modes[i].getWidth() + "x" + modes[i].getHeight();
             screenResolutions.addListItem(resolution, resolution);
         }
    }
    
    private void fillRefreshRateSelectBox() {
        GraphicsDevice device = GraphicsEnvironment.getLocalGraphicsEnvironment()
                .getDefaultScreenDevice();
        DisplayMode[] modes = device.getDisplayModes();
        SelectBox refreshRates = (SelectBox)screen.getElementById("refresh_rate_select_box");
        for(int i = 0; i < modes.length; i++){
            refreshRates.addListItem(modes[i].getRefreshRate() + "Hz", modes[i].getRefreshRate());
        }
    }
    
    private void addColorDepthButtonsToGroup() {
        colorDepthButtonsGroup = new RadioButtonGroup(screen,
                "color_depth_group") {
            @Override
            public void onSelect(int index, Button value) {}
        };
        colorDepthButtonsGroup.addButton((RadioButton)screen
                .getElementById("color_depth_16"));
        colorDepthButtonsGroup.addButton((RadioButton)screen
                .getElementById("color_depth_24"));
        colorDepthButtonsGroup.addButton((RadioButton)screen
                .getElementById("color_depth_32"));
    }
    
//    private void fillAntialiasingSelectBox(){
//        GraphicsDevice device = GraphicsEnvironment.getLocalGraphicsEnvironment()
//                .getDefaultScreenDevice();
//        GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().
//        DisplayMode[] modes = device.getDisplayModes();
//        SelectBox refreshRates = (SelectBox)screen.getElementById("antialiasing_select_box");
//        refreshRates.addListItem("Disabled", 0);
//        for(int i = 1; i < modes.length; i++){
//            refreshRates.addListItem(modes[i].getRefreshRate() + "x", modes[i].getRefreshRate());
//        }
//    }
}
