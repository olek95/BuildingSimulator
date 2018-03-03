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
import cranes.CameraType;
import cranes.CraneAbstract;
import cranes.mobileCrane.MobileCrane;
import cranes.mobileCrane.MobileCraneArmControl;
import menu.HUD;
import menu.Shop;
import settings.Control.Actions;
import texts.Translator;

/**
 * Obiekt klasy <code>BirdsEyeView</code> reprezentuje widok z lotu ptaka (widok 
 * patrząć od góry). 
 * @author Aleksander Sklorz
 */
public class BirdsEyeView implements ActionListener{
    private static VisibleFromAbove viewOwner;
    private static boolean movingAvailable;
    private boolean mouseDisabled = false;
    public BirdsEyeView(VisibleFromAbove viewOwner, boolean movingAvailable) {
        BirdsEyeView.viewOwner = viewOwner;
        Control.addListener(this);
        BirdsEyeView.movingAvailable = movingAvailable; 
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
        if(isPressed && !mouseDisabled) {
            if(name.equals(Control.Actions.SELECT_WAREHOUSE.toString())) {
                BuildingSimulator game = BuildingSimulator.getBuildingSimulator();
                Camera cam = game.getCamera();
                CollisionResults results = new CollisionResults();
                Vector2f click2d = game.getInputManager().getCursorPosition().clone();
                Vector3f click3d = cam.getWorldCoordinates(click2d, 0f).clone();
                game.getRootNode().getChild(ElementName.SCENE).collideWith(new Ray(click3d, 
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
                    BuildingSimulator.getPhysicsSpace().removeCollisionGroupListener(6);
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
        FlyByCamera camera = BuildingSimulator.getGameFlyByCamera();
        camera.setEnabled(true);
        camera.setDragToRotate(false);
        camera.setRotationSpeed(1);
        MobileCrane mobileCrane = GameManager.getMobileCrane();
        CraneAbstract crane = GameManager.getActualUnit();
        boolean isMobileCrane = crane.equals(mobileCrane);
        if(isMobileCrane) {
            if(!mobileCrane.isDuringStateChanging())
                Control.addListener(Control.getActualListener());
            MobileCraneArmControl armControl = (MobileCraneArmControl)mobileCrane
                    .getArmControl();
            if(!armControl.isUsing()) {
                Control.addListener(mobileCrane);
                HUD.changeHUDColor(!mobileCrane.getCamera().getType().equals(CameraType.CABIN));
            } else {
                HUD.changeHUDColor(!armControl.getCamera().getType()
                        .equals(CameraType.ARM_CABIN));
            }
        } else {
            Control.addListener(Control.getActualListener());
        }
        GameManager.getCrane().setView(null);
        viewOwner = null;
        crane.getCamera().restore();
        GameManager.displayActualUnitControlsInHUD();
    }
    
    /**
     * Wyświetla sterowanie stosowane w trybie poruszania się. 
     */
    public static void displayMovingModeHUD() {
        HUD.fillControlInformation(new Actions[] {Actions.FLYCAM_FORWARD, 
                Actions.FLYCAM_BACKWARD, Actions.FLYCAM_STRAFE_LEFT, Actions.FLYCAM_STRAFE_RIGHT},
                    new String[]{Translator.RIGHT_CLICK_CANCELLATION.getValue(),
                    Translator.LEFT_CLICK_CLONE.getValue()});
    }
    
    /**
     * Wyświetla sterowanie stosowne w trybie zablokowanego poruszania się. 
     */
    public static void displayNotMovingModeHUD() {
        HUD.fillControlInformation(null, new String[] {Translator
                    .RIGHT_CLICK_CANCELLATION.getValue(), Translator
                    .LEFT_CLICK_DROPPING.getValue()});
    }
    
    /**
     * Okresla czy aktualnie tryb widoku z lotu ptaka jest włączony. 
     * @return true jeśli tryb widoku z lotu ptaka jest włączony, false w przeciwnym przypadku 
     */
    public static boolean isActive() {
        return viewOwner != null; 
    }
    
    /**
     * Określa czy sprawdzane są kliknięcia myszką. 
     * @param disabled true jeśli kliknięcia myszką mają być zablokowane, 
     * false w przeciwnym przypadku 
     */
    public void setMouseDisabled(boolean disabled) { mouseDisabled = disabled; }
    
    /**
     * Określa czy widok z lotu ptaka jest w trybie poruszania się. 
     * @return true jeśli jest tryb poruszania się, false jeśli poruszanie się 
     * jest zabronione 
     */
    public static boolean isMovingAvailable() { return movingAvailable; }
    
    private void changeViewMode() {
        BuildingSimulator game = BuildingSimulator.getBuildingSimulator();
        Camera cam = game.getCamera();
        Vector3f actualUnitLocation = GameManager.getActualUnit().getArmControl()
                .getCrane().getWorldTranslation();
        cam.setLocation(actualUnitLocation.add(0, 100, 0));
        cam.lookAt(actualUnitLocation, Vector3f.UNIT_Z);
        if(movingAvailable) {
            game.getFlyByCamera().setRotationSpeed(0);
            game.getInputManager().setCursorVisible(true);
            displayMovingModeHUD();
        } else game.getFlyByCamera().setEnabled(false);
        Control.removeListener(Control.getActualListener());
    }
}
