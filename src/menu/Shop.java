package menu;

import buildingsimulator.BuildingSimulator;
import com.jme3.font.BitmapFont;
import com.jme3.input.event.MouseButtonEvent;
import texts.Translator;
import tonegod.gui.controls.lists.SelectBox;
import tonegod.gui.controls.windows.Window;
import tonegod.gui.core.Element;
import tonegod.gui.core.Screen;

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
            "type_label", "amount_label", "dimensions_label", "cost_label", "change_page_button"},
                new Translator[]{Translator.BUYING, Translator.CANCELLATION, Translator.TYPE,
                    Translator.AMOUNT, Translator.DIMENSIONS, Translator.COST, Translator.NEXT}, screen);
        fillTypeSelectBox();
        BuildingSimulator.getBuildingSimulator().getGuiNode().addControl(screen);
        displayedShop = this; 
    }
    
    public void buy(MouseButtonEvent evt, boolean isToggled) {
        
    }
    
    public void cancel(MouseButtonEvent evt, boolean isToggled) {
        displayedShop = null; 
        BuildingSimulator.getBuildingSimulator().getFlyByCamera().setDragToRotate(false);
        goNextMenu(screen, null);
    }
    
    public void changePage(MouseButtonEvent evt, boolean isToggled) {
        Element elementsPanel = screen.getElementById("elements_panel");
        if(elementsPanel.getIsVisible()) {
            screen.getElementById("change_page_button").setText(Translator.PREVIOUS.getValue());
            elementsPanel.hide();
        } else {
            screen.getElementById("change_page_button").setText(Translator.NEXT.getValue());
            elementsPanel.show();
        }
    }
    
    public static Shop getDisplayedShop() { return displayedShop; }
    
    private void fillTypeSelectBox() {
        Translator[] values = {Translator.BLANK_WALL, Translator.WINDOWS,
            Translator.ONE_BIG_WINDOW, Translator.ONE_BIGGER_WINDOW, Translator.DOOR};
        SelectBox typeSelectBox = (SelectBox)screen.getElementById("type_select_box");
        for(int i = 0; i < values.length; i++) {
            typeSelectBox.addListItem(values[i].getValue(), values[i]);
        }
    }
}
