package buildingsimulator;

import building.Construction;
import building.Wall;
import com.jme3.export.InputCapsule;
import com.jme3.export.JmeExporter;
import com.jme3.export.JmeImporter;
import com.jme3.export.OutputCapsule;
import com.jme3.export.Savable;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import cranes.crane.CraneState;
import cranes.mobileCrane.MobileCraneState;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Klasa <code>SavedData</code> reprezentuje obecny stan gry. Umożliwia jego zapisanie. 
 * @author AleksanderSklorz 
 */
public class SavedData implements Savable{
    private ArrayList<Wall> walls;
    private ArrayList<Construction> buildings;
    private MobileCraneState mobileCrane; 
    private CraneState crane; 
    private String actualUnit; 
    
    @Override
    public void write(JmeExporter ex) throws IOException {
        OutputCapsule capsule = ex.getCapsule(this);
        capsule.write(new MobileCraneState(), "mobileCrane", null);
        capsule.write(new CraneState(), "crane", null);
        Node root = BuildingSimulator.getBuildingSimulator().getRootNode();
        List<Spatial> gameObjects = root.getChildren();
        ArrayList<Wall> savedWalls = new ArrayList();
        ArrayList<Construction> savedBuildings = new ArrayList();
        for(int i = 0; i < gameObjects.size(); i++) {
            Spatial object = gameObjects.get(i);
            String objectName = object.getName();
            if(objectName != null) {
                if(objectName.startsWith(ElementName.WALL_BASE_NAME)) {
                    savedWalls.add((Wall)object);
                } else {
                    if(objectName.startsWith(ElementName.BUILDING_BASE_NAME))
                        savedBuildings.add((Construction)object);
                }
            }
        }
        capsule.writeSavableArrayList(savedWalls, "walls", null);
        capsule.writeSavableArrayList(savedBuildings, "buildings", null);
        capsule.write(GameManager.getActualUnit().getCrane().getName().contains("zuraw")
                ? "crane" : "mobileCrane", "actualUnit", null);
    }

    @Override
    public void read(JmeImporter im) throws IOException {
        InputCapsule capsule = im.getCapsule(this);
        mobileCrane = (MobileCraneState)capsule.readSavable("mobileCrane", null);
        crane = (CraneState)capsule.readSavable("crane", null);
        walls = capsule.readSavableArrayList("walls", null);
        buildings = capsule.readSavableArrayList("buildings", null);
        actualUnit = capsule.readString("actualUnit", null);
    }
    
    /**
     * Zwraca stan żurawia. 
     * @return stan żurawia 
     */
    public CraneState getCrane() { return crane; }
    
    /**
     * Zwraca stan dźwigu mobilnego. 
     * @return stan dźwigu mobilnego 
     */
    public MobileCraneState getMobileCrane() { return mobileCrane; }
    
    /**
     * Zwraca listę zapisanych ścian.  
     * @return lista ścian  
     */
    public ArrayList<Wall> getWalls() { return walls; }
    
    /**
     * Zwraca listę zapisanych budynków. 
     * @return lista budynków 
     */
    public ArrayList<Construction> getBuildings() { return buildings; }
    
    /**
     * Zwraca zapisaną aktualnie używaną podczas zapisu jednostkę. 
     * @return używana podczas zapisu jednostka
     */
    public String getActualUnit() { return actualUnit; }
}
