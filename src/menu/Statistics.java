package menu;

import authorization.DBManager;
import buildingsimulator.GameManager;
import java.sql.SQLException;
import java.util.Map;
import texts.Translator;
import tonegod.gui.core.Screen;

/**
 * Klasa <code>Statistics</code> reprezentuje okno wyświetlające statystyki 
 * wszystkich graczy. 
 * @author AleksanderSklorz
 */
public class Statistics extends TableMenu{
    public Statistics(){
        super("statistics");
        Screen screen = getScreen(); 
        window.addChild(createTable(new String[]{"user_column", "points_column"},
                new Translator[]{Translator.USER, Translator.POINTS}));
        createReturnButton(screen.getWidth() * 0.45f);
        Translator.setTexts(new String[]{"return_button"},
                new Translator[]{Translator.RETURN}, screen);
    }
    
    @Override
    protected void clickReturnButton() {
        doWhenAcceptedExit(getScreen(), GameManager.isPausedGame()
                ? MenuTypes.PAUSE_MENU : MenuTypes.STARTING_MENU);
    }
    
    @Override
    protected void addRows(){
        try{
            Map<String, String> statistics = DBManager.getAllStatistics(); 
            for(Map.Entry<String, String> entry : statistics.entrySet()){
                String login = entry.getKey();
                addRow(login, login, entry.getValue());
            }
        }catch(SQLException|ClassNotFoundException ex){
        }
    }
}
