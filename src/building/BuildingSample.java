package building;

import buildingsimulator.BuildingSimulator;
import buildingsimulator.ElementName;
import buildingsimulator.GameManager;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import java.util.List;

public class BuildingSample extends Construction{
    private Node catchNode; 
    private boolean perpendicularity;
    public BuildingSample(Vector3f location) {
        setName("123");
        GameManager.addToScene(this);
        Wall wall = (Wall)WallsFactory.createWall(WallType.WALL, location, 
                new Vector3f(6, 0.2f, 4), 0.00001f, false);
        GameManager.addToScene(wall);
        wall.setRecentlyHitObject(BuildingSimulator.getBuildingSimulator()
                .getRootNode().getChild(ElementName.MAP_FIELD_PART_NAME));
        add(wall, null, WallMode.HORIZONTAL, false);
        wall.setLocalTranslation(location);
        
        Wall wall2 = (Wall)WallsFactory.createWall(WallType.WALL, location, 
                new Vector3f(6, 0.2f, 4), 0.00001f, false);
        wall2.setRecentlyHitObject(wall);
        this.perpendicularity = false; 
        this.catchNode = (Node)wall.getChild(CatchNode.UP.toString());
        this.add(wall2, wall, WallMode.VERTICAL, false);
        wall2.setLocalTranslation(wall2.getControl(RigidBodyControl.class).getPhysicsLocation());
        
        Wall wall3 = (Wall)WallsFactory.createWall(WallType.WALL, location, 
                new Vector3f(6, 0.2f, 4), 0.00001f, false);
        wall3.setRecentlyHitObject(wall);
        this.perpendicularity = false; 
        this.catchNode = (Node)wall.getChild(CatchNode.BOTTOM.toString());
        wall.setLocalTranslation(location);
        this.add(wall3, wall, WallMode.VERTICAL, false);
        wall3.setLocalTranslation(wall3.getControl(RigidBodyControl.class).getPhysicsLocation());
        
        Wall wall4 = (Wall)WallsFactory.createWall(WallType.WALL, location, 
                new Vector3f(3.6f, 0.2f, 4), 0.00001f, false);
        wall4.setRecentlyHitObject(wall);
        this.perpendicularity = true; 
        this.catchNode = (Node)wall.getChild(CatchNode.LEFT.toString());
        wall.setLocalTranslation(location);
        this.add(wall4, wall, WallMode.VERTICAL, false);
        wall4.setLocalTranslation(wall4.getControl(RigidBodyControl.class).getPhysicsLocation());
        
        Wall wall5 = (Wall)WallsFactory.createWall(WallType.WALL, location, 
                new Vector3f(3.6f, 0.2f, 4), 0.00001f, false);
        wall5.setRecentlyHitObject(wall);
        this.perpendicularity = false; 
        this.catchNode = (Node)wall.getChild(CatchNode.RIGHT.toString());
        wall.setLocalTranslation(location);
        this.add(wall5, wall, WallMode.VERTICAL, false);
        wall5.setLocalTranslation(wall5.getControl(RigidBodyControl.class).getPhysicsLocation());
        
        Wall wall6 = (Wall)WallsFactory.createWall(WallType.WALL, location, 
                new Vector3f(6f, 0.2f, 4), 0.00001f, false);
        wall6.setRecentlyHitObject(wall);
        this.perpendicularity = false; 
        this.catchNode = (Node)wall.getChild(CatchNode.NORTH.toString());
        wall.setLocalTranslation(location);
        this.add(wall6, wall, WallMode.HORIZONTAL, false);
        wall5.setLocalTranslation(wall6.getControl(RigidBodyControl.class).getPhysicsLocation());
        
    }
    
    @Override
    protected Node merge(Wall wall1, Wall wall2, boolean ceiling, WallMode mode,
            boolean protruding, int start, int end){
        if(wall2 != null){ 
            if(setWallInProperPosition(wall1, wall2, catchNode, perpendicularity, 
                    ceiling, mode.equals(WallMode.HORIZONTAL), protruding))
                return catchNode;
            return null;
        }
        return this;
    }
}
