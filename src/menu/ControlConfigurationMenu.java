package menu;

import buildingsimulator.BuildingSimulator;
import buildingsimulator.Control;
import com.jme3.input.InputManager;
import com.jme3.input.RawInputListener;
import com.jme3.input.awt.AwtKeyInput;
import com.jme3.input.event.JoyAxisEvent;
import com.jme3.input.event.JoyButtonEvent;
import com.jme3.input.event.KeyInputEvent;
import com.jme3.input.event.MouseButtonEvent;
import com.jme3.input.event.MouseMotionEvent;
import com.jme3.input.event.TouchEvent;
import com.jme3.math.Vector2f;
import com.jme3.scene.Spatial;
import java.awt.event.KeyEvent;
import java.util.List;
import tonegod.gui.controls.lists.Table;
import tonegod.gui.controls.lists.Table.TableColumn;
import tonegod.gui.controls.lists.Table.TableRow;
import tonegod.gui.controls.windows.Window;
import tonegod.gui.core.Screen;

public class ControlConfigurationMenu extends Menu implements RawInputListener{
    private static Screen screen;
    private static Table controlTable;
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
        createTable(); 
        window.addChild(controlTable);
        BuildingSimulator.getBuildingSimulator().getGuiNode().addControl(screen);
        InputManager inputManager = BuildingSimulator.getBuildingSimulator().getInputManager();
        inputManager.addRawInputListener(this);
    }
    
    
    private void createTable(){
        float width = screen.getWidth();
        controlTable = new Table(screen, new Vector2f(0, 0), new Vector2f(width,
                screen.getHeight())) {
            @Override
            public void onChange() {}
        };
        controlTable.center();
        TableColumn actionsColumn = new TableColumn(controlTable, screen, "action_column"),
                keysColumn = new TableColumn(controlTable, screen, "action_column"); 
        actionsColumn.setWidth(width / 2);
        controlTable.addColumn(actionsColumn);
        keysColumn.setWidth(width / 2);
        controlTable.addColumn(keysColumn);
        addRows(controlTable); 
    }
    
    private void addRows(Table table){
        Control.Actions[] actions = Control.Actions.values();
        for(int i = 0; i < actions.length; i++){
            TableRow row = new TableRow(screen, table);
            String actionName = actions[i].getValue();
            char key = actions[i].getKey(); 
            row.addCell(actionName, actionName);
            row.addCell(key + "", key);
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
        String key = String.valueOf((char)AwtKeyInput.convertJmeCode(evt.getKeyCode()));
        if(controlTable.isAnythingSelected() && !existDuplicate(key)){
            ((Table.TableCell)controlTable.getSelectedRows().get(0).getChild(2)).setText(key); 
        }
    }

    @Override
    public void onTouchEvent(TouchEvent evt) {}
    
    private boolean existDuplicate(String key) {
        List<TableRow> rows = controlTable.getRows();
        int rowsNumber = rows.size();
        for(int i = 0; i < rowsNumber; i++){
            if(((Table.TableCell)rows.get(i).getChild(2)).getText().equals(key)){
                return true; 
            }
        }
        return false; 
    }
}
