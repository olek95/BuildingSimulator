package texts;

public enum Labels {
    SETTINGS,
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
    RETURN;
    private String value; 
    
    public String getValue(){ return value; }
    
    public void setValue(String value) { this.value = value; }
}
