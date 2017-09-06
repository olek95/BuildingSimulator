package menu;

import buildingsimulator.BuildingSimulator;
import buildingsimulator.Control;
import static buildingsimulator.Control.Actions.values;
import com.jme3.input.event.MouseButtonEvent;
import com.jme3.system.AppSettings;
import java.awt.DisplayMode;
import java.awt.GraphicsEnvironment;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Properties;
import texts.Translator;
import tonegod.gui.controls.buttons.CheckBox;
import tonegod.gui.controls.lists.SelectBox;
import tonegod.gui.controls.windows.Window;
import tonegod.gui.core.Element;
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
    private int counter = 0; 
    public Options(){
        screen = new Screen(BuildingSimulator.getBuildingSimulator());
        screen.parseLayout("Interface/options.gui.xml", this);
        window = (Window)screen.getElementById("options");
        window.getDragBar().setIsMovable(false);
        fillResolutionsSelectBox();
        setTexts();
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
        Properties values = new Properties();
        values.setProperty("RESOLUTION", (String)((SelectBox)screen
                .getElementById("screen_resolution_select_box")).getSelectedListItem().getValue());
        String[] selectedResolution = values.getProperty("RESOLUTION").split("x");
        newWidth = Integer.parseInt(selectedResolution[0]);
        newHeight = Integer.parseInt(selectedResolution[1]);
        settings.setResolution(newWidth, newHeight);
        int frequency = (int)((SelectBox)screen.getElementById("refresh_rate_select_box"))
                .getSelectedListItem().getValue();
        values.setProperty("FREQUENCY", frequency + "");
        settings.setFrequency(frequency);
        int samples = (int)((SelectBox)screen.getElementById("antialiasing_select_box"))
                .getSelectedListItem().getValue();
        values.setProperty("SAMPLES", samples + "");
        settings.setSamples(samples);
        int bitsPerPixel = (int)((SelectBox)screen.getElementById("color_depth_select_box"))
                .getSelectedListItem().getValue();
        values.setProperty("BITS_PER_PIXEL", bitsPerPixel + "");
        settings.setBitsPerPixel(bitsPerPixel);
        boolean fullscreen = ((CheckBox)screen.getElementById("fullscreen_checkbox"))
                .getIsChecked();
        values.setProperty("FULLSCREEN", fullscreen + "");
        settings.setFullscreen(((CheckBox)screen.getElementById("fullscreen_checkbox"))
                .getIsChecked());
        Locale locale = (Locale)((SelectBox)screen.getElementById("language_select_box"))
                .getSelectedListItem().getValue();
        values.setProperty("LANGUAGE", locale.getDisplayLanguage());
        Translator.translate((Locale)((SelectBox)screen.getElementById("language_select_box"))
                .getSelectedListItem().getValue());
        setTexts();
        BuildingSimulator game = BuildingSimulator.getBuildingSimulator(); 
        game.setSettings(settings);
        game.restart();
        window.hide();
        saveSettings(values); 
        stale = true; 
    }
    
    /**
     * Cofa do menu głównego. 
     * @param evt
     * @param isToggled 
     */
    public void back(MouseButtonEvent evt, boolean isToggled) {
        if(stale){
            screen.addElement(createNotSavedChangesAlert(screen));
        } else closeWindow(); 
    }
    
    public void showControlConfiguration(MouseButtonEvent evt, boolean isToggled) {
        window.hide();
        BuildingSimulator.getBuildingSimulator().getGuiNode().removeControl(screen);
        MenuFactory.showMenu(MenuTypes.CONTROL_CONFIGURATION);
    }
    
    /**
     * Odświeża menu opcji, aby widoczne były np. zmiany rozdzielczości. 
     */
    public static void refresh(){
        BuildingSimulator.getBuildingSimulator().getGuiNode().removeControl(screen);
        stale = false;
        MenuFactory.showMenu(MenuTypes.OPTIONS);
    }
    
    @Override
    public void closeWindow() {
        window.hide();
        Element closingAlert = screen.getElementById("closing_alert");
        if(closingAlert != null){
            closingAlert.hide();
            screen.removeElement(closingAlert);
        }
        BuildingSimulator.getBuildingSimulator().getGuiNode()
                .removeControl(screen);
        MenuFactory.showMenu(MenuTypes.MAIN_MENU);
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
    
    public void setStale(int selectedIndex, Object value) {
        if(counter > 4) stale = true; 
        else counter++; 
    }
    
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
        languages.addListItem(Translator.ENGLISH.getValue(), Locale.ENGLISH);
        languages.addListItem(Translator.POLISH.getValue(), new Locale("pl"));
    }
    
    private void fillAntialiasingSelectBox() { 
        int[] values = {0, 2, 4, 6, 8, 16};
        SelectBox antialiasingSelectBox = (SelectBox)screen
                .getElementById("antialiasing_select_box");
        antialiasingSelectBox.addListItem(Translator.DISABLED_ANTIALIASING.getValue(),
                values[0]);
        for(int i = 1; i < values.length; i++)
            antialiasingSelectBox.addListItem(values[i] + "x", values[i]);
    }
    
    private void setTexts() {
        Translator.setTexts(new String[]{"settings_label", "language_label", "graphics_label",
            "screen_resolution_label", "color_depth_label", "antialiasing_label",
            "fullscreen_label", "refresh_rate_label", "accepting_button", 
            "return_button", "control_configuration_button"},
            new Translator[]{Translator.GAME_SETTINGS, Translator.LANGUAGE, Translator.GRAPHICS,
            Translator.SCREEN_RESOLUTION, Translator.COLOR_DEPTH, Translator.ANTIALIASING, 
            Translator.FULLSCREEN, Translator.REFRESH_RATE, Translator.ACCEPTING, Translator.RETURN,
            Translator.CONTROL_CONFIGURATION}, screen);
    }
    
    private static void saveSettings(Properties settings){
        try(OutputStream output = new FileOutputStream("src/settings/settings.properties")){
            settings.store(output, null);
        }catch(IOException ex){
            ex.printStackTrace();
        }
    }
}
