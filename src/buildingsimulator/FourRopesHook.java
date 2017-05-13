package buildingsimulator;

import static buildingsimulator.GameManager.*;
import com.jme3.bounding.BoundingBox;
import com.jme3.bullet.collision.shapes.CompoundCollisionShape;
import com.jme3.collision.CollisionResults;
import com.jme3.math.Ray;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import java.util.List;

public class FourRopesHook extends Hook{
    private Node littleHookHandle;
    private Node[] ropes = new Node[4];
    public FourRopesHook(Node ropeHook, Spatial hookHandle){
        super(ropeHook, hookHandle);
        List<Spatial> ropeHookChildren = ropeHook.getChildren();
        int index = 0;
        for(int i = 0; i < ropeHookChildren.size(); i++){
            Spatial children = ropeHookChildren.get(i);
            if(children.getName().startsWith("rope")){
                ropes[index] = (Node)children;
                index++;
            }
        }
        littleHookHandle = (Node)ropeHook.getChild("littleHookHandle");
        hookDisplacement = calculateDisplacementAfterScaling(ropes[0], 
                new Vector3f(1f, hookLowering + HOOK_LOWERING_SPEED, 1f),
                false, true, false);
        hookDisplacement.y *= 2;
        createRopeHookPhysics();
    }
    @Override
    public void lower(){
        CollisionResults results = new CollisionResults();
        /* jeśli nie dotknęło żadnego obiektu, to zbędne jest sprawdzanie 
        kolizji w dół*/
        if(recentlyHitObject != null){
            Ray ray = new Ray(hook.getWorldTranslation(), new Vector3f(0,-0.5f,0));
            if(recentlyHitObject.getName().startsWith("prop")){
                // new Ray tworzy pomocniczy promień sprawdzający kolizję w dół
                Node crane = recentlyHitObject.getParent();
                ((Node)crane.getChild("prop0")).getChild(0).collideWith(ray, results);
                ((Node)crane.getChild("prop1")).getChild(0).collideWith(ray, results);
            }else{
                ray.collideWith((BoundingBox)recentlyHitObject.getWorldBound(), results);
            }
        }
        super.lower(results);
    }
    
    private void createRopeHookPhysics(){
        CompoundCollisionShape ropeHookCompound = createCompound(((Node)ropeHook), ropes[0].getChild(0)
                .getName());
        ropeHookCompound.getChildren().get(0).location = ropes[0].getLocalTranslation();
        for(int i = 1; i < ropes.length; i++)
            addNewCollisionShapeToCompound(ropeHookCompound, ropes[i], ropes[i]
                    .getChild(0).getName(), ropes[i].getLocalTranslation(), null);
        addNewCollisionShapeToCompound(ropeHookCompound, littleHookHandle, littleHookHandle
                .getChild(0).getName(), littleHookHandle.getLocalTranslation(), null);
        super.createRopeHookPhysics(ropeHookCompound, new Vector3f(0, 0.6f,0));
    }
    
    @Override
    protected void changeHookPosition(Vector3f scallingVector,
            boolean heightening){
        moveWithScallingObject(heightening, hookDisplacement, scallingVector, 
                ropes, hook, littleHookHandle);
        createRopeHookPhysics();
    }
}
