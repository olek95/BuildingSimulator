package listeners;

import buildingsimulator.ElementName;
import buildingsimulator.GameManager;
import static buildingsimulator.GameManager.getFPS;
import com.jme3.bullet.collision.PhysicsCollisionEvent;
import com.jme3.bullet.collision.PhysicsCollisionListener;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import cranes.ArmControl;

public class ArmCollisionListener {
    /**
     * Dodaje listener sprawdzający kolizję haka z obiektami otoczenia.
     */
    public static PhysicsCollisionListener createRotateAfterImpactListener() {
        return new PhysicsCollisionListener(){
            @Override
            public void collision(PhysicsCollisionEvent event) {
                Spatial a = event.getNodeA(), b = event.getNodeB();
                if(a != null && b != null){
                    String aName = a.getName(), bName = b.getName();
                    if(aName.equals(ElementName.RETRACTABLE_CRANE_PART) 
                            || aName.equals(ElementName.HOOK_HANDLE)){
                        if(bName.startsWith(ElementName.RACK) || bName.contains(ElementName.BILLBOARD))
                            rotateAfterImpact(a);
                    }else{
                        if(bName.equals(ElementName.RETRACTABLE_CRANE_PART) 
                                || bName.equals(ElementName.HOOK_HANDLE)){
                            if(aName.startsWith(ElementName.RACK) || aName.contains(ElementName.BILLBOARD))
                                rotateAfterImpact(b);
                        }
                    }
                }
            }
        };
    }
    
    private static void rotateAfterImpact(Spatial object){
        float rotate;
        if(object.getName().equals(ElementName.RETRACTABLE_CRANE_PART)) {
            rotate = getFPS() <= 10 ? 0.09f : 0.04f;
        } else rotate = getFPS() <= 10 ? 0.09f : 0.02f;
        ArmControl control = GameManager.getActualUnit().getArmControl();
        Node craneControlNode = control.getCraneControl();
        float yRotation = craneControlNode.getLocalRotation().getY();
        if(yRotation > 0){
            control.setObstacleLeft(true);
            craneControlNode.rotate(0f, -rotate, 0f);
        }else{
            control.setObstacleRight(true);
            craneControlNode.rotate(0f, rotate, 0f);
        }
    }
}
