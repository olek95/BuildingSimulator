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
import java.util.List;
import texts.Translator;
import tonegod.gui.controls.buttons.ButtonAdapter;
import tonegod.gui.controls.lists.Table;
import tonegod.gui.controls.lists.Table.TableColumn;
import tonegod.gui.controls.lists.Table.TableRow;
import tonegod.gui.controls.windows.Window;
import tonegod.gui.core.Element;
import tonegod.gui.core.Screen;

/**
 * Obiekt klasy <code>ControlConfigurationMenu</code> reprezentuje menu umożliwiające 
 * zmianę ustawień sterowania w grze. Wyświetla tabelkę z dwoma kolumnami - nazwa 
 * czynności oraz przypisany do niej przycisk. 
 * @author AleksanderSklorz 
 */
public class ControlConfigurationMenu extends Menu implements RawInputListener{
    private static Screen screen;
    private static Table controlTable;
    private static boolean stale; 
    public ControlConfigurationMenu(){
        screen = new Screen(BuildingSimulator.getBuildingSimulator());
        window = new Window(screen, "controlConfiguration", new Vector2f(0, 0),
                new Vector2f(screen.getWidth(), screen.getHeight()));
        screen.addElement(window);
        window.centerToParent();
        createTable(); 
        createAcceptingButton(); 
        createReturnButton();
        window.addChild(controlTable);
        BuildingSimulator.getBuildingSimulator().getGuiNode().addControl(screen);
        InputManager inputManager = BuildingSimulator.getBuildingSimulator().getInputManager();
        inputManager.addRawInputListener(this);
        Translator.setTexts(new String[]{"accepting_button", "return_button"}, 
                new Translator[]{Translator.ACCEPTING, Translator.RETURN}, screen);
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
    
    /**
     * Ustawia nowy przycisk dla wybranego wiersza (czynności). 
     * @param evt 
     */
    @Override
    public void onKeyEvent(KeyInputEvent evt) { 
        String key = String.valueOf((char)AwtKeyInput.convertJmeCode(evt.getKeyCode()));
        if(controlTable.isAnythingSelected() && !existsDuplicate(key)){
            stale = true; 
            ((Table.TableCell)controlTable.getSelectedRows().get(0).getChild(2)).setText(key); 
        }
    }

    @Override
    public void onTouchEvent(TouchEvent evt) {}
    
    @Override
    public void closeWindow() {
        window.hide();
        Element closingAlert = screen.getElementById("closing_alert");
        if(closingAlert != null){
            closingAlert.hide();
            screen.removeElement(closingAlert);
            stale = false;
        }
        BuildingSimulator.getBuildingSimulator().getGuiNode()
                .removeControl(screen);
        MenuFactory.showMenu(MenuTypes.OPTIONS);
    }
    
    private boolean existsDuplicate(String key) {
        List<TableRow> rows = controlTable.getRows();
        int rowsNumber = rows.size();
        for(int i = 0; i < rowsNumber; i++){
            if(((Table.TableCell)rows.get(i).getChild(2)).getText().equals(key)){
                return true; 
            }
        }
        return false; 
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
        actionsColumn.setText(Translator.ACTIVITY.getValue());
        actionsColumn.setWidth(width / 2 + 100);
        controlTable.addColumn(actionsColumn);
        keysColumn.setText(Translator.KEY.getValue());
        keysColumn.setWidth(width / 2 - 100);
        controlTable.addColumn(keysColumn);
        addRows(controlTable); 
    }
    
    private void addRows(Table table){
        Control.Actions[] actions = Control.Actions.values();
        for(int i = 0; i < actions.length; i++){
            TableRow row = new TableRow(screen, table);
            char key = actions[i].getKey(); 
            row.addCell(actions[i].getValue(), actions[i]);
            row.addCell(key + "", key);
            table.addRow(row);
        }
    }
    
    private void createAcceptingButton(){
        ButtonAdapter button = new ButtonAdapter(screen, "accepting_button", 
                new Vector2f(screen.getWidth() * 0.4f, screen.getHeight() * 0.9f),
                new Vector2f(100, 30)) {
                    @Override
                    public void onButtonMouseLeftUp(MouseButtonEvent mbe, boolean bln) {
                        List<Table.TableRow> rows = controlTable.getRows();
                        int rowsAmount = rows.size();
                        String[] keys = new String[rowsAmount];
                        for(int i = 0; i < rowsAmount; i++){
                            keys[i] = ((Table.TableCell)rows.get(i).getChild(2)).getText();
                        }
                        Control.Actions.saveSettings(keys);
                        closeWindow();
                    }
                };
        controlTable.addChild(button);
    }
    
    private void createReturnButton(){
        ButtonAdapter button = new ButtonAdapter(screen, "return_button", 
                new Vector2f(screen.getWidth() * 0.6f, screen.getHeight() * 0.9f),
                new Vector2f(100, 30)) {
                    @Override
                    public void onButtonMouseLeftUp(MouseButtonEvent mbe, boolean bln) {
                        if(stale){
                            ControlConfigurationMenu.screen
                                    .addElement(createNotSavedChangesAlert(ControlConfigurationMenu.screen));
                        } else closeWindow();
                    }
                };
        controlTable.addChild(button);
    }
}
