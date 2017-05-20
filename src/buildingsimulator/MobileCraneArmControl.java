package buildingsimulator;

import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.bullet.control.VehicleControl;
import com.jme3.bullet.joints.HingeJoint;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import static buildingsimulator.GameManager.*;
import com.jme3.bullet.collision.PhysicsCollisionEvent;
import com.jme3.bullet.collision.PhysicsCollisionListener;

/**
 * Obiekt klasy <code>MobileCraneArmControl</code> reprezentuje kabinę operatora ramienia 
 * dźwigu. Posiada on umiejętność obracania ramienia w pionie i poziomie, 
 * wysuwania go i wsuwania, a także opuszczania i podnoszenia liny z hakiem. 
 * @author AleksanderSklorz
 */
public class MobileCraneArmControl extends ArmControl{
    private Node lift, rectractableCranePart, craneProps;
    private float yCraneOffset = 0f, stretchingOut = 1f, cranePropsProtrusion = 1f;
    private Vector3f hookHandleDisplacement;
    private static final float LIFTING_SPEED = 0.005f, STRETCHING_OUT_SPEED = 0.05f,
            CRANE_PROP_GOING_OUT_SPEED = 0.07f,
            MIN_CRANE_PROP_PROTRUSION = 1f;
    private Geometry leftProtractilePropGeometry, rightProtractilePropGeometry;
    private boolean obstacleLeft = false, obstacleRight = false, using = false;
    public MobileCraneArmControl(Node crane){
        super(crane, 9.5f, 1f);
        hookHandleDisplacement = calculateDisplacementAfterScaling(rectractableCranePart, 
                new Vector3f(1f, 1f, stretchingOut + STRETCHING_OUT_SPEED), false,
                true, true);
    }
    
    @Override
    protected void rotate(float yAngle){
        boolean obstacle = yAngle < 0 ? obstacleRight : obstacleLeft;
        if(!obstacle) craneControl.rotate(0f, yAngle, 0f);
        else{
            if(yAngle < 0) obstacleRight = false; 
            else obstacleLeft = false;
        }
    }
    
    @Override
    protected void moveHandleHook(float limit, boolean movingForward, float speed){
        if(movingForward && stretchingOut <= limit)
            changeHandleHookPosition(rectractableCranePart, new Vector3f(1f, 1f, 
                    stretchingOut += STRETCHING_OUT_SPEED), movingForward);
        else if(!movingForward && stretchingOut > limit)
            changeHandleHookPosition(rectractableCranePart, new Vector3f(1f, 1f, 
                    stretchingOut -= STRETCHING_OUT_SPEED), movingForward);
    }
    
    @Override 
    protected void changeArmHeight(float limit, boolean lowering){
        if(limit == maxArmHeight && yCraneOffset + LIFTING_SPEED < limit
                || limit == minArmHeight && yCraneOffset - LIFTING_SPEED >= limit)
            controlCrane(lowering);
    }
    
    @Override
    protected void getOff(String actionName){
        Control.removeListener(GameManager.getUnit(0).getArmControl());
        using = !using;
        GameManager.setLastAction(actionName);
    }
    
    @Override
    protected void initCraneCabinElements(){
        super.initCraneCabinElements();
        lift = (Node)craneControl.getChild("lift");
        rectractableCranePart = (Node)lift.getChild("retractableCranePart");
        craneProps = (Node)craneControl.getChild("craneProps");
        leftProtractilePropGeometry = (Geometry)((Node)craneProps
                .getChild("leftProtractileProp")).getChild(0);
        rightProtractilePropGeometry = (Geometry)((Node)craneProps
                .getChild("rightProtractileProp")).getChild(0);
        // do aktualnej kolizji dołącza kolizję z grupą 1
        hookHandle = lift.getChild("hookHandle");
        hook = new OneRopeHook((Node)crane.getChild("ropeHook"), hookHandle);
        createCranePhysics();
    }
    
    private void createCranePhysics(){
        PhysicsSpace physics = BuildingSimulator.getBuildingSimulator()
                .getBulletAppState().getPhysicsSpace();
        physics.add(hookHandle.getControl(0));
        createObjectPhysics(craneControl, 1f, true, "outsideCabin", "turntable");
        createObjectPhysics(rectractableCranePart, 1f, true, rectractableCranePart
                .getChild(0).getName());
        rectractableCranePart.getControl(RigidBodyControl.class).setCollisionGroup(3);
        HingeJoint cabinAndMobilecraneJoin = new HingeJoint(crane
                .getControl(VehicleControl.class),craneControl
                .getControl(RigidBodyControl.class), craneControl.getLocalTranslation(),
                Vector3f.ZERO,Vector3f.ZERO, Vector3f.ZERO);
        physics.add(cabinAndMobilecraneJoin);
        physics.add(lift.getChild("longCraneElement").getControl(0));
        /* Dodaje listener sprawdzający kolizję haka z obiektami otoczenia.
         Dla optymalizacji sprawdzam kolizję tylko dla grupy 2, czyli tej w 
         której znajduje sie hak.*/
        //physics.addCollisionGroupListener(hook.createCollisionListener(), 2);
        physics.addCollisionListener(new PhysicsCollisionListener(){
            @Override
            public void collision(PhysicsCollisionEvent event) {
                Spatial a = event.getNodeA(), b = event.getNodeB();
                if(a != null && b != null){
                    if(a.equals(rectractableCranePart) || a.equals(hook.getHookHandle())){
                        if(b.equals(GameManager.getCraneRack())) rotateAfterImpact(a);
                    }else{
                        if(b.equals(rectractableCranePart) || b.equals(hook.getHookHandle())){
                            if(a.equals(GameManager.getCraneRack())) rotateAfterImpact(b);
                        }
                    }
                }
            }
        });
    }
    
    private void changeHandleHookPosition(Node scallingGeometryParent, 
            Vector3f scallingVector, boolean pullingOut){
        Geometry rectractableCranePartGeometry = (Geometry)scallingGeometryParent.getChild(0);
        moveWithScallingObject(pullingOut, hookHandleDisplacement,scallingVector,
                scallingGeometryParent, hook.getHookHandle());
        createObjectPhysics(rectractableCranePart, 1f, true, rectractableCranePartGeometry
                .getName());
        rectractableCranePart.getControl(RigidBodyControl.class).setCollisionGroup(3);
    }
    
    private void controlCrane(boolean lowering){
        if(lowering){
            yCraneOffset -= LIFTING_SPEED;
            cranePropsProtrusion -= CRANE_PROP_GOING_OUT_SPEED;
            if(cranePropsProtrusion >= MIN_CRANE_PROP_PROTRUSION)
                craneProps.rotate(LIFTING_SPEED, 0, 0);
            lift.rotate(LIFTING_SPEED, 0, 0);
        }else{
            yCraneOffset += LIFTING_SPEED;
            cranePropsProtrusion += CRANE_PROP_GOING_OUT_SPEED;
            craneProps.rotate(-LIFTING_SPEED, 0, 0);
            lift.rotate(-LIFTING_SPEED, 0, 0);
        }
        leftProtractilePropGeometry.setLocalScale(1f, cranePropsProtrusion, 1f);
        rightProtractilePropGeometry.setLocalScale(1f, cranePropsProtrusion, 1f);
    }
    
    private void rotateAfterImpact(Spatial object){
        float rotate;
        if(object.equals(rectractableCranePart)) rotate = getFPS() <= 10 ? 0.09f : 0.04f;
        else rotate = getFPS() <= 10 ? 0.09f : 0.02f;
        float yRotation = craneControl.getLocalRotation().getY();
        if(yRotation > 0){
            obstacleLeft = true;
            craneControl.rotate(0f, -rotate, 0f);
        }else{
            obstacleRight = true; 
            craneControl.rotate(0f, rotate, 0f);
        }
    }
    
    public boolean isUsing(){
        return using;
    }
    
    public void setUsing(boolean using){
        this.using = using;
    }
}
