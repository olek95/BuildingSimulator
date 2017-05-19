package buildingsimulator;

import com.jme3.input.controls.AnalogListener;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import buildingsimulator.Control.Actions;

/**
 * Klasa <code>Cabin</code> jest klasą abstrakcyjną dla wszystkich klas kabin 
 * w grze. Implementuje ona interfejs AnalogListener, dzięki czemu sterowanie 
 * kabiny każdego typu odbywa się w metodzie onAnalog tej klasy. 
 */
public abstract class Cabin implements AnalogListener, Controllable{
    protected Hook hook;
    protected float maxHandleHookDisplacement, minHandleHookDisplacement;
    protected final float maxArmHeight = 0.6f, minArmHeight = 0f;
    private boolean usedNotUsingKey = false; 
    protected Node crane, craneControl;
    protected Spatial hookHandle;
    private Actions[] availableActions = {Actions.RIGHT, Actions.LEFT,
        Actions.PULL_OUT, Actions.PULL_IN, Actions.LOWER_HOOK, Actions.HEIGHTEN_HOOK,
        Actions.UP, Actions.DOWN};
    /**
     * Konstruktor tworzący kabinę. Używany, gdy wartość maksymalnego i 
     * minimalnego przesunięcia uchwytu nie jest znana od początku. Należy 
     * pamiętać, aby przed użyciem kabiny ustawić te wartości. 
     * @param crane dźwig będący właścicielem sterowanego ramienia 
     */
    public Cabin(Node crane){ 
        this.crane = crane; 
        initCraneCabinElements();
    }
    /**
     * Konstruktor tworzący kabinę. Używany, gdy wartość maksymalnego i 
     * minimalnego przesunięcia uchwytu haka jest znana od początku. 
     * @param crane dźwig będący właścicielem sterowanego ramienia 
     * @param maxHandleHookDisplacement maksymalne przesunięcie uchwytu haka 
     * @param minHandleHookDisplacement minimalne przesunięcie uchwytu haka 
     */
    public Cabin(Node crane, float maxHandleHookDisplacement, float minHandleHookDisplacement){
        this.crane = crane;
        initCraneCabinElements();
        this.maxHandleHookDisplacement = maxHandleHookDisplacement; 
        this.minHandleHookDisplacement = minHandleHookDisplacement;
    }
    /**
     * Zezwala na sterowanie ramieniem żurawia. Żuraw może się obracać, 
     * przesuwać uchwyt haka oraz opuszczać i wciągać hak. Dodatkowo ustawia 
     * ostatnio dotknięty element przez hak, jako null, jeśli istnieje 
     * pradopodobieństwo, że hak już niczego nie dotyka. 
     * @param name nazwa akcji 
     * @param value
     * @param tpf 
     */
    @Override
    public void onAnalog(String name, float value, float tpf) {
        switch(Actions.valueOf(name)){
            case RIGHT:
                rotate(-tpf / 5);
                break;
            case LEFT:
                rotate(tpf / 5);
                break;
            case PULL_OUT:
                moveHandleHook(maxHandleHookDisplacement, true, -tpf);
                break;
            case PULL_IN:
                moveHandleHook(minHandleHookDisplacement, false, tpf);
                break;
            case LOWER_HOOK:
                hook.lower();
                break;
            case HEIGHTEN_HOOK:
                if(hook.getHookLowering() > 1f)
                    hook.heighten();
                break;
            case UP:
                changeArmHeight(maxArmHeight, false);
                break;
            case DOWN:
                changeArmHeight(minArmHeight, true); 
        }
        if(!name.equals("Lower hook") && !usedNotUsingKey) hook.setRecentlyHitObject(null);
        else usedNotUsingKey = false;
    }
    
    /**
     * Obraca ramie dźwigu. 
     * @param yAngle kąt obrotu według osi Y. 
     */
    protected abstract void rotate(float yAngle);
    
    /**
     * Przesuwa uchwyt haka. 
     * @param limit ograniczenie decydujące jak daleko można przesunąć hak 
     * @param movingForward true jeśli przesuwamy uchwyt haka do przodu, false jeśli do tyłu 
     * @param speed prędkość przesuwania uchwytu haka 
     */
    protected abstract void moveHandleHook(float limit, boolean movingForward, float speed);
    
    /**
     * Podnosi lub opuszcza ramię dźwigu. 
     * @param limit ograniczenie decydujące na jaką wysokość można podnieść lub opuścić ramię
     * @param lowering true jeśli opuszczamy ramię, false w przeciwnym wypadku 
     */
    protected void changeArmHeight(float limit, boolean lowering){
        usedNotUsingKey = true;
    } 
    
    /**
     * Zwraca hak dźwigu. 
     * @return hak 
     */
    public Hook getHook(){
        return hook;
    }
    
    /**
     * Metoda inicjuje węzeł posiadający elementy składające się na całe sterowane 
     * ramię dźwigu. 
     */
    protected void initCraneCabinElements(){
        craneControl = (Node)crane.getChild("craneControl");
    }
    
    @Override
    public Control.Actions[] getAvailableActions(){
        return availableActions;
    }
}
