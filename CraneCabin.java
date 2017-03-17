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
    private Node craneCabin, lift, rectractableCranePart;
    private Node mobileCrane;
    private float yCraneOffset = 0f, stretchingOut = 1f, loweringHeight = 1f;
    private Vector3f displacement = new Vector3f();
    private static final float MAX_PROTRUSION = 9.5f, MIN_PROTRUSION = 1f,
            LIFTING_SPEED = 0.04f;
    private HingeJoint lineAndHookHandleJoint;
    public CraneCabin(Spatial craneSpatial){
        mobileCrane = (Node)craneSpatial;
        craneCabin = (Node)mobileCrane.getChild("crane");
        lift = (Node)craneCabin.getChild("lift");
        rectractableCranePart = (Node)lift.getChild("retractableCranePart");
        createCranePhysics();
        calculateDisplacement();
        //loweringHeight = 
    }
    public void onAnalog(String name, float value, float tpf) {
        switch(name){
            case "Right":
                craneCabin.rotate(0, -tpf, 0);
                break;
            case "Left": 
                craneCabin.rotate(0, tpf, 0);
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
                    ((Geometry)rectractableCranePart.getChild(0)).setLocalScale(1, 1, stretchingOut);
                    Vector3f localTranslation = lift.getChild("hookHandle").getLocalTranslation();
                    localTranslation.z += displacement.z;
                    localTranslation.y += displacement.y;
                    createScaledPartPhysics(rectractableCranePart, true, 1f);
                }
                break;
            case "Pull in":
                if(stretchingOut > MIN_PROTRUSION){
                    stretchingOut -= 0.1f;
                    ((Geometry)rectractableCranePart.getChild(0)).setLocalScale(1, 1, stretchingOut);
                    Vector3f localTranslation = lift.getChild("hookHandle").getLocalTranslation();
                    localTranslation.z -= displacement.z;
                    localTranslation.y -= displacement.y;
                    createScaledPartPhysics(rectractableCranePart, true, 1f);
                }
                break;
            case "Lower hook":
                loweringHeight += 0.05f;
                Vector3f scale = ((Geometry)(mobileCrane.getChild("Cylinder.0021"))).getLocalScale();
                System.out.println(scale.x + " " + scale.y);
                mobileCrane.getChild("Cylinder.0021").setLocalScale(scale.x, loweringHeight, scale.z);
                Spatial rope = mobileCrane.getChild("rope");
                createScaledPartPhysics((Node)rope, false, 2f);
                PhysicsSpace physics = BuildingSimulator.getBuildingSimulator()
                        .getBulletAppState().getPhysicsSpace();
                physics.remove(lineAndHookHandleJoint);
                lineAndHookHandleJoint = new HingeJoint((RigidBodyControl)lift.getChild("hookHandle")
                        .getControl(0), rope.getControl(RigidBodyControl.class),
                        Vector3f.ZERO, new Vector3f(0, 0.06f,0), Vector3f.ZERO, 
                        Vector3f.ZERO);
                physics.add(lineAndHookHandleJoint);
        }
    }
    private void createCranePhysics(){
        PhysicsSpace physics = BuildingSimulator.getBuildingSimulator()
                .getBulletAppState().getPhysicsSpace();
        if(craneCabin.getControl(RigidBodyControl.class) != null){
            physics.remove(craneCabin.getControl(RigidBodyControl.class));
            craneCabin.removeControl(RigidBodyControl.class);
        }
        createObjectPhysics(craneCabin, craneCabin, "outsideCabin", "turntable");
        HingeJoint cabinAndMobilecraneJoin = new HingeJoint(mobileCrane.getControl(VehicleControl.class),
                (RigidBodyControl)craneCabin.getControl(0), craneCabin.getLocalTranslation(), Vector3f.ZERO,
                Vector3f.ZERO, Vector3f.ZERO);
        physics.add(cabinAndMobilecraneJoin);
        createObjectPhysics(lift, null, "longCraneElementGeometry");
        createScaledPartPhysics(rectractableCranePart, true, 1f);
        createObjectPhysics(lift, lift.getChild("hookHandle"), "hookHandleGeometry0",
                "hookHandleGeometry1", "hookHandleGeometry2");
        Spatial rope = mobileCrane.getChild("rope");
        createScaledPartPhysics((Node)rope, false, 2f);
        lineAndHookHandleJoint = new HingeJoint((RigidBodyControl)lift.getChild("hookHandle").getControl(0),
                rope.getControl(RigidBodyControl.class), Vector3f.ZERO, new Vector3f(0, 0.06f,0), Vector3f.ZERO, Vector3f.ZERO);
        physics.add(lineAndHookHandleJoint);
        //g.lookAt(new Vector3f(0,1,0), new Vector3f(0,1,0));
    }
    private void calculateDisplacement(){
        Geometry g = (Geometry)((Node)rectractableCranePart.clone())
                .getChild(0);
        Vector3f initialSize  = ((BoundingBox)g.getWorldBound()).getExtent(null);
        g.setLocalScale(1, 1, 1.1f);
        ((BoundingBox)g.getWorldBound()).getExtent(displacement);
        displacement.z -= initialSize.z;
        displacement.y -= initialSize.y;
    }
    private void createScaledPartPhysics(Node parent, boolean kinematic, float mass){
        Geometry g = (Geometry)parent.getChild(0);
        CollisionShape c = CollisionShapeFactory.createDynamicMeshShape(g);
        c.setScale(g.getWorldScale());
        CompoundCollisionShape com = new CompoundCollisionShape(); 
        com.addChildShape(c, Vector3f.ZERO);
        if(parent.getControl(RigidBodyControl.class) != null){
            BuildingSimulator.getBuildingSimulator()
                    .getBulletAppState().getPhysicsSpace().remove(parent.getControl(0));
            parent.removeControl(RigidBodyControl.class);
        }
        RigidBodyControl rgc = new RigidBodyControl(com, mass);
        rgc.setKinematic(kinematic);
        parent.addControl(rgc);
        BuildingSimulator.getBuildingSimulator().getBulletAppState().getPhysicsSpace()
                .add(rgc);
    }
    private void createObjectPhysics(Node parent, Spatial controlOwner, String... children){
        CompoundCollisionShape com = new CompoundCollisionShape(); 
        Geometry g = new Geometry();
        for(int i = 0; i < children.length; i++){
            g = (Geometry)parent.getChild(children[i]);
            CollisionShape c = CollisionShapeFactory.createDynamicMeshShape(g);
            c.setScale(g.getWorldScale()); 
            com.addChildShape(c, Vector3f.ZERO);
        }
        RigidBodyControl rgc = new RigidBodyControl(com, 1f);
        rgc.setKinematic(true);
        if(controlOwner == null) g.addControl(rgc);
        else controlOwner.addControl(rgc);
        BuildingSimulator.getBuildingSimulator().getBulletAppState()
                .getPhysicsSpace().add(rgc);
    }
}

