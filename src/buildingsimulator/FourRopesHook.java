package buildingsimulator;

import static buildingsimulator.GameManager.*;
import com.jme3.bounding.BoundingBox;
import com.jme3.bullet.collision.PhysicsCollisionGroupListener;
import com.jme3.bullet.collision.PhysicsCollisionObject;
import com.jme3.bullet.collision.shapes.CompoundCollisionShape;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.bullet.joints.HingeJoint;
import com.jme3.collision.CollisionResults;
import com.jme3.math.Ray;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import java.util.List;

public class FourRopesHook extends Hook{
    private Node littleHookHandle;
    private Node[] ropes = new Node[4];
    private HingeJoint lineAndHookHandleJoint = null;
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
    public void lower(){
        CollisionResults results = new CollisionResults();
        /* jeśli nie dotknęło żadnego obiektu, to zbędne jest sprawdzanie 
        kolizji w dół*/
        if(recentlyHitObject != null){
            System.out.println(recentlyHitObject);
            Ray ray = new Ray(hook.getWorldTranslation(), new Vector3f(0,-0.5f,0));
            if(recentlyHitObject.getName().startsWith("prop")){
                // new Ray tworzy pomocniczy promień sprawdzający kolizję w dół
                Node crane = recentlyHitObject.getParent();
                ((Node)crane.getChild("prop0")).getChild(0).collideWith(ray, results);
                ((Node)crane.getChild("prop1")).getChild(0).collideWith(ray, results);
                //ray.collideWith((BoundingBox)BuildingSimulator.getScene().getWorldBound(), results);
            }else{
                ray.collideWith((BoundingBox)recentlyHitObject.getWorldBound(), results);
            }
        }
        // obniża hak, jeśli w żadnym punkcie z dołu nie dotyka jakiegoś obiektu
        if(results.size() == 0){
            changeHookPosition(new Vector3f(1f,hookLowering += HOOK_LOWERING_SPEED, 1f),
                    false);
            recentlyHitObject = null;
        }
    }
    
    protected void createRopeHookPhysics(){
        CompoundCollisionShape ropeHookCompound = createCompound(((Node)ropeHook), ropes[0].getChild(0)
                .getName());
        ropeHookCompound.getChildren().get(0).location = ropes[0].getLocalTranslation();
        addNewCollisionShapeToCompound(ropeHookCompound, ropes[1], ropes[1].getChild(0).getName(),
                ropes[1].getLocalTranslation(), null);
        addNewCollisionShapeToCompound(ropeHookCompound, ropes[2], ropes[2].getChild(0).getName(),
                ropes[2].getLocalTranslation(), null);
        addNewCollisionShapeToCompound(ropeHookCompound, ropes[3], ropes[3].getChild(0).getName(),
                ropes[3].getLocalTranslation(), null);
        addNewCollisionShapeToCompound(ropeHookCompound, littleHookHandle, littleHookHandle
                .getChild(0).getName(), littleHookHandle.getLocalTranslation(), null);
        addNewCollisionShapeToCompound(ropeHookCompound, (Node)hook, ((Node)hook).getChild(0).getName(),
                hook.getLocalTranslation(), hook.getLocalRotation());
        createPhysics(ropeHookCompound, ropeHook, 4f, false);
        RigidBodyControl ropeHookControl = ropeHook.getControl(RigidBodyControl.class);
        ropeHookControl.setCollisionGroup(2); 
        lineAndHookHandleJoint = joinsElementToOtherElement(lineAndHookHandleJoint,
                hookHandle, ropeHook, Vector3f.ZERO, new Vector3f(0, 0.6f,0));
    }
    /**
     * Zwraca węzeł z hakiem z liniami. 
     * @return węzeł z hakiem z liniami 
     */
    public Node getRopeHook(){
        return ropeHook;
    }
    protected void changeHookPosition(Vector3f scallingVector,
            boolean heightening){
        moveWithScallingObject(heightening, hookDisplacement, scallingVector, 
                ropes, hook, littleHookHandle);
        createRopeHookPhysics();
    }
    public PhysicsCollisionGroupListener createCollisionListener(){
        return new PhysicsCollisionGroupListener(){
            @Override
            public boolean collide(PhysicsCollisionObject nodeA, PhysicsCollisionObject nodeB){
                Object a = nodeA.getUserObject(), b = nodeB.getUserObject();
                /*boolean isGround = false;
                if(recentlyHitObject != null) 
                    isGround = recentlyHitObject.getName().equals("New Scene");
                if(!isGround)*/
                if(a.equals(ropeHook) && !b.equals(hookHandle)){
                    Spatial spatialA = (Spatial)a, spatialB = (Spatial)b;
                    float y1 = ((BoundingBox)spatialA.getWorldBound()).getMin(null).y,
                            y2 = ((BoundingBox)spatialB.getWorldBound()).getMax(null).y;
                    if(Math.abs(y1-y2) >= 0 && Math.abs(y1-y2) < 0.1f)
                        recentlyHitObject = spatialB;
                    int collisionGroup = nodeB.getCollisionGroup();
                    if(collisionGroup != 3 && collisionGroup != 1) return false;
                }else{
                    if(b.equals(ropeHook) && !a.equals(hookHandle)){
                        Spatial spatialA = (Spatial)a, spatialB = (Spatial)b;
                    float y1 = ((BoundingBox)spatialA.getWorldBound()).getMax(null).y,
                            y2 = ((BoundingBox)spatialB.getWorldBound()).getMin(null).y;
                    if(Math.abs(y1-y2) >= 0 && Math.abs(y1-y2) < 0.1f)
                            recentlyHitObject = spatialA;
                        int collisionGroup = nodeA.getCollisionGroup();
                        if(collisionGroup != 3 && collisionGroup != 1) return false;
                    }
                }
                return true;
            }
        };
    }
}
