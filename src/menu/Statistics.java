package menu;

import authorization.DBManager;
import authorization.User;
import buildingsimulator.GameManager;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
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
        getWindow().addChild(createTable(new String[]{"user_column", "points_column", "time_column"},
                new Translator[]{Translator.USER, Translator.POINTS, Translator.TIME}));
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
            List<User> statistics = DBManager.getAllStatistics(); 
            int statisticsNumber = statistics.size();
            for(int i = 0; i < statisticsNumber; i++){
                User user = statistics.get(i);
                String login = user.getLogin();
                addRow(login, login, user.getPoints() +  "", user.getTime());
            }
        }catch(SQLException|ClassNotFoundException ex){
            Logger.getLogger(Statistics.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
