package authorization;

/**
 * Klasa <code>User</code> reprezentuje użytkownika w grze. Niezalogowany użytkownik 
 * jest określany jako Anonim. 
 * @author AleksanderSklorz 
 */
public class User {
    private String login;
    private int points = 999; 
    public User(String login){
        this.login = login; 
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
}
