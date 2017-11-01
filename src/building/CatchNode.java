package building;

import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import java.util.List;

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
            boolean perpendicularity, boolean ceiling, float translate, boolean protruding){
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
                        .getHeight(), -wall1.getHeight() + (protruding ? 0 :
                        wall2.getWidth()));
            case UP:
                width1 = wall1.getWidth(); 
                return new Vector3f(translate, init ? width1 : width1 + wall2
                        .getHeight(), wall1.getHeight() - (protruding ? 0 :
                        wall2.getWidth()));
            case RIGHT: 
                width1 = wall1.getWidth(); 
                return new Vector3f((protruding ? 0 : wall2.getWidth()) - wall1.getLength(), init ? width1 :
                        width1 + wall2.getHeight(), translate);
            case LEFT: 
                width1 = wall1.getWidth(); 
                return new Vector3f(-(protruding ? 0 : wall2.getWidth()) + wall1.getLength(), init ? width1 :
                        width1 + wall2.getHeight(), translate);
            case SOUTH: 
                return new Vector3f(translate, 0, -wall1.getHeight() - CatchNode
                        .getProperFoundationsDimension(wall2, perpendicularity,
                        true, init));
            case NORTH: 
                float y, z;
                if(ceiling) {
                    switch(valueOf(wall1.getParent().getName())) {
                        case UP: 
                            System.out.println(1);
                            y = wall2.getHeight() - getProperOffsetForCeiling(wall1);
                            break; 
                        case BOTTOM:
                            System.out.println(2);
                            y = -wall2.getHeight() + getProperOffsetForCeiling(wall1);
                            break; 
                        case EAST: 
                            y = wall2.getLength() - getProperOffsetForCeiling(wall1);
                            break; 
                        default: 
                            y = -wall2.getLength() + getProperOffsetForCeiling(wall1);
                            
                    }
                    if(isOnTheOtherSide(wall1, wall2)) y = -y;
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
                        .getProperFoundationsDimension(wall2, perpendicularity, false,
                        init), 0, translate); 
            case WEST: 
                return new Vector3f(wall1.getLength() + CatchNode
                        .getProperFoundationsDimension(wall2, perpendicularity, false,
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
    
    private static boolean isNearlyMatchedSize(Wall wall1, Wall wall2) {
        float difference = ((Wall)wall1.getParent().getParent()).getHeight()
                - wall2.getHeight(), width = wall1.getWidth(); 
        return difference > width - 0.1f && difference < width + 0.1f;
    }
    
    private static boolean isOnTheOtherSide(Wall wall1, Wall wall2) {
        Vector3f wall2Location = wall2.getWorldTranslation();
        return wall2Location.distance(wall1.getWorldTranslation().subtract(0, 
                wall1.getHeight() + wall2.getWidth(), 0)) < 
                wall2Location.distance(wall1.getParent().getParent()
                .getWorldTranslation());
    }
    
    private static float getProperOffsetForCeiling(Wall wall) {
        Vector3f wallLocation = wall.getWorldTranslation();
        Node catchNode = wall.getParent();
        List<Spatial> floorChildren = catchNode.getParent().getChildren();
        Vector3f outerCatchNodeLocation = floorChildren.get(floorChildren
                .indexOf(catchNode) + 4).getWorldTranslation();
        return wallLocation.distance(catchNode.getWorldTranslation().clone()
                .setY(outerCatchNodeLocation.y)) < wallLocation.
                distance(outerCatchNodeLocation) ? wall.getWidth() : 0; 
    } 
}
