package authorization;

import buildingsimulator.GameManager;
import com.jme3.app.state.AbstractAppState;
import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Klasa <code>DBManager</code> odpowiada za operacje związane z komunikacją z
 * bazą danych SQLite. Łączenie się z bazą jest zabezpieczone przed sytuacją 
 * usunięcia pliku bazy danych. 
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
     * Tworzy bazę danych wraz z tabelką. 
     * @throws ClassNotFoundException
     * @throws SQLException 
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
        createDatabaseFile(null);
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
     * @throws ClassNotFoundException
     * @throws SQLException 
     */
    public static void signUp(String login, String password) throws ClassNotFoundException, SQLException{
        createDatabaseFile(null);
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
     * @throws ClassNotFoundException
     * @throws SQLException 
     */
    public static User signIn(String login, String password) throws ClassNotFoundException, SQLException {
        createDatabaseFile(null);
        try(Connection connection = connect()){
            PreparedStatement statement = connection
                    .prepareStatement("SELECT points, time , buildings FROM Users WHERE login = ?"
                    + " AND password = ?");
            statement.setString(1, login);
            statement.setString(2, password);
            ResultSet rs = statement.executeQuery();
            return rs.next() ? new User(login, password, rs.getInt(1), 
                    rs.getString(2), rs.getInt(3)) : null;
        }
    }
    
    /**
     * Zapisuje wynik (punkty, czas i liczba zbudowanych budynków) dla danego użytkownika. 
     * @param user użytkownik dla którego zapisuje się wynik  
     */
    public static void saveScore(User user){
        String login = user.getLogin();
        createDatabaseFile(user);
        try(Connection connection = connect()) {
            PreparedStatement statement = connection
                    .prepareStatement("UPDATE Users SET points = ?, time = ?,"
                    + " buildings = ? WHERE login=?");
            statement.setInt(1, user.getPoints());
            statement.setString(2, user.getTime());
            statement.setInt(3, user.getBuildingsNumber());
            statement.setString(4, login);
            statement.executeUpdate(); 
        } catch (ClassNotFoundException|SQLException ex) {
            Logger.getLogger(DBManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * Zwraca listę zapisanych użytkowników, bądź pustą tablicę, jeśli w bazie 
     * nie ma zapisanych użytkowników.
     * @return lista użytkowników 
     */
    public static List<User> getAllUsers(){
        List<User> users = new ArrayList();
        createDatabaseFile(GameManager.getUser());
        try(Connection connection = connect()) {
            PreparedStatement statement = connection
                    .prepareStatement("SELECT login, password, points, time, buildings FROM Users");
            ResultSet restoredUsers = statement.executeQuery();
            while(restoredUsers.next()){
                users.add(new User(restoredUsers.getString(1), 
                        restoredUsers.getString(2), restoredUsers.getInt(3),
                        restoredUsers.getString(4), restoredUsers.getInt(5)));
            }
        }finally{
            return users;
        }
    }
    
    /**
     * Zwraca użytkownika o podanym loginie i haśle, bądź null, jeśli taki użytkownik 
     * nie istnieje. 
     * @param login login użytkownika 
     * @param password hasło użytkownika
     * @return użytkownik, bądź null, jeśli taki użytkownik nie istnieje 
     */
    public static User getUser(String login, String password) {
        User user = null;
        try(Connection connection = connect()) {
            PreparedStatement statement = connection
                    .prepareStatement("SELECT points, time, buildings "
                    + "FROM Users WHERE login = ?");
            statement.setString(1, login);
            ResultSet rs = statement.executeQuery();
            user = rs.next() ? new User(login, password, rs.getInt(1), rs.getString(2),
                    rs.getInt(3)) : null;
        } catch (ClassNotFoundException|SQLException ex) {
                Logger.getLogger(DBManager.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            return user;
        }
    }
    
    private static void createDatabaseFile(User user) {
        if(!GameManager.checkIfFileExists("building_simulator.db")){
            try(Connection connection = connect()) {
                PreparedStatement statement = connection.prepareStatement("CREATE TABLE Users "
                        + "(id_user INTEGER PRIMARY KEY AUTOINCREMENT, login VARCHAR(20),"
                        + " password VARCHAR(20), points INTEGER, time VARCHAR(20),"
                        + " buildings INTEGER)");
                statement.execute();
                if(user != null) {
                    String login = user.getLogin();
                    if(!login.equals(User.DEFAULT_LOGIN)) {
                        statement = connection.prepareStatement("INSERT INTO Users(login,"
                                + " password, points, time, buildings)" 
                                + " VALUES(?, ?, ?, ?, ?)");
                        statement.setString(1, login);
                        statement.setString(2, user.getPassword());
                        statement.setInt(3, user.getPoints());
                        statement.setString(4, user.getTime());
                        statement.setInt(5, user.getBuildingsNumber());
                        statement.executeUpdate();
                    }
                }
            } catch (ClassNotFoundException|SQLException ex) {
                Logger.getLogger(DBManager.class.getName()).log(Level.SEVERE, null, ex);
            } 
        }
    }
    
    private static Connection connect() throws ClassNotFoundException, SQLException{
        Class.forName(DRIVER);
        Connection connection = DriverManager.getConnection(DB_URL);
        return connection; 
    }
}
