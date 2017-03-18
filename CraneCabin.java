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
                    ((Geometry)rectractableCranePart.getChild(0)).setLocalScale(1, 1, stretchingOut);
                    Vector3f localTranslation = lift.getChild("hookHandle").getLocalTranslation();
                    localTranslation.z += hookHandleDisplacement.z;
                    localTranslation.y += hookHandleDisplacement.y;
                    createScaledPartPhysics(rectractableCranePart, true, 1f);
                }
                break;
            case "Pull in":
                if(stretchingOut > MIN_PROTRUSION){
                    stretchingOut -= 0.1f;
                    ((Geometry)rectractableCranePart.getChild(0)).setLocalScale(1, 1, stretchingOut);
                    Vector3f localTranslation = lift.getChild("hookHandle").getLocalTranslation();
                    localTranslation.z -= hookHandleDisplacement.z;
                    localTranslation.y -= hookHandleDisplacement.y;
                    createScaledPartPhysics(rectractableCranePart, true, 1f);
                }
                break;
            case "Lower hook":
                loweringHeight += 0.05f;
                ((Geometry)mobileCrane.getChild("Cylinder.0021")).setLocalScale(1f, loweringHeight, 1f);
                Vector3f localTranslation = mobileCrane.getChild("hook").getLocalTranslation();
                localTranslation.y -= hookDisplacement.y * 2; // razy 2 bo zwiększam tylko w jedną strone?
                Spatial rope = mobileCrane.getChild("rope");
                createScaledPartPhysics((Node)rope, false, 2f);
                PhysicsSpace physics = BuildingSimulator.getBuildingSimulator()
                        .getBulletAppState().getPhysicsSpace();
                physics.remove(lineAndHookHandleJoint);
                lineAndHookHandleJoint = new HingeJoint((RigidBodyControl)lift.getChild("hookHandle")
                        .getControl(0), mobileCrane.getChild("ropeHook").getControl(RigidBodyControl.class),
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
        createScaledPartPhysics((Node)rope, false, 4f);
        lineAndHookHandleJoint = new HingeJoint((RigidBodyControl)lift.getChild("hookHandle").getControl(0),
                mobileCrane.getChild("ropeHook").getControl(RigidBodyControl.class), Vector3f.ZERO,
                new Vector3f(0f, 0.06f,0f), Vector3f.ZERO, Vector3f.ZERO);
        physics.add(lineAndHookHandleJoint);
        //g.lookAt(new Vector3f(0,1,0), new Vector3f(0,1,0));
        //Spatial g2 = mobileCrane.getChild("Mesh1");
            //CollisionShape c2 = CollisionShapeFactory.createDynamicMeshShape(g2);
            //c2.setScale(g2.getWorldScale());
            //CompoundCollisionShape com = new CompoundCollisionShape(); 
            //com.addChildShape(c2, Vector3f.ZERO);
            //RigidBodyControl rgc = new RigidBodyControl(c2, 1f);
        //rgc.setKinematic(true);
       // mobileCrane.getChild("hook").addControl(rgc);
        //BuildingSimulator.getBuildingSimulator().getBulletAppState().getPhysicsSpace()
          //      .add(rgc);
        //physics.add(mobileCrane.getChild("hook").getControl(0));
        //physics.add(new HingeJoint(rope.getControl(RigidBodyControl.class),
        //        mobileCrane.getChild("hook").getControl(RigidBodyControl.class),
        //        Vector3f.ZERO, new Vector3f(0f, 1f, 0f), Vector3f.ZERO, Vector3f.ZERO));
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
    private void createScaledPartPhysics(Node parent, boolean kinematic, float mass){
        Geometry g = (Geometry)parent.getChild(0);
        CollisionShape c = CollisionShapeFactory.createDynamicMeshShape(g);
        c.setScale(g.getWorldScale());
        CompoundCollisionShape com = new CompoundCollisionShape(); 
        com.addChildShape(c, Vector3f.ZERO);
        if(kinematic){
        if(parent.getControl(RigidBodyControl.class) != null){
            BuildingSimulator.getBuildingSimulator()
                    .getBulletAppState().getPhysicsSpace().remove(parent.getControl(0));
            parent.removeControl(RigidBodyControl.class);
        }
        }else{
            if(mobileCrane.getChild("ropeHook").getControl(RigidBodyControl.class) != null){
                BuildingSimulator.getBuildingSimulator()
                    .getBulletAppState().getPhysicsSpace()
                        .remove(mobileCrane.getChild("ropeHook").getControl(0));
                mobileCrane.getChild("ropeHook").removeControl(RigidBodyControl.class);
            }
        }
        RigidBodyControl rgc = null;
        //parent.addControl(rgc);
        //BuildingSimulator.getBuildingSimulator().getBulletAppState().getPhysicsSpace()
         //       .add(rgc);
        if(!kinematic){
            Geometry g2 = (Geometry)mobileCrane.getChild("Mesh1");
            CollisionShape c2 = CollisionShapeFactory.createDynamicMeshShape(g2);
            c2.setScale(g2.getWorldScale());
            System.out.println(parent.getWorldTranslation());
            //com.addChildShape(c2, 
            //        mobileCrane.getChild("hook").getWorldTranslation()
            //        .subtract(parent.getWorldTranslation()),
            //        mobileCrane.getChild("hook").getWorldRotation().toRotationMatrix());
            com.addChildShape(c2,
                    mobileCrane.getChild("hook").getLocalTranslation(),
                    mobileCrane.getChild("hook").getLocalRotation().toRotationMatrix());
            rgc = new RigidBodyControl(com, mass);
        rgc.setKinematic(kinematic);
            mobileCrane.getChild("ropeHook").addControl(rgc);
            BuildingSimulator.getBuildingSimulator().getBulletAppState().getPhysicsSpace()
                .add(rgc);
            /*Geometry g2 = (Geometry)mobileCrane.getChild("Mesh1");
            CollisionShape c2 = CollisionShapeFactory.createDynamicMeshShape(g2);
            c2.setScale(g2.getWorldScale());
            CompoundCollisionShape com2 = new CompoundCollisionShape();
            com2.addChildShape(c2, Vector3f.ZERO);
            RigidBodyControl rgc2 = new RigidBodyControl(com2, 4f);
            rgc.setKinematic(false);
            mobileCrane.getChild("hook").addControl(rgc2);
            BuildingSimulator.getBuildingSimulator().getBulletAppState().getPhysicsSpace()
                .add(rgc2);
            HingeJoint hj = new HingeJoint(rgc,
                rgc2, new Vector3f(0f,-0.4f,0f),
                new Vector3f(0f, 0.1f,0f), Vector3f.ZERO, Vector3f.ZERO);
            BuildingSimulator.getBuildingSimulator().getBulletAppState().getPhysicsSpace()
                .add(hj);*/
            //com.addChildShape(c2, 
                   // mobileCrane.getChild("hook").getWorldTranslation()
                    //.subtract(parent.getWorldTranslation()),
                    //mobileCrane.getChild("hook").getWorldRotation().toRotationMatrix());
        }else{
            rgc = new RigidBodyControl(com, mass);
        rgc.setKinematic(kinematic);
            parent.addControl(rgc);
        BuildingSimulator.getBuildingSimulator().getBulletAppState().getPhysicsSpace()
                .add(rgc);
        }
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
