package buildingsimulator;

import com.jme3.bounding.BoundingBox;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.collision.shapes.CompoundCollisionShape;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.bullet.joints.HingeJoint;
import com.jme3.bullet.util.CollisionShapeFactory;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;

public class GameManager {
    public static void createObjectPhysics(Node parent, Spatial controlOwner, 
            float mass, boolean kinematic, String... children){
        CompoundCollisionShape compound = createCompound(parent, children);
        createPhysics(compound, controlOwner, mass, kinematic,
                (Geometry)parent.getChild(children[0]));
    }
    public static CompoundCollisionShape createCompound(Node parent, String... children){
        CompoundCollisionShape compound = new CompoundCollisionShape(); 
        for(int i = 0; i < children.length; i++){
            addNewCollisionShapeToComponent(compound, parent, children[i], 
                    Vector3f.ZERO, null);
        }
        return compound;
    }
    public static void createPhysics(CompoundCollisionShape compound, Spatial controlOwner,
            float mass, boolean kinematic, Geometry elementGeometry){
        PhysicsSpace physics = BuildingSimulator.getBuildingSimulator().getBulletAppState().getPhysicsSpace();
        if(controlOwner == null){
            if(elementGeometry.getControl(RigidBodyControl.class) != null){
                physics.remove(elementGeometry.getControl(0));
                elementGeometry.removeControl(RigidBodyControl.class);
            }
        }else{
            if(controlOwner.getControl(RigidBodyControl.class) != null){
                physics.remove(controlOwner.getControl(0));
                controlOwner.removeControl(RigidBodyControl.class);
            }
        }
        RigidBodyControl control = new RigidBodyControl(compound, mass);
        control.setKinematic(kinematic);
        if(controlOwner == null) elementGeometry.addControl(control);
        else controlOwner.addControl(control);
        physics.add(control);
    }
    public static Vector3f calculateDisplacementAfterScaling(Node parent, 
            Vector3f scale, boolean x, boolean y, boolean z){
        Geometry parentGeometry = (Geometry)((Node)parent.clone())
                .getChild(0);
        Vector3f displacement = new Vector3f(),
                initialSize  = ((BoundingBox)parentGeometry.getWorldBound()).getExtent(null);
        parentGeometry.setLocalScale(scale);
        ((BoundingBox)parentGeometry.getWorldBound()).getExtent(displacement);
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
    public static HingeJoint joinsElementToOtherElement(HingeJoint joint, Spatial nodeA,
            Spatial nodeB, Vector3f pivotA, Vector3f pivotB){
        PhysicsSpace physics = BuildingSimulator.getBuildingSimulator()
                        .getBulletAppState().getPhysicsSpace();
        if(joint != null) physics.remove(joint);
        joint = new HingeJoint(nodeA.getControl(RigidBodyControl.class), nodeB
               .getControl(RigidBodyControl.class), pivotA, pivotB, Vector3f.ZERO,
                Vector3f.ZERO);
        physics.add(joint);
        return joint;
    }
    public static void addNewCollisionShapeToComponent(CompoundCollisionShape compound,
            Node parent, String child, Vector3f location, Quaternion rotation){
        Geometry parentGeometry = (Geometry)parent.getChild(child);
        CollisionShape elementCollisionShape = CollisionShapeFactory
                .createDynamicMeshShape(parentGeometry);
        elementCollisionShape.setScale(parentGeometry.getWorldScale()); 
        if(rotation == null) compound.addChildShape(elementCollisionShape, location);
        else compound.addChildShape(elementCollisionShape, location, rotation
                .toRotationMatrix());
    }
}
