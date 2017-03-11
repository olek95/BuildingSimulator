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
    private float yCraneOffset = 0f, liftingSpeed = 0.04f, stretchingOut = 1f;
    private Vector3f displacement = new Vector3f();
    public CraneCabin(Spatial craneSpatial){
        mobileCrane = (Node)craneSpatial;
        craneCabin = (Node)mobileCrane.getChild("crane");
        lift = (Node)craneCabin.getChild("lift");
        rectractableCranePart = (Node)lift.getChild("retractableCranePart");
        createCranePhysics();
        calculateDisplacement();
    }
    public void onAnalog(String name, float value, float tpf) {
        Geometry g1;
        CollisionShape c1;
        CompoundCollisionShape com;
        RigidBodyControl rgc;
        switch(name){
            case "Right":
                craneCabin.rotate(0, -tpf, 0);
                break;
            case "Left": 
                craneCabin.rotate(0, tpf, 0);
                break;
            case "Up":
                if(yCraneOffset + liftingSpeed < 0.6f){
                    yCraneOffset += liftingSpeed;
                    lift.rotate(-liftingSpeed, 0, 0);
                }
                break;
            case "Down":
                if(yCraneOffset - liftingSpeed >= 0f){
                    yCraneOffset -= liftingSpeed;
                    lift.rotate(liftingSpeed, 0, 0);
                }
                break;
            case "Pull out":
                g1 = ((Geometry)rectractableCranePart.getChild(0));
                stretchingOut += 0.1f;
                g1.setLocalScale(1, 1, stretchingOut);
                lift.getChild("hookHandle").getLocalTranslation().z += displacement.z;
                lift.getChild("hookHandle").getLocalTranslation().y += displacement.y;
                createRectractableCranePartPhysics();
                break;
            case "Pull in":
                g1 = ((Geometry)rectractableCranePart.getChild(0));
                stretchingOut -= 0.1f;
                g1.setLocalScale(1, 1, stretchingOut);
                lift.getChild("hookHandle").getLocalTranslation().z -= displacement.z;
                lift.getChild("hookHandle").getLocalTranslation().y -= displacement.y;
                createRectractableCranePartPhysics();
        }
    }
    private void createCranePhysics(){
        PhysicsSpace physics = BuildingSimulator.getBuildingSimulator()
                .getBulletAppState().getPhysicsSpace();
        physics.remove(craneCabin.getControl(RigidBodyControl.class));
        String[] cabinElements = {"outsideCabin", "turntable"};
        CompoundCollisionShape cabinCollision = new CompoundCollisionShape();
        for(int i = 0; i < cabinElements.length; i++){
            Geometry elementGeometry = (Geometry)mobileCrane.getChild(cabinElements[i]);
            CollisionShape elementCollision = CollisionShapeFactory.createDynamicMeshShape(elementGeometry);
            elementCollision.setScale(elementGeometry.getWorldScale());
            cabinCollision.addChildShape(elementCollision, Vector3f.ZERO);
        }
        RigidBodyControl cabinControl = new RigidBodyControl(cabinCollision, 1f);
        cabinControl.setKinematic(true);
        cabinControl.setCollisionGroup(2);
        craneCabin.addControl(cabinControl);
        physics.add(cabinControl);
        HingeJoint cabinAndMobilecraneJoin = new HingeJoint(mobileCrane.getControl(VehicleControl.class),
                cabinControl, craneCabin.getLocalTranslation(), Vector3f.ZERO,
                Vector3f.ZERO, Vector3f.ZERO);
        physics.add(cabinAndMobilecraneJoin);
        
        createObjectPhysics(lift, null, "Cube.0041");
        createRectractableCranePartPhysics();
        createObjectPhysics(lift, "hookHandle", "Cube.0081", "Cube.0082", "Cube.0083");
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
        RigidBodyControl rgc = new RigidBodyControl(com, 0f);
        rgc.setKinematic(true);
        g.addControl(rgc);
        BuildingSimulator.getBuildingSimulator().getBulletAppState().getPhysicsSpace()
                .add(rgc);
    }
    private void createObjectPhysics(Node parent, String controlOwner, String... children){
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
        else parent.getChild(controlOwner).addControl(rgc);
        BuildingSimulator.getBuildingSimulator().getBulletAppState()
                .getPhysicsSpace().add(rgc);
    }
}
