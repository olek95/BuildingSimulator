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


class WallData {
    private WallType type;
    private boolean perpendicularity = false;
    private CatchNode catchNode; 
    private WallMode mode; 
    private Vector3f dimensions;
    private boolean protruding; 
    public WallData(WallType type, CatchNode catchNode, WallMode mode, Vector3f dimensions,
            boolean protruding) {
        this.type = type; 
        this.catchNode = catchNode; 
        this.mode = mode;
        this.dimensions = dimensions;
        this.protruding = protruding;
    }
    
    public WallData(WallType type, boolean perpendicularity, CatchNode catchNode,
            WallMode mode, Vector3f dimensions, boolean protruding) {
        this.type = type; 
        this.catchNode = catchNode; 
        this.mode = mode;
        this.protruding = protruding;
        this.dimensions = dimensions;
        this.perpendicularity = perpendicularity; 
    }
    
    public WallType getType() { return type; }
    
    public boolean isPerpendicularity() { return perpendicularity; }
    
    public CatchNode getCatchNode() { return catchNode; }
    
    public WallMode getMode() { return mode; }
    
    public boolean isProtruding() { return protruding; }
    
    public Vector3f getDimensions() { return dimensions; }
}

public class BuildingSample extends Construction{
    private Node catchNode; 
    private boolean perpendicularity;
    public BuildingSample(Vector3f location) {
        setName("123");
        GameManager.addToScene(this);
        createRoom(this, location, new WallData(WallType.WALL, null, 
            WallMode.HORIZONTAL, new Vector3f(6, 0.2f, 4), false),
                new WallData(WallType.WALL, CatchNode.UP, WallMode.VERTICAL,
                new Vector3f(6, 0.2f, 4), true), new WallData(WallType.WALL,
                CatchNode.BOTTOM, WallMode.VERTICAL, new Vector3f(6, 0.2f, 4), false), 
                new WallData(WallType.WALL, CatchNode.LEFT, WallMode.VERTICAL, 
                new Vector3f(3.6f, 0.2f, 4), false), new WallData(WallType.WALL, CatchNode.RIGHT,
                WallMode.VERTICAL, new Vector3f(3.6f, 0.2f, 4), false));
        
        /*Wall wall6 = (Wall)WallsFactory.createWall(WallType.WALL, location, 
                new Vector3f(6f, 0.2f, 4), 0.00001f, false);
        wall6.setRecentlyHitObject(wall);
        this.perpendicularity = false; 
        this.catchNode = (Node)wall.getChild(CatchNode.NORTH.toString());
        wall.setLocalTranslation(location);
        this.add(wall6, wall, WallMode.HORIZONTAL, false);
        wall6.setLocalTranslation(wall6.getControl(RigidBodyControl.class).getPhysicsLocation());
        
        Wall wall7 = (Wall)WallsFactory.createWall(WallType.WALL, location, 
                new Vector3f(6f, 0.2f, 4), 0.00001f, false);
        wall7.setRecentlyHitObject(wall);
        this.perpendicularity = false; 
        this.catchNode = (Node)wall.getChild(CatchNode.EAST.toString());
        wall.setLocalTranslation(location);
        this.add(wall7, wall, WallMode.HORIZONTAL, false);
        wall7.setLocalTranslation(wall7.getControl(RigidBodyControl.class).getPhysicsLocation());*/
        
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
    
    private Wall createRoom(Node owner, Vector3f location, WallData... data) {
        Wall floor = null;
        Spatial firstWall = null;
        if(children.size() > 0) firstWall = getChild(0);
        for(int i = 0; i < data.length; i++) {
            Wall wall = (Wall)WallsFactory.createWall(data[i].getType(), location, 
                data[i].getDimensions(), 0.00001f, false);
            if(floor == null) floor = wall;
            if(this.equals(owner)) {
                wall.setRecentlyHitObject(BuildingSimulator.getBuildingSimulator()
                        .getRootNode().getChild(ElementName.MAP_FIELD_PART_NAME));
                add(wall, null, data[i].getMode(), data[i].isProtruding());
                firstWall = wall; 
            } else {
                perpendicularity = data[i].isPerpendicularity(); 
                catchNode = (Node)floor.getChild(data[i].getCatchNode().toString());
                wall.setRecentlyHitObject(floor);
                firstWall.setLocalTranslation(location);
                add(wall, floor, data[i].getMode(), data[i].isProtruding());
            }
            wall.setLocalTranslation(wall.getControl(RigidBodyControl.class).getPhysicsLocation());
            owner = wall;
        }
        return floor; 
    }
}
