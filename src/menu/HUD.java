package menu;

import authorization.User;
import buildingsimulator.BuildingSimulator;
import buildingsimulator.GameManager;
import com.jme3.app.state.AbstractAppState;
import com.jme3.font.BitmapFont;
import com.jme3.font.LineWrapMode;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector2f;
import eyeview.BirdsEyeView;
import java.util.Timer;
import java.util.TimerTask;
import settings.Control.Actions;
import texts.Translator;
import tonegod.gui.controls.text.Label;
import tonegod.gui.controls.windows.Panel;
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
    private static boolean controlsLabelVisibilityBeforeHiding;
    public HUD(){
        screen = new Screen(BuildingSimulator.getBuildingSimulator());
        addNewButton("finish_building_button", "Interface/hudIcons/end_building_icon.png",
                0.95f); 
        addNewButton("shop_button", "Interface/hudIcons/shop_icon.png", 0.9f);
        addNewButton("cleaning_button", "Interface/hudIcons/cleaning_icon.png", 0.85f);
        User user = GameManager.getUser();
        addLabel("points_label", 0, 0, 0.4f, Translator.POINTS.getValue() + ": " 
                + user.getPoints(), BitmapFont.Align.Left, 40, ColorRGBA.Black);
        addLabel("message_label", 0.35f, 0, 0.4f, "", BitmapFont.Align.Center, 40,
                ColorRGBA.Black);
        addLabel("time_label", 0, 0.05f, 0.4f, Translator.TIME.getValue() + ": "
                + user.calculateActualTime(), BitmapFont.Align.Left, 40, ColorRGBA.Black);
        addLabel("control_label", 0.6f, 0.76f, 0.4f, "", BitmapFont.Align.Right,
                20, ColorRGBA.White).hide();
        addLabel("general_controls_label", 0.17f, 0.92f, 0.9f, "", BitmapFont.Align.Center,
                16, ColorRGBA.White);
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
        screen.getElementById("general_controls_label").hide();
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
    
    /**
     * Uaktualnia licznik czasu. 
     */
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
    
    /**
     * Usuwa wyświetlony komunikat. 
     */
    public static void removeMessage() {
        screen.getElementById("message_label").setText("");
        shouldMessageBeDeleted = false; 
        if(messageTimer != null) {
            messageTimer.cancel();
            messageTimer = null;
        }
    }
    
    /**
     * Zmienia kolor komunikatów, liczby punktów, czasu oraz ikon w HUD. 
     * Przełączają się między kolorem białym a czarnym. 
     * @param black true jeśli elementy mają mieć kolor czarny, false jeśli biały
     */
    public static void changeHUDColor(boolean black) {
        ColorRGBA color = black ? ColorRGBA.Black : ColorRGBA.White;
        screen.getElementById("message_label").setFontColor(color);
        screen.getElementById("time_label").setFontColor(color);
        screen.getElementById("points_label").setFontColor(color);
        changeButtonsIcon(black);
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
    
    public static void fillGeneralControlsLabel(boolean showedAll) {
        Actions[] actions = showedAll ? new Actions[]{Actions.CHANGING_CONTROLS_HUD_VISIBILITY, 
            Actions.COPY_BUILDING, Actions.BUY_BUILDING, Actions.SHOW_CURSOR}
                : new Actions[] {Actions.CHANGING_CONTROLS_HUD_VISIBILITY};
        String controls = "";
        for(int i = 0; i < actions.length; i++) {
            controls += actions[i].getKey() + " - "  + actions[i].getValue() + "   ";
        }
        screen.getElementById("general_controls_label")
                .setText(controls);
    }
    
    /**
     * Przełącza widocznność etykiety z obecnym sterowaniem. 
     */
    public static void switchControlsVisibility() {
        Element controlsLabel = screen.getElementById("control_label");
        if(controlsLabel.getIsVisible()) {
            controlsLabel.hide();
            fillGeneralControlsLabel(false);
        } else {
            controlsLabel.show();
            fillGeneralControlsLabel(true);
        }
    }
    
    /**
     * Ustawia widoczność etykiety ze sterowaniem. 
     * @param visible widoczność etykiety 
     */
    public static void setControlsVisibility(boolean visible) {
        Element controlLabel = screen.getElementById("control_label");
        if(visible) controlLabel.show();
        else {
            controlsLabelVisibilityBeforeHiding = controlLabel.getIsVisible();
            controlLabel.hide();
        }
    }
    
    public static void setGeneralControlsLabelVisibility(boolean visible) {
        if(visible) screen.getElementById("general_controls_label").show();
        else screen.getElementById("general_controls_label").hide();
    }
    
    /**
     * Określa czy etykieta ze sterowaniem była wcześniej ukryta czy widoczna. 
     * @return true jeśli etykieta ze sterowaniem była wcześniej widoczna, 
     * false w przeciwnym przypadku 
     */
    public static boolean isControlsLabelVisibilityBeforeHiding() {
        return controlsLabelVisibilityBeforeHiding;
    }
    
    /**
     * Ustawia czy etykieta ze sterowaniem była wcześniej ukryta czy widoczna. 
     * @param visible true jeśli etykieta ze sterowaniem była wcześniej widoczna, 
     * false w przeciwnym przypadku 
     */
    public static void setControlsLabelVisibilityBeforeHiding(boolean visible) {
        controlsLabelVisibilityBeforeHiding = visible;
    }
    
    /**
     * Zwraca aktualną widoczność etykiety ze sterowaniem. 
     * @return true jeśli etykieta ze sterowaniem jest widoczna, false w przeciwnym 
     * przypadku 
     */
    public static boolean isControlsLabelVisible() {
        return screen.getElementById("control_label").getIsVisible();
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
        screen.addElement(button);
    }
    
    private static void changeButtonsIcon(boolean black) {
        String color = black ? "black" : "white";
        screen.getElementById("finish_building_button")
                .setColorMap("Interface/hudIcons/end_building_icon_" + color +".png");
        screen.getElementById("shop_button")
                .setColorMap("Interface/hudIcons/shop_icon_" + color +".png");
        screen.getElementById("cleaning_button")
                .setColorMap("Interface/hudIcons/cleaning_icon_" + color +".png");
    }
    
    private Label addLabel(String id, float xPosition, float yPosition, float xDimension, String text,
            BitmapFont.Align alignment, float fontSize, ColorRGBA color) {
        int width = (int)screen.getWidth(), height = (int)screen.getHeight(); 
        Label label = new Label(screen, id, new Vector2f(width * xPosition, 
                height * yPosition), new Vector2f(width * xDimension, 40));
        label.setFontSize(fontSize);
        label.setText(text);
        label.setFontColor(color);
        label.setTextAlign(alignment);
        label.setTextWrap(LineWrapMode.Word);
        screen.addElement(label);
        return label;
    }
}
