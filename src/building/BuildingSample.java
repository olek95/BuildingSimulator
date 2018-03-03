package building;

import buildingsimulator.BuildingSimulator;
import buildingsimulator.ElementName;
import buildingsimulator.GameManager;
import buildingsimulator.PhysicsManager;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import java.util.ArrayList;
import java.util.List;
import net.wcomohundro.jme3.csg.CSGGeometry;

/**
 * Klasa <code>BuildingSample</code> reprezentuje przykładowy (gotowy) budynek. 
 * Pozwala na automatycznie postawenie całego budynku. 
 * @author AleksanderSklorz
 */
public class BuildingSample extends Construction{
    private Node catchNode; 
    private boolean perpendicularity;
    
    public BuildingSample() {
        setName(ElementName.BUILDING_BASE_NAME + getCounter() + " sample");
    }
    
    /**
     * Umieszcza przykładowy budynek w podanej lokalizacji. 
     * @param location
     * @return true jeśli udało sie umieścić budynek, false jeśli zabrakło dla
     * niego miejsca 
     */
    public boolean drop(Vector3f location, int sampleIndex) {
        location.setY(0.2f);
        setLocalTranslation(location);
        List<Wall> walls; 
        if(sampleIndex == 0) walls = createFirstSample(location);
        else walls = sampleIndex == 1 ? createSecondSample(location) : createThirdSample(location);
        if(!BuildingCreator.checkIntersection(getWorldBound())) {
            int wallsNumber = walls.size(); 
            for(int i = 0; i < wallsNumber; i++) {
                Wall wall = walls.get(i);
                PhysicsManager.addPhysicsToGame(wall);
            }
            GameManager.addToScene(this);
            renovateBuilding();
            return true; 
        }
        return false; 
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
    
    private List<Wall> createRoom(Node owner, Vector3f location, WallData... data) {
        List<Wall> walls = new ArrayList(data.length);
        BuildingSimulator game = BuildingSimulator.getBuildingSimulator();
        for(int i = 0; i < data.length; i++) {
            Wall wall = (Wall)WallsFactory.createWall(data[i].getType(), location, 
                data[i].getDimensions(), 0.00001f, false, null);
            walls.add(wall);
            if(owner.equals(this)) {
                wall.setRecentlyHitObject(game.getRootNode().getChild(ElementName.MAP_FIELD_PART_NAME));
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
                Wall newOwner = walls.get(0);
                newOwner.setLocalTranslation(newOwner.worldToLocal(newOwner
                        .getControl(RigidBodyControl.class).getPhysicsLocation(), null));
            }
            game.getBulletAppState().getPhysicsSpace()
                    .remove(wall.getControl(RigidBodyControl.class));
            randomColor(wall);
            if(i == 0) owner = wall;
        }
        return walls; 
    }
    
    private void randomColor(Wall wall) {
        ColorRGBA[] colors = new ColorRGBA[] {ColorRGBA.Black, ColorRGBA.BlackNoAlpha, 
            ColorRGBA.Blue, ColorRGBA.Brown, ColorRGBA.Cyan, ColorRGBA.DarkGray, 
            ColorRGBA.Green, ColorRGBA.LightGray, ColorRGBA.Magenta, ColorRGBA.Orange,
            ColorRGBA.Pink, ColorRGBA.Yellow};
        ((CSGGeometry)wall.getChild(ElementName.WALL_GEOMETRY)).getMaterial().setColor("Color",
                colors[FastMath.rand.nextInt(colors.length)]);
    }
    
    private List<Wall> createFirstSample(Vector3f location) {
        List<Wall> walls1 = createRoom(this, location, new WallData(WallType.WALL, null, 
            WallMode.HORIZONTAL, new Vector3f(6, 0.2f, 4), false),
                new WallData(WallType.DOOR, CatchNode.UP, WallMode.VERTICAL,
                new Vector3f(6, 0.2f, 4), true), new WallData(WallType.FRONT_DOOR,
                CatchNode.BOTTOM, WallMode.VERTICAL, new Vector3f(6, 0.2f, 4), false), 
                new WallData(WallType.ONE_BIG_WINDOW, CatchNode.LEFT, WallMode.VERTICAL, 
                new Vector3f(3.7f, 0.2f, 4), false));
        List<Wall> walls2 = createRoom(walls1.get(0), location, new WallData(WallType.WALL, CatchNode.NORTH, 
            WallMode.HORIZONTAL, new Vector3f(6, 0.2f, 4), false), 
                new WallData(WallType.WINDOWS, CatchNode.UP, WallMode.VERTICAL,
                new Vector3f(6, 0.2f, 4), false), new WallData(WallType.ONE_BIG_WINDOW,
                CatchNode.LEFT, WallMode.VERTICAL, new Vector3f(3.7f, 0.2f, 4), false),
                new WallData(WallType.WALL, CatchNode.RIGHT, WallMode.VERTICAL,
                new Vector3f(3.7f, 0.2f, 4), false));
        List<Wall> walls3 = createRoom(walls1.get(0), location, new WallData(WallType.WALL, CatchNode.EAST, 
            WallMode.HORIZONTAL, new Vector3f(6, 0.2f, 4), false), 
                new WallData(WallType.WALL, CatchNode.BOTTOM, 
                WallMode.VERTICAL, new Vector3f(6, 0.2f, 4), false), 
                new WallData(WallType.WINDOWS, CatchNode.UP, WallMode.VERTICAL,
                new Vector3f(6f, 0.2f, 4), false), new WallData(WallType.ONE_BIG_WINDOW,
                CatchNode.RIGHT, WallMode.VERTICAL, new Vector3f(3.6f, 0.2f, 4), false));
        List<Wall> walls4 = createRoom(walls1.get(2), location, new WallData(WallType.WALL, CatchNode.NORTH, 
                new Vector3f(6, 0.2f, 4), false, true, walls1.get(1)), 
                new WallData(WallType.WINDOWS, CatchNode.BOTTOM, WallMode.VERTICAL,
                new Vector3f(6, 0.2f, 4), false), new WallData(WallType.WINDOWS,
                CatchNode.UP, WallMode.VERTICAL, new Vector3f(6, 0.2f, 4), false),
                new WallData(WallType.WALL, CatchNode.LEFT, WallMode.VERTICAL,
                new Vector3f(3.6f, 0.2f, 4), false), new WallData(WallType.WALL,
                CatchNode.RIGHT, WallMode.VERTICAL, new Vector3f(3.6f, 0.2f, 4), false));
        List<Wall> walls5 = createRoom(walls1.get(1), location, new WallData(WallType.WALL, CatchNode.NORTH, 
                new Vector3f(6, 0.2f, 4), false, true, walls2.get(1)));
        List<Wall> walls6 = createRoom(walls3.get(1), location, new WallData(WallType.WALL, CatchNode.NORTH, 
                new Vector3f(6, 0.2f, 4), false, true, walls3.get(2)));
        List<Wall> walls7 = createRoom(walls4.get(1), location, new WallData(WallType.WALL, CatchNode.NORTH, 
                new Vector3f(6, 0.2f, 4), false, true, walls4.get(2)),
                new WallData(WallType.WINDOWS, CatchNode.BOTTOM, WallMode.VERTICAL,
                new Vector3f(6, 0.2f, 4), false), new WallData(WallType.WINDOWS,
                CatchNode.UP, WallMode.VERTICAL, new Vector3f(6, 0.2f, 4), false),
                new WallData(WallType.WALL, CatchNode.LEFT, WallMode.VERTICAL,
                new Vector3f(3.6f, 0.2f, 4), false), new WallData(WallType.WALL,
                CatchNode.RIGHT, WallMode.VERTICAL, new Vector3f(3.6f, 0.2f, 4), false));
        List<Wall> walls8 = createRoom(walls7.get(1), location, new WallData(WallType.WALL, CatchNode.NORTH, 
                new Vector3f(6, 0.2f, 4), false, true, walls7.get(2)));
        walls1.addAll(walls2);
        walls1.addAll(walls3);
        walls1.addAll(walls4);
        walls1.addAll(walls5);
        walls1.addAll(walls6);
        walls1.addAll(walls7);
        walls1.addAll(walls8);
        return walls1;
    }
    
    private List<Wall> createSecondSample(Vector3f location) {
        List<Wall> walls1 = createRoom(this, location, new WallData(WallType.WALL, null, 
            WallMode.HORIZONTAL, new Vector3f(6, 0.2f, 4), false),
                new WallData(WallType.DOOR, CatchNode.UP, WallMode.VERTICAL,
                new Vector3f(6, 0.2f, 4), true), new WallData(WallType.FRONT_DOOR,
                CatchNode.BOTTOM, WallMode.VERTICAL, new Vector3f(6, 0.2f, 4), false), 
                new WallData(WallType.ONE_BIG_WINDOW, CatchNode.LEFT, WallMode.VERTICAL, 
                new Vector3f(3.7f, 0.2f, 4), false));
        List<Wall> walls2 = createRoom(walls1.get(0), location, new WallData(WallType.WALL, CatchNode.NORTH, 
            WallMode.HORIZONTAL, new Vector3f(6, 0.2f, 4), false), 
                new WallData(WallType.WINDOWS, CatchNode.UP, WallMode.VERTICAL,
                new Vector3f(6, 0.2f, 4), false), new WallData(WallType.ONE_BIG_WINDOW,
                CatchNode.LEFT, WallMode.VERTICAL, new Vector3f(3.7f, 0.2f, 4), false),
                new WallData(WallType.WALL, CatchNode.RIGHT, WallMode.VERTICAL,
                new Vector3f(3.7f, 0.2f, 4), false));
        List<Wall> walls3 = createRoom(walls1.get(0), location, new WallData(WallType.WALL, CatchNode.EAST, 
            WallMode.HORIZONTAL, new Vector3f(6, 0.2f, 4), false), 
                new WallData(WallType.WALL, CatchNode.BOTTOM, 
                WallMode.VERTICAL, new Vector3f(6, 0.2f, 4), false), 
                new WallData(WallType.DOOR, CatchNode.UP, WallMode.VERTICAL,
                new Vector3f(6f, 0.2f, 4), true));
        List<Wall> walls4 = createRoom(walls3.get(0), location,
                new WallData(WallType.WALL, CatchNode.NORTH, WallMode.HORIZONTAL,
                new Vector3f(6, 0.2f, 4), false), new WallData(WallType.WINDOWS, CatchNode.UP,
                WallMode.VERTICAL, new Vector3f(6, 0.2f, 4), false), new WallData(WallType.WALL,
                CatchNode.RIGHT, WallMode.VERTICAL, new Vector3f(3.7f, 0.2f, 4), false));
        List<Wall> walls5 = createRoom(walls4.get(0), location, new WallData(WallType.WALL,
                CatchNode.EAST, WallMode.HORIZONTAL, new Vector3f(6, 0.2f, 4), false),
                new WallData(WallType.WINDOWS, CatchNode.UP, WallMode.VERTICAL,
                new Vector3f(6, 0.2f, 4), false), new WallData(WallType.ONE_BIG_WINDOW,
                CatchNode.RIGHT, WallMode.VERTICAL, new Vector3f(3.7f, 0.2f, 4), false)); 
        List<Wall> walls6 = createRoom(walls5.get(0), location, new WallData(WallType.WALL,
                CatchNode.SOUTH, WallMode.HORIZONTAL, new Vector3f(6, 0.2f, 4), false),
                new WallData(WallType.DOOR, CatchNode.UP, WallMode.VERTICAL,
                new Vector3f(6, 0.2f, 4), true), new WallData(WallType.ONE_BIG_WINDOW,
                CatchNode.RIGHT, WallMode.VERTICAL, new Vector3f(3.7f, 0.2f, 4), false),
                new WallData(WallType.WINDOWS, CatchNode.BOTTOM, WallMode.VERTICAL,
                new Vector3f(6, 0.2f, 4), false));
        List<Wall> walls7 = createRoom(walls1.get(2), location, new WallData(WallType.WALL,
                CatchNode.NORTH, new Vector3f(6, 0.2f, 4), false, true, walls1.get(1)));
        List<Wall> walls8 = createRoom(walls1.get(1), location, new WallData(WallType.WALL,
                CatchNode.NORTH, new Vector3f(6, 0.2f, 4), false, true, walls2.get(1)));
        List<Wall> walls9 = createRoom(walls3.get(1), location, new WallData(WallType.WALL,
                CatchNode.NORTH,  new Vector3f(6, 0.2f, 4), false, true, walls3.get(2)));
        List<Wall> walls10 = createRoom(walls3.get(2), location, new WallData(WallType.WALL, 
                CatchNode.NORTH, new Vector3f(6, 0.2f, 4), false, true, walls4.get(1)));
        List<Wall> walls11 = createRoom(walls6.get(3), location, new WallData(WallType.WALL, 
                CatchNode.NORTH, new Vector3f(6, 0.2f, 4), false, true, walls5.get(1)));
        List<Wall> walls12 = createRoom(walls6.get(1), location, new WallData(WallType.WALL, 
                CatchNode.NORTH, new Vector3f(6, 0.2f, 4), false, true, walls5.get(1)));
        walls1.addAll(walls2);
        walls1.addAll(walls3);
        walls1.addAll(walls4);
        walls1.addAll(walls5);
        walls1.addAll(walls6);
        walls1.addAll(walls7);
        walls1.addAll(walls8);
        walls1.addAll(walls9);
        walls1.addAll(walls10);
        walls1.addAll(walls11);
        return walls1;
    }
    
    private List<Wall> createThirdSample(Vector3f location) {
        List<Wall> walls1 = createRoom(this, location, new WallData(WallType.WALL, null, 
            WallMode.HORIZONTAL, new Vector3f(6, 0.2f, 4), false),
                new WallData(WallType.WALL, CatchNode.UP, WallMode.VERTICAL,
                new Vector3f(6, 0.2f, 4), false), new WallData(WallType.FRONT_DOOR,
                CatchNode.BOTTOM, WallMode.VERTICAL, new Vector3f(6, 0.2f, 4), false), 
                new WallData(WallType.ONE_BIG_WINDOW, CatchNode.LEFT, WallMode.VERTICAL, 
                new Vector3f(3.6f, 0.2f, 4), false), new WallData(WallType.WALL,
                CatchNode.RIGHT, WallMode.VERTICAL, new Vector3f(3.6f, 0.2f, 4), false));
        List<Wall> walls2 = createRoom(walls1.get(2), location, new WallData(WallType.WALL, CatchNode.NORTH, 
                new Vector3f(6, 0.2f, 4), false, true, walls1.get(1)), 
                new WallData(WallType.WINDOWS, CatchNode.BOTTOM, WallMode.VERTICAL,
                new Vector3f(6, 0.2f, 4), false), new WallData(WallType.WALL,
                CatchNode.UP, WallMode.VERTICAL, new Vector3f(6, 0.2f, 4), false),
                new WallData(WallType.ONE_BIG_WINDOW, CatchNode.LEFT, WallMode.VERTICAL,
                new Vector3f(3.6f, 0.2f, 4), false), new WallData(WallType.WALL,
                CatchNode.RIGHT, WallMode.VERTICAL, new Vector3f(3.6f, 0.2f, 4), false));
        List<Wall> walls3 = createRoom(walls2.get(1), location, new WallData(WallType.WALL, CatchNode.NORTH, 
                new Vector3f(6, 0.2f, 4), false, true, walls2.get(2)), 
                new WallData(WallType.WINDOWS, CatchNode.BOTTOM, WallMode.VERTICAL,
                new Vector3f(6, 0.2f, 4), false), new WallData(WallType.WALL,
                CatchNode.UP, WallMode.VERTICAL, new Vector3f(6, 0.2f, 4), false),
                new WallData(WallType.ONE_BIG_WINDOW, CatchNode.LEFT, WallMode.VERTICAL,
                new Vector3f(3.6f, 0.2f, 4), false), new WallData(WallType.WALL,
                CatchNode.RIGHT, WallMode.VERTICAL, new Vector3f(3.6f, 0.2f, 4), false));
        List<Wall> walls4 = createRoom(walls3.get(1), location, new WallData(WallType.WALL, CatchNode.NORTH, 
                new Vector3f(6, 0.2f, 4), false, true, walls3.get(2)), 
                new WallData(WallType.WINDOWS, CatchNode.BOTTOM, WallMode.VERTICAL,
                new Vector3f(6, 0.2f, 4), false), new WallData(WallType.WALL,
                CatchNode.UP, WallMode.VERTICAL, new Vector3f(6, 0.2f, 4), false),
                new WallData(WallType.ONE_BIG_WINDOW, CatchNode.LEFT, WallMode.VERTICAL,
                new Vector3f(3.6f, 0.2f, 4), false), new WallData(WallType.WALL,
                CatchNode.RIGHT, WallMode.VERTICAL, new Vector3f(3.6f, 0.2f, 4), false));
        List<Wall> walls5 = createRoom(walls4.get(1), location, new WallData(WallType.WALL, CatchNode.NORTH, 
                new Vector3f(6, 0.2f, 4), false, true, walls4.get(2)), 
                new WallData(WallType.WINDOWS, CatchNode.BOTTOM, WallMode.VERTICAL,
                new Vector3f(6, 0.2f, 4), false), new WallData(WallType.WALL,
                CatchNode.UP, WallMode.VERTICAL, new Vector3f(6, 0.2f, 4), false),
                new WallData(WallType.ONE_BIG_WINDOW, CatchNode.LEFT, WallMode.VERTICAL,
                new Vector3f(3.6f, 0.2f, 4), false), new WallData(WallType.WALL,
                CatchNode.RIGHT, WallMode.VERTICAL, new Vector3f(3.6f, 0.2f, 4), false));
        List<Wall> walls6 = createRoom(walls5.get(1), location, new WallData(WallType.WALL, CatchNode.NORTH, 
                new Vector3f(6, 0.2f, 4), false, true, walls5.get(2)));
        walls1.addAll(walls2);
        walls1.addAll(walls3);
        walls1.addAll(walls4);
        walls1.addAll(walls5); 
        walls1.addAll(walls6);
        return walls1; 
    }
}
