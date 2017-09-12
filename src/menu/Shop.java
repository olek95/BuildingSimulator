package menu;

import buildingsimulator.BuildingSimulator;
import com.jme3.input.event.MouseButtonEvent;
import tonegod.gui.controls.windows.Window;
import tonegod.gui.core.Screen;

public class Shop extends Menu{
    private static Screen screen;
    public Shop(){
        screen = new Screen(BuildingSimulator.getBuildingSimulator());
        screen.parseLayout("Interface/shop.gui.xml", this);
        window = (Window)screen.getElementById("options");
        BuildingSimulator.getBuildingSimulator().getGuiNode().addControl(screen);
    }
    
    public void buy(MouseButtonEvent evt, boolean isToggled) {
        
    }
}
