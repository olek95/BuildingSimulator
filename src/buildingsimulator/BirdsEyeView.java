package buildingsimulator;

import com.jme3.collision.CollisionResult;
import com.jme3.collision.CollisionResults;
import com.jme3.input.FlyByCamera;
import com.jme3.input.controls.ActionListener;
import com.jme3.math.Ray;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import java.util.List;
import menu.Shop;

public class BirdsEyeView implements ActionListener{
    private static BirdsEyeView birdsEyeView = null;
    private static Shop shop;
    private BirdsEyeView() {
        Control.addListener(this);
    }
    public static void changeViewMode(Shop shop) {
        if(birdsEyeView == null) birdsEyeView = new BirdsEyeView();
        BirdsEyeView.shop = shop;
        BuildingSimulator game = BuildingSimulator.getBuildingSimulator();
        Camera cam = game.getCamera();
        Vector3f actualUnitLocation = GameManager.findActualUnit().getArmControl().getCrane()
                .getWorldTranslation();
        cam.setLocation(actualUnitLocation.add(0, 100, 0));
        cam.lookAt(actualUnitLocation, Vector3f.UNIT_Z);
        game.getFlyByCamera().setEnabled(false);
    }

    @Override
    public void onAction(String name, boolean isPressed, float tpf) {
        Camera cam = BuildingSimulator.getBuildingSimulator().getCamera();
//        List<Spatial> gameObjects = BuildingSimulator.getBuildingSimulator()
//                .getRootNode().getChildren();
//        int objectsCount = gameObjects.size(); 
//        for(int i = 0; i < objectsCount; i++) {
//            System.out.println(gameObjects.get(i));
//        }
//        shop.buyWalls();
        Node scene = (Node)BuildingSimulator.getBuildingSimulator().getRootNode()
                .getChild("New Scene");
        CollisionResults results = new CollisionResults();
                Vector2f click2d = BuildingSimulator.getBuildingSimulator().getInputManager().getCursorPosition().clone();
        Vector3f click3d = cam.getWorldCoordinates(
            click2d, 0f).clone();
        Vector3f dir = cam.getWorldCoordinates(
            click2d, 1f).subtractLocal(click3d).normalizeLocal();
        Ray ray = new Ray(click3d, dir);
        scene.collideWith(ray, results);
        Vector3f location = null;
        List<Spatial> sceneChilds = scene.getChildren();
//        for(int i = 0; i < sceneChilds.size(); i++) {
//            System.out.println(sceneChilds.get(i)); 
//        }
        for(int i = 0; i < results.size(); i++) {
            CollisionResult result = results.getCollision(i);
            if(result.getGeometry().getName().startsWith("terrain")) {
                location = result.getContactPoint();
            }
        }
        location.setY(0.3f);
        shop.buyWalls(location);
    }
}
