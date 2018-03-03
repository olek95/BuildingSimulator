package building;

import authorization.User;
import buildingsimulator.ElementName;
import buildingsimulator.GameManager;
import buildingsimulator.PhysicsManager;
import com.jme3.bounding.BoundingVolume;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.collision.CollisionResults;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.scene.SceneGraphVisitor;
import com.jme3.scene.SceneGraphVisitorAdapter;
import com.jme3.scene.Spatial;
import eyeview.BirdsEyeView;
import eyeview.VisibleFromAbove;
import java.util.ArrayList;
import java.util.List;
import listeners.DummyCollisionListener;
import menu.HUD;
import menu.Shop;

/**
 * Klasa <code>BuildingCreator</code> umożliwia kupowanie gotowego budynku bądź 
 * klonowanie już istniejącego. Zarówno kupowanie, jak i kopiowanie skutkuje 
 * odjęciem pewnej wartości punktów. 
 * @author AleksanderSklorz 
 */
public class BuildingCreator implements VisibleFromAbove{
    private BirdsEyeView view; 
    private boolean foundSelectedConstruction = false;
    private Construction selectedConstruction; 
    private boolean cloning; 
    
    public BuildingCreator(boolean cloning) {
        this.cloning = cloning; 
    }
    
    /**
     * Zwraca widok z lotu ptaka. 
     * @return widok z lotu ptaka 
     */
    public BirdsEyeView getView() { return view; }
    
    /**
     * Rozpoczyna kopiowanie bądź kupowanie nowego budynku. Przełącza automatycznie 
     * w tryb widoku z lotu ptaka. 
     */
    public void start() {
        HUD.changeShopButtonVisibility(false);
        view = new BirdsEyeView(this, true); 
        BirdsEyeView.displayMovingModeHUD(cloning);
    }

    @Override
    public void setDischargingLocation(Vector3f location) {
        if(cloning) {
            if(selectedConstruction == null) {
                selectedConstruction = getSelectedConstruction(location);
            } else {
                copy(location);
            }
        } else {
            buyBuilding(location);
        }
    }

    @Override
    public void setListener(DummyCollisionListener listener) {}

    @Override
    public DummyCollisionListener getListener() { return null; }

    @Override
    public void unload() {}
    
    /**
     * Sprawdza czy granice budynku przecinają się z jakimkolwiek obiektem ze świata gry. 
     * @param bounding granice sprawdzanego budynku 
     * @return true jeśli granice budynku przecinają się z jakimkolwiek obiektem, 
     * false gdy dana przestrzeń jest pusta 
     */
    public static boolean checkIntersection(BoundingVolume bounding) {
        List<Spatial> gameObjects = GameManager.getGameObjects();
        int gameObjectsNumber = gameObjects.size();
        for(int i = 0; i < gameObjectsNumber; i++) {
            Spatial gameObject = gameObjects.get(i); 
            String gameObjectName = gameObject.getName(); 
            if(!gameObjectName.equals(ElementName.SCENE) 
                    && gameObject.getWorldBound().intersects(bounding)) {
                if(gameObjectName.contains(ElementName.STATIC_CRANE)){
                    if(gameObject.collideWith(bounding, new CollisionResults()) != 0) {
                        return true;
                    }
                } else return true;
            }
        }
        return false;
    }
    
    /**
     * Zwraca informację czy twórca budynków jest w trybie klonowania czy kupowania 
     * przykładowego budynku. 
     * @return true jeśli tryb klonowania, false w przeciwnym przypadku 
     */
    public boolean isCloning() { return cloning; }
    
    private Construction getSelectedConstruction(final Vector3f location) {
        List<Spatial> gameObjects = GameManager.getGameObjects();
        int gameObjectsNumber = gameObjects.size();
        for(int i = 0; i < gameObjectsNumber; i++) {
            Spatial object = gameObjects.get(i);
            if(object.getName().startsWith(ElementName.BUILDING_BASE_NAME)) {
                object.depthFirstTraversal(new SceneGraphVisitor(){
                    @Override
                    public void visit(Spatial spatial) {
                        if(spatial.getName().startsWith(ElementName.WALL_BASE_NAME)) {
                            if(location.distance(spatial.getWorldTranslation()) <= 6) {
                                foundSelectedConstruction = true;
                            }
                        }
                    }
                });
                if(foundSelectedConstruction) return (Construction)object; 
            }
        }
        return null;
    }
    
    private void copy(Vector3f location) {
        final List<Vector3f> locations = new ArrayList();
        final List<Quaternion> rotations = new ArrayList(); 
        selectedConstruction.breadthFirstTraversal(new SceneGraphVisitorAdapter() {
            @Override
            public void visit(Node object) {
                if(object.getName().startsWith(ElementName.WALL_BASE_NAME)) {
                    Vector3f old = object.getControl(RigidBodyControl.class).getPhysicsLocation();
                    locations.add(old.clone());
                    rotations.add(object.getControl(RigidBodyControl.class).getPhysicsRotation().clone());
                }
            }}
        );
        final Vector3f distance = location.subtract(locations.get(0));
        Construction clonedConstruction = (Construction)selectedConstruction.clone();
        clonedConstruction.setName(createUniqueName(clonedConstruction.getName()));
        final List<Wall> clonedWalls = new ArrayList();
        clonedConstruction.breadthFirstTraversal(new SceneGraphVisitorAdapter() {
            private int i = 0;
            @Override
            public void visit(Node object) {
                if(object.getName().startsWith(ElementName.WALL_BASE_NAME)) {
                    Wall wall = (Wall)object; 
                    wall.getControl(RigidBodyControl.class)
                            .setPhysicsLocation(locations.get(i).add(distance));
                    wall.getControl(RigidBodyControl.class).setPhysicsRotation(rotations.get(i));
                    i++;
                    clonedWalls.add(wall);
                }
            }}
        );
        BoundingVolume bounding = clonedConstruction.getWorldBound();
        bounding.setCenter(location);
        if(!checkIntersection(bounding)) {
            int clonedWallsNumber = clonedWalls.size(); 
            for(int i = 0; i < clonedWallsNumber; i++) {
                Wall wall = clonedWalls.get(i);
                PhysicsManager.addPhysicsToGame(wall);
                GameManager.getUser().addPoints(-Shop.calculateWallCost(wall.getLength(),
                            wall.getHeight(), wall.getType()));
            }
            GameManager.addToScene(clonedConstruction);
            HUD.updatePoints();
            if(clonedConstruction.isSold()) {
                User user = GameManager.getUser();
                user.setBuildingsNumber(user.getBuildingsNumber() + 1);
            }
        }
    }
    
    private String createUniqueName(String oldName) {
        int counter = Construction.getCounter(); 
        Construction.setCounter(++counter);
        return ElementName.BUILDING_BASE_NAME + counter + (oldName.endsWith("sample") ? 
                " sample" : "");
    }
    
    private void buyBuilding(Vector3f location) {
        BuildingSample building = new BuildingSample();
        if(building.drop(location, FastMath.rand.nextInt(3))) {
            building.setSold(true);
            User user = GameManager.getUser();
            user.setBuildingsNumber(user.getBuildingsNumber() + 1);
            building.breadthFirstTraversal(new SceneGraphVisitorAdapter() {
                @Override
                public void visit(Node object) {
                    if(object.getName().startsWith(ElementName.WALL_BASE_NAME)) {
                        Wall wall = (Wall)object;
                        GameManager.getUser().addPoints(-Shop.calculateWallCost(wall.getLength(),
                                wall.getHeight(), wall.getType()));
                    }
                }
            });
            HUD.updatePoints();
        } else Construction.setCounter(Construction.getCounter() - 1);
    }
}
