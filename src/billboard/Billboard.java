package billboard;

import buildingsimulator.BuildingSimulator;
import buildingsimulator.ElementName;
import buildingsimulator.GameManager;
import buildingsimulator.PhysicsManager;
import com.jme3.animation.LoopMode;
import com.jme3.asset.AssetManager;
import com.jme3.cinematic.Cinematic;
import com.jme3.material.Material;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;

/**
 * Klasa <code>Billboard</code> reprezentuje billboard wyświetlający reklamy. 
 * @author AleksanderSklorz 
 */
public class Billboard {
    private Node billboard;
    private Cinematic advertisementAnimation;
    public Billboard(float x, float z) {
        billboard = GameManager.loadModel("Models/billboard/billboard.j3o");
        billboard.setLocalTranslation(x, 0f, z);
        advertisementAnimation = createAnimation();
        PhysicsManager.createBoxPhysics(billboard, new Vector3f(60, 0, 0), 
                new Spatial[] {((Node)billboard.getChild("pole")).getChild(0),
                    ((Node)billboard.getChild("board1")).getChild(0)}, 
                new Vector3f(0, 6, 0), new Vector3f(0.5f, 11.7f, 0));
    }
    
    /**
     * Zmienia wyświetlaną na billboardzie reklamę. 
     * @param path ścieżka do teksury reklamy 
     */
    public void changeAdvertisement(String path) {
        AssetManager manager = BuildingSimulator.getGameAssetManager();
        Material material = new Material(manager, "Common/MatDefs/Misc/Unshaded.j3md");
        material.setTexture("ColorMap", manager.loadTexture(path));
        billboard.getChild(ElementName.BOARD).setMaterial(material);
    }
    
    /**
     * Zatrzymuje animację reklam. 
     */
    public void pauseAdvertisement() {
        advertisementAnimation.pause();
    }
    
    /**
     * Wznawia zatrzymaną animację reklam. 
     */
    public void resumeAdvertisement() {
        advertisementAnimation.play();
    }
    
    /**
     * Zwraca obiekt billboardu. 
     * @return billboard 
     */
    public Spatial getBillboard() { return billboard; }
    
    private Cinematic createAnimation() {
        Cinematic cinematic = new Cinematic(BuildingSimulator.getGameRootNode(),
                30, LoopMode.Loop);
        cinematic.addCinematicEvent(0, new AdvertisementEvent(this, "Textures/advertisements/advertisement1.jpeg"));
        cinematic.addCinematicEvent(10, new AdvertisementEvent(this, "Textures/advertisements/advertisement2.jpeg"));
        cinematic.addCinematicEvent(20, new AdvertisementEvent(this, "Textures/advertisements/advertisement3.jpeg"));
        GameManager.startAnimation(cinematic);
        return cinematic;
    }
}
