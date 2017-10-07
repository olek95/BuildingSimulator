package building;

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
    SOUTH,
    NORTH, 
    EAST,
    WEST;
    
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
            boolean perpendicularity, boolean ceiling, float translate){
        boolean init = false; 
        if(wall2 == null){
            init = true; 
            wall2 = wall1;
        } 
        float width1;
        switch(type){
            case BOTTOM: 
                width1 = wall1.getWidth();
                return new Vector3f(translate, init ? width1 : width1 + wall2
                        .getHeight(), -wall1.getHeight() + wall2.getWidth());
            case UP:
                width1 = wall1.getWidth(); 
                return new Vector3f(translate, init ? width1 : width1 + wall2
                        .getHeight(), wall1.getHeight() - wall2.getWidth()); 
            case RIGHT: 
                width1 = wall1.getWidth(); 
                return new Vector3f(wall2.getWidth() - wall1.getLength(), init ? width1 :
                        width1 + wall2.getHeight(), translate);
            case LEFT: 
                width1 = wall1.getWidth(); 
                return new Vector3f(-wall2.getWidth() + wall1.getLength(), init ? width1 :
                        width1 + wall2.getHeight(), translate);
            case SOUTH: 
                return new Vector3f(translate, 0, -wall1.getHeight() - CatchNode
                        .getProperFoundationsDimension(wall2, perpendicularity,
                        true, init));
            case NORTH: 
                float y, z;
                if(ceiling) {
                    String catchNodeName = wall1.getParent().getName();
                    switch(valueOf(catchNodeName)) {
                        case UP: 
                            y = wall2.getHeight() - wall1.getWidth();
                            break; 
                        case BOTTOM: 
                            y = -wall2.getHeight() + wall1.getWidth();
                            break; 
                        case EAST: 
                            y = wall2.getLength() - wall1.getWidth();
                            break; 
                        default: 
                            y = -wall2.getLength() + wall1.getWidth();
                            
                    }
                    z = wall1.getHeight();
                } else {
                    y = 0;
                    z = wall1.getHeight() + CatchNode
                            .getProperFoundationsDimension(wall2, perpendicularity,
                            true, init);
                }
                return new Vector3f(translate, y, z);
            case EAST: 
                return new Vector3f(-wall1.getLength() - CatchNode
                        .getProperFoundationsDimension(wall2, perpendicularity, true,
                        init), 0, translate); 
            case WEST: 
                return new Vector3f(wall1.getLength() + CatchNode
                        .getProperFoundationsDimension(wall2, perpendicularity, true,
                        init), 0, translate); 
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
    
    /**
     * Zwraca odpowiedni wymiar w przypadku, gdy możliwe jest połączenie dwóch 
     * elementów w sposób równoległy bądź prostopadły np. podczas łączenia fundamentów.
     * @param wall ściana którą dołącza się 
     * @param perpendicularity true jeśli elementy są względem siebie prostopadł, false w przeciwnym przypadku 
     * @param z true jeśli łączymy względem osi z, false jeśli względem osi x 
     * @param init true jeśli są to pomocnicze węzły określające kierunki i tworzone na początku 
     * @return poprawny wymiar 
     */
    public static float getProperFoundationsDimension(Wall wall, 
            boolean perpendicularity, boolean z, boolean init){
        if(init) return 0;
        if(z) return perpendicularity ? wall.getLength() : wall.getHeight();
        return perpendicularity ? wall.getHeight() : wall.getLength();
    }
}
