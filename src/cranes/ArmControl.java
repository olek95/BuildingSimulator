package cranes;

import building.Wall;
import listeners.BottomCollisionListener;
import settings.Control;
import com.jme3.input.controls.AnalogListener;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import settings.Control.Actions;
import buildingsimulator.Controllable;
import buildingsimulator.GameManager;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.input.controls.ActionListener;
/**
 * Klasa <code>ArmControl</code> jest klasą abstrakcyjną dla wszystkich klas 
 * reprezentujących sterowanie ramieniem dźwigu w grze. Implementuje ona interfejs
 * AnalogListener, dzięki czemu sterowanie ramieniem dźwigu każdego typu odbywa 
 * się w metodzie onAnalog tej klasy. 
 */
public abstract class ArmControl implements AnalogListener, Controllable, ActionListener{
    private Hook hook;
    private float maxHandleHookDisplacement, minHandleHookDisplacement,
            maxArmHeight = 0, minArmHeight = 0;
    private boolean usedNotUsingKey = false, changingHookLocation = false,
            changingArmLocation = false; 
    private Node crane, craneControl;
    private Spatial hookHandle;
    private Actions[] availableActions = { Actions.RIGHT, Actions.LEFT,
        Actions.PULL_OUT, Actions.PULL_IN, Actions.LOWER_HOOK, Actions.HEIGHTEN_HOOK,
        Actions.UP, Actions.DOWN, Actions.ACTION, Actions.ATTACH,
        Actions.VERTICAL_ATTACH, Actions.DETACH, Actions.MERGE, Actions.MERGE_PROTRUDING};
    /**
     * Konstruktor tworzący kabinę. Używany, gdy wartość maksymalnego i 
     * minimalnego przesunięcia uchwytu nie jest znana od początku. Należy 
     * pamiętać, aby przed użyciem kabiny ustawić te wartości. 
     * @param crane dźwig będący właścicielem sterowanego ramienia 
     */
    public ArmControl(Node crane){ 
        this.crane = crane; 
        initCraneArmElements();
    }
    /**
     * Konstruktor tworzący kabinę. Używany, gdy wartość maksymalnego i 
     * minimalnego przesunięcia uchwytu haka jest znana od początku. 
     * @param crane dźwig będący właścicielem sterowanego ramienia 
     * @param maxHandleHookDisplacement maksymalne przesunięcie uchwytu haka 
     * @param minHandleHookDisplacement minimalne przesunięcie uchwytu haka 
     */
    public ArmControl(Node crane, float maxHandleHookDisplacement, float minHandleHookDisplacement,
            float maxArmHeight, float minArmHeight){
        this.crane = crane;
        initCraneArmElements();
        this.maxHandleHookDisplacement = maxHandleHookDisplacement; 
        this.minHandleHookDisplacement = minHandleHookDisplacement;
        this.maxArmHeight = maxArmHeight; 
        this.minArmHeight = minArmHeight;
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
                if(!changingHookLocation) rotate(-tpf / 5);
                break;
            case LEFT:
                if(!changingHookLocation) rotate(tpf / 5);
                break;
            case PULL_OUT:
                if(!changingHookLocation) 
                    moveHandleHook(maxHandleHookDisplacement, true, -tpf / 2);
                break;
            case PULL_IN:
                if(!changingHookLocation)
                    moveHandleHook(minHandleHookDisplacement, false, tpf / 2);
                break;
            case LOWER_HOOK:
                if(!changingArmLocation) hook.lower();
                break;
            case HEIGHTEN_HOOK:
                if(hook.getActualLowering() > 1f && !changingArmLocation)
                    hook.heighten();
                break;
            case UP:
                if(!changingHookLocation) changeArmHeight(maxArmHeight, false);
                break;
            case DOWN:
                Wall attachedObject = (Wall)hook.getAttachedObject();
                if((attachedObject == null && hook.getCollisionListener().isNothingBelow(null))
                        || (attachedObject != null && attachedObject
                        .getCollisionListener().isNothingBelow(null)) 
                        && !changingHookLocation){
                    changeArmHeight(minArmHeight, true); 
                    hook.setRecentlyHitObject(null);
                }
                break;
            case ACTION:
                getOff(name);
        }
        Spatial attachedObject = hook.getAttachedObject(); 
        if(!usedNotUsingKey){
            boolean attachedObjectNotNull = attachedObject != null;
            Wall wall = (Wall)attachedObject; 
            if(attachedObjectNotNull) wall.activateIfInactive();
            if(!name.equals(Actions.LOWER_HOOK.toString()) && !usedNotUsingKey
                    && !name.equals((Actions.DOWN.toString()))){ 
                hook.setRecentlyHitObject(null);
                if(attachedObjectNotNull){
                    wall.setRecentlyHitObject(null);
                    wall.getHitObjects().clear();
                }
            }
        }else usedNotUsingKey = false;
    }
    
    @Override
    public void onAction(String name, boolean isPressed, float tpf){
        Spatial recentlyHitObject;
        if(isPressed) {
            switch(Actions.valueOf(name)) {
                case ATTACH: 
                    recentlyHitObject = hook.getRecentlyHitObject();
                    if(recentlyHitObject != null && recentlyHitObject.getName()
                            .startsWith("Wall"))
                        hook.attach(false);
                    break; 
                case VERTICAL_ATTACH: 
                    recentlyHitObject = hook.getRecentlyHitObject();
                    if(recentlyHitObject != null && recentlyHitObject.getName()
                            .startsWith("Wall"))
                        hook.attach(true);
                    break;
                case DETACH: 
                    hook.detach(false, false);
                    break; 
                case MERGE: 
                    hook.detach(true, false); 
                    break;
                case MERGE_PROTRUDING: 
                    hook.detach(true, true);
            }
            for(int i = 0; i < 8; i++){
                if(name.equals(availableActions[i].toString())){
                    if(i == 4 || i == 5) changingHookLocation = true; 
                    else changingArmLocation = true; 
                }
            }
        } else {
            changingHookLocation = false; 
            changingArmLocation = false; 
        }
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
     * Podnosi lub opuszcza ramię dźwigu. Domyślna implementacja ustawia znacznik 
     * usedNotUsingKey, aby akcja nie była liczona podczas sprawdzania kolizji 
     * haka. 
     * @param limit ograniczenie decydujące na jaką wysokość można podnieść lub opuścić ramię
     * @param lowering true jeśli opuszczamy ramię, false w przeciwnym wypadku 
     */
    protected void changeArmHeight(float limit, boolean lowering){
        usedNotUsingKey = true;
    } 
    
    /**
     * Pozwala na opuszczenie trybu kontroli ramienia dźwigu. Domyślna 
     * implementacja ustawia znacznik usedNotUsingKey, aby akcja nie była liczona
     * podczas sprawdzania kolizji haka. 
     * @param actionName nazwa akcji wychodząca z trybu kontroli dźwigu 
     */
    protected void getOff(String actionName){
        usedNotUsingKey = true;
    };
    
    /**
     * Zwraca hak dźwigu. 
     * @return hak 
     */
    public Hook getHook(){
        return hook;
    }
    
    /**
     * Ustawia hak dźwigu 
     * @param hook hak
     */
    public void setHook(Hook hook){
        this.hook = hook;
    }
    
    /**
     * Ustawia maksymalną odległość na jaką można przesunąć uchwyt haka. 
     * @param max maksymalna odległość przesunięcia uchwytu haka 
     */
    public void setMaxHandleHookDisplacement(float max){
        maxHandleHookDisplacement = max;
    }
    
    /**
     * Ustawia minimalną odległość na jaką można przesunąć uchwyt haka. 
     * @param min minimalna odległość przesunięcia uchwytu haka 
     */
    public void setMinHandleHookDisplacement(float min){
        minHandleHookDisplacement = min;
    }
    
    /**
     * Zwraca maksymalną wysokość, na jaką można podnieść ramię dźwigu. 
     * @return maksymalna wysokość położenia ramienia 
     */
    public float getMaxArmHeight(){
        return maxArmHeight;
    }
    
    /**
     * Zwraca minimalną wysokość, na jaką można podnieść ramię dźwigu. 
     * @return minimalną wysokość położenia ramienia 
     */
    public float getMinArmHeight(){
        return minArmHeight;
    }
    
    /**
     * Zwraca węzeł zawierający wszystkie elementy związane z kontrolą ramienia 
     * dźwigu. Są to wszystkie elementy które poruszają się wraz z ramieniem. 
     * @return węzeł zawierający elementy związane z kontrolą ramienia dźwigu 
     */
    public Node getCraneControl(){
        return craneControl;
    }
    
    /**
     * Zwraca cały dźwig. 
     * @return cały dźwig 
     */
    public Node getCrane(){
        return crane;
    }
    
    /**
     * Zwraca uchwyt na hak. 
     * @return uchwyt na hak
     */
    public Spatial getHookHandle(){
        return hookHandle;
    }
    
    /**
     * Ustawia uchwyt na hak 
     * @param hookHandle uchwyt na hak 
     */
    public void setHookHandle(Spatial hookHandle){
        this.hookHandle = hookHandle;
    }
    
    /**
     * Zwraca wszystkie dostępne akcje, które może wykonać dźwig. 
     * @return wszystkie akcje dźwigu
     */
    @Override
    public Control.Actions[] getAvailableActions(){
        return availableActions;
    }
    
    /**
     * Metoda inicjuje węzeł posiadający elementy składające się na całe sterowane 
     * ramię dźwigu. 
     */
    protected void initCraneArmElements(){
        craneControl = (Node)crane.getChild("craneControl");
    }
    
    /**
     * Obraca hak wraz z przyczepionym do niego obiektem. 
     */
    protected void rotateHook(){
        hook.getRopeHook().getControl(RigidBodyControl.class).setPhysicsRotation(
                getCraneControl().getWorldRotation());
        Spatial attachedObject = hook.getAttachedObject(); 
        if(attachedObject != null) ((Wall)attachedObject).rotateToCrane();
    }
}
