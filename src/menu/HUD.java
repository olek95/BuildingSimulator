package menu;

import building.BuildingValidator;
import buildingsimulator.BuildingSimulator;
import com.jme3.app.state.AbstractAppState;
import com.jme3.input.event.MouseButtonEvent;
import tonegod.gui.controls.buttons.Button;
import tonegod.gui.core.Screen;
import tonegod.gui.effects.Effect;

public class HUD extends AbstractAppState{
    private static Screen screen;
    public HUD(){
        BuildingSimulator game = BuildingSimulator.getBuildingSimulator();
        screen = new Screen(game);
        screen.parseLayout("Interface/hud.gui.xml", this);
        improveButtonAppearance("finish_building_button", "Interface/hudIcons/end_building_icon.png"); 
        improveButtonAppearance("shop_button", "Interface/hudIcons/shop_icon.png");
    }
    
    public void validateBuildings(MouseButtonEvent evt, boolean isToggled) {
        BuildingValidator.validate();
    }
    
    public void showShop(MouseButtonEvent evt, boolean isToggled) {
        System.out.println(123); 
    }
    
    public static void hideElements() {
        screen.getElementById("finish_building_button").hide();
        screen.getElementById("shop_button").hide();
    }
    
    public static Screen getScreen() { return screen; }
    
    private void improveButtonAppearance(String id, String path) {
        Button button = (Button)screen.getElementById(id);
        button.removeEffect(Effect.EffectEvent.Hover);
        button.removeEffect(Effect.EffectEvent.Press);
        button.setColorMap(path);
    }
}
