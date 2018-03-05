package building;

import buildingsimulator.GameManager;
import buildingsimulator.PhysicsManager;
import com.jme3.math.ColorRGBA;
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
     * @param dimensions rozmiary ściany 
     * @param mass waga ściany 
     * @param dummy true jeśli sciana sztuczna (nie stosowana do budowy), false 
     * w przeciwnym przypadku 
     * @return materiał budowlany 
     */
    public static AbstractWall createWall(WallType type, Vector3f location, Vector3f dimensions,
            float mass, boolean dummy, ColorRGBA color){
        Box box = new Box(dimensions.x, dimensions.y, dimensions.z); 
        if(WallType.WALL.equals(type)) 
            return createWall(mass, dummy, type, box, location, null, color, (Vector3f)null);
        else if(WallType.WINDOWS.equals(type)){
            return createWall(mass, dummy, type, box, location, 
                    new Box[]{createLittleWindow(dimensions), createLittleWindow(dimensions)},
                    color,  new Vector3f(-dimensions.x * 0.37f, 0, dimensions.z * 0.37f), 
                    new Vector3f(dimensions.x * 0.37f, 0, dimensions.z * 0.37f));
        }else if(WallType.ONE_BIG_WINDOW.equals(type)){
            return createWall(mass, dummy, type, box, location, 
                    new Box[]{new Box(dimensions.x * 0.23f, 0.2f, dimensions.z * 0.46f)},
                    color, new Vector3f(0, 0, dimensions.z * 0.37f));
        }else if(WallType.ONE_BIGGER_WINDOW.equals(type)){
            return createWall(mass, dummy, type, box, location, 
                    new Box[]{createLittleWindow(dimensions), new Box(dimensions.x * 0.3f, 0.2f,
                    dimensions.z * 0.46f)}, color, new Vector3f(-dimensions.x * 0.37f,
                    0, dimensions.z * 0.37f), new Vector3f(dimensions.x * 0.37f, 0,
                    dimensions.z * 0.37f));
        }else if(WallType.FRONT_DOOR.equals(type)){
            return createWall(mass, dummy, type, box, location, 
                    new Box[]{new Box(dimensions.x * 0.23f, 0.2f, dimensions.z * 0.74f),
                        createLittleWindow(dimensions)}, color, 
                        new Vector3f(dimensions.x * 0.37f, 0, -dimensions.z * 0.14f), 
                        new Vector3f(-dimensions.x * 0.37f, 0, dimensions.z * 0.37f));
        }else if(WallType.DOOR.equals(type)) {
            return createWall(mass, dummy, type, box, location, 
                    new Box[]{new Box(dimensions.x * 0.23f, 0.2f, dimensions.z * 0.74f)},
                    color, new Vector3f(dimensions.x * 0f,  0, -dimensions.z * 0.14f));
        }
        return null; 
    }
    
    /**
     * Przywraca ściany z listy do gry. 
     * @param walls lista ścian 
     */
    public static void restoreWalls(List<Wall> walls) {
        int wallsCount = walls.size();
        int wallModeCount =  WallMode.values().length;
        for(int i = 0; i < wallsCount; i++) {
            Wall wall = walls.get(i);
            GameManager.addToScene(wall);
            wall.initCollisionListener();
            for(int j = 0; j < wallModeCount; j++) {
                PhysicsManager.addPhysicsToGame(wall, j);
            }
            wall.swapControl(wall.getActualMode());
        }
    }
    
    private static Box createLittleWindow(Vector3f dimensions) {
        return new Box(dimensions.x * 0.19f, 0.2f, dimensions.z * 0.46f);
    }
    
    private static AbstractWall createWall(float mass, boolean dummy, 
            WallType type, Box wallBox, Vector3f wallLocation, Box[] elements, 
            ColorRGBA color, Vector3f... locations) {
        CSGShape[] shapes = null;
        if(elements != null) {
            shapes = new CSGShape[elements.length];
            for(int i = 0; i < elements.length; i++) {
                shapes[i] = new CSGShape("Box" + i, elements[i]);
                shapes[i].setLocalTranslation(locations[i]);
            }
        }
        return dummy ? new DummyWall(type, new CSGShape("Box", wallBox), 
                wallLocation, mass, color, shapes) : new Wall(type, 
                new CSGShape("Box", wallBox), wallLocation, mass, color, shapes);
    }
}
