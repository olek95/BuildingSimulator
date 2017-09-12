package authorization;

import com.jme3.app.state.AbstractAppState;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

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
                    + " password VARCHAR(20), points INTEGER)");
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
                   .prepareStatement("INSERT INTO Users(login, password, points) VALUES(?, ?, 0)");
           statement.setString(1, login);
           statement.setString(2, password);
           statement.execute();
        }
    }
    
    /**
     * Pozwala użytkownikowi się zalogować. Sprawdza tez czy taki użytkownik 
     * już istnieje. 
     * @param login nazwa użytkownika 
     * @param password hasło użytkownika 
     * @return true jeśli udało się zalogować, false w przeciwnym przypadku 
     */
    public static boolean signIn(String login, String password) throws ClassNotFoundException, SQLException{
        try(Connection connection = connect()){
            PreparedStatement statement = connection
                    .prepareStatement("SELECT COUNT(*) FROM Users WHERE login = ?"
                    + " AND password = ?");
            statement.setString(1, login);
            statement.setString(2, password);
            return statement.executeQuery().getInt(1) != 0;
        }
    }
    
    /**
     * Zapisuje punkty dla danego użytkownika. 
     * @param user użytkownik dla którego zapisują się punkty 
     * @throws SQLException
     * @throws ClassNotFoundException 
     */
    public static void savePoints(User user) throws SQLException, ClassNotFoundException{
        String login = user.getLogin();
        try(Connection connection = connect()) {
            PreparedStatement statement = connection
                    .prepareStatement("UPDATE Users SET points = ? WHERE login=?");
            statement.setInt(1, user.getPoints());
            statement.setString(2, login);
        }
    }
    
    private static Connection connect() throws ClassNotFoundException, SQLException{
        Connection connection = null;
        Class.forName(DRIVER);
        connection = DriverManager.getConnection(DB_URL);
        return connection; 
    }
}
