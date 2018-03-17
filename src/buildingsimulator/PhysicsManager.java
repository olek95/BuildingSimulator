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
import net.wcomohundro.jme3.csg.CSGGeometry;

/**
 * Klasa <code>PhysicsManager</code> reprezentuje zarządcę ułatwiający pracę z 
 * fizyką - posiada zbiór pomocniczych metod fizycznych. 
 * @author AleksanderSklorz 
 */
public class PhysicsManager {
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
            Vector3f scale, boolean x, boolean y, boolean z) {
        Geometry parentGeometry = (Geometry) ((Node) parentScallingElement.clone()).getChild(0);
        Vector3f displacement = new Vector3f();
        Vector3f initialSize = ((BoundingBox) parentGeometry.getWorldBound()).getExtent(null);
        parentGeometry.setLocalScale(scale);
        ((BoundingBox) parentGeometry.getWorldBound()).getExtent(displacement);
        displacement.x = x ? displacement.x - initialSize.z : 0.0F;
        displacement.y = y ? displacement.y - initialSize.y : 0.0F;
        displacement.z = z ? displacement.z - initialSize.z : 0.0F;
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
            Vector3f scallingVector, Node[] scallingElements, Spatial... movingElements) {
        for (int i = 0; i < scallingElements.length; i++) {
            ((Geometry) scallingElements[i].getChild(0)).setLocalScale(scallingVector);
        }
        Vector3f displacement = elementDisplacement.clone();
        if (!direction) {
            displacement.negateLocal();
        }
        for (int i = 0; i < movingElements.length; i++) {
            if (PhysicsManager.isNonDynamicSpatial(movingElements[i])) {
                movingElements[i].getLocalTranslation().addLocal(displacement);
            } else {
                PhysicsManager.moveDynamicObject(movingElements[i], displacement);
            }
        }
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
            String... children) {
        CompoundCollisionShape compound = createCompound(parent, children);
        createPhysics(compound, parent, mass, kinematic);
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
            Node parent, String child, Vector3f location, Quaternion rotation) {
        Geometry parentGeometry = (Geometry) parent.getChild(child);
        CollisionShape elementCollisionShape;
        if (!(parentGeometry instanceof CSGGeometry)) {
            elementCollisionShape = CollisionShapeFactory.createDynamicMeshShape(parentGeometry);
        } else {
            elementCollisionShape = CollisionShapeFactory.createDynamicMeshShape(((CSGGeometry) parentGeometry).asSpatial());
        }
        elementCollisionShape.setScale(parentGeometry.getWorldScale());
        if (rotation == null) {
            compound.addChildShape(elementCollisionShape, location);
        } else {
            compound.addChildShape(elementCollisionShape, location, rotation.toRotationMatrix());
        }
    }

    /**
     * Tworzy siatkę kolizji dla danych elementów.
     * @param parent węzeł nadrzędny z którego pobierane są elementy podrzędne
     * dla których tworzona jest siatka kolizji
     * @param children nazwy elementów podrzędnych węzła parent, z których
     * powstanie siatka kolizji
     * @return obiekt typu CompoundCollisionShape reprezentujący siatkę kolizji
     * dla podanych elementów
     */
    public static CompoundCollisionShape createCompound(Node parent, String... children) {
        CompoundCollisionShape compound = new CompoundCollisionShape();
        for (int i = 0; i < children.length; i++) {
            addNewCollisionShapeToCompound(compound, parent, children[i], Vector3f.ZERO, null);
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
            float mass, boolean kinematic) {
        PhysicsSpace physics = BuildingSimulator.getPhysicsSpace();
        RigidBodyControl oldControl = controlOwner.getControl(RigidBodyControl.class);
        if (oldControl != null) {
            physics.remove(oldControl);
            controlOwner.removeControl(oldControl);
        }
        RigidBodyControl control = compound != null ? new RigidBodyControl(compound, mass) : new RigidBodyControl(mass);
        if (kinematic) {
            control.setKinematic(kinematic);
        }
        controlOwner.addControl(control);
        physics.add(control);
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
            Spatial nodeB, Vector3f pivotA, Vector3f pivotB) {
        PhysicsSpace physics = BuildingSimulator.getPhysicsSpace();
        if (joint != null) {
            physics.remove(joint);
        }
        joint = new HingeJoint(nodeA.getControl(RigidBodyControl.class), nodeB.getControl(RigidBodyControl.class), pivotA, pivotB, Vector3f.ZERO, Vector3f.ZERO);
        physics.add(joint);
        return joint;
    }
    
    /**
     * Dodaje fizykę podanych obiektów do świata gry. 
     * @param objects tablica obiektów których fizyka jest dodawana 
     */
    public static void addPhysicsToGame(Spatial... objects) {
        PhysicsSpace physics = BuildingSimulator.getPhysicsSpace();
        for(int i = 0; i < objects.length; i++)
            physics.add(objects[i].getControl(RigidBodyControl.class));
    }
    
    /**
     * Dodaje fizykę o podanym indeksie podanego obiektu do świata gry. 
     * @param object obiekt którego fizyka jest dodawana 
     * @param i indeks dodawanej fizyki 
     */
    public static void addPhysicsToGame(Spatial object, int i) {
        BuildingSimulator.getPhysicsSpace().add(object.getControl(i));
    }
    
    /**
     * Usuwa fizykę dla danego obiektu z gry. 
     * @param object obiekt dla którego jest usuwana fizyka  
     */
    public static void removeFromScene(Node object) {
        RigidBodyControl control = object.getControl(RigidBodyControl.class);
        object.removeControl(control);
        BuildingSimulator.getPhysicsSpace().remove(control);
    }
    
    /**
     * Tworzy fizykę dla obiektu z typem kolizji Box. 
     * @param parent właściciel fizyki
     * @param parentLocation lokalizacja właściciela fizyki po jej stworzeniu 
     * @param children dzieci właściciela dla których tworzona jest kolizja 
     * @param locations położenie dzieci właściciela 
     */
    public static void createBoxPhysics(Node parent, Vector3f parentLocation, 
            Spatial[] children, Vector3f... locations) {
        CompoundCollisionShape compound = new CompoundCollisionShape();
        for(int i = 0; i < children.length; i++) {
            CollisionShape elementCollisionShape = CollisionShapeFactory
                    .createBoxShape(children[i]);
            compound.addChildShape(elementCollisionShape, locations[i]);
        }
        RigidBodyControl control = new RigidBodyControl(compound, 0f);
        parent.addControl(control);
        parent.setLocalTranslation(parentLocation);
        PhysicsManager.addPhysicsToGame(parent);
        
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
}
