package listeners;

import building.Wall;
import buildingsimulator.ElementName;
import buildingsimulator.RememberingRecentlyHitObject;
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
    private String hittingObjectName, handleName; 
    public BottomCollisionListener(RememberingRecentlyHitObject hittingObject,
            String hittingObjectName, String handleName){
        this.hittingObject = hittingObject;
        this.hittingObjectName = hittingObjectName; 
        this.handleName = handleName; 
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
        //if((aName.startsWith(Wall.BASE_NAME) || aName.startsWith(ElementName.SCENE))
          //      && (bName.startsWith(Wall.BASE_NAME) || bName.startsWith(ElementName.SCENE)))
        if(aName.equals(hittingObjectName) && !bName.equals(handleName)){
           // if(!isProperCollisionGroup(bSpatial)) return false;
            hittingObject.setCollision(bSpatial);
        }else if(bName.equals(hittingObjectName) && !aName.equals(handleName)){
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
    
    /**
     * Ustawia nazwę obiektu który jest obiektem zderzającym się. 
     * @param name nazwa obiektu zderzającego się 
     */
    public void setHittingObjectName(String name){ hittingObjectName = name; }
    
    /**
     * Sprawdza czy ostatnio uderzony obiekt znajduje się dokładnie poniżej 
     * danego obiekt czy np. dany obiekt uderzył w tamten obiekt od boku. 
     * @param rayDirection kierunek niewidzialnego promiania, sprawdzającego kolizję
     * @return false jeśli uderzony obiekt został uderzony dolną częścią obiektu 
     * podanego jako argument, true w przeciwnym przypadku 
     */
    public boolean isNothingBelow(Vector3f rayDirection){
        if(rayDirection == null) rayDirection = new Vector3f(0, -0.5f, 0);
        CollisionResults results = new CollisionResults();
        Spatial recentlyHitObject = hittingObject.getRecentlyHitObject();
        /* jeśli nie dotknęło żadnego obiektu, to zbędne jest sprawdzanie 
        kolizji w dół*/
        if(recentlyHitObject != null){
            Ray ray = new Ray(hittingObject.getWorldTranslation(), rayDirection);
            if(recentlyHitObject.getName().startsWith(ElementName.PROP)){
                Node crane = recentlyHitObject.getParent();
                ((Node)crane.getChild(ElementName.PROP0)).getChild(0)
                        .collideWith(ray, results);
                ((Node)crane.getChild(ElementName.PROP1)).getChild(0).collideWith(ray, results);
            }else{
                recentlyHitObject.collideWith(ray, results);
            }
        }
        return results.size() == 0; 
    }
    
    private static boolean isProperCollisionGroup(Spatial b){
        // PhysicsCollisionObject bo control nie musi być tylko typu RigidBodyControl
        int collisionGroup = ((PhysicsCollisionObject)b.getControl(0)).getCollisionGroup();
        Node parent = b.getParent();
        do{
            if(parent.getName().contains(ElementName.CRANE))
                return collisionGroup != 4; 
            parent = parent.getParent();
        }while(parent != null);
        return true;
    }
}
