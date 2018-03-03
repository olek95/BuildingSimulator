package listeners;

import building.DummyWall;
import building.WallType;
import building.WallsFactory;
import buildingsimulator.BuildingSimulator;
import buildingsimulator.ElementName;
import com.jme3.bullet.collision.PhysicsCollisionGroupListener;
import com.jme3.bullet.collision.PhysicsCollisionObject;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.math.Vector3f;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.Control;

/**
 * Obiekt klasy <code>DummyCollisionListener</code> reprezentuje słuchacza 
 * sprawdzającego kolizje. Słuchacz ten sprawdza kolizje poprzez dodanie sztucznego 
 * obiektu. Następnie sprawdza się czy występują kolizje z tym obiektem. 
 * @author AleksanderSklorz
 */
public class DummyCollisionListener implements PhysicsCollisionGroupListener {
    private Spatial first = null;
    private DummyWall collisionOwner = null; 
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
                if(!collision) {
                    collisionOwner.startDropSound();
                }
            }
        }
        if(!collision) {
            if(nameA.equals(ElementName.DUMMY_WALL)) {
                if(!b.getName().startsWith(ElementName.TERRAIN))
                    collision = true;
            } else {
                if(!nameA.startsWith(ElementName.TERRAIN))
                    collision = true;
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
        collisionOwner = (DummyWall)WallsFactory.createWall(WallType.WALL, location,
                    dimensions, 0.00001f, true, null);
        collisionOwner.getControl(RigidBodyControl.class).setCollisionGroup(6);
        BuildingSimulator.getPhysicsSpace().addCollisionGroupListener(this, 6);
    }
    
    /**
     * Usuwa fizyczną postać sztucznej ściany. 
     */
    public void deleteDummyWallControl() {
        Control control = collisionOwner.getControl(RigidBodyControl.class);
        if(control != null) {
            collisionOwner.removeControl(control);
            BuildingSimulator.getPhysicsSpace().remove(control);
        }
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
