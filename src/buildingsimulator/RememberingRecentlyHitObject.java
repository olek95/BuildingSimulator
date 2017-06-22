package buildingsimulator;

import com.jme3.scene.Spatial;

public interface RememberingRecentlyHitObject {
    public Spatial getRecentlyHitObject();
    
    public void setRecentlyHitObject(Spatial object);
    
    public void setCollision(Spatial b);
}
