package buildingsimulator;

import authorization.User;
import billboard.Billboard;
import building.WallType;
import building.WallsFactory;
import com.jme3.audio.AudioNode;
import cranes.Hook;
import cranes.CraneAbstract;
import com.jme3.bounding.BoundingBox;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.collision.shapes.CompoundCollisionShape;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.bullet.joints.HingeJoint;
import com.jme3.bullet.util.CollisionShapeFactory;
import com.jme3.cinematic.Cinematic;
import com.jme3.light.DirectionalLight;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import cranes.crane.Crane;
import cranes.mobileCrane.MobileCrane;
import java.util.ArrayList;
import menu.HUD;
import menu.MenuFactory;
import menu.MenuTypes;
import net.wcomohundro.jme3.csg.CSGGeometry;

/**
 * Klasa <code>GameManager</code> reprezentuje zarządcę gry, posiadającego 
 * metody ułatwiające sterowanie grą i jej fizyką. 
 * @author AleksanderSklorz
 */
public class GameManager {
    private static String lastAction;
    private static ArrayList<CraneAbstract> units = new ArrayList();
    private static User user; 
    private static boolean startedGame = false;
    private static boolean pausedGame = false; 
    private static Billboard billboard; 
    
    /**
     * Uruchamia grę. 
     */
    public static void runGame(){
        BuildingSimulator game = BuildingSimulator.getBuildingSimulator(); 
        addHUD();
        game.getFlyByCamera().setDragToRotate(false);
        BulletAppState bas = game.getBulletAppState(); 
        game.getStateManager().attach(bas);
        addToGame(new Map(9).getScene());
        MobileCrane mobileCrane = new MobileCrane();
        addUnit(mobileCrane);
        addToGame(mobileCrane.getCrane());
        Crane crane = new Crane(); 
        addUnit(crane);
        addToGame(crane.getCrane());
        mobileCrane.setUsing(true);
//        billboard = new Billboard(-720, 20);
//        addToGame(billboard.getBillboard());
        Control.addListener(mobileCrane);
        Control.addListener(game);
        bas.getPhysicsSpace().addCollisionListener(BuildingCollisionListener
                .createBuildingCollisionListener());
        startedGame = true; 
    }
    
    /**
     * Kontynuuje grę od stanu z przed zatrzymania. 
     */
    public static void continueGame() {
        addHUD();
        if(!BirdsEyeView.isActive()) {
            BuildingSimulator.getBuildingSimulator().getFlyByCamera().setDragToRotate(false);
            Control.addListener(Control.getActualListener());
        } else {
            HUD.changeShopButtonVisibility(false);
        }
        // billboard.resumeAdvertisement();
        startedGame = true;
        pausedGame = false; 
    }
    
    /**
     * Zatrzymuje grę. 
     */
    public static void pauseGame() {
        BuildingSimulator.getBuildingSimulator().getFlyByCamera().setDragToRotate(true);
        Control.removeListener(Control.getActualListener());
        startedGame = false;
        pausedGame = true; 
        // billboard.pauseAdvertisement();
        MenuFactory.showMenu(MenuTypes.PAUSE_MENU);
    }
    
    /**
     * Usuwa aktualną grę. 
     */
    public static void deleteGame() {
        BuildingSimulator game = BuildingSimulator.getBuildingSimulator();
        game.getRenderer().cleanup();
        Node rootNode = game.getRootNode(); 
        game.getBulletAppState().getPhysicsSpace().removeAll(rootNode);
        rootNode.detachAllChildren();
        pausedGame = false; 
        removeAllUnits();
    }
    
    private static void addHUD() {
        BuildingSimulator.getBuildingSimulator().getGuiNode()
                .addControl(new HUD().getScreen());
    }
    
    /**
     * Usuwa HUD. 
     */
    public static void removeHUD() {
        HUD.hideElements();
        BuildingSimulator.getBuildingSimulator().getGuiNode().removeControl(HUD.getScreen());
    }
    
    /**
     * Tworzy fizykę dla danego obiektu. Stosuje ona klasę RigidBodyControl, 
     * natomiast do wykrywania kolizji używa CompoundCollisionShape. 
     * @param parent węzeł z którego pobierane są elementy podrzędne tworzące
     * obiekt fizyczny. Jest też właścicielem fizyki tego obiektu. 
     * @param mass masa obiektu 
     * @param kinematic true jeśli używamy fizyki kinematycznej, false jeśli dynamicznej
     * @param children nazwy elementów podrzędnych węzła parent, które tworzą
     * obiekt fizyczny
     */
    public static void createObjectPhysics(Node parent, float mass, boolean kinematic,
            String... children){
        CompoundCollisionShape compound = createCompound(parent, children);
        createPhysics(compound, parent, mass, kinematic);
    }
    
    /**
     * Tworzy siatkę kolizji dla danych elementów. 
     * @param parent węzeł nadrzędny z którego pobierane są elementy podrzędne 
     * dla których tworzona jest siatka kolizji
     * @param children nazwy elementów podrzędnych węzła parent, z których 
     * powstanie siatka kolizji
     * @return obiekty typu CompoundCollisionShape reprezentujący siatkę kolizji 
     * dla podanych elementów
     */
    public static CompoundCollisionShape createCompound(Node parent, String... children){
        CompoundCollisionShape compound = new CompoundCollisionShape(); 
        for(int i = 0; i < children.length; i++){
            addNewCollisionShapeToCompound(compound, parent, children[i], 
                    Vector3f.ZERO, null);
        }
        return compound;
    }
    
    /**
     * Tworzy fizykę z podanego kształtu kolizji. 
     * @param compound kształt kolizji z którego tworzy się fizykę
     * @param controlOwner właściciel fizyki w obiekcie 
     * @param mass masa obiektu 
     * @param kinematic true jeśli używamy fizyki kinematycznej, false jeśli dynamicznej
     * ma wartość null
     */
    public static void createPhysics(CompoundCollisionShape compound, Spatial controlOwner,
            float mass, boolean kinematic){
        PhysicsSpace physics = BuildingSimulator.getBuildingSimulator()
                .getBulletAppState().getPhysicsSpace();
        RigidBodyControl oldControl = controlOwner.getControl(RigidBodyControl.class);
        if(oldControl != null){
            physics.remove(oldControl);
            controlOwner.removeControl(oldControl);
        }
        RigidBodyControl control = compound != null ? 
                new RigidBodyControl(compound, mass) : new RigidBodyControl(mass);
        if(kinematic) control.setKinematic(kinematic);
        controlOwner.addControl(control);
        physics.add(control);
    }
    
    /**
     * Pozwala na przewidzenie wektora zawierającego informację o tym, o jaki 
     * wektor należy przesunąć elementy połączone ze skalowanym elementem. 
     * @param parentScallingElement rodzic skalowanego elementu
     * @param scale wartość skalowania 
     * @param x true jeśli skaluje się według osi X, false w przeciwnym razie 
     * @param y true jeśli skaluje się według osi Y, false w przeciwnym razie 
     * @param z true jeśli skaluje się według osi Z, false w przeciwnym razie 
     * @return wektor przesunięcia elementów połączonych ze skalowanym węzłem
     */
    public static Vector3f calculateDisplacementAfterScaling(Node parentScallingElement, 
            Vector3f scale, boolean x, boolean y, boolean z){
        Geometry parentGeometry = (Geometry)((Node)parentScallingElement.clone())
                .getChild(0); // tworzę kopię węzła, aby nie zmieniać rozmiaru oryginału
        Vector3f displacement = new Vector3f(),
                initialSize  = ((BoundingBox)parentGeometry.getWorldBound()).getExtent(null);
        parentGeometry.setLocalScale(scale);
        ((BoundingBox)parentGeometry.getWorldBound()).getExtent(displacement);
        displacement.x = x ? displacement.x - initialSize.z : 0f;
        displacement.y = y ? displacement.y - initialSize.y : 0f;
        displacement.z = z ? displacement.z - initialSize.z : 0f;
        return displacement;
    }
    
    /**
     * Przesuwa elementy o podany wektor, jednocześnie skalując ich sąsiada. 
     * @param direction true jeśli ma się przesuwać w kierunku dodatnim, false 
     * w przeciwnym kierunku 
     * @param elementDisplacement wektor przesunięcia 
     * @param scallingVector wektor skalowania 
     * @param scallingElements skalowane elementy 
     * @param movingElements przesuwane elementy  
     */
    public static void moveWithScallingObject(boolean direction, Vector3f elementDisplacement,
            Vector3f scallingVector, Node[] scallingElements, Spatial... movingElements){
        for(int i = 0; i < scallingElements.length; i++)
            ((Geometry)scallingElements[i].getChild(0)).setLocalScale(scallingVector);
        Vector3f displacement = elementDisplacement.clone();
        if(!direction) displacement.negateLocal();
            for(int i = 0; i < movingElements.length; i++)
                if(isNonDynamicSpatial(movingElements[i]))
                    movingElements[i].getLocalTranslation().addLocal(displacement);
                else 
                    moveDynamicObject(movingElements[i], displacement);
    }
    
    /**
     * Tworzy połączenie dwóch obiektów. 
     * @param joint obiekt przechowujący połączenie. Może mieć wartość null, wtedy 
     * tworzony jest nowy obiekt. 
     * @param nodeA pierwszy obiekt 
     * @param nodeB drugi obiekt 
     * @param pivotA punkt połączenia dla pierwszego obiektu 
     * @param pivotB punkt połaczenia dla drugiego obiektu 
     * @return połaczenie obiektów 
     */
    public static HingeJoint joinsElementToOtherElement(HingeJoint joint, Spatial nodeA,
            Spatial nodeB, Vector3f pivotA, Vector3f pivotB){
        PhysicsSpace physics = BuildingSimulator.getBuildingSimulator()
                        .getBulletAppState().getPhysicsSpace();
        if(joint != null) physics.remove(joint);
        joint = new HingeJoint(nodeA.getControl(RigidBodyControl.class), nodeB
               .getControl(RigidBodyControl.class), pivotA, pivotB, Vector3f.ZERO,
                Vector3f.ZERO);
        physics.add(joint);
        return joint;
    }
    
    /**
     * Dodaje kształt kolizji do obiektu CompoundCollisionShape reprezentującego 
     * złożony kształt kolizji. 
     * @param compound złożony kształt kolizji
     * @param parent rodzic elementu dla którego tworzona jest kolizja 
     * @param child nazwa obiektu dla którego tworzona jest kolizji
     * @param location położenie dodawanej kolizji w złożonym obiekcie kolizji 
     * @param rotation obrót dodawanej kolizji w złożonym obiekcie kolizji. 
     * Może mieć wartość null, wtedy pozostaje bez obrotu. 
     */
    public static void addNewCollisionShapeToCompound(CompoundCollisionShape compound,
            Node parent, String child, Vector3f location, Quaternion rotation){
        Geometry parentGeometry = (Geometry)parent.getChild(child);
        CollisionShape elementCollisionShape;
        if(!(parentGeometry instanceof CSGGeometry))
        elementCollisionShape = CollisionShapeFactory.createDynamicMeshShape(parentGeometry);
        else
            elementCollisionShape = CollisionShapeFactory
                    .createDynamicMeshShape(((CSGGeometry)parentGeometry).asSpatial());
        elementCollisionShape.setScale(parentGeometry.getWorldScale()); 
        if(rotation == null) compound.addChildShape(elementCollisionShape, location);
        else compound.addChildShape(elementCollisionShape, location, rotation
                .toRotationMatrix());
    }
    
    /**
     * Uruchamia animację. 
     * @param animation animacja 
     */
    public static void startAnimation(Cinematic animation) {
        BuildingSimulator.getBuildingSimulator().getStateManager().attach(animation);
        animation.play();
    }
    
    public static Node loadModel(String path) {
        return (Node)BuildingSimulator.getBuildingSimulator().getAssetManager().loadModel(path);
    }
    
    /**
     * Zwraca ostatnio wykonaną akcję. 
     * @return ostatnio wykonana akcja
     */
    public static String getLastAction(){
        return lastAction;
    }
    
    /**
     * Ustawia ostatnio wykonaną akcję. 
     * @param action ostatnio wykonana akcja
     */
    public static void setLastAction(String action){
        lastAction = action;
    }
    
    /**
     * Zwraca aktualną liczbę klatek na sekundę w postaci liczby. 
     * @return FPS w postaci liczby 
     */
    public static int getFPS(){
        String fpsString = BuildingSimulator.getFPSString(), tempFPSString = "";
        int length = fpsString.length(), i = length - 1;  
        do{
            i--; // bo na początku może być minimum jedna cyfra 
        }while(fpsString.charAt(i) != ' ');
        i++;
        for(; i < length; i++) tempFPSString += fpsString.charAt(i);
        return Integer.parseInt(tempFPSString);
    }
    
    /**
     * Doaje nową jednostkę do listy wszystkich jednostek w grze. 
     * @param unit dodawana jednostka 
     */
    public static void addUnit(CraneAbstract unit){
        units.add(unit);
    }
    
    /**
     * Zwraca jednostkę z listy wszystkich jednostek w grze, o podanym indeksie. 
     * @param i indeks zwracanej jednostki 
     * @return jednostka 
     */
    public static CraneAbstract getUnit(int i){
        return units.get(i);
    }
    
    /**
     * Usuwa wszystkie jednostki (dźwigi mobilne i zurawie), które występują w grze. 
     */
    public static void removeAllUnits() { units.clear(); }
    
    /**
     * Zwraca jednostkę, która jest aktualnie używana przez gracza. 
     * @return aktualnie używana jednostka 
     */
    public static CraneAbstract findActualUnit(){
        for(int i = 0; i < units.size(); i++){
            CraneAbstract unit = units.get(i);
            if(unit.isUsing()) return unit;
        }
        return null;
    }
    
    /**
     * Dodaje obiekt do gry. 
     * @param object dodawany obiekt 
     */
    public static void addToGame(Spatial object){
        BuildingSimulator.getBuildingSimulator().getRootNode().attachChild(object);
    }
    
    /**
     * Tworzy podłoże po którym gracz się porusza. 
     */
    public static void createTerrain(){
        BuildingSimulator game = BuildingSimulator.getBuildingSimulator();
        Node scene = (Node)game.getAssetManager().loadModel("Scenes/gameMap.j3o");
        int x = 0, z = 254, end = 4;
        PhysicsSpace physics = game.getBulletAppState().getPhysicsSpace();
        Spatial firstPart = scene.getChild("terrain-gameMap");
        float offset = -z * 2; // przesunięcie planszy o połowę 
        for(int i = 0; i < 5; i++){
            for(int k = 0; k < end; k++){
                Spatial scenePart = firstPart.clone(true);
                scene.attachChild(scenePart);
                RigidBodyControl rgc = new RigidBodyControl(0.0f);
                scenePart.addControl(rgc);
                physics.add(rgc);
                rgc.setPhysicsLocation(new Vector3f(x + offset, 0, z + offset));
                z += 254;
            }
            x += 254;
            z = 0;
            end = 5;
        }
        RigidBodyControl firstPartControl = new RigidBodyControl(0.0f); 
        firstPart.addControl(firstPartControl);
        physics.add(firstPartControl); 
        firstPartControl.setPhysicsLocation(new Vector3f(offset, 0, offset));
        game.getRootNode().attachChild(scene);
    }
    
    public static AudioNode startSound(String path, float volume, boolean looping, Node owner) {
        BuildingSimulator game = BuildingSimulator.getBuildingSimulator();
        AudioNode sound = new AudioNode(game.getAssetManager(),
                path, false, true);
        sound.setVolume(volume);
        sound.setLooping(looping);
        if(owner == null) game.getRootNode().attachChild(sound);
        else owner.attachChild(sound);
        return sound;
    }
    
    public static void stopSound(AudioNode sound, boolean autoDetaching) {
        sound.stop();
        if(autoDetaching)
            BuildingSimulator.getBuildingSimulator().getRootNode().detachChild(sound);
    }
    
    /**
     * Zwraca aktualnego użytkownika. 
     * @return aktualny użytkownik 
     */
    public static User getUser() { return user; }
    
    /**
     * Ustawia aktualnego użytkownika. 
     * @param user aktualny użytkownik 
     */
    public static void setUser(User user) { GameManager.user = user; }
    
    /**
     * Określa czy gra się już rozpoczeła. 
     * @return true jeśli gra się rozpoczęła, false w przeciwnym przypadku 
     */
    public static boolean isStartedGame() { return startedGame; }
    
    public static void setStarted(boolean startedGame) { GameManager.startedGame = startedGame; }
    
    /**
     * Okresla czy gra jest zatrzymana. 
     * @return true jesli gra jest zatrzymana, false w przeciwnym przypadku 
     */
    public static boolean isPausedGame() { return pausedGame; }
    
    private static void moveDynamicObject(Spatial element, Vector3f displacement){
        RigidBodyControl elementControl = element.getControl(RigidBodyControl.class);
        elementControl.setPhysicsLocation(elementControl.getPhysicsLocation()
                .addLocal(displacement));
    }
    
    private static boolean isNonDynamicSpatial(Spatial element){
        RigidBodyControl elementControl = element.getControl(RigidBodyControl.class);
        return elementControl == null || elementControl.isKinematic();
    }
}
