package buildingsimulator;

import building.Construction;
import building.Wall;
import com.jme3.bullet.collision.PhysicsCollisionEvent;
import com.jme3.bullet.collision.PhysicsCollisionListener;
import com.jme3.scene.Spatial;

/**
 * Singleton <code>BuildingCollisionListener</code> jest słuchaczem sprawdzającym
 * czy nastąpiło zderzenie elementu potrafiącego zniszczyć budynek z budynkiem.
 * Jeśli tak, to rejestruje budynek jako uderzony. 
 * @author AleksanderSklorz
 */
public class BuildingCollisionListener implements PhysicsCollisionListener{
    private static BuildingCollisionListener listener; 
    private BuildingCollisionListener(){}
    public static synchronized BuildingCollisionListener createBuildingCollisionListener(){
        if(listener == null) listener = new BuildingCollisionListener(); 
        return listener; 
    }
    
    @Override
    public void collision(PhysicsCollisionEvent event) {
        Spatial a = event.getNodeA(), b = event.getNodeB();
        if(a != null && b != null){
            if(checkIfHitWall(a, b)){
                Construction building = Construction.getWholeConstruction(a);
                if(building != null) building.setHit(true);
            }else{
                if(checkIfHitWall(b, a)){
                    Construction building = Construction.getWholeConstruction(b);
                    if(building != null) building.setHit(true);
                    
                }
            }
        }
    }
    
    private boolean checkIfHitWall(Spatial a, Spatial b){
        String bName = b.getName();
        boolean hit = false;
        if(a.getName().startsWith("Wall") && !bName.startsWith("terrain-gameMap") 
                && !((Wall)a).isStale()){
            if(bName.startsWith("Wall")){
                if(((Wall)b).isStale() || Construction.getWholeConstruction(b) == null)
                    hit = true;
            }else hit = true; 
        }
        return hit; 
    }
}
