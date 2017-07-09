package buildingmaterials;

/**
 * Typ wyliczeniowy <code>WallType</code> reprezentuje dostępne typy materiałów 
 * budowlanych. Dostępne typy to: WALL - zwykła ściana, WINDOWS - ściana z dwoma
 * oknami, ONE_BIG_WINDOW - ściana z jednym dużym oknem, ONE_BIGGER_WINDOW - ściana
 * z jednym dużym i jednym małym oknem, DOOR - ściana z drzwiami i oknem, . 
 * @author AleksanderSklorz
 */
public enum WallType {
    WALL, 
    WINDOWS,
    ONE_BIG_WINDOW,
    ONE_BIGGER_WINDOW,
    DOOR;
}
