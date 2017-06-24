package buildingsimulator;

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
}
