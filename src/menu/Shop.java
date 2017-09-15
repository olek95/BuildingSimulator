package menu;

import building.Wall;
import building.WallType;
import building.WallsFactory;
import buildingsimulator.BirdsEyeView;
import buildingsimulator.BuildingSimulator;
import buildingsimulator.Control;
import buildingsimulator.GameManager;
import com.jme3.bullet.collision.PhysicsCollisionGroupListener;
import com.jme3.bullet.collision.PhysicsCollisionObject;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.font.BitmapFont;
import com.jme3.input.FlyByCamera;
import com.jme3.input.event.KeyInputEvent;
import com.jme3.input.event.MouseButtonEvent;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.scene.Spatial;
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
public class Shop extends Menu{
    private static Screen screen;
    private static Shop displayedShop = null; 
    boolean found = false; 
    public Shop(){
        screen = new Screen(BuildingSimulator.getBuildingSimulator());
        screen.parseLayout("Interface/shop.gui.xml", this);
        window = (Window)screen.getElementById("shop");
        window.setWindowTitle(Translator.SHOP.getValue());
        window.getDragBar().setTextAlign(BitmapFont.Align.Center);
        Translator.setTexts(new String[]{"buying_button", "cancellation_button",
            "type_label", "amount_label", "dimensions_label", "cost_label", "change_page_button", 
            "height_change_label", "actual_height_label", "new_height_label"},
                new Translator[]{Translator.BUYING, Translator.CANCELLATION, Translator.TYPE,
                    Translator.AMOUNT, Translator.DIMENSIONS, Translator.COST, Translator.NEXT, 
                    Translator.HEIGHT_CHANGE, Translator.ACTUAL_HEIGHT, Translator.NEW_HEIGHT}, screen);
        fillTypeSelectBox();
        createTextField("x_text_field", 0.35f, 0.55f);
        createTextField("z_text_field", 0.35f, 0.65f);
        screen.getElementById("vehicles_panel").hide();
        int craneHeight = ((Crane)GameManager.getUnit(1)).getHeightLevel();
        screen.getElementById("actual_height_value").setText(craneHeight + "");
        ((Spinner)screen.getElementById("crane_height_spinner")).setSelectedIndex(craneHeight);
        setCost();
        BuildingSimulator.getBuildingSimulator().getGuiNode().addControl(screen);
        displayedShop = this; 
    }
    
    /**
     * Kupuje wybrane elementy. 
     * @param evt
     * @param isToggled 
     */
    public void buy(MouseButtonEvent evt, boolean isToggled) {
        buyCraneHeight();
        BirdsEyeView.changeViewMode(this);
//        buyWalls();
        displayedShop = null; 
        goNextMenu(screen, null);
    }
    
    /**
     * Anuluje zakupy i wychodzi z okienka. 
     * @param evt
     * @param isToggled 
     */
    public void cancel(MouseButtonEvent evt, boolean isToggled) {
        displayedShop = null; 
        BuildingSimulator.getBuildingSimulator().getFlyByCamera().setDragToRotate(false);
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
    
    public void beginCalculateCost(int selectedIndex, Object value) {
        setCost(); 
    }
    
    public static void setCost() {
        int cost = calculateCost(); 
        if(cost == -1) {
            screen.getElementById("cost_value_label").setText(Translator.BAD_DATA.getValue());
            screen.getElementById("buying_button").setIsEnabled(false);
        } else {
            screen.getElementById("cost_value_label").setText(cost + "");
            screen.getElementById("buying_button").setIsEnabled(true);
        }
    }
    /**
     * Zwraca aktualnie wyświetlany obiekt sklepu. 
     * @return aktualny obiekt sklepu 
     */
    public static Shop getDisplayedShop() { return displayedShop; }
    
    private void fillTypeSelectBox() {
        WallType[] types = WallType.values(); 
        SelectBox typeSelectBox = (SelectBox)screen.getElementById("type_select_box");
        for(int i = 0; i < types.length; i++){
            typeSelectBox.addListItem(types[i].getTranslatedName(), types[i]);
        }
    }
    
    private static int calculateCost() {
        Element xTextField = screen.getElementById("x_text_field"),
                zTextField = screen.getElementById("z_text_field");
        int result = -1;
        if(xTextField != null && zTextField != null) {
            String x = xTextField.getText(), z = zTextField.getText();
            if(x.matches("\\d+(\\.\\d+)*") && z.matches("\\d+(\\.\\d+)*")) { 
                result = ((Spinner)screen.getElementById("amount_spinner")).getSelectedIndex()
                        * ((int)Float.parseFloat(x) * (int)Float.parseFloat(z)
                        + ((WallType)((SelectBox)screen.getElementById("type_select_box")).getSelectedListItem()
                        .getValue()).getPrice());
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
        DimensionTextField textField = new DimensionTextField(screen, id,
                new Vector2f(panel.getWidth() * x, panel.getHeight() * y));
        textField.setType(TextField.Type.NUMERIC);
        textField.setText("0");
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
        if(actualHeight != selectedHeight) {
            actualHeightLabel.setText(selectedHeight + "");
            setCost();
        }
    }
    
    public void buyWalls(Vector3f location, BirdsEyeView view) {
        int amount = ((Spinner)screen.getElementById("amount_spinner")).getSelectedIndex();
        WallType type = (WallType)((SelectBox)screen.getElementById("type_select_box"))
                .getSelectedListItem().getValue();
        float x = ((TextField)screen.getElementById("x_text_field")).parseFloat(),
                z = ((TextField)screen.getElementById("z_text_field")).parseFloat();
        if(x != 0 && z != 0) {
            Vector3f dimensions = new Vector3f(x, 0.2f, z);
            Vector3f tempDimensions = dimensions.clone(); 
            tempDimensions.multLocal(1, amount, 1);
            Wall tempWall = WallsFactory.createWall(type, location, tempDimensions);
            tempWall.getControl(RigidBodyControl.class).setCollisionGroup(6);
            for(int i = 0; i < amount; i++) {
                Wall wall = WallsFactory.createWall(type, location, dimensions);
                BuildingSimulator.getBuildingSimulator().getBulletAppState().getPhysicsSpace()
                        .addCollisionGroupListener(new PhysicsCollisionGroupListener(){

                    @Override
                    public boolean collide(PhysicsCollisionObject nodeA, PhysicsCollisionObject nodeB) {
                        String nameA = ((Spatial)nodeA.getUserObject()).getName(),
                                nameB = ((Spatial)nodeB.getUserObject()).getName();
                        if(!nameA.startsWith("terrain") && !nameB.startsWith("terrain") && !found) {
                            found = true;
                        }
                        return true;
                    }}, 6);
                if(found)
                GameManager.addToGame(wall);
                location.y += 0.4f;
            }
        }
        Control.removeListener(view);
        FlyByCamera camera = BuildingSimulator.getBuildingSimulator().getFlyByCamera();
        camera.setEnabled(true);
        camera.setDragToRotate(false);
    }
}
