package authorization;

import com.jme3.app.state.AbstractAppState;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Klasa <code>Authorization</code> odpowiada za obsługę logowania i rejestracji
 * użytkownika.
 * @author AleksanderSklorz 
 */
public class Authorization extends AbstractAppState{
    private static final String DRIVER = "org.sqlite.JDBC";
    private static final String DB_URL = "jdbc:sqlite:building_simulator.db";
    private static Authorization authorization; 
    private Authorization() throws ClassNotFoundException, SQLException{
        try(Connection connection = connect()){
            Statement statement = connection.createStatement();
            statement.execute("CREATE TABLE IF NOT EXISTS Users "
                    + "(id_user INTEGER PRIMARY KEY AUTOINCREMENT, login varchar(20),"
                    + " password varchar(20))");
        }
    }
    
    /**
     * Tworzy bazę danych wraz z tabelkami. 
     */
    public static void createDatabase() throws ClassNotFoundException, SQLException{
        if(authorization == null) authorization = new Authorization(); 
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
                   .prepareStatement("INSERT INTO Users(login, password) VALUES(?, ?)");
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
    
    private static Connection connect() throws ClassNotFoundException, SQLException{
        Connection connection = null;
        Class.forName(DRIVER);
        connection = DriverManager.getConnection(DB_URL);
        return connection; 
    }
}
