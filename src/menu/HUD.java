package menu;

import authorization.User;
import building.BuildingCreator;
import building.BuildingValidator;
import buildingsimulator.BuildingSimulator;
import buildingsimulator.Controllable;
import buildingsimulator.GameManager;
import com.jme3.app.state.AbstractAppState;
import com.jme3.font.BitmapFont;
import com.jme3.font.LineWrapMode;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.event.KeyInputEvent;
import com.jme3.input.event.MouseButtonEvent;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector2f;
import eyeview.BirdsEyeView;
import java.util.Timer;
import java.util.TimerTask;
import settings.Control;
import settings.Control.Actions;
import texts.Translator;
import tonegod.gui.controls.buttons.Button;
import tonegod.gui.controls.buttons.ButtonAdapter;
import tonegod.gui.controls.text.Label;
import tonegod.gui.core.Element;
import tonegod.gui.core.Screen;
import tonegod.gui.effects.Effect;

/**
 * Klasa <code>HUD</code> reprezentuje wyświetlacz z takimi elementami jak przyciski
 * albo aktualnie wybrany pojazd. 
 * @author AleksanderSklorz
 */
public class HUD extends AbstractAppState implements ActionListener, Controllable{
    private static Screen screen;
    private static boolean shouldMessageBeDeleted; 
    private static Timer messageTimer; 
    private static boolean controlsLabelVisibilityBeforeHiding;
    private static Actions[] availableActions = new Actions[]{Actions.SHOW_SHOP, 
        Actions.SHOW_CLEANING_DIALOG_WINDOW, Actions.SELL_BUILDINGS}; 
    public HUD(){
        screen = new Screen(BuildingSimulator.getBuildingSimulator());
        addNewButton(createCleaningDialogWindowButton(), "F1");
        addNewButton(createShopButton(), "F2");
        addNewButton(createSellingBuildingsButton(), "F3"); 
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
        Control.addListener(this, false);
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
                String value;
                if(values.length != 1) {
                    value = values[indexes[k]].substring(0, 1).toUpperCase() 
                            + values[indexes[k]].substring(1);
                    k++;
                } else value = values[0];
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
            if(BirdsEyeView.isMovingAvailable()) {
                BirdsEyeView.displayMovingModeHUD(((BuildingCreator)BirdsEyeView.getViewOwner())
                        .isCloning());
            }
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
     * Pokazuje okno dialogowe ze sklepem umożliwiającym zakup materiałów budowlanych. 
     * Pozwala na drobną modyfikację kupowanej ściany - typ, kolor, ilość, rozmiar. 
     * Zezwala także na zmianę wysokości żurawia. 
     */
    public static void showShop() {
        if(CleaningDialogWindow.getDisplayedCleaningDialogWindow() == null) {
            Shop shop = Shop.getDisplayedShop();
            if(shop != null) {
                    if(!shop.isShopPanelShowed()) {
                        shop.getView().setOff();
                        BuildingSimulator.getGameFlyByCamera().setDragToRotate(true);
                        MenuFactory.showMenu(MenuTypes.SHOP); 
                        setControlsVisibility(false);
                        setGeneralControlsLabelVisibility(false);
                    }
                } else {
                    BuildingSimulator.getGameFlyByCamera().setDragToRotate(true);
                    MenuFactory.showMenu(MenuTypes.SHOP); 
                    setControlsVisibility(false);
                    setGeneralControlsLabelVisibility(false);
                }
                GameManager.getActualUnit().getCamera().setOff();
        }
    }
    
    /**
     * Wyświetla okno dialogowe umożliwiające oczyszczenie mapy ze wszystkich 
     * budowli, bądź tylko z tych niezbudowanych. 
     */
    public static void showCleaningDialogWindow() {
        if(CleaningDialogWindow.getDisplayedCleaningDialogWindow() == null) {
            if(Shop.getDisplayedShop() == null) {
                BuildingSimulator.getGameFlyByCamera().setDragToRotate(true);
                MenuFactory.showMenu(MenuTypes.CLEANING_DIALOG_WINDOW);
                setControlsVisibility(false);
            }
        }
    }
    
    /**
     * Sprzedaje (ocenia) dotychczas zbudowane, nieocenione budowle. Po każdej 
     * ocenie gracz otrzymuje stosowną liczbę punktów. 
     */
    public static void sellBuildings() {
        if(CleaningDialogWindow.getDisplayedCleaningDialogWindow() == null) {
            if(Shop.getDisplayedShop() == null) {
                int points = BuildingValidator.validate();
                GameManager.getUser().addPoints(points);
                updatePoints();
                setMessage(Translator.MESSAGE_POINTS.getValue().replace("x", points + ""));
            }
        }
    }
    
    @Override
    public void onAction(String name, boolean isPressed, float tpf){
        if(isPressed) {
            if(name.equals(Actions.SHOW_SHOP.toString())) showShop();
            else if(name.equals(Actions.SHOW_CLEANING_DIALOG_WINDOW.toString()))
                showCleaningDialogWindow(); 
            else sellBuildings();
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
    
    @Override
    public Actions[] getAvailableActions() { return availableActions; }
    
    private ButtonAdapter createShopButton() {
        return new ButtonAdapter(screen, "shop_button", new Vector2f((int)screen.getWidth() * 0.9f,
                0), new  Vector2f(32, 32)){
            @Override
            public void onButtonMouseLeftUp(MouseButtonEvent evt, boolean isToggled) {
                showShop();
            }
            
            @Override
            public void onKeyRelease(KeyInputEvent evt) {}
        };
    }
    
    private ButtonAdapter createCleaningDialogWindowButton() {
        return new ButtonAdapter(screen, "cleaning_button", new Vector2f((int)screen.getWidth() * 0.85f,
                0), new  Vector2f(32, 32)){
            @Override
            public void onButtonMouseLeftUp(MouseButtonEvent evt, boolean isToggled) {
                showCleaningDialogWindow();
            }
            
            @Override
            public void onKeyRelease(KeyInputEvent evt) {}
        };
    }
    
    private ButtonAdapter createSellingBuildingsButton() {
        return new ButtonAdapter(screen, "finish_building_button", new Vector2f((int)screen
                .getWidth() * 0.95f, 0), new  Vector2f(32, 32)){
            @Override
            public void onButtonMouseLeftUp(MouseButtonEvent evt, boolean isToggled) {
                sellBuildings();
            }
            
            @Override
            public void onKeyRelease(KeyInputEvent evt) {}
        };
    }
    
    private void addNewButton(Button button, String shortcutKey) {
        button.removeEffect(Effect.EffectEvent.Hover);
        button.removeEffect(Effect.EffectEvent.Press);
        button.setText(shortcutKey);
        button.setTextPosition(1, 30);
        screen.addElement(button);
    }
    
    private static void changeButtonsIcon(boolean black) {
        String color = black ? "black" : "white";
        screen.getElementById("finish_building_button")
                .setColorMap("Interface/hudIcons/selling_icon_" + color +".png");
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
