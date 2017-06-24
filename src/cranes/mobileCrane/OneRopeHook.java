package cranes.mobileCrane;

import buildingmaterials.Wall;
import cranes.Hook;
import static buildingsimulator.GameManager.*;
import com.jme3.bounding.BoundingBox;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.collision.CollisionResults;
import com.jme3.math.Ray;
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
        super(ropeHook, hookHandle, speed, 0.05f);
        rope = (Node)ropeHook.getChild("rope");
        hookHandle.getControl(RigidBodyControl.class).addCollideWithGroup(1);
       setHookDisplacement(calculateDisplacementAfterScaling(rope, 
                new Vector3f(1f, getActualLowering() + speed, 1f), false, true, false));
        getHookDisplacement().y *= 2; // wyrównuje poruszanie się haka wraz z liną 
        createRopeHookPhysics();
    }
    
    /**
     * Opuszcza hak do momentu wykrycia przeszkody. 
     */
    @Override
    public void lower(){ 
        CollisionResults results = new CollisionResults();
        Spatial recentlyHitObject = getRecentlyHitObject();
        /* jeśli nie dotknęło żadnego obiektu, to zbędne jest sprawdzanie 
        kolizji w dół*/
        if(recentlyHitObject != null)
            // tworzy pomocniczy promień sprawdzający kolizję w dół
            new Ray(getHook().getWorldTranslation(), new Vector3f(0,-0.5f,0))
                    .collideWith((BoundingBox)recentlyHitObject.getWorldBound(), results);
        super.lower(results);
    }
    
    @Override
    protected void createRopeHookPhysics(){
        super.addHookPhysics(createCompound(rope, rope.getChild(0).getName()),
                new Vector3f(0, 0.06f,0));
    }
    
    @Override
    protected Node[] getRopes(){
        return new Node[] {rope};
    }
}
