package menu;

import buildingsimulator.BuildingSimulator;
import com.jme3.font.BitmapFont;
import texts.Translator;
import tonegod.gui.controls.text.Label;
import tonegod.gui.controls.windows.Window;
import tonegod.gui.core.Screen;

public class CleaningDialogWindow extends Menu{
    private static Screen screen;
    public CleaningDialogWindow() {
        screen = new Screen(BuildingSimulator.getBuildingSimulator());
        screen.parseLayout("Interface/cleaning_dialog.gui.xml", this);
        Window window = (Window)screen.getElementById("cleaning_window");
        window.setWindowTitle(Translator.CLEANING_MAP.getValue());
        window.getDragBar().setTextAlign(BitmapFont.Align.Center);
        setWindow(window); 
        Translator.setTexts(new String[]{"cleaning_message", "entire_map_button",
            "infinite_buildings_button", "cancellation_button"}, new Translator[]{Translator.CLEANING_MAP_MESSAGE,
                Translator.ENTIRE_MAP, Translator.INFINITE_BUILDINGS, Translator.CANCELLATION}, screen);
        BuildingSimulator.getBuildingSimulator().getGuiNode().addControl(screen);
    }
}
