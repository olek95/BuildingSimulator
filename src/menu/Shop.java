package menu;

import building.Wall;
import building.WallType;
import building.WallsFactory;
import eyeview.BirdsEyeView;
import buildingsimulator.BuildingSimulator;
import listeners.DummyCollisionListener;
import buildingsimulator.GameManager;
import eyeview.VisibleFromAbove;
import com.jme3.audio.AudioNode;
import com.jme3.font.BitmapFont;
import com.jme3.input.FlyByCamera;
import com.jme3.input.event.MouseButtonEvent;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import cranes.crane.Crane;
import texts.Translator;
import tonegod.gui.controls.lists.SelectBox;
import tonegod.gui.controls.lists.Spinner;
import tonegod.gui.controls.text.TextField;
import tonegod.gui.controls.windows.Window;
import tonegod.gui.core.Element;
import tonegod.gui.core.Screen;

/**
 * Obiekt klasy <code>Shop</code> reprezentuje okienko sklepu. Zawiera ono dwa 
 * panele. Jeden umożliwiający kupowanie materiałów budowlanych a drugi rozwijanie 
 * popjazdów. 
 * @author AleksanderSklorz
 */
public class Shop extends Menu implements VisibleFromAbove{
    private static Screen screen;
    private static Shop displayedShop = null; 
    private int costForMaterials;
    private Vector3f dischargingLocation = null;
    private DummyCollisionListener listener; 
    private BirdsEyeView view; 
    private static AudioNode dropSound;
    public Shop(){
        if(displayedShop != null) { 
            GameManager.getUser().addPoints(displayedShop.costForMaterials); 
            HUD.updatePoints();
        }
        screen = new Screen(BuildingSimulator.getBuildingSimulator());
        screen.parseLayout("Interface/shop.gui.xml", this);
        Window window = (Window)screen.getElementById("shop");
        window.setWindowTitle(Translator.SHOP.getValue());
        window.getDragBar().setTextAlign(BitmapFont.Align.Center);
        setWindow(window); 
        Translator.setTexts(new String[]{"buying_button", "cancellation_button",
            "type_label", "amount_label", "dimensions_label", "cost_label",
            "change_page_button", "height_change_label", "actual_height_label",
            "new_height_label"}, new Translator[]{Translator.BUYING, 
                Translator.CANCELLATION, Translator.TYPE, Translator.AMOUNT,
                Translator.DIMENSIONS, Translator.COST, Translator.NEXT, 
                Translator.HEIGHT_CHANGE, Translator.ACTUAL_HEIGHT, Translator.NEW_HEIGHT}, screen);
        fillTypeSelectBox();
        createTextField("x_text_field", 0.35f, 0.55f);
        createTextField("z_text_field", 0.35f, 0.65f);
        screen.getElementById("vehicles_panel").hide();
        int craneHeight = ((Crane)GameManager.getUnit(1)).getHeightLevel();
        screen.getElementById("actual_height_value").setText(craneHeight + "");
        ((Spinner)screen.getElementById("crane_height_spinner"))
                .setSelectedIndex(craneHeight);
        dropSound = GameManager.createSound("Sounds/drop.wav", GameManager.getGameSoundVolume(),
                false, null);
        setCost();
        BuildingSimulator.getBuildingSimulator().getGuiNode().addControl(screen);
        displayedShop = this; 
    }
    
    /**
     * Kupuje wybrane elementy. Zamyka automatycznie okno sklepu i włącza tryb 
     * widoku z lotu ptaka. 
     * @param evt
     * @param isToggled 
     */
    public void buy(MouseButtonEvent evt, boolean isToggled) {
        int cost = Integer.parseInt(screen.getElementById("cost_value_label").getText());
        if(cost <= GameManager.getUser().getPoints()) {
            buyCraneHeight();
            GameManager.getUser().addPoints(-cost);
            HUD.updatePoints();
            if(isMaterialsBought()) view = new BirdsEyeView(this); 
            else {
                displayedShop = null;
                FlyByCamera camera = BuildingSimulator.getBuildingSimulator().getFlyByCamera();
                camera.setDragToRotate(false);
            }
            goNextMenu(screen, null);
        }
    }
    
    /**
     * Anuluje zakupy i wychodzi z okienka. 
     * @param evt
     * @param isToggled 
     */
    public void cancel(MouseButtonEvent evt, boolean isToggled) {
        displayedShop = null; 
        BuildingSimulator.getBuildingSimulator().getFlyByCamera().setDragToRotate(false);
        detachDropSound();
        goNextMenu(screen, null);
    }
    
    /**
     * Przełącza sie między panelem z kupowaniem elementów budowlanych i panelem 
     * umożliwiającym edycję pojazdów. 
     * @param evt
     * @param isToggled 
     */
    public void changePage(MouseButtonEvent evt, boolean isToggled) {
        Element elementsPanel = screen.getElementById("elements_panel");
        if(elementsPanel.getIsVisible()) {
            screen.getElementById("change_page_button").setText(Translator.PREVIOUS.getValue());
            elementsPanel.hide();
            screen.getElementById("vehicles_panel").show();
        } else {
            screen.getElementById("change_page_button").setText(Translator.NEXT.getValue());
            screen.getElementById("vehicles_panel").hide();
            elementsPanel.show();
        }
    }
    
    /**
     * Rozpoczyna obliczanie kosztów zakupów. 
     * @param selectedIndex
     * @param value 
     */
    public void beginCalculateCost(int selectedIndex, Object value) {
        setCost(); 
    }
    
    /**
     * Ustawia aktualną cenę za zakupy w odpowiedniej etykiecie. Jeśli podano 
     * błędne dane to wyświetlony jest stosowny komunikat oraz blokowany jest 
     * przycisk zakupów. 
     */
    public void setCost() {
        int cost = calculateCost(); 
        if(cost == -1) {
            screen.getElementById("cost_value_label").setText(Translator.BAD_DATA.getValue());
            screen.getElementById("buying_button").setIsEnabled(false);
        } else {
            screen.getElementById("cost_value_label").setText(cost + "");
            screen.getElementById("buying_button").setIsEnabled(true);
        }
    }
    
    @Override
    public void unload() {
        int amount = ((Spinner)screen.getElementById("amount_spinner")).getSelectedIndex();
        WallType type = (WallType)((SelectBox)screen.getElementById("type_select_box"))
                .getSelectedListItem().getValue();
        Vector3f dimensions = getWallDimensions();
        Vector3f tempDimensions = dimensions.clone(); 
        tempDimensions.multLocal(1, amount, 1);
        for(int i = 0; i < amount; i++) {
            Wall wall = WallsFactory.createWall(type, dischargingLocation,
                    dimensions);
            GameManager.addToGame(wall);
            dischargingLocation.y += 0.4f; 
        }
        dropSound.play();
        view.setOff();
        displayedShop = null; 
        BuildingSimulator.getBuildingSimulator().getBulletAppState()
                .getPhysicsSpace().removeCollisionGroupListener(6);
    }
    
    
    @Override
    public void setListener(DummyCollisionListener listener) {
        this.listener = listener; 
        if(listener != null) 
            listener.createDummyWall(dischargingLocation, getWallDimensions());
    }
    
    /**
     * Zwraca wymiary kupionej ściany przy następujących założeniach: długość x 
     * jest dłuższą długością od z. Jeśli będzie odwrotnie, to odwraca rozmiar 
     * podczas tworzenia sciany. 
     * @return wymiary kupionej ściany
     */
    public Vector3f getWallDimensions() {
        float x = ((TextField)screen.getElementById("x_text_field")).parseFloat(),
                z = ((TextField)screen.getElementById("z_text_field")).parseFloat();
        return x >= z ? new Vector3f(x, 0.2f, z) : new Vector3f(z, 0.2f, x); 
    }
    
    /**
     * Odłącza dźwięk spadającego obiektu. 
     */
    public static void detachDropSound() {
        if(dropSound != null) GameManager.stopSound(dropSound, true);
    }
    
    @Override
    public DummyCollisionListener getListener() { return listener; }
    
    @Override
    public void setDischargingLocation(Vector3f location) { 
        dischargingLocation = location; 
    }
    
    /**
     * Zwraca ekran sklepu. 
     * @return ekran 
     */
    public Screen getScreen() { return screen; }
    
    /**
     * Zwraca aktualnie wyświetlany obiekt sklepu. 
     * @return aktualny obiekt sklepu 
     */
    public static Shop getDisplayedShop() { return displayedShop; }
    
    /**
     * Zwraca aktualny kosz za zakupy. 
     * @return aktualny koszt za zakupy 
     */
    public int  getCostForMaterials() { return costForMaterials; } 
    
    /**
     * Zeruje obiekt przechowujący aktualnie wyświetlony sklep. 
     */
    public static void removeDisplayedShop() { displayedShop = null; }
    
    /**
     * Zwraca widok z lotu ptaka, którego właścicielem jest sklep. 
     * @return widok z lotu ptaka 
     */
    public BirdsEyeView getView() { return view; }
    
    private void fillTypeSelectBox() {
        WallType[] types = WallType.values(); 
        SelectBox typeSelectBox = (SelectBox)screen.getElementById("type_select_box");
        for(int i = 0; i < types.length; i++){
            typeSelectBox.addListItem(types[i].getTranslatedName(), types[i]);
        }
    }
    
    private int calculateCost() {
        Element xTextField = screen.getElementById("x_text_field"),
                zTextField = screen.getElementById("z_text_field");
        int result = -1;
        if(xTextField != null && zTextField != null) {
            String x = xTextField.getText(), z = zTextField.getText();
            if(isProperDimension(x) && isProperDimension(z)) { 
                result = ((Spinner)screen.getElementById("amount_spinner")).getSelectedIndex()
                        * ((int)Float.parseFloat(x) * (int)Float.parseFloat(z)
                        + ((WallType)((SelectBox)screen.getElementById("type_select_box")).getSelectedListItem()
                        .getValue()).getPrice());
                costForMaterials = result; 
                int selectedHeight = ((Spinner)screen.getElementById("crane_height_spinner"))
                        .getSelectedIndex(), actualHeight = Integer.parseInt(screen
                        .getElementById("actual_height_value").getText());
                if(actualHeight < selectedHeight) {
                    result += selectedHeight * 100; 
                } else {
                    result += (actualHeight - selectedHeight) * 100;
                }
            }
        }
        return result; 
    }
    
    private void createTextField(String id, float x, float y) {
        Element panel = screen.getElementById("elements_panel");
        DimensionTextField textField = new DimensionTextField(this, id,
                new Vector2f(panel.getWidth() * x, panel.getHeight() * y));
        textField.setType(TextField.Type.NUMERIC);
        textField.setText("1");
        panel.addChild(textField);
    }
    
    private void buyCraneHeight() {
        Element actualHeightLabel = screen.getElementById("actual_height_value");
        int selectedHeight = ((Spinner)screen.getElementById("crane_height_spinner"))
                .getSelectedIndex(), actualHeight = Integer
                .parseInt(actualHeightLabel.getText());
        if(actualHeight < selectedHeight) {
            ((Crane)GameManager.getUnit(1)).raiseHeight(selectedHeight);
        } else {
            if(actualHeight > selectedHeight) 
                ((Crane)GameManager.getUnit(1)).decreaseHeight(selectedHeight);
        }
    }
    
    private boolean isMaterialsBought() {
        return ((Spinner)screen.getElementById("amount_spinner")).getSelectedIndex() != 0;
    }
    
    private boolean isProperDimension(String x) {
        return x.matches("([1-5](\\.\\d+)?)|[6]");
    }
}
