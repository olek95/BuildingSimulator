package building;

import com.jme3.math.Vector3f;

/**
 * Klasa <code>WallData</code> reprezentuje dane potrzebne do postawienia danej 
 * ściany podczas tworzenia gotowego budynku. Wykorzystywane w budynkach przykładowych,
 * tworzonych programowo. 
 * @author AleksanderSklorz 
 */
public class WallData {
    private WallType type;
    private boolean perpendicularity = false;
    private CatchNode catchNode; 
    private WallMode mode; 
    private Vector3f dimensions;
    private boolean protruding; 
    private boolean ceiling = false; 
    private Wall additionalHitWall; 
    public WallData(WallType type, CatchNode catchNode, WallMode mode, Vector3f dimensions,
            boolean protruding) {
        this.type = type; 
        this.catchNode = catchNode; 
        this.mode = mode;
        this.dimensions = dimensions;
        this.protruding = protruding;
    }
    
    public WallData(WallType type, boolean perpendicularity, CatchNode catchNode,
            WallMode mode, Vector3f dimensions, boolean protruding) {
        this.type = type; 
        this.catchNode = catchNode; 
        this.mode = mode;
        this.protruding = protruding;
        this.dimensions = dimensions;
        this.perpendicularity = perpendicularity; 
    }
    
    public WallData(WallType type, CatchNode catchNode,
            Vector3f dimensions, boolean protruding, boolean ceiling, Wall additionalHitWall) {
        this.type = type; 
        this.catchNode = catchNode; 
        this.mode = WallMode.HORIZONTAL;
        this.protruding = protruding;
        this.dimensions = dimensions;
        this.ceiling = ceiling;
        this.additionalHitWall = additionalHitWall; 
    }
    
    /**
     * Zwraca typ ściany. 
     * @return typ ściany 
     */
    public WallType getType() { return type; }
    
    /**
     * Określa czy jest prostopadła do krawędzi.
     * @return prostopadłość 
     */
    public boolean isPerpendicularity() { return perpendicularity; }
    
    /**
     * Zwraca węzeł zaczepu. 
     * @return węzeł zaczepu 
     */
    public CatchNode getCatchNode() { return catchNode; }
    
    /**
     * Zwraca tryb w którym jest ściana np. HORIZONTAL
     * @return tryb 
     */
    public WallMode getMode() { return mode; }
    
    /**
     * Określa czy znajduje się na krawędzi, pomiędzy dwoma podłogami 
     * @return informacja czy znajduje się na krawędzi 
     */
    public boolean isProtruding() { return protruding; }
    
    /**
     * Rozmiar ściany 
     * @return rozmiar ściany 
     */
    public Vector3f getDimensions() { return dimensions; }
    
    /**
     * Określa czy jest to sufit 
     * @return informacja czy jest to sufit 
     */
    public boolean isCeiling() { return ceiling; }
    
    /**
     * Dodatkowo uderzona ściana (używane gdy stawia się sufit bo wtedy są dwie 
     * ściany)
     * @return dodatkowo uderzona śćiana  
     */
    public Wall getAdditionalHitWall() { return additionalHitWall; }
}
