package authorization;

import com.jme3.system.NanoTimer;
import com.jme3.system.Timer;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Klasa <code>User</code> reprezentuje użytkownika w grze. Niezalogowany użytkownik 
 * jest określany jako Anonim. 
 * @author AleksanderSklorz 
 */
public class User {
    private String login;
    private int points, buildingsNumber; 
    private String time;
    private float seconds, stoppedTime = 0;
    private Timer timer; 
    public static final String DEFAULT_LOGIN = "Anonim", TIME_FORMAT = "HH:mm:ss";
    
    public User(String login, int points, String time, int buildingsNumber) {
        this.login = login; 
        this.points = points; 
        this.time = time;
        this.buildingsNumber = buildingsNumber; 
        stoppedTime = getTimeInSeconds(time);
        timer = new NanoTimer();
    }
    
    /**
     * Oblicza czas gry w formacie HH:mm:ss. 
     * @return czas gry 
     */
    public String calculateActualTime() {
        SimpleDateFormat df = new SimpleDateFormat(TIME_FORMAT);
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
     * Resetuje wszystkie zmienne związane z czasem. 
     */
    public void resetAllTime() {
        resetTimer(); 
        seconds = 0;
        stoppedTime = 0;
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
    
    /**
     * Zwraca aktualny czas w postaci stringu (HH:mm:ss). 
     * @return czas jako string 
     */
    public String getTime() { return time; }
    
    /**
     * Zwraca liczbę budynków. 
     * @return liczba budynków 
     */
    public int getBuildingsNumber() { return buildingsNumber; }
    
    /**
     * Ustawia liczbę budynków. 
     * @param buildingsNumber ustawia liczbę budynków
     */
    public void setBuildingsNumber(int buildingsNumber) { 
        this.buildingsNumber = buildingsNumber;
    }
    
    private float getTimeInSeconds(String time) {
        DateFormat dateFormat = new SimpleDateFormat(TIME_FORMAT);
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        float timeInSeconds = 0;
        try {
            timeInSeconds = dateFormat.parse(time).getTime() / 1000f;
        }catch(ParseException ex) {
            Logger.getLogger(User.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            return timeInSeconds;
        }
    }
}
