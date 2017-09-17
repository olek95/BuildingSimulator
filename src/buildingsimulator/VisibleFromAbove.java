package buildingsimulator;

import com.jme3.math.Vector3f;

/**
 * Interfejs <code>VisibleFromAbove</code> reprezentuje obiekty które mogą posiadać
 * tryb widoczności z lotu ptaka. 
 * @author AleksanderSklorz
 */
public interface VisibleFromAbove {
    /**
     * Ustawia miejsce układania elementów (położenie "miejsca zrzutu").
     * @param location miejsce układania elementów 
     */
    public void setDischargingLocation(Vector3f location);
    
    /**
     * Ustawia aktualnego słuchacza sprawdzającego kolizje dla układanych elementów.
     * @param listener słuchacz sprawdzający kolizje dla układanych elementów 
     */
    public void setListener(DummyCollisionListener listener);
    
    /**
     * Zwraca aktualnego słuchacza sprawdzającego kolizje dla układanych elementów.
     * @return słuchacz sprawdzający kolizje dla układanych elementów 
     */
    public DummyCollisionListener getListener();
    
    /**
     * Ustawia dany element w wybranym miejscu. Następnie wyłącza tryb widoku z
     * lotu ptaka. 
     */
    public void unload();
}
