package building;

import buildingsimulator.ElementName;
import buildingsimulator.GameManager;
import buildingsimulator.PhysicsManager;
import com.jme3.audio.AudioNode;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import net.wcomohundro.jme3.csg.CSGShape;

/**
 * Obiekt klasy <code>DummyWall</code> reprezentuje sztuczną ścianę. 
 * @author AleksanderSklorz
 */
public class DummyWall extends AbstractWall{
    private AudioNode dropSound; 
    public DummyWall(WallType type, CSGShape shape, Vector3f location, 
            float mass, ColorRGBA color, CSGShape... differenceShapes) {
        super(type, shape, location, mass, ElementName.DUMMY_WALL, color, differenceShapes);
        addDropSound();
    }
    
    /**
     * Uruchamia dźwięk uderzenia spadającego obiektu. 
     */
    public void startDropSound() {
        dropSound.play();
    }
    
    /**
     * Wyłacza fizykę dla sztucznej ściany. 
     */
    public void setOffPhysics() {
        PhysicsManager.removeFromScene(this);
    }
    
    private void addDropSound() {
        dropSound = GameManager.createSound("Sounds/drop.wav", GameManager.getGameSoundVolume(),
                false, this);
    }
}
