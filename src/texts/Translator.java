package texts;

import java.util.Locale;
import java.util.ResourceBundle;
import tonegod.gui.core.Screen;

/**
 * Typ wyliczeniowy <code>Translator</code> reprezentuje wszystkie teksty interfejsu 
 * użytkownika. Umożliwia ich przetłumaczenie na język angielski lub polski. 
 * @author AleksanderSklorz
 */
public enum Translator {
    GAME_SETTINGS,
    LANGUAGE,
    POLISH,
    ENGLISH,
    GRAPHICS,
    SCREEN_RESOLUTION,
    COLOR_DEPTH,
    ANTIALIASING,
    DISABLED_ANTIALIASING,
    FULLSCREEN,
    REFRESH_RATE,
    ACCEPTING,
    RETURN,
    NOT_SAVED_CHANGES,
    CONFIRMATION,
    CONTROL_CONFIGURATION,
    LEFT,
    RIGHT,
    UP,
    DOWN,
    ACTION,
    PULL_OUT,
    PULL_IN,
    LOWER_HOOK,
    HEIGHTEN_HOOK,
    ATTACH,
    VERTICAL_ATTACH,
    DETACH,
    MERGE,
    PHYSICS,
    FIRST,
    SECOND,
    PAUSE,
    ACTIVITY,
    KEY,
    NEW_GAME, 
    LOAD_GAME,
    STATISTICS,
    SETTINGS,
    QUIT_GAME,
    AUTHORIZATION,
    REGISTRATION,
    USERNAME,
    PASSWORD,
    LOGIN,
    REGISTERING,
    CANCELLATION,
    LOGOUT,
    DB_EXCEPTION,
    REGISTRATION_SUCCESSFUL,
    DUPLICATED_USER,
    INCORRECT_DATA,
    EMPTY_LOGIN_PASSWORD,
    EMPTY_LOGIN,
    EMPTY_PASSWORD,
    TOO_LONG_LOGIN_PASSWORD,
    NOT_LOGGED_IN_ALERT,
    GAME_CONTINUATION,
    EXIT_WARNING,
    EXIT_DESKTOP,
    RETURN_TO_STARTING_MENU;
    
    private String value; 
    
    /**
     * Tłumaczy wszystkie teksty na wybrany język. 
     * @param locale lokalizacja z której pobrany jest język 
     */
    public static void translate(Locale locale){
        ResourceBundle bundle = ResourceBundle.getBundle("texts.labels", locale);
        Translator[] labels = values();
        for(int i = 0; i < labels.length; i++){
            labels[i].value = bundle.getString(labels[i].toString());
        }
    }
    
    /**
     * Ustawia wszystkie teksty z danego ekranu. 
     * @param ids tablica id elementów dla których ustawiamy teksty 
     * @param newLabels tablica przetłumaczonych tekstów 
     * @param screen ekran z którego pobieramy elementy do przetłumaczenia 
     */
    public static void setTexts(String[] ids, Translator[] newLabels, Screen screen) {
        for(int i = 0; i < ids.length; i++)
            screen.getElementById(ids[i]).setText(newLabels[i].value);
    }
    
    /**
     * Zwraca tekst w aktualnym języku. 
     * @return tekst w aktualnym języku 
     */
    public String getValue(){ return value; }
}
