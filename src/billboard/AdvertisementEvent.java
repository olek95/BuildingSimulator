package billboard;

import com.jme3.cinematic.events.AbstractCinematicEvent;

/**
 * Klasa <code>AdvertisementEvent</code> reprezentuje zdarzenie wyświetlania 
 * danej reklamy na bilboardzie. 
 * @author AleksanderSklorz
 */
public class AdvertisementEvent extends AbstractCinematicEvent{
    private Billboard billboard; 
    private String path;
    public AdvertisementEvent(Billboard billboard, String path) {
        this.billboard = billboard; 
        this.path = path;
    }
    
    @Override
    protected void onPlay() {
        billboard.changeAdvertisement(path);
    }

    @Override
    protected void onUpdate(float tpf) {}

    @Override
    protected void onStop() {}

    @Override
    public void onPause() {}
    
}
