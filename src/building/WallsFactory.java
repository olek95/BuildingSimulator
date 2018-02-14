package building;

import buildingsimulator.GameManager;
import buildingsimulator.PhysicsManager;
import com.jme3.math.Vector3f;
import com.jme3.scene.shape.Box;
import java.util.List;
import net.wcomohundro.jme3.csg.CSGShape;

/**
 * Klasa <code>WallsFactory</code> reprezentuje fabrykę służącą do tworzenia 
 * nowych materiałów budowlanych wybranego typu. 
 * @author AleksanderSklorz
 */
public class WallsFactory {
    /**
     * Tworzy podany typ materiału budowlanego, w wybranym miejscu w świecie gry. 
     * @param type typ materiału budowlanego 
     * @param location lokalizacja 
     * @return materiał budowlany 
     */
    public static Wall createWall(WallType type, Vector3f location, Vector3f dimensions){
        Box box = new Box(dimensions.x, dimensions.y, dimensions.z); 
        if(WallType.WALL.equals(type)) 
            return createWall(type, box, location, null, (Vector3f)null);
        else if(WallType.WINDOWS.equals(type)){
            return createWall(type, box, location, new Box[]{createLittleWindow(dimensions), 
                createLittleWindow(dimensions)}, new Vector3f(-dimensions.x * 0.37f, 0, dimensions.z * 0.37f),
                new Vector3f(dimensions.x * 0.37f, 0, dimensions.z * 0.37f));
        }else if(WallType.ONE_BIG_WINDOW.equals(type)){
            return createWall(type, box, location, new Box[]{new Box(dimensions.x * 0.23f, 0.2f,
                    dimensions.z * 0.46f)}, new Vector3f(0, 0, dimensions.z * 0.37f));
        }else if(WallType.ONE_BIGGER_WINDOW.equals(type)){
            return createWall(type, box, location, new Box[]{createLittleWindow(dimensions), 
                new Box(dimensions.x * 0.3f, 0.2f, dimensions.z * 0.46f)},
                    new Vector3f(-dimensions.x * 0.37f, 0, dimensions.z * 0.37f),
                    new Vector3f(dimensions.x * 0.37f, 0, dimensions.z * 0.37f));
        }else if(WallType.FRONT_DOOR.equals(type)){
            return createWall(type, box, location, new Box[]{new Box(dimensions.x * 0.23f,
                    0.2f, dimensions.z * 0.74f), createLittleWindow(dimensions)},
                    new Vector3f(dimensions.x * 0.37f, 0, -dimensions.z * 0.14f),
                    new Vector3f(-dimensions.x * 0.37f, 0, dimensions.z * 0.37f));
        }else if(WallType.DOOR.equals(type)) {
            return createWall(type, box, location, new Box[]{new Box(dimensions.x * 0.23f,
                    0.2f, dimensions.z * 0.74f)}, new Vector3f(dimensions.x * 0f, 
                    0, -dimensions.z * 0.14f));
        }
        return null; 
    }
    
    public static void restoreWalls(List<Wall> walls) {
        int wallsCount = walls.size();
        int wallModeCount =  WallMode.values().length;
        for(int i = 0; i < wallsCount; i++) {
            Wall wall = walls.get(i);
            GameManager.addToGame(wall);
            wall.initCollisionListener();
            for(int j = 0; j < wallModeCount; j++) {
                PhysicsManager.addPhysicsToGame(wall, j);
            }
            //wall.swapControl(WallMode.LOOSE);
        }
    }
    
    private static Box createLittleWindow(Vector3f dimensions) {
        return new Box(dimensions.x * 0.19f, 0.2f, dimensions.z * 0.46f);
    }
    
    private static Wall createWall(WallType type, Box wallBox, Vector3f wallLocation, Box[] elements,
            Vector3f... locations) {
        CSGShape[] shapes = null;
        if(elements != null) {
            shapes = new CSGShape[elements.length];
            for(int i = 0; i < elements.length; i++) {
                shapes[i] = new CSGShape("Box" + i, elements[i]);
                shapes[i].setLocalTranslation(locations[i]);
            }
        }
        return new Wall(type, new CSGShape("Box", wallBox), wallLocation, shapes);
    }
}
