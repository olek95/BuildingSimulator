package menu;

import buildingsimulator.BuildingSimulator;
import buildingsimulator.FilesManager;
import buildingsimulator.GameManager;
import com.jme3.input.event.MouseButtonEvent;
import com.jme3.system.AppSettings;
import java.awt.DisplayMode;
import java.awt.GraphicsEnvironment;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import texts.Translator;
import tonegod.gui.controls.buttons.Button;
import tonegod.gui.controls.buttons.CheckBox;
import tonegod.gui.controls.lists.SelectBox;
import tonegod.gui.controls.windows.Window;
import tonegod.gui.core.Screen;

/**
 * Klasa <code>Options</code> reprezentuje menu opcji gry. Pozwala ono na zmianę 
 * ustawień gry, graficznych oraz sterowania. W przypadku checi opuszczenia menu 
 * bez wykonania uprzedniego zapisu, zostanie wyswietlone ostrzezenie. Jezeli 
 * uzytkownik nie wykonal zadnych zmian. 
 * @author AleksanderSklorz 
 */
public class Options extends Menu  {
    private static Screen screen;
    private static boolean stale = false;
    private static int newHeight = 0, newWidth = 0; 
    private int counter = 0; 
    private static Map<String, String> restoredSettings = new HashMap(), 
            storedSettings; 
    public Options(){
        screen = new Screen(BuildingSimulator.getBuildingSimulator());
        screen.parseLayout("Interface/options.gui.xml", this);
        window = (Window)screen.getElementById("options");
        window.getDragBar().removeFromParent();
        fillResolutionsSelectBox();
        fillSelectBoxSingleValue("refresh_rate_select_box");
        fillSelectBoxSingleValue("color_depth_select_box");
        fillLanguageSelectBox();
        fillAntialiasingSelectBox();
        loadSettings();
        setTexts();
        setStale(); 
        BuildingSimulator.getBuildingSimulator().getGuiNode().addControl(screen);
    }
    
    /**
     * Potwierdza wybrane zmiany. 
     * @param evt
     * @param isToggled 
     */
    public void accept(MouseButtonEvent evt, boolean isToggled) {
        Map<String, String> settings = getSelectedSettings(); 
        Translator.translate((Locale)((SelectBox)screen.getElementById("language_select_box"))
                .getSelectedListItem().getValue());
        setTexts();
        BuildingSimulator game = BuildingSimulator.getBuildingSimulator(); 
        game.setSettings(restoreSettings(settings, false));
        saveSettings(settings);
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
        if(stale){
            screen.addElement(createNotSavedChangesAlert(screen, 
                    Translator.NOT_SAVED_CHANGES.getValue(), GameManager.isPausedGame()
                    ? MenuTypes.PAUSE_MENU : MenuTypes.STARTING_MENU));
        } else doWhenAcceptedExit(screen, GameManager.isPausedGame()
                    ? MenuTypes.PAUSE_MENU : MenuTypes.STARTING_MENU); 
    }
    
    /**
     * Przechodzi do opcji sterowania. 
     * @param evt
     * @param isToggled 
     */
    public void showControlConfiguration(MouseButtonEvent evt, boolean isToggled) {
        storedSettings = getSelectedSettings();
        goNextMenu(screen, MenuTypes.CONTROL_CONFIGURATION);
    }
    
    /**
     * Odświeża menu opcji, aby widoczne były np. zmiany rozdzielczości. 
     */
    public static void refresh(){
        BuildingSimulator.getBuildingSimulator().getGuiNode().removeControl(screen);
        stale = false;
        newHeight = 0; 
        newWidth = 0;
        MenuFactory.showMenu(MenuTypes.OPTIONS);
    }
    
    /**
     * Przywraca zapisane podczas poprzedniej gry ustawienia. 
     * @param settings mapa z ustawieniami 
     * @param startingGame true jeśli ustawiamy podczas uruchamiania gry, false 
     * w przeciwnym przypadku 
     * @return wcześniej zapisane ustawienia dla aplikacji 
     */
    public static AppSettings restoreSettings(Map<String, String> settings, boolean startingGame){
        AppSettings appSettings = new AppSettings(true);
        String[] selectedResolution = settings.get("RESOLUTION").split("x");
        int width = Integer.parseInt(selectedResolution[0]), 
                height = Integer.parseInt(selectedResolution[1]);
        appSettings.setResolution(width, height);
        if(!startingGame) {
            newWidth = width; 
            newHeight = height; 
        }
        appSettings.setFrequency(Integer.valueOf(settings.get("FREQUENCY")));
        appSettings.setSamples(Integer.valueOf(settings.get("SAMPLES")));
        appSettings.setBitsPerPixel(Integer.valueOf(settings.get("BITS_PER_PIXEL")));
        appSettings.setFullscreen(Boolean.valueOf(settings.get("FULLSCREEN")));
        Translator.translate(new Locale(settings.get("LANGUAGE")));
        return appSettings; 
    }
    
    /**
     * Wczytuje plik z zapisanymi wcześniej właściwościami. 
     * @return mapa z zapisanymi właściwościami 
     */
    public static Map<String, String> loadProperties(){
        return FilesManager.loadAllProperties("settings/settings.properties");
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
     * Sprawdza czy zmieniono jakąś opcję z listy rozwijanej. Jeśli tak to ustawia 
     * jako zmodyfkowane.
     * @param selectedIndex indeks wybranego elementu 
     * @param value wybrana wartość 
     */
    public void checkIfChanged(int selectedIndex, Object value) { setStale(); }
    
    /**
     * Ustawia opcje jako zmienone w przypadku gdy następuje zmiana trybu fullscreen. 
     * @param evt
     * @param isToggled 
     */
    public void changeFullscreen(MouseButtonEvent evt, boolean isToggled) { setStale(); }
    
    /**
     * Zwraca widoczny ekran opcji. 
     * @return ekran 
     */
    public static Screen getScreen () { return screen; }
    
    /**
     * Sprawdza czy stan opcji jest nieświeży (został zmieniony). Jeśli tak to 
     * ustawia true, w przeciwnym przypadku false. Dodatkowo blokuje przycisk 
     * akceptacji, jeśli nic nie zostało zmienione. 
     */
    private void setStale() {
        if(counter > 5){
            stale = isChanged(); 
            ((Button)screen.getElementById("accepting_button")).setIsEnabled(stale);
        }else counter++;
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
    
    private Map<String, String> getSelectedSettings() {
        Map<String, String> settings = new HashMap();
        settings.put("RESOLUTION", (String)((SelectBox)screen
                .getElementById("screen_resolution_select_box")).getSelectedListItem().getValue());
        int frequency = (int)((SelectBox)screen.getElementById("refresh_rate_select_box"))
                .getSelectedListItem().getValue();
        settings.put("FREQUENCY", frequency + "");
        int samples = (int)((SelectBox)screen.getElementById("antialiasing_select_box"))
                .getSelectedListItem().getValue();
        settings.put("SAMPLES", samples + "");
        int bitsPerPixel = (int)((SelectBox)screen.getElementById("color_depth_select_box"))
                .getSelectedListItem().getValue();
        settings.put("BITS_PER_PIXEL", bitsPerPixel + "");
        boolean fullscreen = ((CheckBox)screen.getElementById("fullscreen_checkbox"))
                .getIsChecked();
        settings.put("FULLSCREEN", fullscreen + "");
        Locale locale = (Locale)((SelectBox)screen.getElementById("language_select_box"))
                .getSelectedListItem().getValue();
        settings.put("LANGUAGE", locale.getLanguage());
        return settings; 
    }
    
    private static void saveSettings(Map<String, String> settings){
        try(PrintWriter output = new PrintWriter(new FileWriter("settings/settings.properties"))){
            for(Map.Entry<String, String> entry : settings.entrySet()){
                output.println(entry.getKey() + "=" + entry.getValue());
            }
        }catch(IOException ex){
            ex.printStackTrace();
        }
    }
    
    private static void loadSettings() {
        Map<String, String> temp = null; 
        if(storedSettings == null){
            restoredSettings = FilesManager.loadAllProperties("settings/settings.properties");
        }else{
            temp = restoredSettings; 
            restoredSettings = storedSettings;
        }
        ((SelectBox)screen.getElementById("screen_resolution_select_box"))
                .setSelectedByValue(restoredSettings.get("RESOLUTION"), false);
        ((SelectBox)screen.getElementById("refresh_rate_select_box"))
                .setSelectedByValue(Integer.valueOf(restoredSettings.get("FREQUENCY")),
                false);
        ((SelectBox)screen.getElementById("antialiasing_select_box"))
                .setSelectedByValue(Integer.valueOf(restoredSettings.get("SAMPLES")),
                false);
        ((SelectBox)screen.getElementById("color_depth_select_box"))
                .setSelectedByValue(Integer.valueOf(restoredSettings.get("BITS_PER_PIXEL")),
                false);
        ((CheckBox)screen.getElementById("fullscreen_checkbox"))
                .setIsChecked(Boolean.parseBoolean(restoredSettings.get("FULLSCREEN")));
        ((SelectBox)screen.getElementById("language_select_box"))
                .setSelectedByValue(new Locale(restoredSettings.get("LANGUAGE")), false);
        storedSettings = null; 
        if(temp != null) restoredSettings = temp;
    }
    
    private boolean isChanged() {
        Map<String, String> settings = getSelectedSettings(); 
        for(Map.Entry<String, String> entry : restoredSettings.entrySet()) {
            if(!settings.get(entry.getKey()).equals(entry.getValue()))
                return true; 
        }
        return false; 
    }
}
