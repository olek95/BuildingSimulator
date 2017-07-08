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
        else if(WallType.WINDOW.equals(type)){
            CSGShape window1 = new CSGShape("Box0", new Box(1, 0.2f, 1)),
                    window2 = new CSGShape("Box1", new Box(1, 0.2f, 1));
            window1.setLocalTranslation(-2f, 0, 1);
            window2.setLocalTranslation(2f, 0, 1);
            return new Wall(new CSGShape("Box", new Box(4, 0.2f, 2.5f)), location,
                    window1, window2); 
        }else if(WallType.DOOR.equals(type)){
            CSGShape door = new CSGShape("Box", new Box(1, 0.2f, 2));
            door.setLocalTranslation(0, 0, -0.5f);
            return new Wall(new CSGShape("Box", new Box(4, 0.2f, 2.5f)), location,
                    door);
        } 
        return null; 
    }
}
