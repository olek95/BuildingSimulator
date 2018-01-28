package eyeview;

import buildingsimulator.BuildingSimulator;
import buildingsimulator.ElementName;
import settings.Control;
import buildingsimulator.GameManager;
import buildingsimulator.Map;
import listeners.DummyCollisionListener;
import com.jme3.collision.CollisionResult;
import com.jme3.collision.CollisionResults;
import com.jme3.input.FlyByCamera;
import com.jme3.input.controls.ActionListener;
import com.jme3.math.Ray;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.scene.Spatial;
import cranes.CraneAbstract;
import cranes.mobileCrane.MobileCrane;
import menu.HUD;
import menu.Shop;
import texts.Translator;

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
            if(name.equals(Control.Actions.SELECT_WAREHOUSE.toString())) {
                Camera cam = BuildingSimulator.getBuildingSimulator().getCamera();
                CollisionResults results = new CollisionResults();
                Vector2f click2d = BuildingSimulator.getBuildingSimulator().getInputManager()
                        .getCursorPosition().clone();
                Vector3f click3d = cam.getWorldCoordinates(click2d, 0f).clone();
                BuildingSimulator.getBuildingSimulator().getRootNode()
                        .getChild(ElementName.SCENE).collideWith(new Ray(click3d,
                        cam.getWorldCoordinates(click2d, 1f).subtractLocal(click3d)
                        .normalizeLocal()), results);
                CollisionResult result;
                int i = 0; 
                do {
                    result = results.getCollision(i++);
                }while(!result.getGeometry().getName().startsWith(ElementName.TERRAIN));
                Vector3f location = result.getContactPoint().setY(0.3f);
                float positiveLocation = Map.calculateBorderLocation(false);
                if(positiveLocation > Math.abs(location.x) && positiveLocation > 
                        Math.abs(location.z)) {
                    viewOwner.setDischargingLocation(location);
                    viewOwner.setListener(new DummyCollisionListener());
                } else HUD.setMessage(Translator.INACCESSIBLE_SPACE.getValue());
            } else {
                Shop shop = Shop.getDisplayedShop();
                if(shop != null) {
                    GameManager.getUser().addPoints(shop.getCostForMaterials()); 
                    HUD.updatePoints();
                    Shop.removeDisplayedShop();
                    BuildingSimulator.getBuildingSimulator().getBulletAppState()
                            .getPhysicsSpace().removeCollisionGroupListener(6);
                }
                HUD.changeShopButtonVisibility(true);
                setOff();
            }
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
        MobileCrane mobileCrane = GameManager.getMobileCrane();
        boolean isMobileCrane = GameManager.getActualUnit().equals(mobileCrane);
        if((isMobileCrane && !mobileCrane.isDuringStateChanging()) || !isMobileCrane) 
            Control.addListener(Control.getActualListener());
        viewOwner = null;
    }
    
    /**
     * Okresla czy aktualnie tryb widoku z lotu ptaka jest włączony. 
     * @return true jeśli tryb widoku z lotu ptaka jest włączony, false w przeciwnym przypadku 
     */
    public static boolean isActive() {
        return viewOwner != null; 
    }
    
    private void changeViewMode() {
        BuildingSimulator game = BuildingSimulator.getBuildingSimulator();
        Camera cam = game.getCamera();
        Vector3f actualUnitLocation = GameManager.getActualUnit().getArmControl()
                .getCrane().getWorldTranslation();
        cam.setLocation(actualUnitLocation.add(0, 100, 0));
        cam.lookAt(actualUnitLocation, Vector3f.UNIT_Z);
        game.getFlyByCamera().setEnabled(false);
        Control.removeListener(Control.getActualListener());
    }
}
