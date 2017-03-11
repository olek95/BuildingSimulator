package buildingsimulator;

import com.jme3.bounding.BoundingBox;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.collision.shapes.CompoundCollisionShape;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.bullet.control.VehicleControl;
import com.jme3.bullet.joints.HingeJoint;
import com.jme3.bullet.joints.Point2PointJoint;
import com.jme3.bullet.joints.SixDofJoint;
import com.jme3.bullet.util.CollisionShapeFactory;
import com.jme3.input.controls.AnalogListener;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Line;

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
                g1 = ((Geometry)mobileCrane.getChild("Cube.0071"));
                stretchingOut += 0.1f;
                g1.setLocalScale(1, 1, stretchingOut);
                mobileCrane.getChild("hookHandle").getLocalTranslation().z += displacement.z;
                mobileCrane.getChild("hookHandle").getLocalTranslation().y += displacement.y;
                c1 = CollisionShapeFactory.createDynamicMeshShape(g1);
                c1.setScale(g1.getWorldScale());
                com = new CompoundCollisionShape(); 
                com.addChildShape(c1, Vector3f.ZERO);
                BuildingSimulator.getBuildingSimulator()
                        .getBulletAppState().getPhysicsSpace().remove(g1.getControl(0));
                g1.removeControl(RigidBodyControl.class);
                rgc = new RigidBodyControl(com, 1f);
                rgc.setKinematic(true);
                g1.addControl(rgc);
                BuildingSimulator.getBuildingSimulator()
                .getBulletAppState().getPhysicsSpace().add(rgc); 
                break;
                //((BoundingBox)g1.getParent().getWorldBound()).getExtent(beforeZ);
                //Point2PointJoint hj = new Point2PointJoint(r2,
                //r, Vector3f.ZERO, new Vector3f(0,0,-2));
               // BuildingSimulator.getBuildingSimulator()
               // .getBulletAppState().getPhysicsSpace().add(hj);
                /*HingeJoint hj = new HingeJoint(r2,
                r, Vector3f.ZERO, new Vector3f(0,0,-2),
                Vector3f.ZERO, Vector3f.ZERO);
                BuildingSimulator.getBuildingSimulator()
                .getBulletAppState().getPhysicsSpace().add(hj);*/
            case "Pull in":
                g1 = ((Geometry)mobileCrane.getChild("Cube.0071"));
                stretchingOut -= 0.1f;
                g1.setLocalScale(1, 1, stretchingOut);
                mobileCrane.getChild("hookHandle").getLocalTranslation().z -= displacement.z;
                mobileCrane.getChild("hookHandle").getLocalTranslation().y -= displacement.y;
                c1 = CollisionShapeFactory.createDynamicMeshShape(g1);
                c1.setScale(g1.getWorldScale());
                com = new CompoundCollisionShape(); 
                com.addChildShape(c1, Vector3f.ZERO);
                BuildingSimulator.getBuildingSimulator()
                        .getBulletAppState().getPhysicsSpace().remove(g1.getControl(0));
                g1.removeControl(RigidBodyControl.class);
                rgc = new RigidBodyControl(com, 1f);
                rgc.setKinematic(true);
                g1.addControl(rgc);
                BuildingSimulator.getBuildingSimulator()
                .getBulletAppState().getPhysicsSpace().add(rgc); 
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
        //physics.add(((Node)mobileCrane.getChild("lift")).getControl(0));
        //physics.add(((Node)mobileCrane.getChild("longCraneElement")).getControl(0));
        Geometry g1 = (Geometry)mobileCrane.getChild("Cube.0041");
        CollisionShape c1 = CollisionShapeFactory.createDynamicMeshShape(g1);
        c1.setScale(g1.getWorldScale());
        CompoundCollisionShape com = new CompoundCollisionShape(); 
        com.addChildShape(c1, Vector3f.ZERO);
        
        /*Geometry g2 = (Geometry)mobileCrane.getChild("Cube.0071");
        CollisionShape c2 = CollisionShapeFactory.createDynamicMeshShape(g2);
        c2.setScale(g2.getWorldScale());
        com.addChildShape(c2, g2.getWorldTranslation());*/
        
        RigidBodyControl rgc = new RigidBodyControl(com, 0f);
        rgc.setKinematic(true);
        g1.addControl(rgc);
        physics.add(rgc);
        
        Geometry g2 = (Geometry)mobileCrane.getChild("Cube.0071");
        CollisionShape c2 = CollisionShapeFactory.createDynamicMeshShape(g2);
        c2.setScale(g2.getWorldScale());
        CompoundCollisionShape com2 = new CompoundCollisionShape(); 
        com2.addChildShape(c2, Vector3f.ZERO);
        
        RigidBodyControl rgc2 = new RigidBodyControl(com2, 0f);
        rgc2.setKinematic(true);
        g2.addControl(rgc2);
        physics.add(rgc2);
        
        Geometry g3 = (Geometry)mobileCrane.getChild("Cube.0081");
        CollisionShape c3 = CollisionShapeFactory.createDynamicMeshShape(g3);
        c3.setScale(g3.getWorldScale());
        Geometry g4 = (Geometry)mobileCrane.getChild("Cube.0082");
        CollisionShape c4 = CollisionShapeFactory.createDynamicMeshShape(g4);
        c4.setScale(g4.getWorldScale());
        Geometry g5 = (Geometry)mobileCrane.getChild("Cube.0083");
        CollisionShape c5 = CollisionShapeFactory.createDynamicMeshShape(g5);
        c5.setScale(g5.getWorldScale());
        CompoundCollisionShape com3 = new CompoundCollisionShape(); 
        com3.addChildShape(c3, Vector3f.ZERO);
        com3.addChildShape(c4, Vector3f.ZERO);
        com3.addChildShape(c5, Vector3f.ZERO);
        RigidBodyControl rgc3 = new RigidBodyControl(com3, 1f);
        rgc3.setKinematic(true);
        mobileCrane.getChild("hookHandle").addControl(rgc3);
        physics.add(rgc3);
        //rgc3.setGravity(Vector3f.ZERO);
        //r = rgc3;
        //r2 =rgc2;
        //Vector3f actual = new Vector3f();
        //((BoundingBox)g3.getParent().getWorldBound()).getExtent(actual);
        //Point2PointJoint hj = new Point2PointJoint(r2,
        //        r, new Vector3f(0,0,0.85f), Vector3f.ZERO);
       //         physics.add(hj);
        /*HingeJoint hj = new HingeJoint(rgc2,
                rgc3, Vector3f.ZERO, new Vector3f(0,0,-1.5f),
                Vector3f.ZERO, Vector3f.ZERO);
        physics.add(hj);*/
        //Line l = new Line(g5.getLocalTranslation(), g4.getLocalTranslation());
        //BuildingSimulator.getBuildingSimulator().getRootNode().attachChild(l);
        //SixDofJoint sdj = new SixDofJoint(rgc2, rgc3, (rgc3.getPhysicsLocation().subtract(rgc2.getPhysicsLocation())).mult(1f),
        //       Vector3f.ZERO, false);
        //physics.add(sdj);
        //physics.add(((Node)mobileCrane.getChild("lift")).getControl(0));
    }
    private void calculateDisplacement(){
        Geometry g = (Geometry)((Node)lift.getChild("retractableCranePart").clone())
                .getChild(0);
        Vector3f initialSize  = ((BoundingBox)g.getWorldBound()).getExtent(null);
        g.setLocalScale(1, 1, 1.1f);
        ((BoundingBox)g.getWorldBound()).getExtent(displacement);
        displacement.z -= initialSize.z;
        displacement.y -= initialSize.y;
    }
}
