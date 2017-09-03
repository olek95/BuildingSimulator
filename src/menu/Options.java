package menu;

import buildingsimulator.BuildingSimulator;
import com.jme3.input.event.MouseButtonEvent;
import com.jme3.math.Vector2f;
import com.jme3.system.AppSettings;
import java.awt.DisplayMode;
import java.awt.GraphicsEnvironment;
import java.util.ArrayList;
import java.util.Locale;
import java.util.ResourceBundle;
import texts.Translator;
import tonegod.gui.controls.buttons.CheckBox;
import tonegod.gui.controls.lists.SelectBox;
import tonegod.gui.controls.lists.Table;
import tonegod.gui.controls.lists.Table.TableRow;
import tonegod.gui.controls.windows.AlertBox;
import tonegod.gui.controls.windows.DialogBox;
import tonegod.gui.controls.windows.Window;
import tonegod.gui.core.Screen;
import tonegod.gui.core.layouts.Layout;

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
//        window = new Window(screen, "options", new Vector2f(0, 0),
//                new Vector2f(screen.getWidth(), screen.getHeight()));
//        screen.addElement(window);
//        window.centerToParent();
//        Table table = new Table(screen, new Vector2f(0, 0), new Vector2f(screen.getWidth(), screen.getHeight())) {
//            @Override
//            public void onChange() {
//                ((TableCell)this.getSelectedRows().get(0).getChild(2)).setText("asd");
//            }
//        };
//        table.center();
//        table.addColumn("Akcja");
//        table.addColumn("Przycisk");
//        TableRow row = new TableRow(screen, table); 
//        row.addCell("a", "a1");
//        row.addCell("b", "b1");
//        
//        table.addRow(row);
//        window.addChild(table);
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
        Translator.translate((Locale)((SelectBox)screen.getElementById("language_select_box"))
                .getSelectedListItem().getValue());
        setTexts();
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
        if(stale){
            DialogBox alert = new DialogBox(screen, "closingAlert", new Vector2f(0f, 0f), 
                    new Vector2f(400, 190)
            ) {
                @Override
                public void onButtonCancelPressed(MouseButtonEvent mbe, boolean bln) {
                    screen.removeElement(this);
                }

                @Override
                public void onButtonOkPressed(MouseButtonEvent mbe, boolean bln) {
                    exit();
                    Options.screen.removeElement(this);
                }
            };
            alert.centerToParent();
            alert.getDragBar().setIsMovable(false);
            alert.setIsModal(true);
            alert.showAsModal(true);
            alert.setMsg("Changes aren't saved. Are you sure you want to leave?");
            alert.setButtonOkText("Yes");
            screen.addElement(alert);
        } else exit(); 
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
    
    private void exit(){ 
        window.hide();
        BuildingSimulator.getBuildingSimulator().getGuiNode()
                .removeControl(screen);
        MenuFactory.showMenu(MenuTypes.MAIN_MENU); 
    }
}
