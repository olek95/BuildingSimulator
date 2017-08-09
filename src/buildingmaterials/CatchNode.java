package buildingmaterials;

import com.jme3.math.Vector3f;

/**
 * Typ wyliczeniowy <code>CatchNode</code> zawiera nazwy wszystkich możliwych 
 * pomocniczych węzłów dla ścian budynków. Są to węzły zarówno znajdujące się 
 * na ścianie, jak i poza nią. 
 * @author AleksanderSklorz 
 */
public enum CatchNode {
    BOTTOM, 
    UP, 
    RIGHT, 
    LEFT, 
    CENTER, 
    SOUTH,
    NORTH,
    EAST,
    WEST,
    SOUTH_0, 
    SOUTH_1,
    NORTH_0, 
    NORTH_1,
    EAST_0, 
    EAST_1, 
    WEST_0, 
    WEST_1;
    
    /**
     * Oblicza położenie pomocniczych węzłów dla pierwszej ściany. Jeśli podana jest 
     * też ściana druga to bierzemy pod uwagę również jej dane. 
     * @param type typ węzła 
     * @param wall1 ściana dla której liczymy węzeł 
     * @param wall2 ściana z której dostajemy dodatkowe dane 
     * @return lokalizacja węzła 
     */
    public static Vector3f calculateTranslation(CatchNode type, Wall wall1, Wall wall2){
        if(wall2 == null) wall2 = wall1; 
        float width1;
        switch(type){
            case BOTTOM: 
                width1 = wall1.getWidth();
                return new Vector3f(0, width1, -wall1.getHeight() + width1);
            case UP:
                width1 = wall1.getWidth(); 
                return new Vector3f(0, width1, wall1.getHeight() - width1); 
            case RIGHT: 
                width1 = wall1.getWidth(); 
                return new Vector3f(-wall1.getLength() + width1, width1, 0);
            case LEFT: 
                width1 = wall1.getWidth(); 
                return new Vector3f(wall1.getLength() - width1, width1, 0);
            case CENTER:
                return Vector3f.ZERO;
            case SOUTH:
                return new Vector3f(0, wall1.getWidth(), -wall1.getHeight() - wall2.getHeight());
            case NORTH: 
                return new Vector3f(0, wall1.getWidth(), wall1.getHeight() + wall2.getHeight());
            case EAST:
                return new Vector3f(-wall1.getLength() - wall2.getLength(), wall1.getWidth(), 0);
            case WEST: 
                return new Vector3f(wall1.getLength() + wall2.getLength(), wall1.getWidth(), 0);
            case SOUTH_0: 
                return new Vector3f(wall2.getHeight(), wall2.getWidth(), 
                        -wall2.getLength() + wall1.getHeight());
            case SOUTH_1: 
                return new Vector3f(-wall2.getHeight(), wall2.getWidth(), 
                        -wall2.getLength() + wall1.getHeight());
            case NORTH_0:
                return new Vector3f(wall2.getHeight(), wall2.getWidth(), 
                        wall2.getLength() - wall1.getHeight());
            case NORTH_1:
                return new Vector3f(-wall2.getHeight(), wall2.getWidth(), 
                        wall2.getLength() - wall1.getHeight());
            case EAST_0: 
                return new Vector3f(-wall1.getLength() - wall2.getHeight(), 
                        wall1.getWidth(), wall2.getLength());
            case EAST_1: 
                return new Vector3f(-wall1.getLength() - wall2.getHeight(), 
                        wall1.getWidth(), -wall2.getLength()); 
            case WEST_0: 
                return new Vector3f(wall1.getLength() + wall2.getHeight(), 
                        wall1.getWidth(), wall2.getLength());
            case WEST_1: 
                return new Vector3f(wall1.getLength() + wall2.getHeight(), 
                        wall1.getWidth(), -wall2.getLength());
        }
        return null; 
    }
}
