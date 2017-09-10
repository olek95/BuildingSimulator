package cranes.crane;

import building.Wall;
import buildingsimulator.BuildingSimulator;
import cranes.ArmControl;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import cranes.Hook;

/**
 * Obiekt klasy <code>CraneArmControl</code> reprezentuje obiekt kontrolujący 
 * ramię żurawia. 
 * @author AleksanderSklorz
 */
public class CraneArmControl extends ArmControl{
    private Node hookHandleControl;
    public CraneArmControl(Node crane){
        super(crane);
        setMaxHandleHookDisplacement(-63f);
        setMinHandleHookDisplacement(hookHandleControl.getLocalTranslation().z);
    }
    
    /**
     * Obraca ramię żurawia i przyczepione do niego elementy, a także kabinę 
     * żurawia o podany kąt. 
     * @param yAngle kąt obrotu 
     */
    @Override
    protected void rotate(float yAngle) {
        getCraneControl().rotate(0f, yAngle, 0f);
        rotateHook(); 
    }
    
    /**
     * Przesuwa główny uchwyt na hak. 
     * @param limit odległość na jaką można wysunąć uchwyt do przodu lub do tyłu 
     * @param movingForward true jeśli przesuwamy uchwyt do przudu, false w przeciwnym razie 
     * @param speed prędkość przesuwania 
     */
    @Override
    protected void moveHandleHook(float limit, boolean movingForward, float speed) {
        Vector3f hookHandleTranslation = hookHandleControl.getLocalTranslation();
        if(movingForward && hookHandleTranslation.z >= limit 
                || !movingForward && hookHandleTranslation.z < limit){
            hookHandleControl.setLocalTranslation(hookHandleTranslation
                    .addLocal(0 , 0, speed * 3));
            rotateHook();
        }
    }
    
    /**
     * Inicjuje elementy składowe ramienia żurawia. Są to platforma wejściowa 
     * do kabiny żurawia, platforma obracająca ramieniem, ramię, kabina, uchwyt 
     * na hak i liny wraz z hakiem. 
     */
    @Override
    protected void initCraneArmElements(){
        super.initCraneArmElements();
        PhysicsSpace physics = BuildingSimulator.getBuildingSimulator()
                .getBulletAppState().getPhysicsSpace();
        Node craneControlNode = getCraneControl(), craneNode = getCrane();
        Vector3f craneLocation = craneNode.getLocalTranslation();
        physics.add(setProperControlLocation(craneControlNode.getChild("turntable"),
                craneLocation));
        physics.add(setProperControlLocation(craneControlNode.getChild("mainElement"),
                craneLocation));
        physics.add(setProperControlLocation(craneControlNode.getChild("craneArm"),
                craneLocation));
        physics.add(setProperControlLocation(craneControlNode.getChild("cabin"), craneLocation));
        hookHandleControl = (Node)craneControlNode.getChild("hookHandleControl");
        Spatial hookHandle = hookHandleControl.getChild("hookHandle");
        setHookHandle(hookHandle);
        physics.add(setProperControlLocation(hookHandle, craneLocation));
        setHook(new FourRopesHook((Node)craneNode.getChild("ropeHook"), hookHandle,
                0.1f));
    }
    
    private RigidBodyControl setProperControlLocation(Spatial object, Vector3f displacement){
        RigidBodyControl control = object.getControl(RigidBodyControl.class);
        control.setPhysicsLocation(object.getLocalTranslation().add(displacement));
        return control;
    }
    
    
}
