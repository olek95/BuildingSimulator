package buildingsimulator;

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
    private Spatial craneCabin, lift, rectractableCranePart;
    private Node mobileCrane;
    private float yCraneOffset = 0f, liftingSpeed = 0.04f;
    public CraneCabin(Spatial craneSpatial){
        mobileCrane = (Node)craneSpatial;
        craneCabin = mobileCrane.getChild("crane");
        lift = ((Node)craneCabin).getChild("lift");
        rectractableCranePart = ((Node)lift).getChild("retractableCranePart");
        //System.out.println(rectractableCranePart.getLocalScale());
        //rectractableCranePart.getLocalScale().z = 10f;
        createCranePhysics();
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
                ((Node)rectractableCranePart).getChild(0).getLocalScale().z += 0.1f;
                rectractableCranePart.updateModelBound();
        }
    }
    private void createCranePhysics(){
        String[] cabinElements = {"outsideCabin", "turntable"};
        CompoundCollisionShape cabinCollision = new CompoundCollisionShape();
        PhysicsSpace physics = BuildingSimulator.getBuildingSimulator()
                .getBulletAppState().getPhysicsSpace();
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
        physics.add(((Node)mobileCrane.getChild("lift")).getControl(0));
    }
}

