package menu;

import authorization.User;
import buildingsimulator.BuildingSimulator;
import buildingsimulator.GameManager;
import com.jme3.app.state.AbstractAppState;
import com.jme3.font.BitmapFont;
import com.jme3.font.LineWrapMode;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Vector2f;
import cranes.CameraType;
import eyeview.BirdsEyeView;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import settings.Control.Actions;
import texts.Translator;
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
    private static Timer messageTimer; 
    public HUD(){
        screen = new Screen(BuildingSimulator.getBuildingSimulator());
        addNewButton("finish_building_button", "Interface/hudIcons/end_building_icon.png",
                0.95f); 
        addNewButton("shop_button", "Interface/hudIcons/shop_icon.png", 0.9f);
        addNewButton("cleaning_button", "Interface/hudIcons/cleaning_icon.png", 0.85f);
        User user = GameManager.getUser();
        addLabel("points_label", 0, 0, 0.4f, Translator.POINTS.getValue() + ": " 
                + user.getPoints(), BitmapFont.Align.Left, 40);
        addLabel("message_label", 0.35f, 0, 0.4f, "", BitmapFont.Align.Center, 40);
        addLabel("time_label", 0, 0.05f, 0.4f, Translator.TIME.getValue() + ": "
                + user.calculateActualTime(), BitmapFont.Align.Left, 40);
        addLabel("control_label", 0.6f, 0.76f, 0.4f, "", BitmapFont.Align.Right, 20)
                .setFontColor(ColorRGBA.White);
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
        screen.getElementById("control_label").hide();
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
     * Uzupełnia informację o aktualnym sterowaniu. Funkcja ta posiada następujące
     * znaki specjalne: tekst w okrągłych nawiasach jest niewyświetlany, natomiast
     * jeśli tekst jest oddzielony ukośnikami (/), brana jest ta część której poda się 
     * indeks. 
     * @param actions tablica aktualnie dostępnych akcji 
     * @param additionalInformation tablica stringów zawierających dodatkowe informacje 
     * bądź opisowe przedstawienie sterowania 
     * @param indexes tablica indeksów oznaczających, która część oddzielona 
     * ukośnikiem ma zostać wzięta pod uwagę
     */
    public static void fillControlInformation(Actions[] actions, String[] additionalInformation,
            int... indexes) {
        String information = "";
        int k = 0;
        String lineSeparator = System.getProperty("line.separator");
        if(additionalInformation != null) {
            for(int i = 0; i < additionalInformation.length; i++) {
                information += additionalInformation[i] + lineSeparator;
            }
        }
        if(actions != null) {
            for(int i = 0; i < actions.length; i++) {
                String[] values = actions[i].getValue().split("/");
                String value = values.length != 1 ? values[indexes[k++]] : values[0];
                int optionalPhraseIndex = value.indexOf('(');
                information += value.substring(0, optionalPhraseIndex != -1 
                        ? optionalPhraseIndex : value.length()) + " - " + actions[i].getKey() 
                        + lineSeparator;
            }
        }
        Element controlLabel = screen.getElementById("control_label");
        controlLabel.setText(information);
        // 20 rozmiar pojedynczego wiersza
        controlLabel.setY(20f * ((actions == null ? 0 : actions.length)
                + (additionalInformation == null ? 0 : additionalInformation.length))
                + controlLabel.getHeight());
    }
    
    /**
     * Aktualizuje zawartość etykiety zawierającej aktualne sterowanie. Stosować
     * gdy w czasie gry zmieniono sterowanie. 
     */
    public static void updateControlsLabel() {
        if(BirdsEyeView.isActive()) {
            if(BirdsEyeView.isMovingAvailable()) BirdsEyeView.displayMovingModeHUD();
            else BirdsEyeView.displayNotMovingModeHUD();
        } else {
            GameManager.displayActualUnitControlsInHUD();
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
    
    private Label addLabel(String id, float xPosition, float yPosition, float xDimension, String text,
            BitmapFont.Align alignment, float fontSize) {
        int width = (int)screen.getWidth(), height = (int)screen.getHeight(); 
        Label label = new Label(screen, id, new Vector2f(width * xPosition, 
                height * yPosition), new Vector2f(width * xDimension, 40));
        label.setFontSize(fontSize);
        label.setText(text);
        label.setFontColor(ColorRGBA.Black);
        label.setTextAlign(alignment);
        if(id.equals("control_label")) System.out.println(label.getPosition());
        label.setTextWrap(LineWrapMode.Word);
        screen.addElement(label);
        return label;
    }
}
