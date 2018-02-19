package menu;

import authorization.User;
import buildingsimulator.BuildingSimulator;
import buildingsimulator.GameManager;
import com.jme3.app.state.AbstractAppState;
import com.jme3.font.BitmapFont;
import com.jme3.font.LineWrapMode;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector2f;
import java.util.Timer;
import java.util.TimerTask;
import texts.Translator;
import tonegod.gui.controls.text.Label;
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
    private static Timer messageTimer; 
    public HUD(){
        screen = new Screen(BuildingSimulator.getBuildingSimulator());
        addNewButton("finish_building_button", "Interface/hudIcons/end_building_icon.png",
                0.95f); 
        addNewButton("shop_button", "Interface/hudIcons/shop_icon.png", 0.9f);
        addNewButton("cleaning_button", "Interface/hudIcons/cleaning_icon.png", 0.85f);
        User user = GameManager.getUser();
        addLabel("points_label", 0, 0, 0.4f, Translator.POINTS.getValue() + ": " 
                + user.getPoints(), false);
        addLabel("message_label", 0.35f, 0, 0.4f, "", true);
        addLabel("time_label", 0, 0.05f, 0.4f, Translator.TIME.getValue() + ": "
                + user.calculateActualTime(), false);
    }
    
    /**
     * Ukrywa wszystkie elementy HUDu. 
     */
    public static void hideElements() {
        if(messageTimer != null) removeMessage();
        screen.getElementById("finish_building_button").hide();
        screen.getElementById("shop_button").hide();
        screen.getElementById("points_label").hide();
        screen.getElementById("cleaning_button").hide();
        screen.getElementById("time_label").hide();
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
        screen.getElementById("points_label").setText(Translator.POINTS.getValue() + ": " 
                + GameManager.getUser().getPoints());
    }
    
    public static void updateTime() {
        screen.getElementById("time_label").setText(Translator.TIME.getValue() + ": " 
                + GameManager.getUser().calculateActualTime() + "");
    }
    
    /**
     * Ustawia treść wyświetlonego komunikatu. 
     * @param message treść komunikatu 
     */
    public static void setMessage(String message) {
        screen.getElementById("message_label").setText(message);
        messageTimer = new Timer();
        messageTimer.schedule(new TimerTask() {
            @Override
            public void run() {
               shouldMessageBeDeleted = true;
            }
        }, 3000);
    }
    
    public static void removeMessage() {
        screen.getElementById("message_label").setText("");
        shouldMessageBeDeleted = false; 
        if(messageTimer != null) {
            messageTimer.cancel();
            messageTimer = null;
        }
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
    
    private void addNewButton(String id, String path, float x) {
        HUDButton button = new HUDButton(screen, id, new Vector2f((int)screen.getWidth() * x,
                0), new  Vector2f(32, 32));
        button.removeEffect(Effect.EffectEvent.Hover);
        button.removeEffect(Effect.EffectEvent.Press);
        button.setColorMap(path);
        screen.addElement(button);
    }
    
    private void addLabel(String id, float xPosition, float yPosition, float xDimension, String text,
            boolean centered) {
        int width = (int)screen.getWidth(), height = (int)screen.getHeight(); 
        Label label = new Label(screen, id, new Vector2f(width * xPosition, 
                height * yPosition), new Vector2f(width * xDimension, 40));
        label.setFontSize(40);
        label.setText(text);
        label.setFontColor(ColorRGBA.Black);
        if(centered) label.setTextAlign(BitmapFont.Align.Center);
        label.setTextWrap(LineWrapMode.Word);
        screen.addElement(label);
    }
}
