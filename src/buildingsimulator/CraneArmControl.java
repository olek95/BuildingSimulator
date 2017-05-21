package buildingsimulator;

import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;

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
        Node craneControlNode = getCraneControl();
        craneControlNode.rotate(0f, yAngle, 0f);
        getHook().getRopeHook().getControl(RigidBodyControl.class).setPhysicsRotation(
                craneControlNode.getLocalRotation());
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
                || !movingForward && hookHandleTranslation.z < limit)
            hookHandleControl.setLocalTranslation(hookHandleTranslation
                    .addLocal(0 , 0, speed));
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
        physics.add(setProperLocation(craneNode.getChild("entrancePlatform"), craneLocation));
        physics.add(setProperLocation(craneControlNode.getChild("turntable"), craneLocation));
        physics.add(setProperLocation(craneControlNode.getChild("mainElement"), craneLocation));
        physics.add(setProperLocation(craneControlNode.getChild("craneArm"), craneLocation));
        physics.add(setProperLocation(craneControlNode.getChild("cabin"), craneLocation));
        hookHandleControl = (Node)craneControlNode.getChild("hookHandleControl");
        Spatial hookHandle = hookHandleControl.getChild("hookHandle");
        setHookHandle(hookHandle);
        physics.add(setProperLocation(hookHandle, craneLocation));
        setHook(new FourRopesHook((Node)craneNode.getChild("ropeHook"), hookHandle));
    }
    
    private RigidBodyControl setProperLocation(Spatial object, Vector3f displacement){
        RigidBodyControl control = object.getControl(RigidBodyControl.class);
        control.setPhysicsLocation(object.getLocalTranslation().add(displacement));
        return control;
    }
    
    
}
