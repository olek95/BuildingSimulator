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
        screen = new Screen(BuildingSimulator.getBuildingSimulator());
        Window window = new Window(screen, id, new Vector2f(0, 0),
                new Vector2f(screen.getWidth(), screen.getHeight()));
        setWindow(window); 
        screen.addElement(window);
        window.centerToParent();
        BuildingSimulator.getBuildingSimulator().getGuiNode().addControl(screen);
    }
    
    /**
     * Tworzy tabelkę.
     * @param ids id kolumn 
     * @param labels nagłówki kolumn 
     * @return 
     */
    protected Table createTable(String[] ids, Translator[] labels) {
        float width = screen.getWidth();
        table = new Table(screen, new Vector2f(0, 0), new Vector2f(width,
                screen.getHeight())) {
            @Override
            public void onChange() {}
        };
        table.center();
        Table.TableColumn firstColumn = new Table.TableColumn(table, screen, ids[0]),
                secondColumn = new Table.TableColumn(table, screen, ids[1]); 
        firstColumn.setText(labels[0].getValue());
        firstColumn.setWidth(width / 2 + 100);
        firstColumn.setIgnoreMouseLeftButton(true);
        table.addColumn(firstColumn);
        secondColumn.setText(labels[1].getValue());
        secondColumn.setWidth(width / 2 - 100);
        secondColumn.setIgnoreMouseLeftButton(true);
        table.addColumn(secondColumn);
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
     * Dodaje wiersz do tabelki. Etykiety są wyświetlane. 
     * @param label1 etykieta pierwszej komórki 
     * @param value1 wartość pierwszej komówki 
     * @param label2 etykieta drugiej komórki 
     */
    protected void addRow(String label1, Object value1, String label2) {
        Table.TableRow row = new Table.TableRow(screen, table);
        row.addCell(label1, value1);
        row.addCell(label2, label2);
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
