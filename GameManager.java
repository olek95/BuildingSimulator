package buildingsimulator;

import com.jme3.bounding.BoundingBox;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.collision.shapes.CompoundCollisionShape;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.bullet.joints.HingeJoint;
import com.jme3.bullet.util.CollisionShapeFactory;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;

public class GameManager {
    public static void createObjectPhysics(Node parent, Spatial controlOwner, 
            float mass, boolean kinematic, String... children){
        CompoundCollisionShape com = createCompound(parent, children);
        createPhysics(com, controlOwner, mass, kinematic,
                (Geometry)parent.getChild(children[0]));
    }
    public static CompoundCollisionShape createCompound(Node parent, String... children){
        CompoundCollisionShape com = new CompoundCollisionShape(); 
        Geometry g;
        for(int i = 0; i < children.length; i++){
            g = (Geometry)parent.getChild(children[i]);
            CollisionShape c = CollisionShapeFactory.createDynamicMeshShape(g);
            c.setScale(g.getWorldScale()); 
            com.addChildShape(c, Vector3f.ZERO);
        }
        return com;
    }
    public static void createPhysics(CompoundCollisionShape com, Spatial controlOwner,
            float mass, boolean kinematic, Geometry g){
        PhysicsSpace physics = BuildingSimulator.getBuildingSimulator().getBulletAppState().getPhysicsSpace();
        if(controlOwner == null){
            if(g.getControl(RigidBodyControl.class) != null){
                physics.remove(g.getControl(0));
                g.removeControl(RigidBodyControl.class);
            }
        }else{
            if(controlOwner.getControl(RigidBodyControl.class) != null){
                physics.remove(controlOwner.getControl(0));
                controlOwner.removeControl(RigidBodyControl.class);
            }
        }
        RigidBodyControl rgc = new RigidBodyControl(com, mass);
        rgc.setKinematic(kinematic);
        if(controlOwner == null) g.addControl(rgc);
        else controlOwner.addControl(rgc);
        physics.add(rgc);
    }
    public static Vector3f calculateDisplacementAfterScaling(Node parent, 
            Vector3f scale, boolean x, boolean y, boolean z){
        Geometry g = (Geometry)((Node)parent.clone())
                .getChild(0);
        Vector3f displacement = new Vector3f(),
                initialSize  = ((BoundingBox)g.getWorldBound()).getExtent(null);
        g.setLocalScale(scale);
        ((BoundingBox)g.getWorldBound()).getExtent(displacement);
        if(x) displacement.x -= initialSize.z;
        else displacement.x = 0f;
        if(y) displacement.y -= initialSize.y;
        else displacement.y = 0f;
        if(z) displacement.z -= initialSize.z;
        else displacement.z = 0f;
        return displacement;
    }
    public static void movingDuringStretchingOut(boolean addition, Spatial movingElement,
            Vector3f elementDisplacement){
        Vector3f localTranslation = movingElement.getLocalTranslation();
        Vector3f displacement = elementDisplacement.clone();
        if(!addition) displacement.negateLocal();
        localTranslation.addLocal(displacement);
    }
    public static void joinsElementToOtherElement(HingeJoint joint, Spatial nodeA, Spatial nodeB,
            Vector3f pivotA, Vector3f pivotB){
        PhysicsSpace physics = BuildingSimulator.getBuildingSimulator()
                        .getBulletAppState().getPhysicsSpace();
        if(joint != null) physics.remove(joint);
        joint = new HingeJoint(nodeA.getControl(RigidBodyControl.class), nodeB
               .getControl(RigidBodyControl.class), pivotA, pivotB, Vector3f.ZERO,
                Vector3f.ZERO);
        physics.add(joint);
    }
}
