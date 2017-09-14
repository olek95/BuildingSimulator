package menu;

import building.WallType;
import buildingsimulator.BuildingSimulator;
import buildingsimulator.GameManager;
import com.jme3.font.BitmapFont;
import com.jme3.input.event.KeyInputEvent;
import com.jme3.input.event.MouseButtonEvent;
import com.jme3.math.Vector2f;
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
        createTextField("y_text_field", 0.35f, 0.65f);
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
        int selectedHeight = ((Spinner)screen.getElementById("crane_height_spinner"))
                        .getSelectedIndex();
        Element actualHeightLabel = screen.getElementById("actual_height_value");
        if(Integer.parseInt(actualHeightLabel.getText()) != selectedHeight) {
            ((Crane)GameManager.getUnit(1)).raiseHeight(selectedHeight);
            actualHeightLabel.setText(selectedHeight + "");
            setCost();
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
                yTextField = screen.getElementById("y_text_field");
        int result = -1;
        if(xTextField != null && yTextField != null) {
            String x = xTextField.getText(), y = yTextField.getText();
            if(x.matches("\\d+(\\.\\d+)*") && y.matches("\\d+(\\.\\d+)*")) { 
                result = ((Spinner)screen.getElementById("amount_spinner")).getSelectedIndex()
                        * ((int)Float.parseFloat(x) * (int)Float.parseFloat(y)
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
}
