package menu;

import buildingsimulator.BuildingSimulator;
import buildingsimulator.GameManager;
import com.jme3.app.state.AbstractAppState;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector2f;
import java.util.Timer;
import java.util.TimerTask;
import tonegod.gui.controls.text.Label;
import tonegod.gui.core.Element;
import tonegod.gui.core.Screen;
import tonegod.gui.effects.Effect;

/**
 * Klasa <code>HUD</code> reprezentuje wyświetlacz z takimi elementami jak przyciski
 * albo aktualnie wybrany pojazd. 
 * @author AleksanderSklorz
 */
public class HUD extends AbstractAppState{
    private static Screen screen;
    private static boolean shouldMessageBeDeleted; 
    public HUD(){
        screen = new Screen(BuildingSimulator.getBuildingSimulator());
        addNewButton("finish_building_button", "Interface/hudIcons/end_building_icon.png",
                0.95f, 0, false); 
        addNewButton("shop_button", "Interface/hudIcons/shop_icon.png", 0.9f, 0, true);
        addLabel("points_label", 0, 0.2f, GameManager.getUser().getPoints() + "");
        addLabel("message_label", 0.3f, 0.4f, "");
    }
    
    /**
     * Ukrywa wszystkie elementy HUDu. 
     */
    public static void hideElements() {
        screen.getElementById("message_label").setText("");
        screen.getElementById("finish_building_button").hide();
        screen.getElementById("shop_button").hide();
        screen.getElementById("points_label").hide();
    }
    
    /**
     * Zmienia widoczność przycisku sklepu. 
     * @param visible true jeśli przycisk ma być widoczny, false w przeciwnym przypadku 
     */
    public static void changeShopButtonVisibility(boolean visible) {
        screen.getElementById("shop_button").setIsVisible(visible);
    }
    
    /**
     * Uaktualnia liczbę zdobytych punktów. 
     */
    public static void updatePoints() {
        screen.getElementById("points_label").setText(GameManager.getUser()
                .getPoints() + "");
    }
    
    /**
     * Ustawia treść wyświetlonego komunikatu. 
     * @param message treść komunikatu 
     */
    public static void setMessage(String message) {
        screen.getElementById("message_label").setText(message);
        if(!message.equals(""))
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                   shouldMessageBeDeleted = true;
                }
            }, 3000);
        else shouldMessageBeDeleted = false;
    }
    
    /**
     * Zwraca ekran z HUDem.
     * @return ekran 
     */
    public static Screen getScreen() { return screen; }
    
    /**
     * Sprawdza czy komunikat powinien zostać już usunięty. 
     * @return true jeśli komunikat powinien zostac usunięty, false w przeciwnym przypadku 
     */
    public static boolean shouldMessageBeDeleted() { return shouldMessageBeDeleted; }
    
    private void addNewButton(String id, String path, float x, float y, boolean shopping) {
        HUDButton button = new HUDButton(screen, id, new Vector2f((int)screen.getWidth() * x,
                (int)screen.getHeight() * y), new  Vector2f(32, 32), shopping);
        button.removeEffect(Effect.EffectEvent.Hover);
        button.removeEffect(Effect.EffectEvent.Press);
        button.setColorMap(path);
        screen.addElement(button);
    }
    
    private void addLabel(String id, float xPosition, float xDimension, String text) {
        int width = (int)screen.getWidth(); 
        Label label = new Label(screen, id, new Vector2f(width * xPosition, 0),
                new Vector2f(width * xDimension, 40));
        label.setFontSize(40);
        label.setText(text);
        label.setFontColor(ColorRGBA.Black);
        screen.addElement(label);
    }
}
