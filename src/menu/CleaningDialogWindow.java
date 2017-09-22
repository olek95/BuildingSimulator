package menu;

import buildingsimulator.BuildingSimulator;
import com.jme3.font.BitmapFont;
import texts.Translator;
import tonegod.gui.controls.windows.Window;
import tonegod.gui.core.Screen;

public class CleaningDialogWindow extends Menu{
    private static Screen screen;
    public CleaningDialogWindow() {
        screen = new Screen(BuildingSimulator.getBuildingSimulator());
        screen.parseLayout("Interface/cleaning_dialog.gui.xml", this);
        Window window = (Window)screen.getElementById("cleaning_window");
        window.setWindowTitle("ASD");
        window.getDragBar().setTextAlign(BitmapFont.Align.Center);
        setWindow(window); 
        BuildingSimulator.getBuildingSimulator().getGuiNode().addControl(screen);
    }
}
