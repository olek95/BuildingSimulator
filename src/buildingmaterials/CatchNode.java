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
    CENTER, // do usuniecia? 
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
     * @perpendicularity true jeśli ściany są położone prostopadle względem siebie, 
     * false w przeciwnym razie
     * @return lokalizacja węzła 
     */
    public static Vector3f calculateTranslation(CatchNode type, Wall wall1, Wall wall2,
            boolean perpendicularity){
        boolean init = false; 
        if(wall2 == null){
            init = true; 
            wall2 = wall1;
        } 
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
            case SOUTH_0: 
                return new Vector3f(-getProperDimension(wall2, perpendicularity, false, init)
                        + wall1.getLength(), wall2.getWidth(), -wall1.getHeight()
                        - getProperDimension(wall2, perpendicularity, true, false));
            case SOUTH_1: 
                return new Vector3f(getProperDimension(wall2, perpendicularity, false, init)
                        - wall1.getLength(), wall2.getWidth(), 
                        - wall1.getHeight() - getProperDimension(wall2, perpendicularity, true, false));
            case NORTH_0:
                return new Vector3f(-getProperDimension(wall2, perpendicularity, false, init)
                        + wall1.getLength(), wall2.getWidth(), wall1.getHeight()
                        + getProperDimension(wall2, perpendicularity, true, false));
            case NORTH_1:
                return new Vector3f(getProperDimension(wall2, perpendicularity, false, init)
                        - wall1.getLength(), wall2.getWidth(), wall1.getHeight()
                        + getProperDimension(wall2, perpendicularity, true, false));
            case EAST_0: 
                return new Vector3f(-getProperDimension(wall2, perpendicularity, false, false)
                        - wall1.getLength(), wall1.getWidth(), wall1.getHeight()
                        - getProperDimension(wall2, perpendicularity, true, init));
            case EAST_1: 
                return new Vector3f(-getProperDimension(wall2, perpendicularity, false, false)
                        - wall1.getLength(), wall1.getWidth(), -wall1.getHeight()
                        + getProperDimension(wall2, perpendicularity, true, init)); 
            case WEST_0: 
                return new Vector3f(getProperDimension(wall2, perpendicularity, false, false) 
                        + wall1.getLength(), wall1.getWidth(), wall1.getHeight()
                        - getProperDimension(wall2, perpendicularity, true, init));
            case WEST_1: 
                return new Vector3f(getProperDimension(wall2, perpendicularity, false, false) 
                        + wall1.getLength(), wall1.getWidth(), -wall1.getHeight()
                        + getProperDimension(wall2, perpendicularity, true, init));
        }
        return null; 
    }
    
    /**
     * Poprawia położenie dla elementu sufitu. 
     * @param hitWall dotknięta ściana, do której dołączamy kawałek sufitu  
     * @param wall kawałek sufitu 
     * @param location aktualne położenie pomocniczego węzła 
     */
    public static void correctLocationForCeiling(Wall hitWall, Wall wall, Vector3f location) {
        switch(CatchNode.valueOf(hitWall.getParent().getName())){
            case UP:
                location.addLocal(0, wall.getHeight(), 0);
                break;
            case BOTTOM: 
                location.addLocal(0, -wall.getHeight(), 0);
                break;
            case LEFT: 
                location.addLocal(0, -wall.getLength(), 0);
                break;
            case RIGHT: 
                location.addLocal(0, wall.getLength(), 0);
                break;
        }
    }
    
    private static float getProperDimension(Wall wall, boolean perpendicularity, boolean height,
            boolean init){
        if(init) return 0;
        if(height) return perpendicularity ? wall.getLength() : wall.getHeight();
        return perpendicularity ? wall.getHeight() : wall.getLength();
    }
}
