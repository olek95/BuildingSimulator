package cranes.mobileCrane;

import buildingsimulator.ElementName;
import cranes.Hook;
import buildingsimulator.PhysicsManager;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;

/**
 * Obiekt klasy <code>OneRopeHook</code> reprezentuje hak zawieszony na linie. 
 * Może być zarówno opuszczany jak i podnoszony. 
 * @author AleksanderSklorz 
 */
public class OneRopeHook extends Hook{
    private Node rope;
    public OneRopeHook(Node ropeHook, Spatial hookHandle, float speed){
        super(ropeHook, hookHandle, speed);
        rope = (Node)ropeHook.getChild(ElementName.ROPE);
        hookHandle.getControl(RigidBodyControl.class).addCollideWithGroup(1);
       setHookDisplacement(PhysicsManager.calculateDisplacementAfterScaling(rope, 
                new Vector3f(1f, getActualLowering() + speed, 1f), false, true, false));
        getHookDisplacement().y *= 2; // wyrównuje poruszanie się haka wraz z liną 
        if(getRopeHook().getControl(RigidBodyControl.class) == null)
            createRopeHookPhysics();
        else restoreHookPhysics( new Vector3f(0, 0.06f,0));
    }
    
    @Override
    protected void createRopeHookPhysics(){
        addHookPhysics(PhysicsManager.createCompound(rope, rope.getChild(0).getName()),
                new Vector3f(0, 0.06f,0));
    }
    
    @Override
    public Node[] getRopes(){
        return new Node[] {rope};
    }
}
