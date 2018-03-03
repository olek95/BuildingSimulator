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
    WALL(5), 
    WINDOWS(10),
    ONE_BIG_WINDOW(10),
    ONE_BIGGER_WINDOW(10),
    FRONT_DOOR(10),
    DOOR(10);
    private int price;
    private WallType(int price) { this.price = price; }
    
    /**
     * Zwraca cenę.
     * @return cena 
     */
    public int getPrice() { return price; }
}
