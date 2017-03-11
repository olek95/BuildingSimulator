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
    private float yCraneOffset = 0f, stretchingOut = 1f;
    private Vector3f displacement = new Vector3f();
    private static final float MAX_PROTRUSION = 9.5f, MIN_PROTRUSION = 1f,
            LIFTING_SPEED = 0.04f;
    public CraneCabin(Spatial craneSpatial){
        mobileCrane = (Node)craneSpatial;
        craneCabin = (Node)mobileCrane.getChild("crane");
        lift = (Node)craneCabin.getChild("lift");
        rectractableCranePart = (Node)lift.getChild("retractableCranePart");
        createCranePhysics();
        calculateDisplacement();
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
                    Vector3f localTranslatin = lift.getChild("hookHandle").getLocalTranslation();
                    localTranslatin.z += displacement.z;
                    localTranslatin.y += displacement.y;
                    createRectractableCranePartPhysics();
                }
                break;
            case "Pull in":
                if(stretchingOut > MIN_PROTRUSION){
                    stretchingOut -= 0.1f;
                    ((Geometry)rectractableCranePart.getChild(0)).setLocalScale(1, 1, stretchingOut);
                    Vector3f localTranslatin = lift.getChild("hookHandle").getLocalTranslation();
                    localTranslatin.z -= displacement.z;
                    localTranslatin.y -= displacement.y;
                    createRectractableCranePartPhysics();
                }
        }
    }
    private void createCranePhysics(){
        PhysicsSpace physics = BuildingSimulator.getBuildingSimulator()
                .getBulletAppState().getPhysicsSpace();
        physics.remove(craneCabin.getControl(RigidBodyControl.class));
        craneCabin.removeControl(RigidBodyControl.class);
        createObjectPhysics(craneCabin, craneCabin, "outsideCabin", "turntable");
        HingeJoint cabinAndMobilecraneJoin = new HingeJoint(mobileCrane.getControl(VehicleControl.class),
                (RigidBodyControl)craneCabin.getControl(0), craneCabin.getLocalTranslation(), Vector3f.ZERO,
                Vector3f.ZERO, Vector3f.ZERO);
        physics.add(cabinAndMobilecraneJoin);
        createObjectPhysics(lift, null, "longCraneElementGeometry");
        createRectractableCranePartPhysics();
        createObjectPhysics(lift, lift.getChild("hookHandle"), "hookHandleGeometry0",
                "hookHandleGeometry1", "hookHandleGeometry2");
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
    private void createRectractableCranePartPhysics(){
        Geometry g = (Geometry)rectractableCranePart.getChild(0);
        CollisionShape c = CollisionShapeFactory.createDynamicMeshShape(g);
        c.setScale(g.getWorldScale());
        CompoundCollisionShape com = new CompoundCollisionShape(); 
        com.addChildShape(c, Vector3f.ZERO);
        if(g.getControl(RigidBodyControl.class) != null){
            BuildingSimulator.getBuildingSimulator()
                            .getBulletAppState().getPhysicsSpace().remove(g.getControl(0));
            g.removeControl(RigidBodyControl.class);
        }
        RigidBodyControl rgc = new RigidBodyControl(com, 1f);
        rgc.setKinematic(true);
        g.addControl(rgc);
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
