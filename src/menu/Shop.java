package menu;

import building.DummyWall;
import building.Wall;
import building.WallType;
import building.WallsFactory;
import eyeview.BirdsEyeView;
import buildingsimulator.BuildingSimulator;
import listeners.DummyCollisionListener;
import buildingsimulator.GameManager;
import com.jme3.animation.LoopMode;
import com.jme3.cinematic.Cinematic;
import com.jme3.cinematic.events.AbstractCinematicEvent;
import eyeview.VisibleFromAbove;
import com.jme3.font.BitmapFont;
import com.jme3.input.event.MouseButtonEvent;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import texts.Translator;
import tonegod.gui.controls.lists.SelectBox;
import tonegod.gui.controls.lists.Slider;
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
    private DummyWall wallPreview;
    private boolean previewAnimationStop = true;
    private Cinematic previewAnimation;
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
            "new_height_label", "color_label"}, new Translator[]{Translator.BUYING, 
                Translator.CANCELLATION, Translator.TYPE, Translator.AMOUNT,
                Translator.DIMENSIONS, Translator.COST, Translator.NEXT, 
                Translator.HEIGHT_CHANGE, Translator.ACTUAL_HEIGHT, Translator.NEW_HEIGHT,
                Translator.COLOR}, screen);
        fillTypeSlider(); 
        fillColorSelectBox();
        createTextField("x_text_field", 0.45f, 0.56f);
        createTextField("z_text_field", 0.45f, 0.68f);
        screen.getElementById("vehicles_panel").hide();
        int craneHeight = GameManager.getCrane().getHeightLevel();
        screen.getElementById("actual_height_value").setText(craneHeight + "");
        ((Spinner)screen.getElementById("crane_height_spinner"))
                .setSelectedIndex(craneHeight);
        boolean settingCostCompleted = setCost();
        BuildingSimulator.getBuildingSimulator().getGuiNode().addControl(screen);
        displayedShop = this; 
        view = new BirdsEyeView(this, false);
        view.setMouseDisabled(true);
        if(settingCostCompleted) showPreview();
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
            if(!isMaterialsBought())  {
                displayedShop = null;
                view.setOff();
            } else {
                view.setMouseDisabled(false);
                BirdsEyeView.displayNotMovingModeHUD();
            }
            goNextMenu(screen, null);
        }
        hidePreview(true);
        HUD.setControlsVisibility(HUD.isControlsLabelVisibilityBeforeHiding());
        HUD.setGeneralControlsLabelVisibility(true);
    }
    
    /**
     * Anuluje zakupy i wychodzi z okienka. 
     * @param evt
     * @param isToggled 
     */
    public void cancel(MouseButtonEvent evt, boolean isToggled) {
        displayedShop = null; 
        hidePreview(true);
        view.setOff();
        BuildingSimulator.getBuildingSimulator().getFlyByCamera().setDragToRotate(false);
        goNextMenu(screen, null);
        HUD.setControlsVisibility(HUD.isControlsLabelVisibilityBeforeHiding());
        HUD.setGeneralControlsLabelVisibility(true);
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
    public void prepareOrder(int selectedIndex, Object value) {
        boolean settingCostCompleted = setCost(); 
        hidePreview(false); 
        if(settingCostCompleted) showPreview();
    }
    
    /**
     * Ustawia aktualną cenę za zakupy w odpowiedniej etykiecie. Jeśli podano 
     * błędne dane to wyświetlony jest stosowny komunikat oraz blokowany jest 
     * przycisk zakupów. 
     */
    public boolean setCost() {
        int cost = calculateCost(); 
        if(cost == -1) {
            hidePreview(true);
            screen.getElementById("cost_value_label").setText(Translator.BAD_DATA.getValue());
            screen.getElementById("buying_button").setIsEnabled(false);
            return false; 
        } else {
            screen.getElementById("cost_value_label").setText(cost + "");
            screen.getElementById("buying_button").setIsEnabled(true);
            return true;
        }
    }
    
    @Override
    public void unload() {
        int amount = ((Spinner)screen.getElementById("amount_spinner")).getSelectedIndex();
        WallType type = (WallType)((Slider)screen.getElementById("type_slider"))
                .getSelectedValue();
        Vector3f dimensions = getWallDimensions();
        Vector3f tempDimensions = dimensions.clone(); 
        tempDimensions.multLocal(1, amount, 1);
        for(int i = 0; i < amount; i++) {
            Wall wall = (Wall)WallsFactory.createWall(type, dischargingLocation,
                    dimensions, 0.00001f, false, (ColorRGBA)((Slider)screen
                .getElementById("color_slider")).getSelectedValue());
            GameManager.addToScene(wall);
            dischargingLocation.y += 0.4f; 
        }
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
    
    private void fillTypeSlider() {
        WallType[] types = WallType.values(); 
        Slider typeSlider = (Slider)screen.getElementById("type_slider");
        for(int i = 0; i < types.length; i++){
            typeSlider.addStepValue(types[i]);
        }
    }
    
    private void fillColorSelectBox() {
        ColorRGBA[] colors = new ColorRGBA[] {ColorRGBA.Black, ColorRGBA.BlackNoAlpha, 
            ColorRGBA.Blue, ColorRGBA.Brown, ColorRGBA.Cyan, ColorRGBA.DarkGray, 
            ColorRGBA.Green, ColorRGBA.LightGray, ColorRGBA.Magenta, ColorRGBA.Orange,
            ColorRGBA.Pink, ColorRGBA.Yellow};
        Slider colorSlider = (Slider)screen.getElementById("color_slider");
        for(int i = 0; i < colors.length; i++){
            colorSlider.addStepValue(colors[i]);
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
                        + ((WallType)((Slider)screen.getElementById("type_slider"))
                        .getSelectedValue()).getPrice());
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
            GameManager.getCrane().raiseHeight(selectedHeight);
        } else {
            if(actualHeight > selectedHeight) 
                GameManager.getCrane().decreaseHeight(selectedHeight);
        }
    }
    
    private Vector3f getWallDimensions() {
        float x = ((TextField)screen.getElementById("x_text_field")).parseFloat(),
                z = ((TextField)screen.getElementById("z_text_field")).parseFloat();
        return new Vector3f(x, 0.2f, z) ; 
    }
    
    private boolean isMaterialsBought() {
        return ((Spinner)screen.getElementById("amount_spinner")).getSelectedIndex() != 0;
    }
    
    private boolean isProperDimension(String x) {
        return x.matches("([1-5](\\.\\d+)?)|[6]");
    }
    
    private DummyWall createWallPreview() { 
        Vector3f dimensions = getWallDimensions();
        return (DummyWall)WallsFactory.createWall((WallType)((Slider)screen
                .getElementById("type_slider")).getSelectedValue(),
                Vector3f.NAN, new Vector3f(dimensions.x * 0.05f, 0.02f,
                dimensions.z * 0.05f), 0, true, (ColorRGBA)((Slider)screen
                .getElementById("color_slider")).getSelectedValue());
    }
    
    private void showPreview() {
        wallPreview = createWallPreview();
        if(wallPreview != null) {
            GameManager.addToScene(wallPreview);
            Vector3f location = GameManager.getCamera().getLocation().clone();
            wallPreview.setLocalTranslation(location.add(0, -1.7f, 0.35f));
            wallPreview.setOffPhysics();
            startPreviewAnimation();
        }
    }
    
    private void hidePreview(boolean stopAnimation) {
        if(wallPreview != null) wallPreview.removeFromParent();
        wallPreview = null;
        if(stopAnimation) {
            previewAnimationStop = true; 
            if(previewAnimation != null) previewAnimation.stop();
            previewAnimation = null;
        }
    }
    private void startPreviewAnimation() {
        if(previewAnimationStop) {
            previewAnimation = new Cinematic(BuildingSimulator.getBuildingSimulator()
                    .getRootNode(), 90, LoopMode.DontLoop);
            previewAnimation.addCinematicEvent(0, new AbstractCinematicEvent() {
                @Override
                protected void onPlay() {
                    previewAnimationStop = false;
                }

                @Override
                protected void onUpdate(float tpf) {
                    wallPreview.rotate(0, 0, tpf * 2f);
                }

                @Override
                protected void onStop() {
                    if(!previewAnimationStop) {
                        previewAnimationStop = true;
                        startPreviewAnimation();
                    }
                }

                @Override
                public void onPause() {}

            });
            GameManager.startAnimation(previewAnimation); 
        }
    }
}
