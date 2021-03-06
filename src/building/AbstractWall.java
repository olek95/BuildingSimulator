package building;

import buildingsimulator.BuildingSimulator;
import buildingsimulator.ElementName;
import buildingsimulator.PhysicsManager;
import com.jme3.asset.AssetManager;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import net.wcomohundro.jme3.csg.CSGGeometry;
import net.wcomohundro.jme3.csg.CSGShape;

/**
 * Klasa abstrakcyjna <code>AbstractWall</code> reprezentuje nadklasę dla wszystkich 
 * klas ścian występujących w grze. Umożliwia stworzenie kształtu ściany oraz 
 * podstawowej fizyki dla niej. 
 * @author AleksanderSklorz 
 */
public abstract class AbstractWall extends Node{
    public AbstractWall() {}
    
    public AbstractWall(WallType type, CSGShape shape, Vector3f location, 
            float mass, String name, ColorRGBA color, CSGShape... differenceShapes) {
        this.name = name;
        initShape(shape, color, differenceShapes);
        ((CSGGeometry)getChild(0)).regenerate();
        createLooseControl(location, mass); 
    }
    
    private void initShape(CSGShape shape, ColorRGBA color, CSGShape... differenceShapes){
        CSGGeometry wall = new CSGGeometry(ElementName.WALL_GEOMETRY); 
        wall.addShape(shape);
        AssetManager assetManager = BuildingSimulator.getGameAssetManager(); 
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setTexture("ColorMap", assetManager.loadTexture("Textures/gips.jpg"));
        if(color != null) mat.setColor("Color", color);
        wall.setMaterial(mat);
        if(differenceShapes != null)
            for(int i = 0; i < differenceShapes.length; i++)
                wall.subtractShape(differenceShapes[i]);
        attachChild(wall);
    }
    
    private void createLooseControl(Vector3f location, float mass){
        PhysicsManager.createObjectPhysics(this, mass, false, ElementName.WALL_GEOMETRY);
        getControl(RigidBodyControl.class).setPhysicsLocation(location);
    }
}
