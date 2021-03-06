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
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import texts.Translator;
import tonegod.gui.controls.lists.Slider;
import tonegod.gui.controls.lists.Spinner;
import tonegod.gui.controls.text.TextField;
import tonegod.gui.controls.windows.Window;
import tonegod.gui.core.Element;
import tonegod.gui.core.Screen;

/**
 * Obiekt klasy <code>Shop</code> reprezentuje okienko sklepu. Zawiera ono dwa 
 * panele. Jeden umożliwiający kupowanie materiałów budowlanych a drugi rozwijanie 
 * żurawia. 
 * @author AleksanderSklorz
 */
public class Shop extends Menu implements VisibleFromAbove{
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
        BuildingSimulator game = BuildingSimulator.getBuildingSimulator();
        Screen screen = new Screen(game);
        setScreen(screen);
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
        game.getGuiNode().addControl(screen);
        setDisplayedShop();
        view = new BirdsEyeView(this, false);
        view.setMouseDisabled(true);
        if(settingCostCompleted) showPreview();
    }
    
    /**
     * Kupuje wybrane elementy. Zamyka automatycznie okno sklepu. 
     * @param evt
     * @param isToggled 
     */
    public void buy(MouseButtonEvent evt, boolean isToggled) {
        int cost = Integer.parseInt(getScreen().getElementById("cost_value_label").getText());
        if(cost <= GameManager.getUser().getPoints() || GameManager.isGodmode()) {
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
            goNextMenu(null);
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
        BuildingSimulator.getGameFlyByCamera().setDragToRotate(false);
        goNextMenu(null);
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
        Screen screen = getScreen(); 
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
     * Rozpoczyna obliczanie kosztów zakupów oraz przygotowanie podglądu. 
     * @param selectedIndex
     * @param value 
     */
    public void prepareOrder(int selectedIndex, Object value) {
        boolean settingCostCompleted = setCost(); 
        hidePreview(false); 
        if(settingCostCompleted) showPreview();
    }
    
    @Override
    public void unload() {
        Screen screen = getScreen();
        int amount = ((Spinner)screen.getElementById("amount_spinner")).getSelectedIndex();
        WallType type = (WallType)((Slider)screen.getElementById("type_slider"))
                .getSelectedValue();
        Vector3f dimensions = getWallDimensions();
        Vector3f tempDimensions = dimensions.clone(); 
        tempDimensions.multLocal(1, amount, 1);
        BuildingSimulator game = BuildingSimulator.getBuildingSimulator();
        for(int i = 0; i < amount; i++) {
            Wall wall = (Wall)WallsFactory.createWall(type, dischargingLocation,
                    dimensions, 0.00001f, false, (ColorRGBA)((Slider)screen
                .getElementById("color_slider")).getSelectedValue());
            game.getRootNode().attachChild(wall);
            dischargingLocation.y += 0.4f; 
        }
        view.setOff();
        displayedShop = null; 
        game.getBulletAppState().getPhysicsSpace().removeCollisionGroupListener(6);
    }
    
    /**
     * Oblicza koszt pojedynczej ściany. 
     * @return koszt ściany 
     */
    public static int calculateWallCost(float x, float z, WallType type) {
        return (int)x * (int)z + type.getPrice();
    }
    
    /**
     * Informuje czy panel sklepu jest widoczny. 
     * @return true jeśli jest widoczny, false w przeciwnym przypadku 
     */
    public boolean isShopPanelShowed() {
        return wallPreview != null;
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
     * Zwraca aktualnie wyświetlany obiekt sklepu. 
     * @return aktualny obiekt sklepu 
     */
    public static Shop getDisplayedShop() { return displayedShop; }
    
    /**
     * Zwraca aktualny kosz za zakupy. 
     * @return aktualny koszt za zakupy 
     */
    public int getCostForMaterials() { return costForMaterials; } 
    
    /**
     * Zeruje obiekt przechowujący aktualnie wyświetlony sklep. 
     */
    public static void removeDisplayedShop() { displayedShop = null; }
    
    /**
     * Zwraca widok z lotu ptaka, którego właścicielem jest sklep. 
     * @return widok z lotu ptaka 
     */
    public BirdsEyeView getView() { return view; }
    
    /**
     * Ustawia aktualną cenę za zakupy w odpowiedniej etykiecie. Jeśli podano 
     * błędne dane to wyświetlony jest stosowny komunikat oraz blokowany jest 
     * przycisk zakupów. 
     * return true jeśli udało się obliczyć koszt (podano poprawne dane) i da się kupić 
     * lub false w przeciwnym przypadku 
     */
    private boolean setCost() {
        Screen screen = getScreen(); 
        int cost = calculateCost(); 
        if(cost == -1) {
            hidePreview(true);
            screen.getElementById("cost_value_label").setText(Translator.BAD_DATA.getValue());
            screen.getElementById("buying_button").setIsEnabled(false);
            return false; 
        } else {
            screen.getElementById("cost_value_label").setText(cost + "");
            screen.getElementById("buying_button").setIsEnabled(cost 
                    <= GameManager.getUser().getPoints() || GameManager.isGodmode());
            return true;
        }
    }
    
    private void fillTypeSlider() {
        WallType[] types = WallType.values(); 
        Slider typeSlider = (Slider)getScreen().getElementById("type_slider");
        for(int i = 0; i < types.length; i++){
            typeSlider.addStepValue(types[i]);
        }
    }
    
    private void fillColorSelectBox() {
        ColorRGBA[] colors = new ColorRGBA[] {ColorRGBA.White, ColorRGBA.Black, 
            ColorRGBA.Blue, ColorRGBA.Brown, ColorRGBA.Cyan, ColorRGBA.DarkGray, 
            ColorRGBA.Green, ColorRGBA.LightGray, ColorRGBA.Magenta, ColorRGBA.Orange,
            ColorRGBA.Pink, ColorRGBA.Yellow};
        Slider colorSlider = (Slider)getScreen().getElementById("color_slider");
        for(int i = 0; i < colors.length; i++){
            colorSlider.addStepValue(colors[i]);
        }
    }
    
    private int calculateCost() {
        Screen screen = getScreen(); 
        Element xTextField = screen.getElementById("x_text_field"),
                zTextField = screen.getElementById("z_text_field");
        int result = -1;
        if(xTextField != null && zTextField != null) {
            String x = xTextField.getText(), z = zTextField.getText();
            if(isProperDimension(x) && isProperDimension(z)) { 
                result = ((Spinner)screen.getElementById("amount_spinner")).getSelectedIndex()
                        * calculateWallCost(Float.parseFloat(x), Float.parseFloat(z), 
                        (WallType)((Slider)screen.getElementById("type_slider"))
                        .getSelectedValue());
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
        Element panel = getScreen().getElementById("elements_panel");
        DimensionTextField textField = new DimensionTextField(this, id,
                new Vector2f(panel.getWidth() * x, panel.getHeight() * y));
        textField.setType(TextField.Type.NUMERIC);
        textField.setText("1");
        panel.addChild(textField);
    }
    
    private void buyCraneHeight() {
        Screen screen = getScreen(); 
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
        Screen screen = getScreen(); 
        float x = ((TextField)screen.getElementById("x_text_field")).parseFloat(),
                z = ((TextField)screen.getElementById("z_text_field")).parseFloat();
        return new Vector3f(x, 0.2f, z) ; 
    }
    
    private boolean isMaterialsBought() {
        return ((Spinner)getScreen().getElementById("amount_spinner")).getSelectedIndex() != 0;
    }
    
    private boolean isProperDimension(String x) {
        return x.matches("([1-5](\\.\\d+)?)|[6]");
    }
    
    private DummyWall createWallPreview() { 
        Vector3f dimensions = getWallDimensions();
        Screen screen = getScreen(); 
        return (DummyWall)WallsFactory.createWall((WallType)((Slider)screen
                .getElementById("type_slider")).getSelectedValue(),
                Vector3f.NAN, new Vector3f(dimensions.x * 0.05f, 0.02f,
                dimensions.z * 0.05f), 0, true, (ColorRGBA)((Slider)screen
                .getElementById("color_slider")).getSelectedValue());
    }
    
    private void showPreview() {
        wallPreview = createWallPreview();
        if(wallPreview != null) {
            BuildingSimulator game = BuildingSimulator.getBuildingSimulator();
            game.getRootNode().attachChild(wallPreview);
            Vector3f location = game.getCamera().getLocation().clone();
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
            previewAnimation = new Cinematic(BuildingSimulator.getGameRootNode(),
                    90, LoopMode.DontLoop);
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
    
    private void setDisplayedShop() { displayedShop = this; }
}
