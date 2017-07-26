package buildingmaterials;

import com.jme3.math.Vector3f;
import com.jme3.scene.shape.Box;
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
    public static Wall createWall(WallType type, Vector3f location){
        if(WallType.WALL.equals(type)) 
            return new Wall(new CSGShape("Box", new Box(4, 0.2f, 2.5f)), location); 
        else if(WallType.WINDOWS.equals(type)){
            CSGShape window1 = new CSGShape("Box0", new Box(1, 0.2f, 1.25f)),
                    window2 = new CSGShape("Box1", new Box(1, 0.2f, 1.25f));
            window1.setLocalTranslation(-2f, 0, 1);
            window2.setLocalTranslation(2f, 0, 1);
            return new Wall(new CSGShape("Box", new Box(4, 0.2f, 2.5f)), location,
                    window1, window2); 
        }else if(WallType.ONE_BIG_WINDOW.equals(type)){
            CSGShape window = new CSGShape("Box", new Box(1.25f, 0.2f, 1.25f));
            window.setLocalTranslation(0, 0, 1);
            return new Wall(new CSGShape("Box", new Box(4, 0.2f, 2.5f)), location,
                    window);
        }else if(WallType.ONE_BIGGER_WINDOW.equals(type)){
            CSGShape window1 = new CSGShape("Box0", new Box(1, 0.2f, 1.25f)),
                    window2 = new CSGShape("Box1", new Box(1.6f, 0.2f, 1.25f));
            window1.setLocalTranslation(-2f, 0, 1);
            window2.setLocalTranslation(2f, 0, 1);
            return new Wall(new CSGShape("Box", new Box(4, 0.2f, 2.5f)), location,
                    window1, window2); 
        }else if(WallType.DOOR.equals(type)){
            CSGShape door = new CSGShape("Box0", new Box(1.25f, 0.2f, 2));
            CSGShape window = new CSGShape("Box1", new Box(1, 0.2f, 1.25f));
            door.setLocalTranslation(2, 0, -0.4f);
            window.setLocalTranslation(-2, 0, 1);
            return new Wall(new CSGShape("Box", new Box(4, 0.2f, 2.5f)), location,
                    door, window);
        }else if(WallType.SMALL_WALL.equals(type)){
            return new Wall(new CSGShape("Box", new Box(2.5f, 0.2f, 2.3f)), location);
        }
        return null; 
    }
}
