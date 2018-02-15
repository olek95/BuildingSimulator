package menu;

import buildingsimulator.BuildingSimulator;
import settings.Control;
import settings.Control.Actions;
import com.jme3.input.RawInputListener;
import com.jme3.input.event.JoyAxisEvent;
import com.jme3.input.event.JoyButtonEvent;
import com.jme3.input.event.KeyInputEvent;
import com.jme3.input.event.MouseButtonEvent;
import com.jme3.input.event.MouseMotionEvent;
import com.jme3.input.event.TouchEvent;
import com.jme3.math.Vector2f;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import texts.Translator;
import tonegod.gui.controls.buttons.Button;
import tonegod.gui.controls.buttons.ButtonAdapter;
import tonegod.gui.controls.lists.Table;
import tonegod.gui.controls.lists.Table.TableRow;
import tonegod.gui.core.Screen;

/**
 * Obiekt klasy <code>ControlConfigurationMenu</code> reprezentuje menu umożliwiające 
 * zmianę ustawień sterowania w grze. Wyświetla tabelkę z dwoma kolumnami - nazwa 
 * czynności oraz przypisany do niej przycisk. 
 * @author AleksanderSklorz 
 */
public class ControlConfigurationMenu extends TableMenu implements RawInputListener{
    private static boolean stale; 
    private static Properties restoredSettings;
    public ControlConfigurationMenu(){
        super("controlConfiguration");
        Screen screen = getScreen();
        restoredSettings = new Properties(); 
        Table table = createTable(new String[]{"action_column", "key_column"},
                new Translator[]{Translator.ACTIVITY, Translator.KEY}, new int[]{100, -100}); 
        getWindow().addChild(table);
        createAcceptingButton(); 
        createReturnButton(screen.getWidth() * 0.6f);
        BuildingSimulator.getBuildingSimulator().getInputManager().addRawInputListener(this);
        Translator.setTexts(new String[]{"accepting_button", "return_button"}, 
                new Translator[]{Translator.ACCEPTING, Translator.RETURN}, screen);
        table.setEnableKeyboardNavigation(false);
        setStale();
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
        String key = Actions.getKey(evt.getKeyCode());
        Table table = getTable(); 
        if(table.isAnythingSelected() && !existsDuplicate(key)){
            ((Table.TableCell)table.getSelectedRows().get(0).getChild(2)).setText(key);
            setStale();
        }
    }

    @Override
    public void onTouchEvent(TouchEvent evt) {}
    
    @Override
    protected void addRows(){
        Control.Actions[] actions = Control.Actions.values();
        // -2 bo ostatnie klawisze (lewy  i prawy przycisk myszy) nie da się zmienic
        for(int i = 0; i < actions.length - 2; i++){ 
            String key = actions[i].getKey(), actionName = actions[i].getValue();
            addRow(actionName, actions[i], key); 
            restoredSettings.setProperty(actionName, key);
        }
    }
    
    @Override
    protected void clickReturnButton() {
        Screen screen = getScreen(); 
        if(stale){
            screen.addElement(createNotSavedChangesAlert(screen,
                    Translator.NOT_SAVED_CHANGES.getValue(), MenuTypes.OPTIONS));
        } else doWhenAcceptedExit(screen, MenuTypes.OPTIONS);
    }
    
    private boolean existsDuplicate(String key) {
        List<TableRow> rows = getTable().getRows();
        int rowsNumber = rows.size();
        for(int i = 0; i < rowsNumber; i++){
            if(((Table.TableCell)rows.get(i).getChild(2)).getText().equals(key)){
                return true; 
            }
        }
        return false; 
    }
    
    private void createAcceptingButton(){
        final Screen s = getScreen();
        final Table table = getTable(); 
        ButtonAdapter button = new ButtonAdapter(s, "accepting_button", 
                new Vector2f(s.getWidth() * 0.4f, s.getHeight() * 0.9f),
                new Vector2f(100, 30)) {
                    @Override
                    public void onButtonMouseLeftUp(MouseButtonEvent mbe, boolean bln) {
                        List<Table.TableRow> rows = table.getRows();
                        int rowsAmount = rows.size();
                        String[] keys = new String[rowsAmount];
                        for(int i = 0; i < rowsAmount; i++){
                            keys[i] = ((Table.TableCell)rows.get(i).getChild(2)).getText();
                        }
                        Control.Actions.saveSettings(keys);
                        doWhenAcceptedExit(s, MenuTypes.OPTIONS);
                    }
                };
        table.addChild(button);
    }
    
    private Properties getSelectedSettings() {
        Properties settings = new Properties(); 
        List<TableRow> rows = getTable().getRows();
        int rowsCount = rows.size(); 
        for(int i = 0; i < rowsCount; i++){
            TableRow row = rows.get(i);
            settings.setProperty(((Table.TableCell)row.getChild(1)).getText(), ((Table.TableCell)row
                    .getChild(2)).getText());
        }
        return settings; 
    }
    
    private boolean isChanged() {
        Properties settings = getSelectedSettings(); 
        for(Map.Entry<Object, Object> entry : restoredSettings.entrySet()) {
            if(!settings.getProperty((String)entry.getKey()).equals(entry.getValue()))
                return true; 
        }
        return false; 
    }
    
    private void setStale() {
        stale = isChanged();
        ((Button)getScreen().getElementById("accepting_button")).setIsEnabled(stale);
    }
}
