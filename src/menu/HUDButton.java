package menu;

import building.BuildingValidator;
import eyeview.BirdsEyeView;
import buildingsimulator.BuildingSimulator;
import buildingsimulator.GameManager;
import com.jme3.input.KeyInput;
import com.jme3.input.event.KeyInputEvent;
import com.jme3.input.event.MouseButtonEvent;
import com.jme3.math.Vector2f;
import texts.Translator;
import tonegod.gui.controls.buttons.ButtonAdapter;
import tonegod.gui.core.ElementManager;

/**
 * Obiekt klasy <code>HUDButton</code> reprezentuje przycisk HUDu. Różni się tym 
 * od normalnego przycisku, że nie jest uruchamiany za pomocą spacji. 
 * @author AleksanderSklorz
 */
public class HUDButton extends ButtonAdapter{
    private String buttonId;
    public HUDButton(ElementManager screen, String id, Vector2f position, Vector2f dimensions) {
        super(screen, id, position, dimensions);
        buttonId = id; 
    }
    
    /**
     * Wyświetla sklep, kończy budowlę lub wyświetla okienko umożliwiające oczyszczanie 
     * mapy, w zależności który przycisk został kliknięty. 
     * @param evt
     * @param isToggled 
     */
    @Override
    public void onButtonMouseLeftUp(MouseButtonEvent evt, boolean isToggled) {
        Shop shop = Shop.getDisplayedShop();
        CleaningDialogWindow cleaningWindow = CleaningDialogWindow
                .getDisplayedCleaningDialogWindow();
        if(cleaningWindow == null) {
            if(buttonId.equals("shop_button")) {
                if(shop != null) {
                    BirdsEyeView view = shop.getView(); 
                    if(view != null) {
                        view.setOff();
                        GameManager.getFlyByCamera().setDragToRotate(true);
                        MenuFactory.showMenu(MenuTypes.SHOP); 
                        HUD.setControlsVisibility(false);
                    }
                } else {
                    GameManager.getFlyByCamera().setDragToRotate(true);
                    MenuFactory.showMenu(MenuTypes.SHOP); 
                    HUD.setControlsVisibility(false);
                }
                GameManager.getActualUnit().getCamera().setOff();
            } else {
                if(shop == null) {
                    if(buttonId.equals("finish_building_button")) {
                        int points = BuildingValidator.validate();
                        GameManager.getUser().addPoints(points);
                        HUD.updatePoints();
                        HUD.setMessage(Translator.MESSAGE_POINTS.getValue()
                                .replace("x", points + ""));
                    } else {
                        BuildingSimulator.getBuildingSimulator().getFlyByCamera()
                                .setDragToRotate(true);
                        MenuFactory.showMenu(MenuTypes.CLEANING_DIALOG_WINDOW);
                        HUD.setControlsVisibility(false);
                    }
                }
            }
        }
    }
    
    /**
     * Wyłacza domyślne uaktywnienie przycisku za pomocą klawisza spacji. 
     * @param evt 
     */
    @Override
    public void onKeyRelease(KeyInputEvent evt) {
        if (evt.getKeyCode() == KeyInput.KEY_SPACE) {}
    }
}
