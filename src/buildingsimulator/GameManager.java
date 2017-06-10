package buildingsimulator;

import cranes.Hook;
import cranes.CraneAbstract;
import com.jme3.bounding.BoundingBox;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.collision.shapes.CompoundCollisionShape;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.bullet.joints.HingeJoint;
import com.jme3.bullet.util.CollisionShapeFactory;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import java.util.ArrayList;

/**
 * Klasa <code>GameManager</code> reprezentuje zarządcę gry, posiadającego 
 * metody ułatwiające sterowanie grą i jej fizyką. 
 * @author AleksanderSklorz
 */
public class GameManager {
    private static String lastAction;
    private static Spatial craneRack;
    private static CraneAbstract actualUnit;
    private static ArrayList<CraneAbstract> units = new ArrayList();
    
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
     * @param elementGeometry obiekt będący właścicielem fizyki, gdy controlOwner
     * ma wartość null
     */
    public static void createPhysics(CompoundCollisionShape compound, Spatial controlOwner,
            float mass, boolean kinematic){
        PhysicsSpace physics = BuildingSimulator.getBuildingSimulator().getBulletAppState().getPhysicsSpace();
        if(controlOwner.getControl(RigidBodyControl.class) != null){
            physics.remove(controlOwner.getControl(0));
            controlOwner.removeControl(RigidBodyControl.class);
        }
        RigidBodyControl control = new RigidBodyControl(compound, mass);
        control.setKinematic(kinematic);
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
     * @param scallingElement skalowany element 
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
    
    private static void moveDynamicObject(Spatial element, Vector3f displacement){
        RigidBodyControl elementControl = element.getControl(RigidBodyControl.class);
        elementControl.setPhysicsLocation(elementControl.getPhysicsLocation()
                .addLocal(displacement));
    }
    
    private static boolean isNonDynamicSpatial(Spatial element){
        RigidBodyControl elementControl = element.getControl(RigidBodyControl.class);
        return elementControl == null || elementControl.isKinematic();
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
        CollisionShape elementCollisionShape = CollisionShapeFactory
                .createDynamicMeshShape(parentGeometry);
        elementCollisionShape.setScale(parentGeometry.getWorldScale()); 
        if(rotation == null) compound.addChildShape(elementCollisionShape, location);
        else compound.addChildShape(elementCollisionShape, location, rotation
                .toRotationMatrix());
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
     * @param key ostatnio wykonana akcja
     */
    public static void setLastAction(String action){
        lastAction = action;
    }
    
    /**
     * Zwraca "stojak" żurawia. 
     * @return stojak żurawia 
     */
    public static Spatial getCraneRack(){
        return craneRack;
    }
    
    /**
     * Ustawia "stojak" żurawia. 
     * @param craneRack stojak żurawia 
     */
    public static void setCraneRack(Spatial craneRack){
        GameManager.craneRack = craneRack;
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
     * Inicjuje słuchacza dla kolizji haków. 
     */
    public static void initHookCollisionListener(){
        BuildingSimulator.getBuildingSimulator().getBulletAppState().getPhysicsSpace()
                .addCollisionGroupListener(Hook.createCollisionListener(), 2);
    }
    
    /**
     * Ustawia aktualnie używaną jednostkę. 
     * @param unit jednostka 
     */
    public static void setActualUnit(CraneAbstract unit){
        actualUnit = unit; 
    }
    
    /**
     * Zwraca aktualnie używaną jednostkę. 
     * @return aktualnie używana jednostka.
     */
    public static CraneAbstract getActualUnit(){
        return actualUnit;
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
}
