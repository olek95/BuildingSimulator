package buildingsimulator;

import com.jme3.scene.Spatial;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.collision.shapes.CompoundCollisionShape;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.bullet.control.VehicleControl;
import com.jme3.bullet.joints.HingeJoint;
import com.jme3.bullet.objects.PhysicsRigidBody;
import com.jme3.bullet.util.CollisionShapeFactory;
import com.jme3.input.controls.ActionListener;
import com.jme3.material.Material;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.control.Control;
import com.jme3.texture.Texture;
import com.jme3.water.SimpleWaterProcessor;
import java.util.List;
public class MobileCrane implements ActionListener{
    private BuildingSimulator game = BuildingSimulator.getBuildingSimulator();
    private Spatial craneSpatial = game.getAssetManager().loadModel("Models/dzwig/dzwig.j3o");
    private VehicleControl crane = craneSpatial.getControl(VehicleControl.class);
    private CraneCabin cabin;
    private final float accelerationForce = 100.0f, brakeForce = 20.0f, frictionForce = 10.0f;
    private float steeringValue = 0f;
    private String key = "";
    boolean using = true;
    public MobileCrane(){
        craneSpatial.setLocalTranslation(0, 1.15f, 0); // 100
        Geometry driverCabin = (Geometry)((Node)craneSpatial).getChild("Cube.0001");
        CollisionShape driverCabinCollisionShape = CollisionShapeFactory.createDynamicMeshShape(driverCabin);
        driverCabinCollisionShape.setScale(driverCabin.getWorldScale());
        ((CompoundCollisionShape)crane.getCollisionShape()).addChildShape(driverCabinCollisionShape, 
                new Vector3f(0,0,0));
        scaleTiresTexture();
        createMirrors();
        game.getRootNode().attachChild(craneSpatial);
        PhysicsSpace physics = game.getBulletAppState().getPhysicsSpace();
        physics.add(crane);
        
        Node n1 = (Node)((Node)craneSpatial).getChild("crane");
        Geometry g1  = (Geometry)((Node)craneSpatial).getChild("Circle.0033");
        CollisionShape c1 = CollisionShapeFactory.createDynamicMeshShape(g1);
        c1.setScale(g1.getWorldScale());
        
        Node n2 = (Node)((Node)craneSpatial).getChild("crane");
        Geometry g2  = (Geometry)((Node)craneSpatial).getChild("Circle.0031");
        CollisionShape c2 = CollisionShapeFactory.createDynamicMeshShape(g2);
        c2.setScale(g2.getWorldScale());
        
        Node n3 = (Node)((Node)craneSpatial).getChild("lift");
        Control c3 = n3.getControl(0);
        CompoundCollisionShape com = new CompoundCollisionShape();
        com.addChildShape(c1, Vector3f.ZERO);
        com.addChildShape(c2, Vector3f.ZERO);
        RigidBodyControl rgb = new RigidBodyControl(com, 1f);
        rgb.setKinematic(true);
        rgb.setCollisionGroup(3);
        n1.addControl(rgb);
        physics.add(rgb);
        cabin = new CraneCabin(craneSpatial);
       /* HingeJoint hj = new HingeJoint(rgb, (PhysicsRigidBody)c3, new Vector3f(0f,2,0f), new Vector3f(0f,0f,0f),
                Vector3f.ZERO, Vector3f.ZERO);
        //hj.enableMotor(true, 0.1f, 0.1f);
        physics.add(hj);
        physics.add(c3);*/
        physics.add(c3);
        /*Control c = ((Node)craneSpatial).getChild("crane").getControl(0);
        ((RigidBodyControl)c).setKinematic(true);
        Node n3 = (Node)((Node)craneSpatial).getChild("crane");
        cabin = new CraneCabin(craneSpatial, (RigidBodyControl)c);
        HingeJoint hj = new HingeJoint((PhysicsRigidBody)crane, (PhysicsRigidBody)c, n3.getLocalTranslation(), new Vector3f(0f,0f,0f),
                Vector3f.ZERO, Vector3f.ZERO);
        //hj.enableMotor(true, 0.1f, 0.1f);
        physics.add(hj);
        physics.add(((Node)craneSpatial).getChild("crane").getControl(0));*/
    }
    public void onAction(String name, boolean isPressed, float tpf) {
        switch(name){
            case "Up":
                if(isPressed){
                    key = name;
                    crane.accelerate(0f);
                    crane.brake(brakeForce);
                }else{
                    key = null;
                    if(crane.getCurrentVehicleSpeedKmHour() > 0) crane.accelerate(-frictionForce);
                    else crane.accelerate(frictionForce);
                }
                break;
            case "Down":
                if(isPressed){
                    key = name;
                    crane.accelerate(0f);
                    crane.brake(brakeForce);
                }else{
                    key = null;
                    crane.brake(0f);
                    if(crane.getCurrentVehicleSpeedKmHour() < 0) crane.accelerate(frictionForce);
                    else crane.accelerate(-frictionForce);
                }
                break;
            case "Left":
                if(isPressed){
                    steeringValue += 0.5f;
                }else{
                    steeringValue -= 0.5f;
                }
                crane.steer(steeringValue);
                break;
            case "Right":
                if(isPressed){
                    steeringValue -= 0.5f;
                }else{
                    steeringValue += 0.5f;
                }
                crane.steer(steeringValue);
        }
    }
    public void updateState(){
        if(key == null){
            if(crane.getCurrentVehicleSpeedKmHour() < 1 && crane.getCurrentVehicleSpeedKmHour() > -1)
                stop();
        }else if(!key.equals(""))
            if(key.equals("Down") && crane.getCurrentVehicleSpeedKmHour() < 0){
                crane.brake(0f);
                crane.accelerate(-accelerationForce * 0.5f); // prędkość w tył jest mniejsza 
            }else{
                if(key.equals("Up") && Math.ceil(crane.getCurrentVehicleSpeedKmHour()) >= 0){
                    crane.brake(0f);
                    crane.accelerate(accelerationForce);
                }
            }
        
    }
    public CraneCabin getCabin(){
        return cabin;
    }
    private void scaleTiresTexture(){
        List<Spatial> craneElements = ((Node)craneSpatial).getChildren();
        Texture tireTexture = null;
        for(Spatial element : craneElements)
            if(element.getName().startsWith("wheel")){
                Geometry tire = ((Geometry)((Node)((Node)element).getChild(0)).getChild(0));
                Material tireMaterial = tire.getMaterial();
                if(tireTexture == null){
                    tireTexture = (Texture)tireMaterial.getTextureParam("DiffuseMap").getValue();
                    tireTexture.setWrap(Texture.WrapMode.Repeat);
                    tire.getMesh().scaleTextureCoordinates(new Vector2f(1,6f));
                }else tireMaterial.setTexture("DiffuseMap", tireTexture);
            }
    }
    private void createMirrors(){
        Geometry g;
        float x = 0.2f;
        Node craneNode = (Node)craneSpatial;
        for(int i = 0; i < 2; i++){
            if(i == 0) g = (Geometry)craneNode.getChild("Circle.0024");
            else g = (Geometry)craneNode.getChild("Circle.0003");
            SimpleWaterProcessor waterProcessor = new SimpleWaterProcessor(game.getAssetManager());
            waterProcessor.setReflectionScene(game.getRootNode());
            waterProcessor.setDistortionScale(0.0f);
            waterProcessor.setWaveSpeed(0.0f);
            waterProcessor.setWaterDepth(0f);
            if(i == 1) x = -x;
            waterProcessor.setPlane(new Vector3f(0f, 0f, 0f),new Vector3f(x, 0f, 1f));
            game.getViewPort().addProcessor(waterProcessor);
            Material mat = waterProcessor.getMaterial();
            g.setMaterial(mat);
        }
    }
    private void stop(){
        key = "";
        crane.accelerate(0f);
        crane.brake(0f);
        crane.setLinearVelocity(Vector3f.ZERO); // jeśli jeszcze jest jakaś mała prędkość, to zeruje
    }
}
