package buildingsimulator;

import com.jme3.math.Vector3f;
import com.jme3.scene.Spatial;

/**
 * Interfejs <code>RememberingRecentlyHitObject</code> jest przeznaczony dla 
 * klas, które potrafią zapamiętywać ostatnio uderzony obiekt. 
 * @author AleksanderSklorz 
 */
public interface RememberingRecentlyHitObject {
    /**
     * Zwraca ostatnio uderzony obiekt. 
     * @return ostatnio uderzony obiekt 
     */
    public Spatial getRecentlyHitObject();
    
    /**
     * Ustawia ostatnio uderzony obiekt. 
     * @param object ostatnio uderzony obiekt 
     */
    public void setRecentlyHitObject(Spatial object);
    
    /**
     * Ustawia kolizję z danym obiektem. 
     * @param b obiekt z którym nastąpiła kolizja 
     */
    public void setCollision(Spatial b);
    
    /**
     * Zwraca aktualna pozycję obiektu. 
     * @return aktualna pozycja 
     */
    public Vector3f getWorldTranslation();
}
