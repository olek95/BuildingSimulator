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
        Button finishBuildingButton = (Button)screen.getElementById("finish_building_button");
        finishBuildingButton.removeEffect(Effect.EffectEvent.Hover);
        finishBuildingButton.removeEffect(Effect.EffectEvent.Press);
        finishBuildingButton.setColorMap("Interface/hudIcons/end_building_icon.png");
    }
    
    public void validateBuildings(MouseButtonEvent evt, boolean isToggled) {
        BuildingValidator.validate();
    }
    
    public Screen getScreen() { return screen; }
}
