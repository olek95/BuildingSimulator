package buildingsimulator;

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

/**
 * Klasa <code>GameManager</code> reprezentuje zarządcę gry, posiadającego 
 * metody ułatwiające sterowanie grą i jej fizyką. 
 * @author AleksanderSklorz
 */
public class GameManager {
    private static String lastAction;
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
            addNewCollisionShapeToComponent(compound, parent, children[i], 
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
        if(x) displacement.x -= initialSize.z;
        else displacement.x = 0f;
        if(y) displacement.y -= initialSize.y;
        else displacement.y = 0f;
        if(z) displacement.z -= initialSize.z;
        else displacement.z = 0f;
        return displacement;
    }
    /**
     * Przesuwa połączony z obiektem rozszerzającym się element o podany wektor. 
     * @param scallingGeometry skalowany obiekt 
     * @param scallingVector wektor skalowania obiektu
     * @param direction true jeśli ma się przesuwać w kierunku dodatnim, false 
     * w przeciwnym kierunku 
     * @param movingElement przesuwany element 
     * @param elementDisplacement wektor przesunięcia 
     */
    public static void movingDuringStretchingOut(Geometry scallingGeometry,
            Vector3f scallingVector, boolean direction, Spatial movingElement,
            Vector3f elementDisplacement){
        scallingGeometry.setLocalScale(scallingVector);
        Vector3f localTranslation = movingElement.getLocalTranslation();
        Vector3f displacement = elementDisplacement.clone();
        if(!direction) displacement.negateLocal();
        localTranslation.addLocal(displacement);
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
    public static void addNewCollisionShapeToComponent(CompoundCollisionShape compound,
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
}
