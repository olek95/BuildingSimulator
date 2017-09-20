package building;

import buildingsimulator.BuildingSimulator;
import buildingsimulator.GameManager;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Box;

/**
 * Obiekt klasy <code>DummyWall</code> reprezentuje sztuczną (niewidzialną) ścianę. 
 * @author AleksanderSklorz
 */
public class DummyWall extends Node{
    private DummyWall(Vector3f location, Vector3f dimensions, float mass) {
        setName("DummyWall");
        attachChild(new Geometry("DummyWall", new Box(dimensions.x, 
                dimensions.y, dimensions.z)));
        GameManager.createObjectPhysics(this, mass, false, "DummyWall");
        getControl(RigidBodyControl.class).setPhysicsLocation(location);
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
}
