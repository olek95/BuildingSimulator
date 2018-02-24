package building;

import buildingsimulator.BuildingSimulator;
import buildingsimulator.ElementName;
import buildingsimulator.GameManager;
import buildingsimulator.PhysicsManager;
import com.jme3.bullet.control.RigidBodyControl;
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

public class ConstructionCopier implements VisibleFromAbove{
    private BirdsEyeView view; 
    private boolean foundSelectedConstruction = false;
    private Construction selectedConstruction; 
    
    public BirdsEyeView getView() { return view; }
    
    public void startCopying() {
        HUD.changeShopButtonVisibility(false);
        view = new BirdsEyeView(this, true); 
    }

    @Override
    public void setDischargingLocation(Vector3f location) {
        new BuildingSample(location);
        if(selectedConstruction == null) {
            selectedConstruction = getSelectedConstruction(location);
        } else {
            copy(location);
        }
    }

    @Override
    public void setListener(DummyCollisionListener listener) {}

    @Override
    public DummyCollisionListener getListener() { return null; }

    @Override
    public void unload() {}
    
    private Construction getSelectedConstruction(final Vector3f location) {
        List<Spatial> gameObjects = BuildingSimulator.getBuildingSimulator()
                .getRootNode().getChildren();
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
        clonedConstruction.setName(createUniqueName());
        GameManager.addToScene(clonedConstruction);
        clonedConstruction.breadthFirstTraversal(new SceneGraphVisitorAdapter() {
            private int i = 0;
            @Override
            public void visit(Node object) {
                if(object.getName().startsWith(ElementName.WALL_BASE_NAME)) {
                    object.getControl(RigidBodyControl.class)
                            .setPhysicsLocation(locations.get(i).add(distance));
                    object.getControl(RigidBodyControl.class).setPhysicsRotation(rotations.get(i));
                    i++;
                    PhysicsManager.addPhysicsToGame(object);
                }
            }}
        );
    }
    
    private String createUniqueName() {
        List<Spatial> gameObjects = BuildingSimulator.getBuildingSimulator()
                .getRootNode().getChildren();
        int gameObjectsNumber = gameObjects.size(), counter = 0;
        for(int i = 0; i < gameObjectsNumber; i++) {
            Spatial object = gameObjects.get(i);
            if(object.getName().startsWith(ElementName.BUILDING_BASE_NAME)) {
                counter++;
            }
        }
        return ElementName.BUILDING_BASE_NAME + counter;
    }
}
