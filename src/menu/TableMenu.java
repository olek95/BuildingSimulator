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
    private static Screen screen;
    private static Table table;
    public TableMenu(String id) {
        BuildingSimulator game = BuildingSimulator.getBuildingSimulator(); 
        screen = new Screen(game);
        Window window = new Window(screen, id, new Vector2f(0, 0),
                new Vector2f(screen.getWidth(), screen.getHeight()));
        setWindow(window); 
        screen.addElement(window);
        window.centerToParent();
        game.getGuiNode().addControl(screen);
    }
    
    /**
     * Tworzy tabelkę.
     * @param ids id kolumn 
     * @param labels nagłówki kolumn 
     * @return stworzona tabelka
     */
    protected Table createTable(String[] ids, Translator[] labels, int... margins) {
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
        Table.TableRow row = new Table.TableRow(screen, table);
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
     * Zwraca ekran.
     * @return ekran 
     */
    protected Screen getScreen() { return screen; }
    
    /**
     * Zwraca tabelkę
     * @return tabelka
     */
    protected Table getTable() { return table; }
}
