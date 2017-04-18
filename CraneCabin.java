package buildingsimulator;

import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.bullet.control.VehicleControl;
import com.jme3.bullet.joints.HingeJoint;
import com.jme3.input.controls.AnalogListener;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import static buildingsimulator.GameManager.*;
import com.jme3.bullet.collision.PhysicsCollisionEvent;
import com.jme3.bullet.collision.PhysicsCollisionListener;
import java.util.Arrays;
import java.util.List;

/**
 * Obiekt klasy <code>CraneCabin</code> reprezentuje kabinę operatora ramienia 
 * dźwigu. Posiada on umiejętność obracania ramienia w pionie i poziomie, 
 * wysuwania go i wsuwania, a także opuszczania i podnoszenia liny z hakiem. 
 * @author AleksanderSklorz
 */
public class CraneCabin implements AnalogListener{
    private Node craneCabin, lift, rectractableCranePart, mobileCrane,
            craneProps;
    private float yCraneOffset = 0f, stretchingOut = 1f, propsLowering = 1f,
            cranePropsProtrusion = 1f;
    private Vector3f hookHandleDisplacement, propDisplacement;
    private static final float MAX_PROTRUSION = 9.5f, MIN_PROTRUSION = 1f,
            LIFTING_SPEED = 0.005f, STRETCHING_OUT_SPEED = 0.05f, PROP_LOWERING_SPEED = 0.05f,
            CRANE_PROP_GOING_OUT_SPEED = 0.07f,
            MIN_CRANE_PROP_PROTRUSION = 1f;
    public static final float MAX_PROP_PROTRUSION = 6.35f, MIN_PROP_PROTRUSION = 1f;
    private Geometry leftProtractilePropGeometry, rightProtractilePropGeometry;
    private Hook hook;
    boolean obstacleLeft = false, obstacleRight = false;
    public CraneCabin(Node crane){
        initCraneElements(crane);
        createCranePhysics();
        hookHandleDisplacement = calculateDisplacementAfterScaling(rectractableCranePart, 
                new Vector3f(1f, 1f, stretchingOut + STRETCHING_OUT_SPEED), false,
                true, true);
        propDisplacement =  calculateDisplacementAfterScaling((Node)mobileCrane
                .getChild("protractileProp1"), new Vector3f(1f, propsLowering + PROP_LOWERING_SPEED,
                1f), false, true, false);
    }
    @Override
    public void onAnalog(String name, float value, float tpf) {
        switch(name){
            case "Right":
                setLastAction(name);
                if(!obstacleRight)
                    craneCabin.rotate(0f, -tpf / 5, 0f);
                break;
            case "Left": 
                setLastAction(name);
                if(!obstacleLeft)
                    craneCabin.rotate(0f, tpf / 5, 0f);
                break;
            case "Up":
                if(yCraneOffset + LIFTING_SPEED < 0.6f){
                    controlCrane(false);
                }
                break;
            case "Down":
                if(yCraneOffset - LIFTING_SPEED >= 0f){
                    controlCrane(true);
                }
                break;
            case "Pull out":
                if(stretchingOut <= MAX_PROTRUSION)
                    changeHandleHookPoition(rectractableCranePart, new Vector3f(1f, 1f, 
                            stretchingOut += STRETCHING_OUT_SPEED), true);
                break;
            case "Pull in":
                if(stretchingOut > MIN_PROTRUSION)
                    changeHandleHookPoition(rectractableCranePart, new Vector3f(1f, 1f, 
                            stretchingOut -= STRETCHING_OUT_SPEED), false);
                break;
            case "Lower hook":
                hook.lower();
                break;
            case "Highten hook":
                if(hook.getHookLowering() > 1f) 
                    hook.highten();
        }
         hook.setRecentlyHitObject(null);
    }
    /**
     * Pozwala na kontrolowanie podporami (na opuszczanie i podnioszenie ich). 
     * @param lowering true jeśli podpory mają być opuszczane, false jeśli podnioszone
     */
    public void controlProps(boolean lowering){
        List<Spatial> mobileCraneChildren = mobileCrane.getChildren();
        int i = 0, changed = 0;
        String[] props = {"propParts1", "propParts2", "propParts3",
            "propParts4"};
        Vector3f scallingVector = new Vector3f(1f, propsLowering += lowering ? 
                PROP_LOWERING_SPEED : -PROP_LOWERING_SPEED, 1f);
        do{
            Node prop = (Node)mobileCraneChildren.get(i);
            if(Arrays.binarySearch(props, prop.getName()) >= 0){
                    changed++;
                    movingDuringStretchingOut((Geometry)((Node)prop.getChild(0))
                            .getChild(0), scallingVector, !lowering, (Node)prop.getChild(1),
                            propDisplacement);
            }
            i++;
        }while(changed < 4);
    }
    /**
     * Zwraca wartość określającą jak bardzo opuszczone są podpory. 
     * @return wartość określającą opuszczenie podpór 
     */
    public float getPropsLowering(){
        return propsLowering;
    }
    private void initCraneElements(Node crane){
        mobileCrane = crane;
        craneCabin = (Node)mobileCrane.getChild("crane");
        lift = (Node)craneCabin.getChild("lift");
        rectractableCranePart = (Node)lift.getChild("retractableCranePart");
        craneProps = (Node)craneCabin.getChild("craneProps");
        leftProtractilePropGeometry = (Geometry)((Node)craneProps
                .getChild("leftProtractileProp")).getChild(0);
        rightProtractilePropGeometry = (Geometry)((Node)craneProps
                .getChild("rightProtractileProp")).getChild(0);
        // do aktualnej kolizji dołącza kolizję z grupą 1
        hook = new Hook((Node)mobileCrane.getChild("ropeHook"), lift.getChild("hookHandle"));
    }
    private void createCranePhysics(){
        PhysicsSpace physics = BuildingSimulator.getBuildingSimulator()
                .getBulletAppState().getPhysicsSpace();
        createObjectPhysics(craneCabin, 1f, true, "outsideCabin", "turntable");
        createObjectPhysics(rectractableCranePart, 1f, true, rectractableCranePart
                .getChild(0).getName());
        rectractableCranePart.getControl(RigidBodyControl.class).setCollisionGroup(3);
        HingeJoint cabinAndMobilecraneJoin = new HingeJoint(mobileCrane
                .getControl(VehicleControl.class),craneCabin
                .getControl(RigidBodyControl.class), craneCabin.getLocalTranslation(),
                Vector3f.ZERO,Vector3f.ZERO, Vector3f.ZERO);
        physics.add(cabinAndMobilecraneJoin);
        physics.add(lift.getChild("longCraneElement").getControl(0));
        /* Dodaje listener sprawdzający kolizję haka z obiektami otoczenia.
         Dla optymalizacji sprawdzam kolizję tylko dla grupy 2, czyli tej w 
         której znajduje sie hak.*/
        System.out.println(rectractableCranePart.getWorldTranslation() + " T");
        physics.addCollisionGroupListener(hook.createCollisionListener(), 2);
        physics.addCollisionListener(new PhysicsCollisionListener(){
            @Override
            public void collision(PhysicsCollisionEvent event) {
                Spatial a = event.getNodeA(), b = event.getNodeB();
                if(a instanceof Node && b instanceof Node){
                    Node nodeA = (Node)a;
                    if(nodeA.equals(rectractableCranePart) || nodeA.equals(hook.getHookHandle())){
                        Vector3f craneArmLocation = rectractableCranePart
                                .getWorldTranslation();
                        Vector3f objectLocation = ((Node)b).getWorldTranslation();
                        if(craneArmLocation.z != objectLocation.z 
                                && craneArmLocation.y < objectLocation.y){
                            if(getLastAction().equals("Left")){
                                obstacleLeft = true;
                                craneCabin.rotate(0f, -0.04f, 0f);
                            }else{
                                obstacleRight = true; 
                                craneCabin.rotate(0f, 0.04f, 0f);
                            }
                        }
                    }
                }
            }
        });
    }
    private void changeHandleHookPoition(Node scallingGeometryParent, 
            Vector3f scallingVector, boolean pullingOut){
        Geometry rectractableCranePartGeometry = (Geometry)scallingGeometryParent.getChild(0);
        movingDuringStretchingOut(rectractableCranePartGeometry, scallingVector, 
                pullingOut, hook.getHookHandle(), hookHandleDisplacement);
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
}

