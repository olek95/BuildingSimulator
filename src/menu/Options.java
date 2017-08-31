package menu;

import buildingsimulator.BuildingSimulator;
import com.jme3.app.state.AbstractAppState;
import com.jme3.input.event.MouseButtonEvent;
import com.jme3.scene.Node;
import com.jme3.system.AppSettings;
import java.awt.DisplayMode;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.util.ArrayList;
import java.util.Locale;
import java.util.ResourceBundle;
import texts.Labels;
import tonegod.gui.controls.buttons.Button;
import tonegod.gui.controls.buttons.CheckBox;
import tonegod.gui.controls.buttons.RadioButton;
import tonegod.gui.controls.buttons.RadioButtonGroup;
import tonegod.gui.controls.lists.SelectBox;
import tonegod.gui.controls.windows.Window;
import tonegod.gui.core.Screen;

public class Options extends Menu  {
    private static Screen screen;
    public Options(){
        screen = new Screen(BuildingSimulator.getBuildingSimulator());
        screen.parseLayout("Interface/options.gui.xml", this);
        window = (Window)screen.getElementById("options");
        window.getDragBar().setIsMovable(false);
        fillResolutionsSelectBox();
        translate(new Locale("pl"));
        fillSelectBoxSingleValue("refresh_rate_select_box");
        fillSelectBoxSingleValue("color_depth_select_box");
        fillLanguageSelectBox();
        fillAntialiasingSelectBox();
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
        settings.setSamples((int)((SelectBox)screen
                .getElementById("antialiasing_select_box")).getSelectedListItem()
                .getValue());
        settings.setFrequency((int)((SelectBox)screen.getElementById("color_depth_select_box"))
                .getSelectedListItem().getValue());
        settings.setFullscreen(((CheckBox)screen.getElementById("fullscreen_checkbox"))
                .getIsChecked());
        translate((Locale)((SelectBox)screen.getElementById("language_select_box"))
                .getSelectedListItem().getValue());
        BuildingSimulator game = BuildingSimulator.getBuildingSimulator(); 
        game.setSettings(settings);
        game.restart();
        refresh(); 
    }
    
    public void back(MouseButtonEvent evt, boolean isToggled) {
        window.hide();
        BuildingSimulator.getBuildingSimulator().getGuiNode().removeControl(screen);
        MenuFactory.showMenu(MenuTypes.MAIN_MENU);
    }
    
    private void fillResolutionsSelectBox() {
         DisplayMode[] modes = GraphicsEnvironment.getLocalGraphicsEnvironment()
                .getDefaultScreenDevice().getDisplayModes();
         ArrayList<String> elements = new ArrayList(); 
         SelectBox screenResolutions = (SelectBox)screen.getElementById("screen_resolution_select_box");
         for(int i = 0; i < modes.length; i++){
             String resolution = modes[i].getWidth() + "x" + modes[i].getHeight();
             if(!elements.contains(resolution))
                screenResolutions.addListItem(resolution, resolution);
             elements.add(resolution);
         }
    }
    
    private void fillSelectBoxSingleValue(String selectBoxId) {
        DisplayMode[] modes = GraphicsEnvironment.getLocalGraphicsEnvironment()
                .getDefaultScreenDevice().getDisplayModes();
        ArrayList<Integer> elements = new ArrayList(); 
        SelectBox colorDepths = (SelectBox)screen.getElementById(selectBoxId);
        String suffix;
        int value; 
        for(int i = 0; i < modes.length; i++){
            if(selectBoxId.equals("refresh_rate_select_box")){
                suffix = "Hz";
                value = modes[i].getRefreshRate();
            }else{
                suffix = " bpp";
                value = modes[i].getBitDepth();
            }
            if(!elements.contains(value)){
                colorDepths.addListItem(value + suffix, value);
            }
            elements.add(value);
        }
    }
    
    private void fillLanguageSelectBox(){
        SelectBox languages = (SelectBox)screen.getElementById("language_select_box");
        languages.addListItem(Labels.ENGLISH.getValue(), Locale.ENGLISH);
        languages.addListItem(Labels.POLISH.getValue(), new Locale("pl"));
    }
    
    private void fillAntialiasingSelectBox() { 
        int[] values = {0, 2, 4, 6, 8, 16};
        SelectBox antialiasingSelectBox = (SelectBox)screen
                .getElementById("antialiasing_select_box");
        antialiasingSelectBox.addListItem(Labels.DISABLED_ANTIALIASING.getValue(),
                values[0]);
        for(int i = 1; i < values.length; i++)
            antialiasingSelectBox.addListItem(values[i] + "x", values[i]);
    }
    
    private void translate(Locale locale){
        ResourceBundle bundle = ResourceBundle.getBundle("texts.options", locale);
        Labels[] labels = Labels.values();
        for(int i = 0; i < labels.length; i++){
            labels[i].setValue(bundle.getString(labels[i].toString()));
        }
        setTexts();
    }
    
    private void setTexts() {
        screen.getElementById("settings_label").setText(Labels.SETTINGS.getValue());
        screen.getElementById("language_label").setText(Labels.LANGUAGE.getValue());
        screen.getElementById("graphics_label").setText(Labels.GRAPHICS.getValue());
        screen.getElementById("screen_resolution_label").setText(Labels.SCREEN_RESOLUTION
                .getValue());
        screen.getElementById("color_depth_label").setText(Labels.COLOR_DEPTH
                .getValue());
        screen.getElementById("antialiasing_label").setText(Labels.ANTIALIASING
                .getValue());
        screen.getElementById("fullscreen_label").setText(Labels.FULLSCREEN
                .getValue());
        screen.getElementById("refresh_rate_label").setText(Labels.REFRESH_RATE
                .getValue());
        screen.getElementById("accepting_button").setText(Labels.ACCEPTING.getValue());
        screen.getElementById("return_button").setText(Labels.RETURN.getValue());
    }
    
    private void refresh(){
        window.hide();
        BuildingSimulator.getBuildingSimulator().getGuiNode().removeControl(screen);
        MenuFactory.showMenu(MenuTypes.OPTIONS);
        System.out.println(123);
    }
}
