package billboard;

import buildingsimulator.BuildingSimulator;
import buildingsimulator.GameManager;
import com.jme3.animation.LoopMode;
import com.jme3.asset.AssetManager;
import com.jme3.cinematic.Cinematic;
import com.jme3.cinematic.events.AbstractCinematicEvent;
import com.jme3.cinematic.events.CinematicEvent;
import com.jme3.cinematic.events.CinematicEventListener;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector2f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.texture.Texture;

/**
 * Klasa <code>Billboard</code> reprezentuje billboard wyświetlający reklamy. 
 * @author AleksanderSklorz 
 */
public class Billboard {
    private Node billboard;
    public Billboard(float x, float z) {
        billboard = (Node)GameManager.loadModel("Models/billboard/billboard.j3o");
        billboard.setLocalTranslation(x, 0f, z);
        createAnimation();
    }
    
    /**
     * Zmienia wyświetlaną na billboardzie reklamę. 
     * @param path ścieżka do teksury reklamy 
     */
    public void changeAdvertisement(String path) {
        AssetManager manager = BuildingSimulator.getBuildingSimulator().getAssetManager();
        Material material = new Material(manager, "Common/MatDefs/Misc/Unshaded.j3md");
        Texture advertisement = manager.loadTexture(path);
        material.setTexture("ColorMap", advertisement);
        billboard.getChild("board1").setMaterial(material);
    }
    
    /**
     * Zwraca obiekt billboardu. 
     * @return billboard 
     */
    public Spatial getBillboard() { return billboard; }
    
    private void createAnimation() {
        Cinematic cinematic = new Cinematic(BuildingSimulator.getBuildingSimulator()
                .getRootNode(), 30, LoopMode.Loop);
        cinematic.addCinematicEvent(0, new AdvertisementEvent(this, "Textures/advertisements/advertisement1.jpeg"));
        cinematic.addCinematicEvent(10, new AdvertisementEvent(this, "Textures/advertisements/advertisement2.jpeg"));
        cinematic.addCinematicEvent(20, new AdvertisementEvent(this, "Textures/advertisements/advertisement3.jpeg"));
        GameManager.startAnimation(cinematic);
    }
}
