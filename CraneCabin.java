package buildingsimulator;

import com.jme3.bounding.BoundingBox;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.collision.shapes.CompoundCollisionShape;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.bullet.control.VehicleControl;
import com.jme3.bullet.joints.HingeJoint;
import com.jme3.bullet.util.CollisionShapeFactory;
import com.jme3.input.controls.AnalogListener;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;

public class CraneCabin implements AnalogListener{
    private Node craneCabin, lift, rectractableCranePart, mobileCrane, rope;
    private Spatial hookHandle, hook, ropeHook;
    private float yCraneOffset = 0f, stretchingOut = 1f, loweringHeight = 1f;
    private Vector3f hookHandleDisplacement = new Vector3f(),
            hookDisplacement = new Vector3f();
    private static final float MAX_PROTRUSION = 9.5f, MIN_PROTRUSION = 1f,
            LIFTING_SPEED = 0.04f;
    private HingeJoint lineAndHookHandleJoint;
    public CraneCabin(Spatial craneSpatial){
        mobileCrane = (Node)craneSpatial;
        craneCabin = (Node)mobileCrane.getChild("crane");
        lift = (Node)craneCabin.getChild("lift");
        rectractableCranePart = (Node)lift.getChild("retractableCranePart");
        hookHandle = lift.getChild("hookHandle");
        ropeHook = mobileCrane.getChild("ropeHook");
        hook = ((Node)ropeHook).getChild("hook");
        rope = (Node)((Node)ropeHook).getChild("rope");
        createCranePhysics();
        hookHandleDisplacement = calculateDisplacement(rectractableCranePart, 
                new Vector3f(1f, 1f, 1.1f), false, true, true);
        hookDisplacement = calculateDisplacement((Node)mobileCrane.getChild("rope"), 
                new Vector3f(1f, 1.05f, 1f), false, true, false);
    }
    public void onAnalog(String name, float value, float tpf) {
        switch(name){
            case "Right":
                craneCabin.rotate(0, -tpf / 4, 0);
                break;
            case "Left": 
                craneCabin.rotate(0, tpf / 4, 0);
                break;
            case "Up":
                if(yCraneOffset + LIFTING_SPEED < 0.6f){
                    yCraneOffset += LIFTING_SPEED;
                    lift.rotate(-LIFTING_SPEED, 0, 0);
                }
                break;
            case "Down":
                if(yCraneOffset - LIFTING_SPEED >= 0f){
                    yCraneOffset -= LIFTING_SPEED;
                    lift.rotate(LIFTING_SPEED, 0, 0);
                }
                break;
            case "Pull out":
                if(stretchingOut <= MAX_PROTRUSION){
                    stretchingOut += 0.1f;
                    ((Geometry)rectractableCranePart.getChild(0)).setLocalScale(1, 1,
                            stretchingOut);
                    Vector3f localTranslation = hookHandle.getLocalTranslation();
                    localTranslation.z += hookHandleDisplacement.z;
                    localTranslation.y += hookHandleDisplacement.y;
                    GameManager.createObjectPhysics(rectractableCranePart,
                            rectractableCranePart, 1f, true, rectractableCranePart
                            .getChild(0).getName());
                }
                break;
            case "Pull in":
                if(stretchingOut > MIN_PROTRUSION){
                    stretchingOut -= 0.1f;
                    ((Geometry)rectractableCranePart.getChild(0)).setLocalScale(1, 1,
                            stretchingOut);
                    Vector3f localTranslation = hookHandle.getLocalTranslation();
                    localTranslation.z -= hookHandleDisplacement.z;
                    localTranslation.y -= hookHandleDisplacement.y;
                    GameManager.createObjectPhysics(rectractableCranePart, 
                            rectractableCranePart, 1f, true, rectractableCranePart
                            .getChild(0).getName());
                }
                break;
            case "Lower hook":
                loweringHeight += 0.05f;
                ((Geometry)mobileCrane.getChild("Cylinder.0021")).setLocalScale(1f,
                        loweringHeight, 1f);
                Vector3f localTranslation = hook.getLocalTranslation();
                localTranslation.y -= hookDisplacement.y * 2; // razy 2 bo zwiększam tylko w jedną strone?
                CompoundCollisionShape com = GameManager.createCompound(rope, 
                        rope.getChild(0).getName());
                Geometry g2 = (Geometry)mobileCrane.getChild("Mesh1");
                    CollisionShape c2 = CollisionShapeFactory.createDynamicMeshShape(g2);
                    c2.setScale(g2.getWorldScale());
                    com.addChildShape(c2, hook.getLocalTranslation(),
                            hook.getLocalRotation().toRotationMatrix());
                GameManager.createPhysics(com, ropeHook, 4f, false, (Geometry)rope
                        .getChild(0));
                PhysicsSpace physics = BuildingSimulator.getBuildingSimulator()
                        .getBulletAppState().getPhysicsSpace();
                physics.remove(lineAndHookHandleJoint);
                lineAndHookHandleJoint = new HingeJoint(hookHandle
                        .getControl(RigidBodyControl.class), ropeHook
                        .getControl(RigidBodyControl.class), Vector3f.ZERO,
                        new Vector3f(0, 0.06f,0), Vector3f.ZERO, Vector3f.ZERO);
                physics.add(lineAndHookHandleJoint);
        }
    }
    private void createCranePhysics(){
        PhysicsSpace physics = BuildingSimulator.getBuildingSimulator()
                .getBulletAppState().getPhysicsSpace();
        GameManager.createObjectPhysics(craneCabin, craneCabin, 1f, true, 
                "outsideCabin", "turntable");
        HingeJoint cabinAndMobilecraneJoin = new HingeJoint(mobileCrane
                .getControl(VehicleControl.class),(RigidBodyControl)craneCabin
                .getControl(0), craneCabin.getLocalTranslation(), Vector3f.ZERO,
                Vector3f.ZERO, Vector3f.ZERO);
        physics.add(cabinAndMobilecraneJoin);
        physics.add(lift.getChild("longCraneElement").getControl(0));
        GameManager.createObjectPhysics(rectractableCranePart, rectractableCranePart,
                1f, true, rectractableCranePart.getChild(0).getName());
        physics.add(hookHandle.getControl(0));
        CompoundCollisionShape com = GameManager.createCompound(rope, rope
                .getChild(0).getName());
        Geometry g2 = (Geometry)mobileCrane.getChild("Mesh1");
            CollisionShape c2 = CollisionShapeFactory.createDynamicMeshShape(g2);
            c2.setScale(g2.getWorldScale());
            com.addChildShape(c2, hook.getLocalTranslation(), 
                    hook.getLocalRotation().toRotationMatrix());
        GameManager.createPhysics(com, ropeHook, 4f, false,
                (Geometry)rope.getChild(0));
        ropeHook.getControl(RigidBodyControl.class).setCollisionGroup(2); // kolizja z trzymaniem
        lineAndHookHandleJoint = new HingeJoint(hookHandle
                .getControl(RigidBodyControl.class), ropeHook
                .getControl(RigidBodyControl.class), Vector3f.ZERO,
                new Vector3f(0f, 0.06f,0f), Vector3f.ZERO, Vector3f.ZERO);
        physics.add(lineAndHookHandleJoint);
    }
    private Vector3f calculateDisplacement(Node parent, 
            Vector3f scale, boolean x, boolean y, boolean z){
        Geometry g = (Geometry)((Node)parent.clone())
                .getChild(0);
        Vector3f displacement = new Vector3f(),
                initialSize  = ((BoundingBox)g.getWorldBound()).getExtent(null);
        g.setLocalScale(scale);
        ((BoundingBox)g.getWorldBound()).getExtent(displacement);
        if(x) displacement.x -= initialSize.z;
        if(y) displacement.y -= initialSize.y;
        if(z) displacement.z -= initialSize.z;
        return displacement;
    }
}
