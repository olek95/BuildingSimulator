package authorization;

public class User {
    private String login;
    private int points = 0; 
    public User(String login){
        this.login = login; 
    }
    
    public String getLogin() { return login; }
    
    public void addPoints(int points) { this.points += points; }
    
    public int getPoints() { return points; }
}
