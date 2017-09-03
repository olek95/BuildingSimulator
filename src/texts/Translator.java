package texts;

import java.util.Locale;
import java.util.ResourceBundle;
import tonegod.gui.core.Screen;

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
    CONTROL_CONFIGURATION,
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
    LOGOUT;
    
    private String value; 
    
    public static void translate(Locale locale){
        ResourceBundle bundle = ResourceBundle.getBundle("texts.labels", locale);
        Translator[] labels = values();
        for(int i = 0; i < labels.length; i++){
            labels[i].value = bundle.getString(labels[i].toString());
        }
    }
    
    public static void setTexts(String[] labels, Translator[] newLabels, Screen screen) {
        for(int i = 0; i < labels.length; i++)
            screen.getElementById(labels[i]).setText(newLabels[i].value);
    }
    
    public String getValue(){ return value; }
    
    public void setValue(String value) { this.value = value; }
}
