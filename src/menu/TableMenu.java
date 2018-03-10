package menu;

import buildingsimulator.BuildingSimulator;
import com.jme3.input.event.MouseButtonEvent;
import com.jme3.math.Vector2f;
import texts.Translator;
import tonegod.gui.controls.buttons.ButtonAdapter;
import tonegod.gui.controls.lists.Table;
import tonegod.gui.controls.windows.Window;
import tonegod.gui.core.Screen;

/**
 * Klasa <code>TableMenu</code> reprezentuje nadklasę wszystkich tabelkowych 
 * okienek. 
 * @author AleksanderSklorz
 */
public abstract class TableMenu extends Menu{
    private static Table table;
    public TableMenu(String id) {
        BuildingSimulator game = BuildingSimulator.getBuildingSimulator(); 
        Screen menuScreen = new Screen(game); 
        setScreen(menuScreen);
        Window window = new Window(menuScreen, id, new Vector2f(0, 0),
                new Vector2f(menuScreen.getWidth(), menuScreen.getHeight()));
        setWindow(window); 
        menuScreen.addElement(window);
        window.centerToParent();
        game.getGuiNode().addControl(menuScreen);
    }
    
    /**
     * Tworzy tabelkę.
     * @param ids id kolumn 
     * @param labels nagłówki kolumn 
     * @return stworzona tabelka
     */
    protected Table createTable(String[] ids, Translator[] labels, int... margins) {
        Screen screen = getScreen(); 
        float width = screen.getWidth();
        table = new Table(screen, new Vector2f(0, 0), new Vector2f(width,
                screen.getHeight() * 0.87f)) {
            @Override
            public void onChange() {}
        };
        table.center();
        for(int i = 0; i < ids.length; i++) {
            Table.TableColumn column = new Table.TableColumn(table, screen, ids[i]);
            column.setText(labels[i].getValue());
            column.setWidth(width / ids.length + (margins.length != 0 ? margins[i] : 0));
            column.setIgnoreMouseLeftButton(true);
            table.addColumn(column);
        }
        addRows(); 
        return table; 
    }
    
    /**
     * Tworzy przycisk powrotu do poprzedniego okna. 
     * @param x położenie poziome przycisku 
     */
    protected void createReturnButton(float x){
        Screen screen = getScreen(); 
        ButtonAdapter button = new ButtonAdapter(screen, "return_button", 
                new Vector2f(x, screen.getHeight() * 0.9f),
                new Vector2f(100, 30)) {
                    @Override
                    public void onButtonMouseLeftUp(MouseButtonEvent mbe, boolean bln) {
                        clickReturnButton(); 
                    }
                };
        table.addChild(button);
    }
    
    /**
     * Dodaje wiersz do tabelki. 
     * @param columnLabels tablica wyświetlanych etykiet  
     * @param values tablica wartości jakie posiada dana komórka 
     */
    protected void addRow(String[] columnLabels, Object... values) {
        Table.TableRow row = new Table.TableRow(getScreen(), table);
        for(int i = 0; i < values.length; i++) {
            row.addCell(columnLabels[i], values[i]);
        }
        table.addRow(row);
    }
    
    /**
     * Wykonuje się po kliknięciu przycisku powrotu. 
     */
    protected abstract void clickReturnButton();
    
    /**
     * Dodaje wszystkie wiersze do tabelki. 
     */
    protected abstract void addRows(); 
    
    /**
     * Zwraca tabelkę
     * @return tabelka
     */
    protected Table getTable() { return table; }
}
