package buildingsimulator;

import com.jme3.collision.CollisionResult;
import com.jme3.collision.CollisionResults;
import com.jme3.input.FlyByCamera;
import com.jme3.input.controls.ActionListener;
import com.jme3.math.Ray;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import menu.HUD;

/**
 * Obiekt klasy <code>BirdsEyeView</code> reprezentuje widok z lotu ptaka (widok 
 * patrząć od góry). 
 * @author Aleksander Sklorz
 */
public class BirdsEyeView implements ActionListener{
    private static VisibleFromAbove viewOwner;
    public BirdsEyeView(VisibleFromAbove viewOwner) {
        BirdsEyeView.viewOwner = viewOwner;
        Control.addListener(this);
        changeViewMode();
    }
    
    /**
     * Pozwala na ustawienie kliknietego lewym przyciskiem myszy miejsca widzianego 
     * z lotu ptaka. 
     * @param name nazwa przycisku 
     * @param isPressed czy kliknięty 
     * @param tpf 
     */
    @Override
    public void onAction(String name, boolean isPressed, float tpf) {
        if(isPressed) {
            Camera cam = BuildingSimulator.getBuildingSimulator().getCamera();
            CollisionResults results = new CollisionResults();
            Vector2f click2d = BuildingSimulator.getBuildingSimulator().getInputManager()
                    .getCursorPosition().clone();
            Vector3f click3d = cam.getWorldCoordinates(click2d, 0f).clone();
            BuildingSimulator.getBuildingSimulator().getRootNode().getChild("New Scene")
                    .collideWith(new Ray(click3d, cam.getWorldCoordinates(click2d, 1f)
                    .subtractLocal(click3d).normalizeLocal()), results);
            CollisionResult result;
            int i = 0; 
            do {
                result = results.getCollision(i++);
            }while(!result.getGeometry().getName().startsWith("terrain"));
            Vector3f location = result.getContactPoint().setY(0.3f);
            viewOwner.setDischargingLocation(location);
            viewOwner.setListener(new DummyCollisionListener());
        }
    }
    
    /**
     * Wyłącza tryb widoku z lotu ptaka. 
     */
    public void setOff() {
        Control.removeListener(this);
        FlyByCamera camera = BuildingSimulator.getBuildingSimulator().getFlyByCamera();
        camera.setEnabled(true);
        camera.setDragToRotate(false);
        Control.addListener(Control.getActualListener());
        viewOwner = null;
    }
    
    public static boolean isActive() {
        return viewOwner != null; 
    }
    
    private void changeViewMode() {
        BuildingSimulator game = BuildingSimulator.getBuildingSimulator();
        Camera cam = game.getCamera();
        Vector3f actualUnitLocation = GameManager.findActualUnit().getArmControl().getCrane()
                .getWorldTranslation();
        cam.setLocation(actualUnitLocation.add(0, 100, 0));
        cam.lookAt(actualUnitLocation, Vector3f.UNIT_Z);
        game.getFlyByCamera().setEnabled(false);
        Control.removeListener(Control.getActualListener());
    }
}
