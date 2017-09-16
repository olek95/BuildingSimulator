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
    
    public static DummyCollisionListener createListener() {
        if(listener == null) listener = new DummyCollisionListener(); 
        return listener; 
    }
    
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
    
    public void createDummyWall(Shop shop, Vector3f location) {
        Screen screen = shop.getScreen(); 
        float x = ((TextField)screen.getElementById("x_text_field")).parseFloat(),
                z = ((TextField)screen.getElementById("z_text_field")).parseFloat();
        Vector3f dimensions = new Vector3f(x, 0.2f, z);
        Vector3f tempDimensions = dimensions.clone();
        tempDimensions.multLocal(1, 10, 1);
        final Wall tempWall = WallsFactory.createWall(WallType.WALL, location,
                tempDimensions);
        tempWall.getControl(RigidBodyControl.class).setCollisionGroup(6);
        setCollisionOwner(tempWall);
        BuildingSimulator.getBuildingSimulator().getBulletAppState()
                .getPhysicsSpace().addCollisionGroupListener(listener, 6);
    }
    
    public void setCollisionOwner(Spatial collisionOwner) {
        this.collisionOwner = collisionOwner; 
    }
    
    public boolean isEnd() { return end; }
    
    public boolean isCollision() { return collision; }
}
