package buildingsimulator;

import buildingmaterials.Wall;
import com.jme3.bounding.BoundingBox;
import com.jme3.bullet.collision.PhysicsCollisionGroupListener;
import com.jme3.bullet.collision.PhysicsCollisionObject;
import com.jme3.collision.CollisionResults;
import com.jme3.math.Ray;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
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
        //if((aName.startsWith("Wall") || aName.startsWith("New Scene"))
          //      && (bName.startsWith("Wall") || bName.startsWith("New Scene")))
        if(aName.equals(hittingObjectName) && !bName.equals(hitObjectName)){
           // if(!isProperCollisionGroup(bSpatial)) return false;
            hittingObject.setCollision(bSpatial);
        }else if(bName.equals(hittingObjectName) && !aName.equals(hitObjectName)){
            //if(!isProperCollisionGroup(aSpatial)) return false;
            hittingObject.setCollision(aSpatial);
        }
        return true;
    }
    
    /**
     * Ustawia uderzający obiekt. 
     * @param hittingObject uderzający obiekt (obiekt potrafiący zapamiętać
     * ostatnio uderzony element)
     */
    public void setHittingObject(RememberingRecentlyHitObject hittingObject){
        this.hittingObject = hittingObject; 
    }
    
    public void setHittingObjectName(String name){ hittingObjectName = name; }
    
    /**
     * Sprawdza czy ostatnio uderzony obiekt znajduje się dokładnie poniżej 
     * obiektu podanego jako argument czy np. obiekt podany jako argument uderzył 
     * w tamten obiekt od boku. 
     * @param object obiekt uderzający 
     * @return false jeśli uderzony obiekt został uderzony dolną częścią obiektu 
     * podanego jako argument, true w przeciwnym przypadku 
     */
    public static boolean isNothingBelow(RememberingRecentlyHitObject object){
        CollisionResults results = new CollisionResults();
        Spatial recentlyHitObject = object.getRecentlyHitObject();
        /* jeśli nie dotknęło żadnego obiektu, to zbędne jest sprawdzanie 
        kolizji w dół*/
       // if(recentlyHitObject != null)
            // tworzy pomocniczy promień sprawdzający kolizję w dół
           // new Ray(recentlyHitObject.getWorldTranslation(), new Vector3f(0,-0.5f,0))
              //      .collideWith((BoundingBox)recentlyHitObject.getWorldBound(), results);
        if(recentlyHitObject != null)
            // tworzy pomocniczy promień sprawdzający kolizję w dół
            new Ray(object.getWorldTranslation(), new Vector3f(0,-0.5f,0))
                    .collideWith((BoundingBox)recentlyHitObject.getWorldBound(), results);
        return results.size() == 0; 
    }
    
    private static boolean isProperCollisionGroup(Spatial b){
        // PhysicsCollisionObject bo control nie musi być tylko typu RigidBodyControl
        int collisionGroup = ((PhysicsCollisionObject)b.getControl(0)).getCollisionGroup();
        Node parent = b.getParent();
        do{
            if(parent.getName().contains("dzwig"))
                return collisionGroup != 4; 
            parent = parent.getParent();
        }while(parent != null);
        return true;
    }
}
