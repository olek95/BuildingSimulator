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


/**
 * Klasa <code>WallData</code> reprezentuje dane potrzebne do postawienia danej 
 * ściany podczas tworzenia gotowego budynku. 
 * @author AleksanderSklorz 
 */
class WallData {
    private WallType type;
    private boolean perpendicularity = false;
    private CatchNode catchNode; 
    private WallMode mode; 
    private Vector3f dimensions;
    private boolean protruding; 
    private boolean ceiling = false; 
    private Wall additionalHitWall; 
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
    
    public WallData(WallType type, CatchNode catchNode,
            Vector3f dimensions, boolean protruding, boolean ceiling, Wall additionalHitWall) {
        this.type = type; 
        this.catchNode = catchNode; 
        this.mode = WallMode.HORIZONTAL;
        this.protruding = protruding;
        this.dimensions = dimensions;
        this.ceiling = ceiling;
        this.additionalHitWall = additionalHitWall; 
    }
    
    public WallType getType() { return type; }
    
    public boolean isPerpendicularity() { return perpendicularity; }
    
    public CatchNode getCatchNode() { return catchNode; }
    
    public WallMode getMode() { return mode; }
    
    public boolean isProtruding() { return protruding; }
    
    public Vector3f getDimensions() { return dimensions; }
    
    public boolean isCeiling() { return ceiling; }
    
    public Wall getAdditionalHitWall() { return additionalHitWall; }
}

/**
 * Klasa <code>BuildingSample</code> reprezentuje przykładowy (gotowy) budynek. 
 * Pozwala na automatycznie postawenie całego budynku. 
 * @author AleksanderSklorz
 */
public class BuildingSample extends Construction{
    private Node catchNode; 
    private boolean perpendicularity;
    public BuildingSample(Vector3f location) {
        int newCounter = getCounter(); 
        setCounter(++newCounter); 
        setName(ElementName.BUILDING_BASE_NAME + newCounter + " sample");
        GameManager.addToScene(this);
        location.setY(0.2f);
        this.setLocalTranslation(location);
        Wall[] walls1 = createRoom(this, location, new WallData(WallType.WALL, null, 
            WallMode.HORIZONTAL, new Vector3f(6, 0.2f, 4), false),
                new WallData(WallType.DOOR, CatchNode.UP, WallMode.VERTICAL,
                new Vector3f(6, 0.2f, 4), true), new WallData(WallType.FRONT_DOOR,
                CatchNode.BOTTOM, WallMode.VERTICAL, new Vector3f(6, 0.2f, 4), false), 
                new WallData(WallType.ONE_BIG_WINDOW, CatchNode.LEFT, WallMode.VERTICAL, 
                new Vector3f(3.7f, 0.2f, 4), false));
        Wall[] walls2 = createRoom(walls1[0], location, new WallData(WallType.WALL, CatchNode.NORTH, 
            WallMode.HORIZONTAL, new Vector3f(6, 0.2f, 4), false), 
                new WallData(WallType.WINDOWS, CatchNode.UP, WallMode.VERTICAL,
                new Vector3f(6, 0.2f, 4), false), new WallData(WallType.ONE_BIG_WINDOW,
                CatchNode.LEFT, WallMode.VERTICAL, new Vector3f(3.7f, 0.2f, 4), false),
                new WallData(WallType.WALL, CatchNode.RIGHT, WallMode.VERTICAL,
                new Vector3f(3.7f, 0.2f, 4), false));
        Wall[] walls3 = createRoom(walls1[0], location, new WallData(WallType.WALL, CatchNode.EAST, 
            WallMode.HORIZONTAL, new Vector3f(6, 0.2f, 4), false), 
                new WallData(WallType.WALL, CatchNode.BOTTOM, 
                WallMode.VERTICAL, new Vector3f(6, 0.2f, 4), false), 
                new WallData(WallType.WINDOWS, CatchNode.UP, WallMode.VERTICAL,
                new Vector3f(6f, 0.2f, 4), false), new WallData(WallType.ONE_BIG_WINDOW,
                CatchNode.RIGHT, WallMode.VERTICAL, new Vector3f(3.6f, 0.2f, 4), false));
        Wall[] walls4 = createRoom(walls1[2], location, new WallData(WallType.WALL, CatchNode.NORTH, 
                new Vector3f(6, 0.2f, 4), false, true, walls1[1]), 
                new WallData(WallType.WINDOWS, CatchNode.BOTTOM, WallMode.VERTICAL,
                new Vector3f(6, 0.2f, 4), false), new WallData(WallType.WINDOWS,
                CatchNode.UP, WallMode.VERTICAL, new Vector3f(6, 0.2f, 4), false),
                new WallData(WallType.WALL, CatchNode.LEFT, WallMode.VERTICAL,
                new Vector3f(3.6f, 0.2f, 4), false), new WallData(WallType.WALL,
                CatchNode.RIGHT, WallMode.VERTICAL, new Vector3f(3.6f, 0.2f, 4), false));
        createRoom(walls1[1], location, new WallData(WallType.WALL, CatchNode.NORTH, 
                new Vector3f(6, 0.2f, 4), false, true, walls2[1]));
        createRoom(walls3[1], location, new WallData(WallType.WALL, CatchNode.NORTH, 
                new Vector3f(6, 0.2f, 4), false, true, walls3[2]) );
        Wall[] walls7 = createRoom(walls4[1], location, new WallData(WallType.WALL, CatchNode.NORTH, 
                new Vector3f(6, 0.2f, 4), false, true, walls4[2]),
                new WallData(WallType.WINDOWS, CatchNode.BOTTOM, WallMode.VERTICAL,
                new Vector3f(6, 0.2f, 4), false), new WallData(WallType.WINDOWS,
                CatchNode.UP, WallMode.VERTICAL, new Vector3f(6, 0.2f, 4), false),
                new WallData(WallType.WALL, CatchNode.LEFT, WallMode.VERTICAL,
                new Vector3f(3.6f, 0.2f, 4), false), new WallData(WallType.WALL,
                CatchNode.RIGHT, WallMode.VERTICAL, new Vector3f(3.6f, 0.2f, 4), false));
        createRoom(walls7[1], location, new WallData(WallType.WALL, CatchNode.NORTH, 
                new Vector3f(6, 0.2f, 4), false, true, walls7[2]));
        
    }
    
    @Override
    protected Node merge(Wall wall1, Wall wall2, boolean ceiling, WallMode mode,
            boolean protruding, int start, int end){
        if(wall2 != null){ 
            if(setWallInProperPosition(wall1, wall2, catchNode, perpendicularity, 
                    ceiling, mode.equals(WallMode.HORIZONTAL), protruding)) {
                return catchNode;
            }
            return null;
        }
        return this;
    }
    
    @Override
    protected Node mergeHorizontal(Wall wall1, Wall wall2, boolean foundations,
            WallMode mode){
        if(!foundations){
            if(wall2 != null){
                // sprawdza czy na przeciwko jest druga ściana 
                boolean oppositeWall = getWallFromOpposite(wall1, wall2) != null;
                if(oppositeWall) {
                    Node ceilingCatchNode = merge(wall1, wall2, true, mode, false,
                            4, 8);
                    return ceilingCatchNode;
                }
                return oppositeWall ? 
                        merge(wall1, wall2, (int)(wall1.getWorldTranslation()
                        .y - wall2.getWorldTranslation().y) > 0, mode, false, 4, 8)
                        : null;
            }
        }else{
            return merge(wall1, wall2, false, mode, false, 4, 8); 
        }
        return this; 
    }
    
    private Wall[] createRoom(Node owner, Vector3f location, WallData... data) {
        Wall[] walls = new Wall[data.length];
        for(int i = 0; i < data.length; i++) {
            Wall wall = (Wall)WallsFactory.createWall(data[i].getType(), location, 
                data[i].getDimensions(), 0.00001f, false);
            walls[i] = wall;
            if(owner.equals(this)) {
                wall.setRecentlyHitObject(BuildingSimulator.getBuildingSimulator()
                        .getRootNode().getChild(ElementName.MAP_FIELD_PART_NAME));
                add(wall, null, data[i].getMode(), data[i].isProtruding());
            } else {
                perpendicularity = data[i].isPerpendicularity();
                List<Spatial> ownerChildren = owner.getChildren();
                int ownerChildrenNumber = ownerChildren.size();
                for(int k = 0; k < ownerChildrenNumber; k++) {
                    Spatial child = ownerChildren.get(k);
                    if(child.getName().equals(data[i].getCatchNode().toString())) 
                        catchNode = (Node)child;
                }
                wall.setRecentlyHitObject(owner);
                if(data[i].isCeiling()) {
                    wall.getHitObjects().add(data[i].getAdditionalHitWall());
                    owner.setLocalTranslation(owner.worldToLocal(owner.getControl(RigidBodyControl.class).getPhysicsLocation(), null));
                    Wall additionalHitWall = data[i].getAdditionalHitWall();
                    additionalHitWall.setLocalTranslation(additionalHitWall
                            .worldToLocal(additionalHitWall.getControl(RigidBodyControl.class)
                            .getPhysicsLocation(), null));
                    add(wall, null, data[i].getMode(), data[i].isProtruding());
                    
                } else {
                    add(wall, (Wall)owner, data[i].getMode(), data[i].isProtruding());
                }
                walls[0].setLocalTranslation(walls[0].worldToLocal(walls[0].getControl(RigidBodyControl.class).getPhysicsLocation(), null));
            }
            if(i == 0) owner = wall;
        }
        return walls; 
    }
}
