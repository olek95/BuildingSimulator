package menu;

import buildingsimulator.BuildingSimulator;
import settings.CreatorMockSettings;
import buildingsimulator.GameManager;
import com.jme3.audio.AudioNode;
import com.jme3.input.event.MouseButtonEvent;
import com.jme3.system.AppSettings;
import java.awt.DisplayMode;
import java.awt.GraphicsEnvironment;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import texts.Translator;
import tonegod.gui.controls.buttons.Button;
import tonegod.gui.controls.buttons.CheckBox;
import tonegod.gui.controls.lists.SelectBox;
import tonegod.gui.controls.lists.Slider;
import tonegod.gui.controls.windows.Window;
import tonegod.gui.core.Screen;

/**
 * Klasa <code>Options</code> reprezentuje menu opcji gry. Pozwala ono na zmianę 
 * ustawień gry, graficznych oraz sterowania. W przypadku chęci opuszczenia menu 
 * bez wykonania uprzedniego zapisu, zostanie wyswietlone ostrzeżenie. Jeżeli 
 * użytkownik nie wykonał żadnych zmian. 
 * @author AleksanderSklorz 
 */
public class Options extends Menu  {
    private static boolean stale = false;
    private static int newHeight = 0, newWidth = 0; 
    private int counter = 0; 
    private static Properties restoredSettings = new Properties(), 
            storedSettings; 
    public Options(){
        BuildingSimulator game = BuildingSimulator.getBuildingSimulator();
        Screen screen = new Screen(game);
        setScreen(screen);
        parseLayout("Interface/options.gui.xml");
        Window window = (Window)screen.getElementById("options");
        window.getDragBar().removeFromParent();
        setWindow(window); 
        fillResolutionsSelectBox();
        fillSelectBoxSingleValue("refresh_rate_select_box");
        fillSelectBoxSingleValue("color_depth_select_box");
        fillLanguageSelectBox();
        fillAntialiasingSelectBox();
        loadSettings();
        setTexts();
        setStale(); 
        game.getGuiNode().addControl(screen);
    }
    
    /**
     * Potwierdza wybrane zmiany. 
     * @param evt
     * @param isToggled 
     */
    public void accept(MouseButtonEvent evt, boolean isToggled) {
        Properties settings = getSelectedSettings(); 
        Translator.translate((Locale)((SelectBox)getScreen().getElementById("language_select_box"))
                .getSelectedListItem().getValue());
        setTexts();
        BuildingSimulator game = BuildingSimulator.getBuildingSimulator(); 
        game.setSettings(restoreSettings(settings, false));
        saveSettings(settings);
        game.restart();
        getWindow().hide();
        stale = true; 
    }
    
    /**
     * Cofa do menu głównego. 
     * @param evt
     * @param isToggled 
     */
    public void back(MouseButtonEvent evt, boolean isToggled) {
        if(stale){
            getScreen().addElement(createNotSavedChangesAlert(Translator.NOT_SAVED_CHANGES.getValue(),
                    GameManager.isPausedGame() ? MenuTypes.PAUSE_MENU : MenuTypes.STARTING_MENU));
        } else {
            doWhenAcceptedExit(GameManager.isPausedGame() ? MenuTypes.PAUSE_MENU 
                    : MenuTypes.STARTING_MENU);
        }
    }
    
    /**
     * Przechodzi do opcji sterowania. 
     * @param evt
     * @param isToggled 
     */
    public void showControlConfiguration(MouseButtonEvent evt, boolean isToggled) {
        storedSettings = getSelectedSettings();
        goNextMenu(MenuTypes.CONTROL_CONFIGURATION);
    }
    
    /**
     * Odświeża menu opcji, aby widoczne były np. zmiany rozdzielczości. 
     */
    public static void refresh(){
        GameManager.removeControlFromGui(getScreen());
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
    public static AppSettings restoreSettings(Properties settings, boolean startingGame){
        AppSettings appSettings = new AppSettings(true);
        String[] selectedResolution = settings.getProperty("RESOLUTION").split("x");
        int width = Integer.parseInt(selectedResolution[0]), 
                height = Integer.parseInt(selectedResolution[1]);
        appSettings.setResolution(width, height);
        if(!startingGame) {
            newWidth = width; 
            newHeight = height; 
        }
        appSettings.setFrequency(Integer.valueOf(settings.getProperty("FREQUENCY")));
        appSettings.setSamples(Integer.valueOf(settings.getProperty("SAMPLES")));
        appSettings.setBitsPerPixel(Integer.valueOf(settings.getProperty("BITS_PER_PIXEL")));
        appSettings.setFullscreen(Boolean.valueOf(settings.getProperty("FULLSCREEN")));
        Translator.translate(new Locale(settings.getProperty("LANGUAGE")));
        float volume = Float.valueOf(settings.getProperty("VOLUME"));
        GameManager.setGameSoundVolume(volume);
        AudioNode backgroundSound = getBackgroundSound(); 
        if(backgroundSound != null) backgroundSound.setVolume(volume);
        return appSettings; 
    }
    
    /**
     * Wczytuje plik z zapisanymi wcześniej właściwościami. 
     * @return mapa z zapisanymi właściwościami 
     */
    public static Properties loadProperties(){
        Properties loadedProperties = new Properties();
        try(FileReader file = new FileReader("settings/settings.properties")){
            loadedProperties.load(new BufferedReader(file));
            return loadedProperties; 
        } catch (IOException ex) {
            return CreatorMockSettings.createDefaultProperties(new String[]{"BITS_PER_PIXEL",
                "SAMPLES", "VOLUME", "LANGUAGE", "FREQUENCY", "RESOLUTION", "FULLSCREEN"},
                    new String[]{"32", "6", "0.4", Locale.getDefault().getLanguage(),
                        "60", "800x600", "false"}, "settings/settings.properties");
        }
    }
    
    /**
     * Zmienia głosność dźwięku aplikacji. 
     * @param selectedIndex
     * @param value 
     */
    public void changeVolume(int selectedIndex, Object value) { 
        Slider volumeSlider = (Slider)getScreen().getElementById("sound_volume_slider");
        if(volumeSlider != null) {
            volumeSlider.setText((Math.round((float)volumeSlider.getSelectedValue() * 10) / 10.0)
                    + "");
        }
        setStale(); 
    }
    
    /**
     * Sprawdza czy nastąpiły już zmiany rozdzielczości ekranu. 
     * @return true jesli zmiany są już widoczne, false w przeciwnym przypadku 
     */
    public static boolean isResolutionChanged(){
        Screen screen = getScreen(); 
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
     * Sprawdza czy stan opcji jest nieświeży (został zmieniony). Jeśli tak to 
     * ustawia true, w przeciwnym przypadku false. Dodatkowo blokuje przycisk 
     * akceptacji, jeśli nic nie zostało zmienione. 
     */
    private void setStale() {
        if(counter > 5){
            stale = isChanged(); 
            ((Button)getScreen().getElementById("accepting_button")).setIsEnabled(stale);
        }else counter++;
    }
    
    private void fillResolutionsSelectBox() {
         DisplayMode[] modes = GraphicsEnvironment.getLocalGraphicsEnvironment()
                .getDefaultScreenDevice().getDisplayModes();
         ArrayList<String> elements = new ArrayList(); 
         SelectBox screenResolutions = (SelectBox)getScreen()
                 .getElementById("screen_resolution_select_box");
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
        SelectBox colorDepths = (SelectBox)getScreen().getElementById(selectBoxId);
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
        SelectBox languages = (SelectBox)getScreen().getElementById("language_select_box");
        languages.addListItem(Translator.ENGLISH.getValue(), Locale.ENGLISH);
        languages.addListItem(Translator.POLISH.getValue(), new Locale("pl"));
    }
    
    private void fillAntialiasingSelectBox() { 
        int[] values = {0, 2, 4, 6, 8, 16};
        SelectBox antialiasingSelectBox = (SelectBox)getScreen()
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
            "return_button", "control_configuration_button", "sound_volume_label"},
            new Translator[]{Translator.GAME_SETTINGS, Translator.LANGUAGE, Translator.GRAPHICS,
            Translator.SCREEN_RESOLUTION, Translator.COLOR_DEPTH, Translator.ANTIALIASING, 
            Translator.FULLSCREEN, Translator.REFRESH_RATE, Translator.ACCEPTING, Translator.RETURN,
            Translator.CONTROL_CONFIGURATION, Translator.SOUND_VOLUME}, getScreen());
    }
    
    private Properties getSelectedSettings() {
        Properties settings = new Properties();
        Screen screen = getScreen();
        settings.setProperty("RESOLUTION", (String)((SelectBox)screen
                .getElementById("screen_resolution_select_box")).getSelectedListItem().getValue());
        int frequency = (int)((SelectBox)screen.getElementById("refresh_rate_select_box"))
                .getSelectedListItem().getValue();
        settings.setProperty("FREQUENCY", frequency + "");
        int samples = (int)((SelectBox)screen.getElementById("antialiasing_select_box"))
                .getSelectedListItem().getValue();
        settings.setProperty("SAMPLES", samples + "");
        int bitsPerPixel = (int)((SelectBox)screen.getElementById("color_depth_select_box"))
                .getSelectedListItem().getValue();
        settings.setProperty("BITS_PER_PIXEL", bitsPerPixel + "");
        boolean fullscreen = ((CheckBox)screen.getElementById("fullscreen_checkbox"))
                .getIsChecked();
        settings.setProperty("FULLSCREEN", fullscreen + "");
        Locale locale = (Locale)((SelectBox)screen.getElementById("language_select_box"))
                .getSelectedListItem().getValue();
        settings.setProperty("LANGUAGE", locale.getLanguage());
        float volume = (float)((Slider)screen.getElementById("sound_volume_slider"))
                .getSelectedValue();
        settings.setProperty("VOLUME", (Math.round(volume * 10) / 10.0) + "");
        return settings; 
    }
    
    private static void saveSettings(Properties settings){
        try(PrintWriter output = new PrintWriter(new FileWriter("settings/settings.properties"))){
            settings.store(output, null);
        } catch (IOException ex) {
            Logger.getLogger(Options.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private static void loadSettings() {
        Properties temp = null; 
        if(storedSettings == null){
            restoredSettings = loadProperties();
        }else{
            temp = restoredSettings; 
            restoredSettings = storedSettings;
        }
        Screen screen = getScreen();
        ((SelectBox)screen.getElementById("screen_resolution_select_box"))
                .setSelectedByValue(restoredSettings.getProperty("RESOLUTION"), false);
        ((SelectBox)screen.getElementById("refresh_rate_select_box"))
                .setSelectedByValue(Integer.valueOf(restoredSettings.getProperty("FREQUENCY")),
                false);
        ((SelectBox)screen.getElementById("antialiasing_select_box"))
                .setSelectedByValue(Integer.valueOf(restoredSettings.getProperty("SAMPLES")),
                false);
        ((SelectBox)screen.getElementById("color_depth_select_box"))
                .setSelectedByValue(Integer.valueOf(restoredSettings.getProperty("BITS_PER_PIXEL")),
                false);
        ((CheckBox)screen.getElementById("fullscreen_checkbox"))
                .setIsChecked(Boolean.parseBoolean(restoredSettings.getProperty("FULLSCREEN")));
        ((SelectBox)screen.getElementById("language_select_box"))
                .setSelectedByValue(new Locale(restoredSettings.getProperty("LANGUAGE")), false);
        String volume = restoredSettings.getProperty("VOLUME");
        Slider volumeSlider = (Slider)screen.getElementById("sound_volume_slider");
        volumeSlider.setSelectedByValue(volume);
        volumeSlider.setText(volume);
        storedSettings = null; 
        if(temp != null) restoredSettings = temp;
    }
    
    private boolean isChanged() {
        Properties settings = getSelectedSettings();
        for(Map.Entry<Object, Object> entry : restoredSettings.entrySet()) {
            if(!settings.getProperty((String)entry.getKey()).equals(entry.getValue()))
                return true; 
        }
        return false; 
    }
}
