package cranes.mobileCrane;

import buildingsimulator.BuildingSimulator;
import buildingsimulator.ElementName;
import settings.Control;
import cranes.ArmControl;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.bullet.control.VehicleControl;
import com.jme3.bullet.joints.HingeJoint;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import buildingsimulator.PhysicsManager;
import cranes.CameraType;
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
    private static final float LIFTING_SPEED = 0.2f, STRETCHING_OUT_SPEED = 1f,
            CRANE_PROP_GOING_OUT_SPEED = 2.8f,
            MIN_CRANE_PROP_PROTRUSION = 1f;
    private Geometry leftProtractilePropGeometry, rightProtractilePropGeometry;
    private boolean using = false;
    public MobileCraneArmControl(Node crane, MobileCraneCamera camera){
        super(crane, 9.5f, 1f, 0.6f, 0f, camera);
        hookHandleDisplacement = PhysicsManager.calculateDisplacementAfterScaling(rectractableCranePart, 
                new Vector3f(1f, 1f, stretchingOut + STRETCHING_OUT_SPEED), false,
                true, true);
    }
    
    /**
     * Zwraca informację o tym czy gracz jest w trybie kontroli ramienia dźwigu. 
     * @return true jeśli tryb kontroli ramienia dźwigu, false w przeciwnym razie 
     */
    public boolean isUsing() { return using; }
    
    /**
     * Określa czy włączony jest tryb kontroli ramienia dźwigu. 
     * @param using true jeśli tryb kontroli ramienia dźwigu, false w przeciwnym razie 
     */
    public void setUsing(boolean using) { this.using = using; }
    
    /**
     * Zwraca wektor o jaki przesuwa się część uchwytu na hak podczas wysuwania/wsuwania
     * ramienia. 
     * @return wektor przesuwania się części uchwytu na hak
     */
    public Vector3f getHookHandleDisplacement() { return hookHandleDisplacement; }
    
    /**
     * Ustawia wektor o jaki przesuwa się część uchwytu na hak podczas wysuwania/wsuwania
     * ramienia
     * @param displacement wektor przesuwania się części uchwytu na hak 
     */
    public void setHookHandleDisplacement(Vector3f displacement) { 
        hookHandleDisplacement = displacement;
    }
    
    /**
     * Zwraca aktualne wysunięcie ramienia. 
     * @return aktualne wysunięcie remienia 
     */
    public float getStrechingOut() { return stretchingOut; }
    
    /**
     * Określa aktualne wysunięcie ramienia. 
     * @param stretchingOut aktualne wysunięcie ramienia. 
     */
    public void setStretchingOut(float stretchingOut) { 
        this.stretchingOut = stretchingOut; 
    }
    
    /**
     * Zwraca wartość wysunięcia podpór ramienia dźwigu. 
     * @return wysunięcie podpór ramienia dźwigu 
     */
    public float getCranePropsProtrusion() { return cranePropsProtrusion; }
    
    /**
     * Ustawia wartość wysunięcia podpór ramienia dźwigu. 
     * @param cranePropsProtrusion wysunięcie podpór ramienia dźwigu 
     */
    public void setCranePropsProtrusion(float cranePropsProtrusion) { 
        this.cranePropsProtrusion = cranePropsProtrusion;
    }
    
    /**
     * Zwraca wysokość na jakiej znajduje się ramie dźwigu. 
     * @return wysokość na jakiej znajduje się ramie dźwigu 
     */
    public float getYCraneOffset() { return yCraneOffset; }
    
    /**
     * Ustawia wysokość na jakiej znajduje się ramię dźwigu. 
     * @param y wysokość na jakiej znajduje się ramię dźwigu 
     */
    public void setYCraneOffset(float y) { yCraneOffset = y; }
    
    /**
     * Obraca ramieniem dźwigu wraz z kabiną, o podany kąt, jeżeli nie ma 
     * żadnej przeszkody. 
     * @param yAngle kąt obrotu 
     */
    @Override
    protected void rotate(float yAngle){
        boolean obstacle = yAngle < 0 ? isObstacleRight() : isObstacleLeft();
        if(!obstacle){
            getCraneControl().rotate(0f, yAngle, 0f);
            rotateHook(); 
        }else{
            if(yAngle < 0) setObstacleRight(false); 
            else setObstacleLeft(false);
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
        float tpf = speed * 2;
        if(movingForward){
            if(stretchingOut <= limit)
                changeHandleHookPosition(rectractableCranePart, new Vector3f(1f, 1f, 
                        stretchingOut += STRETCHING_OUT_SPEED * -tpf), movingForward, -tpf);
        }else if(stretchingOut > limit)
            changeHandleHookPosition(rectractableCranePart, new Vector3f(1f, 1f, 
                    stretchingOut -= STRETCHING_OUT_SPEED * tpf), movingForward, tpf);
    }
    
    /**
     * Podnosi lub opuszcza ramię dźwigu. 
     * @param limit wysokość na jaką można podnieść lub opuścić ramię 
     * @param lowering true jeśli opuszczamy, false jeśli podnosimy ramię 
     */
    @Override 
    protected void changeArmHeight(float limit, boolean lowering, float tpf){
        if(limit == getMaxArmHeight() && yCraneOffset + LIFTING_SPEED * tpf < limit
                || limit == getMinArmHeight() && yCraneOffset - LIFTING_SPEED * tpf >= limit){
            controlCrane(lowering, tpf);
            rotateHook();
        }
    }
    
    /**
     * Wychodzi z trybu kontroli ramienia dźwigu. 
     */
    @Override
    protected void getOff(){
        if(getHook().getAttachedObject() == null) {
            Control.removeListener(this);
            using = !using;
        } else {
            HUD.setMessage(Translator.REQUIREMENT_DETACHING_WALL.getValue());
        }
    }
    
    @Override
    protected void changeCamera() {
        MobileCraneCamera cam = (MobileCraneCamera)getCamera();
        cam.changeCamera(true);
        HUD.changeHUDColor(!cam.getType().equals(CameraType.ARM_CABIN));
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
                hookHandle, 1f));
        if(rectractableCranePart.getControl(RigidBodyControl.class) == null)
            createCranePhysics();
        else {
            PhysicsManager.addPhysicsToGame(getHookHandle(), craneControlNode,
                    rectractableCranePart, lift.getChild(ElementName.LONG_CRANE_ELEMENT));
        }
    }
    
    private void createCranePhysics(){
        PhysicsSpace physics = BuildingSimulator.getPhysicsSpace();
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
    }
    
    private void changeHandleHookPosition(Node scallingGeometryParent, 
            Vector3f scallingVector, boolean pullingOut, float tpf){
        Geometry rectractableCranePartGeometry = (Geometry)scallingGeometryParent.getChild(0);
        PhysicsManager.moveWithScallingObject(pullingOut, hookHandleDisplacement.clone().mult(tpf), scallingVector,
                new Node[] { scallingGeometryParent }, getHook().getHookHandle());
        PhysicsManager.createObjectPhysics(rectractableCranePart, 1f, true, rectractableCranePartGeometry
                .getName());
        rectractableCranePart.getControl(RigidBodyControl.class).setCollisionGroup(3);
    }
    
    private void controlCrane(boolean lowering, float tpf){
        float liftingSpeedDependOnTpf = LIFTING_SPEED * tpf;
        if(lowering){
            yCraneOffset -= liftingSpeedDependOnTpf;
            cranePropsProtrusion -= CRANE_PROP_GOING_OUT_SPEED * tpf;
            if(cranePropsProtrusion >= MIN_CRANE_PROP_PROTRUSION)
                craneProps.rotate(liftingSpeedDependOnTpf, 0, 0);
            lift.rotate(liftingSpeedDependOnTpf, 0, 0);
        }else{
            yCraneOffset += liftingSpeedDependOnTpf;
            cranePropsProtrusion += CRANE_PROP_GOING_OUT_SPEED * tpf;
            craneProps.rotate(-liftingSpeedDependOnTpf, 0, 0);
            lift.rotate(-liftingSpeedDependOnTpf, 0, 0);
        }
        leftProtractilePropGeometry.setLocalScale(1f, cranePropsProtrusion, 1f);
        rightProtractilePropGeometry.setLocalScale(1f, cranePropsProtrusion, 1f);
    }
}
