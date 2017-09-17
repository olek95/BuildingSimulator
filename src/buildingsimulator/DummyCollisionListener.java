package buildingsimulator;

import com.jme3.bullet.collision.PhysicsCollisionGroupListener;
import com.jme3.bullet.collision.PhysicsCollisionObject;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Box;
import com.jme3.scene.control.Control;

/**
 * Obiekt klasy <code>DummyCollisionListener</code> reprezentuje słuchacza 
 * sprawdzającego kolizje. Słuchacz ten sprawdza kolizje poprzez dodanie sztucznego 
 * obiektu. Następnie sprawdza się czy występują kolizje z tym obiektem. 
 * @author AleksanderSklorz
 */
public class DummyCollisionListener implements PhysicsCollisionGroupListener {
    private Spatial first = null;
    private Node collisionOwner = null; 
    private boolean end = false, collision = false; 
    
    /**
     * Sprawdza kolizje poprzez sprawdzanie kolizji z sztucznym obiektem. 
     * @param nodeA pierwszy obiekt kolizji 
     * @param nodeB drugi obiekt kolizji 
     * @return true jeśli kolizja występują, false w przeciwnym razie 
     */
    @Override
    public boolean collide(PhysicsCollisionObject nodeA, PhysicsCollisionObject nodeB){
        Spatial a = (Spatial)nodeA.getUserObject(), b = (Spatial)nodeB.getUserObject();
        String nameA = a.getName();
        if(first == null) {
            if(a.equals(collisionOwner)) first = b; 
            else first = a; 
        } else {
            if(first.equals(a) || first.equals(b)) {
                end = true; 
            }
        }
        if(!collision) {
            if(nameA.equals("DummyWall")) {
                if(!b.getName().startsWith("terrain")) collision = true;
            } else {
                if(!nameA.startsWith("terrain")) collision = true;
            }
        }
        return false;
    }
    
    /**
     * Tworzy sztuczny obiekt z którym są sprawdzane kolizje. 
     * @param shop właściciel słuchacza 
     * @param location położenie sztucznego obiektu 
     */
    public void createDummyWall(Vector3f location, Vector3f dimensions) {
        collisionOwner = new Node("DummyWall");
        collisionOwner.attachChild(new Geometry("DummyWall", new Box(dimensions.x, 
                dimensions.y, dimensions.z)));
        GameManager.createObjectPhysics(collisionOwner, 0.00001f, false, "DummyWall");
        RigidBodyControl control = collisionOwner.getControl(RigidBodyControl.class);
        control.setPhysicsLocation(location);
        control.setCollisionGroup(6);
        BuildingSimulator.getBuildingSimulator().getBulletAppState()
                .getPhysicsSpace().addCollisionGroupListener(this, 6);
        System.out.println(111);
    }
    
    /**
     * Usuwa fizyczną postać sztucznej ściany. 
     */
    public void deleteDummyWallControl() {
        System.out.println(collisionOwner.getControl(RigidBodyControl.class));
        Control control = collisionOwner.getControl(0);
        collisionOwner.removeControl(control);
        BuildingSimulator.getBuildingSimulator().getBulletAppState()
                .getPhysicsSpace().remove(control);
    }
    
    /**
     * Określa czy sprawdzona już została kolizja z wszystkimi obiektami. 
     * @return true jeśli sprawdzono już wszystkie obiekty, false w przeciwnym przypadku 
     */
    public boolean isEnd() { return end; }
    
    /**
     * Określa czy wystąpiła kolizja. Jeśli tak to obiekt nie będzie mógł zostać
     * ustawiony. 
     * @return true jeśli wystąpiła kolizja i obiekt nie może być ustawiony, 
     * false w przeciwnym przypadku 
     */
    public boolean isCollision() { return collision; }
}
