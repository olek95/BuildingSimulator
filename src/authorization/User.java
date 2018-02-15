package authorization;

import com.jme3.system.NanoTimer;
import com.jme3.system.Timer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * Klasa <code>User</code> reprezentuje użytkownika w grze. Niezalogowany użytkownik 
 * jest określany jako Anonim. 
 * @author AleksanderSklorz 
 */
public class User {
    private String login;
    private int points; 
    private String time;
    private float seconds, stoppedTime = 0;
    private Timer timer; 
    public static final String DEFAULT_LOGIN = "Anonim";
    
    public User(String login, int points) {
        this.login = login; 
        this.points = points; 
        timer = new NanoTimer();
    }
    
    /**
     * Zwraca czas gry w formacie HH:mm:ss. 
     * @return czas gry 
     */
    public String getTime() {
        SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss");
        df.setTimeZone(TimeZone.getTimeZone("UTC"));
        seconds = stoppedTime + timer.getTimeInSeconds();
        time = df.format(new Date((long)(seconds * 1000)));
        return time; 
    }
    
    /**
     * Zatrzymuje  
     * @return 
     */
    public void resetTimer() {
        timer.reset();
    }
    
    /**
     * Zapamiętuje czas. 
     */
    public void rememberTime() {
        stoppedTime = seconds; 
    }
    
    /**
     * Zwraca login. 
     * @return login 
     */
    public String getLogin() { return login; }
    
    /**
     * Sumuje punkty. 
     * @param points punkty 
     */
    public void addPoints(int points) { this.points += points; }
    
    /**
     * Zwraca aktualny stan punktów. 
     * @return punkty 
     */
    public int getPoints() { return points; }
    
    /**
     * Ustawia stan punktów. 
     * @param points punkty 
     */
    public void setPoints(int points) { this.points = points; }
    
}
