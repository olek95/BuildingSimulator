package building;

import texts.Translator;

/**
 * Typ wyliczeniowy <code>WallType</code> reprezentuje dostępne typy materiałów 
 * budowlanych. Dostępne typy to: WALL - zwykła ściana, WINDOWS - ściana z dwoma
 * oknami, ONE_BIG_WINDOW - ściana z jednym dużym oknem, ONE_BIGGER_WINDOW - ściana
 * z jednym dużym i jednym małym oknem, DOOR - ściana z drzwiami i oknem. Każdy
 * typ ściany ma swoją cenę. 
 * @author AleksanderSklorz
 */
public enum WallType {
    WALL(10), 
    WINDOWS(5),
    ONE_BIG_WINDOW(5),
    ONE_BIGGER_WINDOW(5),
    FRONT_DOOR(5),
    DOOR(5);
    private int price;
    private WallType(int price) { this.price = price; }
    
    /**
     * Zwraca cenę.
     * @return cena 
     */
    public int getPrice() { return price; }
}
