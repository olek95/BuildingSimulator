package authorization;

/**
 * Klasa <code>User</code> reprezentuje użytkownika w grze. Niezalogowany użytkownik 
 * jest określany jako Anonim. 
 * @author AleksanderSklorz 
 */
public class User {
    private String login;
    private int points; 
    public static final String DEFAULT_LOGIN = "Anonim";
    
    public User(String login, int points) {
        this.login = login; 
        this.points = points; 
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
