package menu;

import building.BuildingValidator;
import buildingsimulator.BuildingSimulator;
import com.jme3.app.state.AbstractAppState;
import com.jme3.input.event.MouseButtonEvent;
import tonegod.gui.controls.buttons.Button;
import tonegod.gui.core.Screen;
import tonegod.gui.effects.Effect;

/**
 * Klasa <code>HUD</code> reprezentuje wyświetlacz z takimi elementami jak przyciski
 * albo aktualnie wybrany pojazd. 
 * @author AleksanderSklorz
 */
public class HUD extends AbstractAppState{
    private static Screen screen;
    public HUD(){
        BuildingSimulator game = BuildingSimulator.getBuildingSimulator();
        screen = new Screen(game);
        screen.parseLayout("Interface/hud.gui.xml", this);
        improveButtonAppearance("finish_building_button", "Interface/hudIcons/end_building_icon.png"); 
        improveButtonAppearance("shop_button", "Interface/hudIcons/shop_icon.png");
    }
    
    /**
     * Oblicza punkty z obecnie zbudowanych budynków. 
     * @param evt
     * @param isToggled 
     */
    public void validateBuildings(MouseButtonEvent evt, boolean isToggled) {
        BuildingValidator.validate();
    }
    
    /**
     * Pokazuje okienko sklepu. 
     * @param evt
     * @param isToggled 
     */
    public void showShop(MouseButtonEvent evt, boolean isToggled) {
        System.out.println(123); 
    }
    
    /**
     * Ukrywa wszystkie elementy HUDu. 
     */
    public static void hideElements() {
        screen.getElementById("finish_building_button").hide();
        screen.getElementById("shop_button").hide();
    }
    
    /**
     * Zwraca ekran z HUDem.
     * @return ekran 
     */
    public static Screen getScreen() { return screen; }
    
    private void improveButtonAppearance(String id, String path) {
        Button button = (Button)screen.getElementById(id);
        button.removeEffect(Effect.EffectEvent.Hover);
        button.removeEffect(Effect.EffectEvent.Press);
        button.setColorMap(path);
    }
}
