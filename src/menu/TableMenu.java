package menu;

import buildingsimulator.BuildingSimulator;
import buildingsimulator.GameManager;
import com.jme3.input.event.MouseButtonEvent;
import com.jme3.math.Vector2f;
import texts.Translator;
import tonegod.gui.controls.buttons.ButtonAdapter;
import tonegod.gui.controls.lists.Table;
import tonegod.gui.controls.windows.Panel;
import tonegod.gui.controls.windows.Window;
import tonegod.gui.core.Screen;

public abstract class TableMenu extends Menu{
    private static Screen screen;
    private static Table table;
    public TableMenu(String id) {
        screen = new Screen(BuildingSimulator.getBuildingSimulator());
        window = new Window(screen, id, new Vector2f(0, 0),
                new Vector2f(screen.getWidth(), screen.getHeight()));
        screen.addElement(window);
        window.centerToParent();
        BuildingSimulator.getBuildingSimulator().getGuiNode().addControl(screen);
    }
    
    protected Table createTable(String[] ids, Translator[] labels) {
        float width = screen.getWidth();
        table = new Table(screen, new Vector2f(0, 0), new Vector2f(width,
                screen.getHeight())) {
            @Override
            public void onChange() {}
        };
        table.center();
        Table.TableColumn userColumn = new Table.TableColumn(table, screen, ids[0]),
                pointsColumn = new Table.TableColumn(table, screen, ids[1]); 
        userColumn.setText(labels[0].getValue());
        userColumn.setWidth(width / 2 + 100);
        table.addColumn(userColumn);
        pointsColumn.setText(labels[1].getValue());
        pointsColumn.setWidth(width / 2 - 100);
        table.addColumn(pointsColumn);
        addRows(); 
        return table; 
    }
    
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
    
    protected void addRow(String label1, Object value1, String label2) {
        Table.TableRow row = new Table.TableRow(screen, table);
        row.addCell(label1, value1);
        row.addCell(label2, label2);
        table.addRow(row);
    }
    
    protected abstract void clickReturnButton();
    
    protected abstract void addRows(); 
    
    protected Screen getScreen() { return screen; }
    
    protected Table getTable() { return table; }
}
