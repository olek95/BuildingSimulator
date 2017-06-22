package buildingsimulator;

import buildingmaterials.Wall;
import com.jme3.bullet.collision.PhysicsCollisionGroupListener;
import com.jme3.bullet.collision.PhysicsCollisionObject;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.scene.Spatial;

public class BottomCollisionListener implements PhysicsCollisionGroupListener{
    private RememberingRecentlyHitObject hittingObject; 
    private String hittingObjectName, hitObjectName; 
    public BottomCollisionListener(RememberingRecentlyHitObject hittingObject,
            String hittingObjectName, String hitObjectName){
        this.hittingObject = hittingObject; 
        this.hittingObjectName = hittingObjectName; 
        this.hitObjectName = hitObjectName; 
    }
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
        if(hittingObject instanceof Wall && hittingObject.getRecentlyHitObject() != null){
            Wall wall = (Wall)hittingObject; 
            if(!wall.isAttached())
                    wall.getControl(RigidBodyControl.class).setCollisionGroup(1);
        }
        return true;
   }
    
    private static boolean isProperCollisionGroup(Spatial b){
        // PhysicsCollisionObject bo control nie musi być tylko typu RigidBodyControl
        int collisionGroup = ((PhysicsCollisionObject)b.getControl(0)).getCollisionGroup();
        return collisionGroup != 4;
    }
}
