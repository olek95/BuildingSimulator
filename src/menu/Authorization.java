package menu;

import com.jme3.app.state.AbstractAppState;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import tonegod.gui.controls.windows.Window;
import tonegod.gui.core.Screen;

/**
 * Klasa <code>Authorization</code> odpowiada za obsługę logowania i rejestracji
 * użytkownika. Wyświetla stosowne do tej czynności okienko. 
 * @author AleksanderSklorz 
 */
public class Authorization extends AbstractAppState{
    private static final String DRIVER = "org.sqlite.JDBC";
    private static final String DB_URL = "jdbc:sqlite:building_simulator.db";
    private static Authorization authorization; 
    private Authorization(){
        try(Connection connection = connect()){
            Statement statement = connection.createStatement();
            statement.execute("CREATE TABLE IF NOT EXISTS Users "
                    + "(id_user INTEGER PRIMARY KEY AUTOINCREMENT, login varchar(20),"
                    + " password varchar(20))");
        }catch(SQLException ex){
            ex.printStackTrace();
        }
    }
    
    public static void createDatabase(){
        if(authorization == null) authorization = new Authorization(); 
    }
    
    public static boolean checkIfUserExists(String login, String password){
       try(Connection connection = connect()){
           PreparedStatement statement = connection
                   .prepareStatement("SELECT COUNT(*) FROM Users WHERE login = ?"
                   + " AND password = ?");
           statement.setString(1, login);
           statement.setString(2, password);
           return statement.executeQuery().getInt(1) != 0;
       }catch(SQLException ex){
           ex.printStackTrace();
       }
       return false; 
    }
    
    private static Connection connect(){
        Connection connection = null;
        try {
            Class.forName(DRIVER);
        } catch (ClassNotFoundException e) {
            System.err.println("Brak sterownika JDBC");
            e.printStackTrace();
        }
        try {
            connection = DriverManager.getConnection(DB_URL);
        } catch (SQLException e) {
            System.err.println("Problem z otwarciem polaczenia");
            e.printStackTrace();
        }
        return connection; 
    }
}
