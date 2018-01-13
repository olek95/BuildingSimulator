package cranes.crane;

import buildingsimulator.ElementName;
import cranes.Hook;
import buildingsimulator.PhysicsManager;
import com.jme3.bullet.collision.shapes.CompoundCollisionShape;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import java.util.List;

/**
 * Obiekt klasy <code>FourRopesHook</code> reprezentuje hak zawieszony na 
 * czterech liniach. W porównaniu z hakiem z jedną liną, ten ma jeszcze jeden 
 * dodatkowy uchwyt, do którego przyczepione są te liny. 
 * @author AleksanderSklorz 
 */
public class FourRopesHook extends Hook{
    private Node littleHookHandle;
    private Node[] ropes = new Node[4];
    public FourRopesHook(Node ropeHook, Spatial hookHandle, float speed){
        super(ropeHook, hookHandle, speed);
        List<Spatial> ropeHookChildren = ropeHook.getChildren();
        int index = 0;
        for(int i = 0; i < ropeHookChildren.size(); i++){
            Spatial children = ropeHookChildren.get(i);
            if(children.getName().startsWith(ElementName.ROPE)){
                ropes[index] = (Node)children;
                index++;
            }
        }
        littleHookHandle = (Node)ropeHook.getChild(ElementName.LITTLE_HOOK_HANDLE);
        setHookDisplacement(PhysicsManager.calculateDisplacementAfterScaling(ropes[0], 
                new Vector3f(1f, getActualLowering() + speed, 1f), false, true, false));
        getHookDisplacement().y *= 2;
        if(getRopeHook().getControl(RigidBodyControl.class) == null)
            createRopeHookPhysics();
        else restoreHookPhysics(new Vector3f(0, 0.6f,0));
    }
    
    /**
     * Opuszcza lub podnosi hak.  
     * @param scallingVector wektor wskazujacy o ile ma się przesunąć hak 
     * @param heightening true jeśli podnosimy hak, false w przeciwnym razie 
     */
    @Override
    protected void changeHookPosition(Vector3f scallingVector, boolean heightening){
        littleHookHandle.getLocalTranslation().addLocal(!heightening ? 
                getHookDisplacement().clone().negateLocal() : getHookDisplacement().clone());
        super.changeHookPosition(scallingVector, heightening);
    }
    
    @Override
    protected void createRopeHookPhysics(){
        CompoundCollisionShape ropeHookCompound = PhysicsManager.createCompound(getRopeHook(), ropes[0].getChild(0)
                .getName());
        ropeHookCompound.getChildren().get(0).location = ropes[0].getLocalTranslation();
        for(int i = 1; i < ropes.length; i++)
            PhysicsManager.addNewCollisionShapeToCompound(ropeHookCompound, ropes[i], ropes[i]
                    .getChild(0).getName(), ropes[i].getLocalTranslation(), null);
        PhysicsManager.addNewCollisionShapeToCompound(ropeHookCompound, littleHookHandle, littleHookHandle
                .getChild(0).getName(), littleHookHandle.getLocalTranslation(), null);
        super.addHookPhysics(ropeHookCompound, new Vector3f(0, 0.6f,0));
    }
    
    @Override 
    public Node[] getRopes(){
        return ropes; 
    }
}
