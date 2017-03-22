package buildingsimulator;

import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.collision.shapes.CompoundCollisionShape;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.bullet.util.CollisionShapeFactory;
import com.jme3.math.Transform;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import java.util.LinkedHashMap;
import java.util.Map;

public class GameManager {
    public static void createObjectPhysics(Node parent, Spatial controlOwner, 
            float mass, boolean kinematic, String... children){
        CompoundCollisionShape com = createCompound(parent, children);
        createPhysics(com, controlOwner, mass, kinematic,
                (Geometry)parent.getChild(children[0]));
    }
    public static void createObjectPhysics(Node parent, Spatial controlOwner, 
            float mass, boolean kinematic, LinkedHashMap<String, Transform> children){
        CompoundCollisionShape com = createCompound(parent, children);
        createPhysics(com, controlOwner, mass, kinematic,
                (Geometry)parent.getChild(children.keySet().iterator().next()));
    }
    public static CompoundCollisionShape createCompound(Node parent, String... children){
        CompoundCollisionShape com = new CompoundCollisionShape(); 
        Geometry g = new Geometry();
        for(int i = 0; i < children.length; i++){
            g = (Geometry)parent.getChild(children[i]);
            CollisionShape c = CollisionShapeFactory.createDynamicMeshShape(g);
            c.setScale(g.getWorldScale()); 
            com.addChildShape(c, Vector3f.ZERO);
        }
        return com;
    }
    public static CompoundCollisionShape createCompound(Node parent, 
            LinkedHashMap<String, Transform> children){
        CompoundCollisionShape com = new CompoundCollisionShape(); 
        Geometry g = new Geometry();
        for(Map.Entry<String, Transform> entry : children.entrySet()){
            g = (Geometry)parent.getChild(entry.getKey());
            CollisionShape c = CollisionShapeFactory.createDynamicMeshShape(g);
            c.setScale(g.getWorldScale()); 
            Transform t = entry.getValue();
            com.addChildShape(c, t.getTranslation(), t.getRotation().toRotationMatrix());
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
}
