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

public class DummyWall extends Node{
    public DummyWall(Vector3f location, Vector3f dimensions) {
        setName("DummyWall");
        Box box = new Box(dimensions.x, dimensions.y, dimensions.z);
        Geometry geometry = new Geometry("DummyWall", box);
        Material mat = new Material(BuildingSimulator.getBuildingSimulator()
                .getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
         mat.setColor("Color",ColorRGBA.Blue);
         geometry.setMaterial(mat);
         attachChild(geometry);
         GameManager.addToGame(this);
//        attachChild(new Geometry("DummyWall", new Box(dimensions.x, 
//                dimensions.y, dimensions.z)));
        GameManager.createObjectPhysics(this, 0.00001f, false, "DummyWall");
        getControl(RigidBodyControl.class).setPhysicsLocation(location);
        getControl(RigidBodyControl.class).setMass(0);
    }
}
