package buildingsimulator;

import building.Wall;
import building.WallType;
import building.WallsFactory;
import com.jme3.bullet.collision.PhysicsCollisionGroupListener;
import com.jme3.bullet.collision.PhysicsCollisionObject;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.math.Vector3f;
import com.jme3.scene.Spatial;
import menu.Shop;
import tonegod.gui.controls.text.TextField;
import tonegod.gui.core.Screen;

/**
 * Obiekt klasy <code>DummyCollisionListener</code> reprezentuje słuchacza 
 * sprawdzającego kolizje. Słuchacz ten sprawdza kolizje poprzez dodanie sztucznego 
 * obiektu. Następnie sprawdza się czy występują kolizje z tym obiektem. 
 * @author AleksanderSklorz
 */
public class DummyCollisionListener implements PhysicsCollisionGroupListener {
    private Spatial first, collisionOwner;
    private boolean end, collision; 
    private static DummyCollisionListener listener; 
    public DummyCollisionListener(){
        first = null;
        collisionOwner = null;
        end = false; 
        collision = false; 
    }
    
    /**
     * Tworzy słuchacza. 
     * @return słuchacz 
     */
    public static DummyCollisionListener createListener() {
        if(listener == null) listener = new DummyCollisionListener(); 
        return listener; 
    }
    
    /**
     * Sprawdza kolizje poprzez sprawdzanie kolizji z sztucznym obiektem. 
     * @param nodeA pierwszy obiekt kolizji 
     * @param nodeB drugi obiekt kolizji 
     * @return true jeśli kolizja występują, false w przeciwnym razie 
     */
    @Override
    public boolean collide(PhysicsCollisionObject nodeA, PhysicsCollisionObject nodeB){
        Spatial a = (Spatial)nodeA.getUserObject(), b = (Spatial)nodeB.getUserObject();
        String nameA = a.getName(), nameB = b.getName();
        if(first == null) {
            if(a.equals(collisionOwner)) first = b; 
            else first = a; 
        } else {
            if(first.equals(a) || first.equals(b)) {
                end = true; 
            }
        }
        if(!nameA.startsWith("terrain") && !(a instanceof Wall) && !collision) {
            collision = true;
        } else {
            if(!nameB.startsWith("terrain")  && !(b instanceof Wall) && !collision) {
                collision = true;
            }
        }
        return true;
    }
    
    /**
     * Tworzy sztuczny obiekt z którym są sprawdzane kolizje. 
     * @param shop właściciel słuchacza 
     * @param location położenie sztucznego obiektu 
     */
    public void createDummyWall(Shop shop, Vector3f location) {
        Screen screen = shop.getScreen(); 
        float x = ((TextField)screen.getElementById("x_text_field")).parseFloat(),
                z = ((TextField)screen.getElementById("z_text_field")).parseFloat();
        Vector3f dimensions = new Vector3f(x, 0.2f, z);
        Vector3f tempDimensions = dimensions.clone();
        tempDimensions.multLocal(1, 10, 1);
        collisionOwner = WallsFactory.createWall(WallType.WALL, location,
                tempDimensions);
        collisionOwner.getControl(RigidBodyControl.class).setCollisionGroup(6);
        BuildingSimulator.getBuildingSimulator().getBulletAppState()
                .getPhysicsSpace().addCollisionGroupListener(listener, 6);
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
