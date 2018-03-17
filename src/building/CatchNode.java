package building;

import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import java.util.List;

/**
 * Typ wyliczeniowy <code>CatchNode</code> zawiera nazwy wszystkich możliwych 
 * pomocniczych węzłów dla ścian budynków. Są to węzły zarówno znajdujące się 
 * na ścianie, jak i poza nią. Każdy węzeł symbolizuje inną stronę ściany (kierunek świata). 
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
     * Oblicza położenie pomocniczych węzłów. Jeśli podana jest też ściana druga
     * to brane są pod uwagę również jej dane. 
     * @param type typ węzła 
     * @param wall1 ściana dla której tworzony jest węzeł 
     * @param wall2 ściana z której dostaje się dodatkowe dane 
     * @param perpendicularity true jeśli ściany są położone prostopadle względem siebie, 
     * false w przeciwnym razie
     * @param ceiling true jeśli sufit, false w przeciwnym przypadku 
     * @param translate dodatkowe przesunięcie 
     * @param protruding true jeśli łączy dwie podłogi ze sobą, false w przeciwnym przypadku 
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
                width1 = wall1.getYExtend();
                return new Vector3f(translate, init ? width1 : width1 + wall2
                        .getZExtend(), -wall1.getZExtend() + (protruding ? 0 :
                        wall2.getYExtend()));
            case UP:
                width1 = wall1.getYExtend(); 
                return new Vector3f(translate, init ? width1 : width1 + wall2
                        .getZExtend(), wall1.getZExtend() - (protruding ? 0 :
                        wall2.getYExtend()));
            case RIGHT: 
                width1 = wall1.getYExtend(); 
                return new Vector3f((protruding ? 0 : wall2.getYExtend()) - wall1.getXExtend(), init ? width1 :
                        width1 + wall2.getZExtend(), translate);
            case LEFT: 
                width1 = wall1.getYExtend(); 
                return new Vector3f(-(protruding ? 0 : wall2.getYExtend()) + wall1.getXExtend(), init ? width1 :
                        width1 + wall2.getZExtend(), translate);
            case SOUTH: 
                return new Vector3f(translate, 0, -wall1.getZExtend() - CatchNode
                        .getProperFoundationsDimension(wall2, perpendicularity,
                        true, init));
            case NORTH: 
                float y = 0, z = 0;
                if(ceiling) {
                    boolean sample = Construction.getWholeConstruction(wall1)
                            .getName().contains("sample");
                    switch(valueOf(wall1.getParent().getName())) {
                        case UP: 
                            if(!sample) {
                                y = wall2.getZExtend() - getProperOffsetForCeiling(wall1);
                            } else {
                                z = wall2.getZExtend() - getProperOffsetForCeiling(wall1);
                            }
                            break; 
                        case BOTTOM:
                            if(!sample) {
                                y = -wall2.getZExtend() + getProperOffsetForCeiling(wall1);
                            } else {
                                z = wall2.getZExtend() - getProperOffsetForCeiling(wall1);
                            }
                            break; 
                        case EAST: 
                            if(!sample) {
                                y = wall2.getXExtend() - getProperOffsetForCeiling(wall1);
                            } else {
                                z = wall2.getXExtend() - getProperOffsetForCeiling(wall1);
                            }
                            break; 
                        default: 
                            if(!sample) {
                                y = -wall2.getXExtend() + getProperOffsetForCeiling(wall1);
                            } else {
                                z = -wall2.getXExtend() + getProperOffsetForCeiling(wall1);
                            }
                            
                    }
                    if(isOnTheOtherSide(wall1, wall2)) { 
                        if(!sample) y = -y;
                    }
                    if(!sample) z = wall1.getZExtend();
                    else y = wall1.getZExtend() + wall2.getYExtend(); 
                } else {
                    y = 0;
                    z = wall1.getZExtend() + CatchNode
                            .getProperFoundationsDimension(wall2, perpendicularity,
                            true, init);
                }
                return new Vector3f(translate, y, z);
            case EAST: 
                return new Vector3f(-wall1.getXExtend() - CatchNode
                        .getProperFoundationsDimension(wall2, perpendicularity, false,
                        init), 0, translate); 
            case WEST: 
                return new Vector3f(wall1.getXExtend() + CatchNode
                        .getProperFoundationsDimension(wall2, perpendicularity, false,
                        init), 0, translate); 
        }
        return null; 
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
        if(z) return perpendicularity ? wall.getXExtend() : wall.getZExtend();
        return perpendicularity ? wall.getZExtend() : wall.getXExtend();
    }
    
    private static boolean isOnTheOtherSide(Wall wall1, Wall wall2) {
        Vector3f wall2Location = wall2.getWorldTranslation();
        return wall2Location.distance(wall1.getWorldTranslation().subtract(0, 
                wall1.getZExtend() + wall2.getYExtend(), 0)) < 
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
                distance(outerCatchNodeLocation) ? wall.getYExtend() : 0; 
    } 
}
