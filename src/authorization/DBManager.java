package authorization;

import com.jme3.app.state.AbstractAppState;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Klasa <code>DBManager</code> odpowiada za obsługę logowania i rejestracji
 * użytkownika.
 * @author AleksanderSklorz 
 */
public class DBManager extends AbstractAppState{
    private static final String DRIVER = "org.sqlite.JDBC";
    private static final String DB_URL = "jdbc:sqlite:building_simulator.db";
    private static DBManager authorization; 
    private DBManager() throws ClassNotFoundException, SQLException{
        try(Connection connection = connect()){
            Statement statement = connection.createStatement();
            statement.execute("CREATE TABLE IF NOT EXISTS Users "
                    + "(id_user INTEGER PRIMARY KEY AUTOINCREMENT, login VARCHAR(20),"
                    + " password VARCHAR(20), points INTEGER, time VARCHAR(20),"
                    + " buildings INTEGER)");
        }
    }
    
    /**
     * Tworzy bazę danych wraz z tabelkami. 
     */
    public static void createDatabase() throws ClassNotFoundException, SQLException{
        if(authorization == null) authorization = new DBManager(); 
    }
    
    /**
     * Sprawdza czy dany użytkownik istnieje. 
     * @param login nazwa użytkownika 
     * @return true jeśli użytkownik istnieje, false w przeciwnym przypadku 
     */
    public static boolean checkIfUserExists(String login) throws ClassNotFoundException, SQLException{
       try(Connection connection = connect()){
           PreparedStatement statement = connection
                   .prepareStatement("SELECT COUNT(*) FROM Users WHERE login = ?");
           statement.setString(1, login);
           return statement.executeQuery().getInt(1) != 0;
       }
    }
    
    /**
     * Pozwala na rejestrację nowego użytkownika. 
     * @param login nazwa użytkownika 
     * @param password hasło użytkownika 
     */
    public static void signUp(String login, String password) throws ClassNotFoundException, SQLException{
        try(Connection connection = connect()){
           PreparedStatement statement = connection
                   .prepareStatement("INSERT INTO Users(login, password, points, time, buildings)"
                   + " VALUES(?, ?, 999, '00:00:00', 0)");
           statement.setString(1, login);
           statement.setString(2, password);
           statement.executeUpdate();
        }
    }
    
    /**
     * Pozwala użytkownikowi się zalogować, a następnie zwraca zalogowanego użytkownika.
     * @param login nazwa użytkownika 
     * @param password hasło użytkownika 
     * @return zalogowany użytkownik, jesli taki istnieje, lub null w przeciwnym przypadku 
     */
    public static User signIn(String login, String password) throws ClassNotFoundException, SQLException{
        try(Connection connection = connect()){
            PreparedStatement statement = connection
                    .prepareStatement("SELECT points, time , buildings FROM Users WHERE login = ?"
                    + " AND password = ?");
            statement.setString(1, login);
            statement.setString(2, password);
            ResultSet rs = statement.executeQuery();
            return rs.next() ? new User(login, rs.getInt(1), rs.getString(2), rs.getInt(3))
                    : null;
        }
    }
    
    /**
     * Zapisuje wynik (punkty i czas) dla danego użytkownika. 
     * @param user użytkownik dla którego zapisuje się wynik  
     * @throws SQLException
     * @throws ClassNotFoundException 
     */
    public static void saveScore(User user) throws SQLException, ClassNotFoundException{
        String login = user.getLogin();
        try(Connection connection = connect()) {
            PreparedStatement statement = connection
                    .prepareStatement("UPDATE Users SET points = ?, time = ?,"
                    + " buildings = ? WHERE login=?");
            statement.setInt(1, user.getPoints());
            statement.setString(2, user.getTime());
            statement.setInt(3, user.getBuildingsNumber());
            statement.setString(4, login);
            statement.executeUpdate(); 
        }
    }
    
    public static List<User> getAllStatistics() throws SQLException, ClassNotFoundException{
        List<User> statistics = new ArrayList();
        try(Connection connection = connect()) {
            PreparedStatement statement = connection
                    .prepareStatement("SELECT login, points, time, buildings FROM Users");
            ResultSet restoredStatistics = statement.executeQuery();
            while(restoredStatistics.next()){
                statistics.add(new User(restoredStatistics.getString(1), 
                        restoredStatistics.getInt(2), restoredStatistics.getString(3),
                        restoredStatistics.getInt(4)));
            }
        }finally{
            return statistics; 
        }
    }
    
    public static User getUser(String login) {
        User user = null;
        try(Connection connection = connect()) {
            PreparedStatement statement = connection
                    .prepareStatement("SELECT points, time, buildings "
                    + "FROM Users WHERE login = ?");
            statement.setString(1, login);
            ResultSet rs = statement.executeQuery();
            user = rs.next() ? new User(login, rs.getInt(1), rs.getString(2),
                    rs.getInt(3)) : null;
        } finally{
            return user; 
        }
    }
    
    private static Connection connect() throws ClassNotFoundException, SQLException{
        Class.forName(DRIVER);
        Connection connection = DriverManager.getConnection(DB_URL);
        return connection; 
    }
}
