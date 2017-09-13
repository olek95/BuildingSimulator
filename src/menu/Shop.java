package menu;

import building.WallType;
import buildingsimulator.BuildingSimulator;
import buildingsimulator.GameManager;
import com.jme3.font.BitmapFont;
import com.jme3.input.event.MouseButtonEvent;
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
        screen.getElementById("vehicles_panel").hide();
        screen.getElementById("actual_height_value").setText(((Crane)GameManager
                .getUnit(1)).getHeightLevel() + "");
        BuildingSimulator.getBuildingSimulator().getGuiNode().addControl(screen);
        displayedShop = this; 
    }
    
    /**
     * Kupuje wybrane elementy. 
     * @param evt
     * @param isToggled 
     */
    public void buy(MouseButtonEvent evt, boolean isToggled) {
        System.out.println(calculateCost());
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
    
    private int calculateCost() {
        int result = ((Spinner)screen.getElementById("amount_spinner")).getSelectedIndex()
                * (int)((TextField)screen.getElementById("x_text_field")).parseFloat()
                * (int)((TextField)screen.getElementById("y_text_field")).parseFloat()
                + ((WallType)((SelectBox)screen.getElementById("type_select_box")).getSelectedListItem()
                .getValue()).getPrice();
        return result; 
    }
}
