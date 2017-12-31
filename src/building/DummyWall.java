package building;

import buildingsimulator.ElementName;
import buildingsimulator.GameManager;
import buildingsimulator.PhysicsManager;
import com.jme3.audio.AudioNode;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Box;

/**
 * Obiekt klasy <code>DummyWall</code> reprezentuje sztuczną (niewidzialną) ścianę. 
 * @author AleksanderSklorz
 */
public class DummyWall extends Node{
    private AudioNode dropSound; 
    private DummyWall(Vector3f location, Vector3f dimensions, float mass) {
        setName(ElementName.DUMMY_WALL);
        attachChild(new Geometry(ElementName.DUMMY_WALL, new Box(dimensions.x, 
                dimensions.y, dimensions.z)));
        PhysicsManager.createObjectPhysics(this, mass, false, ElementName.DUMMY_WALL);
        getControl(RigidBodyControl.class).setPhysicsLocation(location);
        dropSound = GameManager.createSound("Sounds/drop.wav", GameManager.getGameSoundVolume(),
                false, this);
    }
    
    /**
     * Tworzy sztuczną ścianę. 
     * @param location położenie ściany 
     * @param dimensions wymiary ściany 
     * @param mass masa ściany 
     * @return sztuczna ściana 
     */
    public static DummyWall createDummyWall(Vector3f location, Vector3f dimensions, float mass) {
        return new DummyWall(location, dimensions, mass);
    }
    
    /**
     * Uruchamia dźwięk uderzenia spadającego obiektu. 
     */
    public void startDropSound() {
        dropSound.play();
    }
}
