package buildingsimulator;

import com.jme3.bullet.collision.PhysicsCollisionGroupListener;
import com.jme3.bullet.collision.PhysicsCollisionObject;
import com.jme3.scene.Spatial;

/**
 * Obiekt klasy <code>BottomCollisionListener</code> reprezentuje obekt będący 
 * słuchaczam kolizji od dołu. 
 * @author AleksanderSklorz 
 */
public class BottomCollisionListener implements PhysicsCollisionGroupListener{
    private RememberingRecentlyHitObject hittingObject; 
    private String hittingObjectName, hitObjectName; 
    public BottomCollisionListener(RememberingRecentlyHitObject hittingObject,
            String hittingObjectName, String hitObjectName){
        this.hittingObject = hittingObject; 
        this.hittingObjectName = hittingObjectName; 
        this.hitObjectName = hitObjectName; 
    }
    /**
     * Metoda sprawdzająca kolizję między dwoma obiektami. Kolizja następuje, jeśli 
     * obiekty spełnią dwa warunki. Po pierwsze - obiekt z którym nastąpiła kolizja 
     * musi należeć do odpowiedniej grupy kolizji. Po drugie - obiekt uderzający 
     * ma mieć taką samą nazwę jaką podano dla obiektu uderzającego tego słuchacza, 
     * natomiast obiekt z którym nastąpiła kolizja ma mieć inną nazwę niż podaną 
     * dla obiektu nasłuchującego. 
     * @param nodeA pierwszy obiekt fizyczny 
     * @param nodeB drugi obiekt fizyczny 
     * @return true jeśli kolizja nastąpiła, false w przeciwnym przypadku 
     */
    @Override
    public boolean collide(PhysicsCollisionObject nodeA, PhysicsCollisionObject nodeB){
        Object a = nodeA.getUserObject(), b = nodeB.getUserObject();
        Spatial aSpatial = (Spatial)a, bSpatial = (Spatial)b;
        String aName = aSpatial.getName(), bName = bSpatial.getName();
        if(!isProperCollisionGroup(bSpatial)) return false;
        if(aName.equals(hittingObjectName) && !bName.equals(hitObjectName)){
            hittingObject.setCollision(bSpatial);
        }else if(bName.equals(hittingObjectName) && !aName.equals(hitObjectName)){
            hittingObject.setCollision(aSpatial);
        }
        return true;
    }
    
    /**
     * Ustawia uderzający obiekt. 
     * @param hittingObject uderzający obiekt (obiekt potrafiący zapamiętać
     * ostatnio uderzony element)
     */
    public  void setHittingObject(RememberingRecentlyHitObject hittingObject){
        this.hittingObject = hittingObject; 
    }
    
    private static boolean isProperCollisionGroup(Spatial b){
        // PhysicsCollisionObject bo control nie musi być tylko typu RigidBodyControl
        int collisionGroup = ((PhysicsCollisionObject)b.getControl(0)).getCollisionGroup();
        return collisionGroup != 4;
    }
}
