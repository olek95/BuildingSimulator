package menu;

import buildingsimulator.BuildingSimulator;
import buildingsimulator.Control;
import com.jme3.input.InputManager;
import com.jme3.input.RawInputListener;
import com.jme3.input.event.JoyAxisEvent;
import com.jme3.input.event.JoyButtonEvent;
import com.jme3.input.event.KeyInputEvent;
import com.jme3.input.event.MouseButtonEvent;
import com.jme3.input.event.MouseMotionEvent;
import com.jme3.input.event.TouchEvent;
import com.jme3.math.Vector2f;
import tonegod.gui.controls.lists.Table;
import tonegod.gui.controls.lists.Table.TableColumn;
import tonegod.gui.controls.lists.Table.TableRow;
import tonegod.gui.controls.windows.Window;
import tonegod.gui.core.Screen;

public class ControlConfigurationMenu extends Menu implements RawInputListener{
    private static Screen screen;
    public ControlConfigurationMenu(){
        screen = new Screen(BuildingSimulator.getBuildingSimulator());
        window = new Window(screen, "controlConfiguration", new Vector2f(0, 0),
                new Vector2f(screen.getWidth(), screen.getHeight()));
        screen.addElement(window);
        window.centerToParent();
//        Table table = new Table(screen, new Vector2f(0, 0), new Vector2f(screen.getWidth(), screen.getHeight())) {
//            @Override
//            public void onChange() {
//                ((TableCell)this.getSelectedRows().get(0).getChild(2)).setText("asd");
//            }
//        };
//        table.center();
//        table.addColumn("Akcja");
//        table.addColumn("Przycisk");
//        TableRow row = new TableRow(screen, table); 
//        row.addCell("a", "a1");
//        row.addCell("b", "b1");
//        table.addRow(row);
        Table table = createTable(); 
        window.addChild(table);
        BuildingSimulator.getBuildingSimulator().getGuiNode().addControl(screen);
        InputManager inputManager = BuildingSimulator.getBuildingSimulator().getInputManager();
        inputManager.addRawInputListener(this);
    }
    
    
    private Table createTable(){
        float width = screen.getWidth();
        Table table = new Table(screen, new Vector2f(0, 0), new Vector2f(width,
                screen.getHeight())) {
            @Override
            public void onChange() {
                ((Table.TableCell)this.getSelectedRows().get(0).getChild(2)).setText("asd");
            }
        };
        table.center();
        TableColumn actionsColumn = new TableColumn(table, screen, "action_column"),
                keysColumn = new TableColumn(table, screen, "action_column"); 
        actionsColumn.setWidth(width / 2);
        table.addColumn(actionsColumn);
        keysColumn.setWidth(width / 2);
        table.addColumn(keysColumn);
        addRows(table); 
        return table; 
    }
    
    private void addRows(Table table){
        Control.Actions[] actions = Control.Actions.values();
        for(int i = 0; i < actions.length; i++){
            TableRow row = new TableRow(screen, table);
            String actionName = actions[i].getValue();
            row.addCell(actionName, actionName);
            row.addCell("b", "b");
            table.addRow(row);
        }
    }

    @Override
    public void beginInput() {}

    @Override
    public void endInput() {}

    @Override
    public void onJoyAxisEvent(JoyAxisEvent evt) {}

    @Override
    public void onJoyButtonEvent(JoyButtonEvent evt) {}

    @Override
    public void onMouseMotionEvent(MouseMotionEvent evt) {}

    @Override
    public void onMouseButtonEvent(MouseButtonEvent evt) {}

    @Override
    public void onKeyEvent(KeyInputEvent evt) {
        System.out.println(evt.getKeyCode());
    }

    @Override
    public void onTouchEvent(TouchEvent evt) {}
}
