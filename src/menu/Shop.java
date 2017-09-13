package menu;

import buildingsimulator.BuildingSimulator;
import com.jme3.input.event.MouseButtonEvent;
import texts.Translator;
import tonegod.gui.controls.lists.SelectBox;
import tonegod.gui.controls.lists.Spinner;
import tonegod.gui.controls.text.TextField;
import tonegod.gui.controls.windows.Window;
import tonegod.gui.core.Element;
import tonegod.gui.core.Screen;

public class Shop extends Menu{
    private static Screen screen;
    public Shop(){
        screen = new Screen(BuildingSimulator.getBuildingSimulator());
        screen.parseLayout("Interface/shop.gui.xml", this);
        window = (Window)screen.getElementById("options");
        Translator.setTexts(new String[]{"buying_button", "cancellation_button",
            "type_label", "amount_label", "dimensions_label"}, new Translator[]{Translator.BUYING, Translator.CANCELLATION,
                Translator.TYPE, Translator.AMOUNT, Translator.DIMENSIONS}, screen);
        fillTypeSelectBox();
        TextField f = (TextField)screen.getElementById("x_text_field");
//        f.setType(TextField.Type.NUMERIC);
        BuildingSimulator.getBuildingSimulator().getGuiNode().addControl(screen);
    }
    
    public void buy(MouseButtonEvent evt, boolean isToggled) {
        
    }
    
    public void cancel(MouseButtonEvent evt, boolean isToggled) {
        
    }
    
    private void fillTypeSelectBox() {
        Translator[] values = {Translator.BLANK_WALL, Translator.WINDOWS,
            Translator.ONE_BIG_WINDOW, Translator.ONE_BIGGER_WINDOW, Translator.DOOR};
        SelectBox typeSelectBox = (SelectBox)screen.getElementById("type_select_box");
        for(int i = 0; i < values.length; i++) {
            typeSelectBox.addListItem(values[i].getValue(), values[i]);
        }
    }
}
