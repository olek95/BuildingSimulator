package cranes.mobileCrane;

import buildingsimulator.BuildingSimulator;
import buildingsimulator.ElementName;
import settings.Control;
import buildingsimulator.GameManager;
import cranes.ArmControl;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.bullet.control.VehicleControl;
import com.jme3.bullet.joints.HingeJoint;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import static buildingsimulator.GameManager.*;
import buildingsimulator.PhysicsManager;
import com.jme3.bullet.collision.PhysicsCollisionEvent;
import com.jme3.bullet.collision.PhysicsCollisionListener;
import menu.HUD;
import texts.Translator;

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
        super(crane, 9.5f, 1f, 0.6f, 0f);
        hookHandleDisplacement = PhysicsManager.calculateDisplacementAfterScaling(rectractableCranePart, 
                new Vector3f(1f, 1f, stretchingOut + STRETCHING_OUT_SPEED), false,
                true, true);
    }
    
    /**
     * Obraca ramieniem dźwigu wraz z kabiną, o podany kąt, jeżeli nie ma 
     * żadnej przeszkody. 
     * @param yAngle kąt obrotu 
     */
    @Override
    protected void rotate(float yAngle){
        boolean obstacle = yAngle < 0 ? obstacleRight : obstacleLeft;
        if(!obstacle){
            getCraneControl().rotate(0f, yAngle, 0f);
            rotateHook(); 
        }else{
            if(yAngle < 0) obstacleRight = false; 
            else obstacleLeft = false;
        }
    }
    
    /**
     * Przesuwa uchwyt haka w przód lub w tył. 
     * @param limit określa jak daleko w przód lub w tył można przesunąć uchwyt haka 
     * @param movingForward true jeśli przesuwamy w przód, false w przeciwnym razie 
     * @param speed prędkość (w tym przypadku niewykorzystana, gdyż użyto zależności 
     * o ile wysuwa się ramię dźwigu podczas wysuwania)
     */
    @Override
    protected void moveHandleHook(float limit, boolean movingForward, float speed){
        if(movingForward){
            if(stretchingOut <= limit)
                changeHandleHookPosition(rectractableCranePart, new Vector3f(1f, 1f, 
                        stretchingOut += STRETCHING_OUT_SPEED), movingForward);
        }else if(stretchingOut > limit)
            changeHandleHookPosition(rectractableCranePart, new Vector3f(1f, 1f, 
                    stretchingOut -= STRETCHING_OUT_SPEED), movingForward);
    }
    
    /**
     * Podnosi lub opuszcza ramię dźwigu. 
     * @param limit wysokość na jaką można podnieść lub opuścić ramię 
     * @param lowering true jeśli opuszczamy, false jeśli podnosimy ramię 
     */
    @Override 
    protected void changeArmHeight(float limit, boolean lowering){
        if(limit == getMaxArmHeight() && yCraneOffset + LIFTING_SPEED < limit
                || limit == getMinArmHeight() && yCraneOffset - LIFTING_SPEED >= limit){
            controlCrane(lowering);
            rotateHook();
        }
    }
    
    /**
     * Wychodzi z trybu kontroli ramienia dźwigu. 
     * @param actionName nazwa akcji która powoduje wyjście 
     */
    @Override
    protected void getOff(String actionName){
        if(getHook().getAttachedObject() == null) {
            Control.removeListener(GameManager.getUnit(0).getArmControl());
            using = !using;
            GameManager.setLastAction(actionName);
        } else {
            HUD.setMessage(Translator.REQUIREMENT_DETACHING_WALL.getValue());
        }
    }
    
    /**
     * Inicjuje wszystkie elementy związane z ramieniem dźwigu, czyli 
     * ramię dźwigu, podpory ramienia dźwigu, uchwyt haka oraz linę z hakiem. 
     */
    @Override
    protected void initCraneArmElements(){
        super.initCraneArmElements();
        Node craneControlNode = getCraneControl();
        lift = (Node)craneControlNode.getChild(ElementName.LIFT);
        rectractableCranePart = (Node)lift.getChild(ElementName.RETRACTABLE_CRANE_PART);
        craneProps = (Node)craneControlNode.getChild(ElementName.MOBILE_CRANE_ARM_PROPS);
        leftProtractilePropGeometry = (Geometry)((Node)craneProps
                .getChild(ElementName.LEFT_PROTRACTILE_PROP)).getChild(0);
        rightProtractilePropGeometry = (Geometry)((Node)craneProps
                .getChild(ElementName.RIGHT_PROTRACTILE_PROP)).getChild(0);
        // do aktualnej kolizji dołącza kolizję z grupą 1
        Spatial hookHandle = lift.getChild(ElementName.HOOK_HANDLE);
        setHookHandle(hookHandle);
        setHook(new OneRopeHook((Node)getCrane().getChild(ElementName.ROPE_HOOK),
                hookHandle, 0.05f));
        createCranePhysics();
    }
    
    /**
     * Zwraca informację o tym czy gracz jest w trybie kontroli ramienia dźwigu. 
     * @return true jeśli tryb kontroli ramienia dźwigu, false w przeciwnym razie 
     */
    public boolean isUsing(){
        return using;
    }
    
    /**
     * Określa czy włączony jest tryb kontroli ramienia dźwigu. 
     * @param using true jeśli tryb kontroli ramienia dźwigu, false w przeciwnym razie 
     */
    public void setUsing(boolean using){
        this.using = using;
    }
    
    private void createCranePhysics(){
        PhysicsSpace physics = BuildingSimulator.getBuildingSimulator()
                .getBulletAppState().getPhysicsSpace();
        physics.add(getHookHandle().getControl(0));
        Node craneControlNode = getCraneControl();
        PhysicsManager.createObjectPhysics(craneControlNode, 1f, true, 
                ElementName.MOBILE_CRANE_ARM_OUTSIDE_CABIN, ElementName.TURNTABLE);
        PhysicsManager.createObjectPhysics(rectractableCranePart, 1f, true, rectractableCranePart
                .getChild(0).getName());
        rectractableCranePart.getControl(RigidBodyControl.class).setCollisionGroup(3);
        HingeJoint cabinAndMobilecraneJoin = new HingeJoint(getCrane()
                .getControl(VehicleControl.class),craneControlNode
                .getControl(RigidBodyControl.class), craneControlNode.getLocalTranslation(),
                Vector3f.ZERO,Vector3f.ZERO, Vector3f.ZERO);
        physics.add(cabinAndMobilecraneJoin);
        physics.add(lift.getChild(ElementName.LONG_CRANE_ELEMENT).getControl(0));
        /* Dodaje listener sprawdzający kolizję haka z obiektami otoczenia.
         Dla optymalizacji sprawdzam kolizję tylko dla grupy 2, czyli tej w 
         której znajduje sie hak.*/
        physics.addCollisionListener(new PhysicsCollisionListener(){
            @Override
            public void collision(PhysicsCollisionEvent event) {
                Spatial a = event.getNodeA(), b = event.getNodeB();
                if(a != null && b != null){
                    if(a.equals(rectractableCranePart) || a.equals(getHook().getHookHandle())){
                        if(b.getName().startsWith(ElementName.RACK))
                            rotateAfterImpact(a);
                    }else{
                        if(b.equals(rectractableCranePart) || b.equals(getHook().getHookHandle())){
                            if(a.getName().startsWith(ElementName.RACK))
                                rotateAfterImpact(b);
                        }
                    }
                }
            }
        });
    }
    
    private void changeHandleHookPosition(Node scallingGeometryParent, 
            Vector3f scallingVector, boolean pullingOut){
        Geometry rectractableCranePartGeometry = (Geometry)scallingGeometryParent.getChild(0);
        PhysicsManager.moveWithScallingObject(pullingOut, hookHandleDisplacement, scallingVector,
                new Node[] { scallingGeometryParent }, getHook().getHookHandle());
        PhysicsManager.createObjectPhysics(rectractableCranePart, 1f, true, rectractableCranePartGeometry
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
        Node craneControlNode = getCraneControl();
        float yRotation = craneControlNode.getLocalRotation().getY();
        if(yRotation > 0){
            obstacleLeft = true;
            craneControlNode.rotate(0f, -rotate, 0f);
        }else{
            obstacleRight = true; 
            craneControlNode.rotate(0f, rotate, 0f);
        }
    }
}
