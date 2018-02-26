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
    SOUND_VOLUME,
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
    MERGE_PROTRUDING,
    PHYSICS,
    FIRST,
    SECOND,
    PAUSE,
    SHOW_CURSOR,
    MOVE_CRANE,
    CHANGE_CAMERA,
    COPY_BUILDING,
    BUY_BUILDING,
    FLYCAM_FORWARD,
    FLYCAM_BACKWARD,
    FLYCAM_STRAFE_LEFT,
    FLYCAM_STRAFE_RIGHT,
    CHANGING_CONTROLS_HUD_VISIBILITY,
    ACTIVITY,
    KEY,
    NEW_GAME, 
    LOAD_GAME,
    SAVE_GAME,
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
    NOT_ALLOWED_USERNAME,
    NOT_LOGGED_IN_ALERT,
    USER,
    POINTS,
    TIME,
    GAME_CONTINUATION,
    EXIT_WARNING,
    EXIT_DESKTOP,
    RETURN_TO_STARTING_MENU,
    SHOP,
    TYPE,
    COLOR,
    AMOUNT,
    DIMENSIONS,
    BUYING,
    COST,
    NEXT,
    PREVIOUS,
    HEIGHT_CHANGE,
    ACTUAL_HEIGHT,
    NEW_HEIGHT,
    BAD_DATA,
    MESSAGE_POINTS,
    MESSAGE_NO_FREE_SPACE,
    LOWERED_PROPS,
    HEIGHTENED_PROPS,
    CLEANING_MAP,
    CLEANING_MAP_MESSAGE,
    ENTIRE_MAP,
    INFINITE_BUILDINGS,
    INACCESSIBLE_SPACE,
    NO_ENOUGH_PLACE,
    REQUIREMENT_DETACHING_WALL,
    NO_FOUNDATIONS,
    MOUSE_MOVEMENT,
    RIGHT_CLICK_CANCELLATION,
    LEFT_CLICK_CLONE,
    LEFT_CLICK_DROPPING;
    
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
