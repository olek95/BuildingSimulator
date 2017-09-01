package menu;

import buildingsimulator.BuildingSimulator;
import com.jme3.input.event.MouseButtonEvent;
import com.jme3.system.AppSettings;
import java.awt.DisplayMode;
import java.awt.GraphicsEnvironment;
import java.util.ArrayList;
import java.util.Locale;
import java.util.ResourceBundle;
import texts.Labels;
import tonegod.gui.controls.buttons.CheckBox;
import tonegod.gui.controls.lists.SelectBox;
import tonegod.gui.controls.windows.Window;
import tonegod.gui.core.Screen;

/**
 * Klasa <code>Options</code> reprezentuje menu opcji gry. Pozwala ono na zmianę 
 * ustawień gry, graficznych oraz sterowania. 
 * @author AleksanderSklorz 
 */
public class Options extends Menu  {
    private static Screen screen;
    private static boolean stale = false;
    private static int newHeight = 0, newWidth = 0; 
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
    
    /**
     * Potwierdza wybrane zmiany. 
     * @param evt
     * @param isToggled 
     */
    public void accept(MouseButtonEvent evt, boolean isToggled) {
        AppSettings settings = new AppSettings(true);
        String[] selectedResolution = ((String)((SelectBox)screen
                .getElementById("screen_resolution_select_box")).getSelectedListItem()
                .getValue()).split("x");
        newWidth = Integer.parseInt(selectedResolution[0]);
        newHeight = Integer.parseInt(selectedResolution[1]);
        settings.setResolution(newWidth, newHeight);
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
        window.hide();
        stale = true; 
    }
    
    /**
     * Cofa do menu głównego. 
     * @param evt
     * @param isToggled 
     */
    public void back(MouseButtonEvent evt, boolean isToggled) {
        window.hide();
        BuildingSimulator.getBuildingSimulator().getGuiNode().removeControl(screen);
        MenuFactory.showMenu(MenuTypes.MAIN_MENU);
    }
    
    /**
     * Odświeża menu opcji, aby widoczne były np. zmiany rozdzielczości. 
     */
    public static void refresh(){
        BuildingSimulator.getBuildingSimulator().getGuiNode().removeControl(screen);
        stale = false;
        MenuFactory.showMenu(MenuTypes.OPTIONS);
    }
    
    /**
     * Sprawdza czy nastąpiły już zmiany rozdzielczości ekranu. 
     * @return true jesli zmiany są już widoczne, false w przeciwnym przypadku 
     */
    public static boolean isResolutionChanged(){
        return screen.getWidth() == newWidth && screen.getHeight() == newHeight; 
    }
    
    /**
     * Informuje czy dokonane zostały jakiekolwiek zmiany.
     * @return true jeśli wykonano jakieś zmiany, false w przeciwnym przypadku 
     */
    public static boolean getStale() { return stale; }
    
    /**
     * Zwraca widoczny ekran opcji. 
     * @return ekran 
     */
    public static Screen getScreen () { return screen; }
    
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
}
