package buildingmaterials;

import buildingsimulator.BuildingSimulator;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Box;

public class Wall {
    public Wall(Box shape, Vector3f location){
        BuildingSimulator game = BuildingSimulator.getBuildingSimulator();
        Geometry wall = new Geometry("Box", shape);  
        wall.setLocalTranslation(location);
        Material mat = new Material(game.getAssetManager(), 
                "Common/MatDefs/Misc/Unshaded.j3md");  
        mat.setColor("Color", ColorRGBA.Blue);   
        wall.setMaterial(mat);                   
        game.getRootNode().attachChild(wall);              
        RigidBodyControl wallControl = new RigidBodyControl(0.00001f);
        wall.addControl(wallControl);
        game.getBulletAppState().getPhysicsSpace().add(wallControl);
    }
}
