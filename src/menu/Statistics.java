package menu;

import authorization.DBManager;
import buildingsimulator.BuildingSimulator;
import buildingsimulator.Control;
import buildingsimulator.GameManager;
import com.jme3.input.InputManager;
import com.jme3.input.event.MouseButtonEvent;
import com.jme3.math.Vector2f;
import java.sql.SQLException;
import java.util.Map;
import java.util.Properties;
import texts.Translator;
import tonegod.gui.controls.buttons.ButtonAdapter;
import tonegod.gui.controls.lists.Table;
import tonegod.gui.controls.windows.Window;
import tonegod.gui.core.Screen;

public class Statistics extends Menu{
    private static Screen screen;
    private static Table controlTable;
    public Statistics(){
        screen = new Screen(BuildingSimulator.getBuildingSimulator());
        window = new Window(screen, "statistics", new Vector2f(0, 0),
                new Vector2f(screen.getWidth(), screen.getHeight()));
        screen.addElement(window);
        window.centerToParent();
        createTable(); 
        createReturnButton();
        window.addChild(controlTable);
        BuildingSimulator.getBuildingSimulator().getGuiNode().addControl(screen);
        Translator.setTexts(new String[]{"return_button"},
                new Translator[]{Translator.RETURN}, screen);
    }
    
    private void createTable() {
        float width = screen.getWidth();
        controlTable = new Table(screen, new Vector2f(0, 0), new Vector2f(width,
                screen.getHeight())) {
            @Override
            public void onChange() {}
        };
        controlTable.center();
        Table.TableColumn userColumn = new Table.TableColumn(controlTable, screen, "user_column"),
                pointsColumn = new Table.TableColumn(controlTable, screen, "points_column"); 
        userColumn.setText(Translator.USER.getValue());
        userColumn.setWidth(width / 2 + 100);
        controlTable.addColumn(userColumn);
        pointsColumn.setText(Translator.POINTS.getValue());
        pointsColumn.setWidth(width / 2 - 100);
        controlTable.addColumn(pointsColumn);
        addRows(controlTable); 
    }
    
    private void createReturnButton(){
        ButtonAdapter button = new ButtonAdapter(screen, "return_button", 
                new Vector2f(screen.getWidth() * 0.6f, screen.getHeight() * 0.9f),
                new Vector2f(100, 30)) {
                    @Override
                    public void onButtonMouseLeftUp(MouseButtonEvent mbe, boolean bln) {
                        doWhenAcceptedExit(Statistics.screen, GameManager.isPausedGame()
                                ? MenuTypes.PAUSE_MENU : MenuTypes.STARTING_MENU);
                    }
                };
        controlTable.addChild(button);
    }
    
    private void addRows(Table table){
        try{
            Map<String, String> statistics = DBManager.getAllStatistics(); 
            for(Map.Entry<String, String> entry : statistics.entrySet()){
                Table.TableRow row = new Table.TableRow(screen, table);
                String login = entry.getKey(), points = entry.getValue();
                row.addCell(login, login);
                row.addCell(points, points);
                table.addRow(row);
            }
        }catch(SQLException|ClassNotFoundException ex){
        }
    }
}
