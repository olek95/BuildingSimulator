package buildingsimulator;

import settings.Control;
import eyeview.VisibleFromAbove;
import eyeview.BirdsEyeView;
import listeners.DummyCollisionListener;
import building.BuildingValidator;
import building.Construction;
import building.Wall;
import building.WallType;
import building.WallsFactory;
import cranes.mobileCrane.MobileCraneArmControl;
import cranes.mobileCrane.MobileCrane;
import cranes.crane.Crane;
import cranes.CraneAbstract;
import com.jme3.app.SimpleApplication;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.input.FlyByCamera;
import com.jme3.input.controls.ActionListener;
import com.jme3.light.DirectionalLight;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.system.AppSettings;
import com.jme3.texture.Texture;
import com.jme3.util.SkyFactory;
import cranes.Hook;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javax.swing.ImageIcon;
import menu.CleaningDialogWindow;
import menu.HUD;
import menu.MainMenu;
import menu.MenuFactory;
import menu.MenuTypes;
import menu.Options;
import menu.Shop;
import org.lwjgl.opengl.Display;
import texts.Translator;
import tonegod.gui.controls.windows.Window;
import tonegod.gui.core.Screen;
        
public class BuildingSimulator extends SimpleApplication implements ActionListener{
    private static BuildingSimulator game;
    private BulletAppState bulletAppState = new BulletAppState();
    private boolean debug = false;
    private int updateLoopCounter = 0; 
    public static void main(String[] args) {
        game = new BuildingSimulator();
        game.setShowSettings(false);
        game.setSettings(Options.restoreSettings(Options.loadProperties(), true));
        game.start();
    }
    
    @Override
    public void simpleInitApp() {
//        restart();
        inputManager.deleteMapping(INPUT_MAPPING_EXIT);
        flyCam.setMoveSpeed(100);
        flyCam.setDragToRotate(true);
        DirectionalLight sun = new DirectionalLight();
        sun.setColor(ColorRGBA.White);
        sun.setDirection(new Vector3f(-.5f,-.5f,-.5f).normalizeLocal());
        rootNode.addLight(sun);
        MenuFactory.showMenu(MenuTypes.STARTING_MENU);
    }

    @Override
    public void simpleUpdate(float tpf) {
        if(Options.getScreen() != null && Options.isResolutionChanged()
                && Options.getStale()){
            Options.refresh(); 
        }
        if(GameManager.isStartedGame()){
            MobileCrane unit = GameManager.getMobileCrane();
            unit.updateState();
            List<Spatial> gameObjects = rootNode.getChildren();
            int gameObjectsNumber = gameObjects.size();
//            for(int i = 0; i < gameObjectsNumber; i++){
//                Spatial gameObject = gameObjects.get(i); 
//                if(gameObject.getName().startsWith(ElementName.BUILDING_BASE_NAME)){
//                    Construction building = (Construction)gameObject; 
//                    if(building.isHit()){
//                        building.setResetWalls(false);
//                        building.updateState((Node)building.getChild(0)); 
//                        if(!building.isResetWalls()) building.setHit(false);
//                        //building.setHit(false); // nieodkomentowac
//                    }
//                }
//            }
            Shop shop = Shop.getDisplayedShop();
            VisibleFromAbove object;
            if(shop == null) {
                Crane crane = GameManager.getCrane();
                object = crane;
                if(updateLoopCounter == 50) {
                    crane.getArmControl().rotateHook();
                    updateLoopCounter = 0;
                }
            } else object = shop;
            DummyCollisionListener dummyListener = object.getListener();
            if(dummyListener != null && dummyListener.isEnd()) {
                dummyListener.deleteDummyWallControl();
                object.setListener(null);
                if(!dummyListener.isCollision()) {
                    object.unload();
                    updateLoopCounter = 1;
                } 
                else HUD.setMessage(Translator.MESSAGE_NO_FREE_SPACE.getValue());
            }
            if(HUD.shouldMessageBeDeleted()) HUD.removeMessage();
            if(updateLoopCounter >= 1) updateLoopCounter++;
        }
    }

    @Override
    public void simpleRender(RenderManager rm) {
        //TODO: add render code
    }
    /** 
     * Metoda zwracająca aktualną grę. 
     * @return aktualna gra
     */
    public static BuildingSimulator getBuildingSimulator(){
        return game;
    }
    /**
     * Zwraca obiekt do symulowania fizyki. 
     * @return obiekt do symulowania fizyki
     */
    public BulletAppState getBulletAppState(){
        return bulletAppState;
    }
    public void onAction(String name, boolean isPressed, float tpf){
        MobileCrane mobileCrane = GameManager.getMobileCrane();
        Crane crane = GameManager.getCrane();
        if(isPressed) {
            switch(Control.Actions.valueOf(name)) {
                case PAUSE:
                    Shop shop = Shop.getDisplayedShop();
                    if(shop != null) shop.cancel(null, true);
                    else {
                        CleaningDialogWindow cleaningWindow = CleaningDialogWindow
                                .getDisplayedCleaningDialogWindow();
                        if(cleaningWindow != null) cleaningWindow.cancel(null, true);
                    }
                    GameManager.pauseGame();
                    GameManager.removeHUD();
                    break;
                case SHOW_CURSOR:
                    flyCam.setDragToRotate(true);
                    break;
                case MOVE_CRANE:
                    BirdsEyeView view = crane.getView();
                    if(view == null) {
                        if(!BirdsEyeView.isActive()) {
                            if(crane.getArmControl().getHook()
                                    .getAttachedObject() == null) {
                                crane.startMoving();
                            } else {
                                HUD.setMessage(Translator.REQUIREMENT_DETACHING_WALL
                                        .getValue());
                            }
                        }
                    }else crane.removeView();
                    break;
                case PHYSICS:
                    if(!debug) bulletAppState.getPhysicsSpace().enableDebug(assetManager);
                    else bulletAppState.getPhysicsSpace().disableDebug();
                    debug = !debug;
                    break; 
                case FIRST:
                    if(BirdsEyeView.isActive()) break;
                    inputManager.removeListener(crane.getArmControl());
                    crane.setUsing(false);
                    Control.addListener(((MobileCraneArmControl)mobileCrane
                            .getArmControl()).isUsing() ? mobileCrane.getArmControl()
                            : mobileCrane);
                    mobileCrane.setUsing(true);
                    GameManager.setActualUnit(mobileCrane);
                    break;
                case SECOND:
                    if(BirdsEyeView.isActive()) break;
                    if(((MobileCraneArmControl)mobileCrane.getArmControl()).isUsing()){
                        inputManager.removeListener(mobileCrane.getArmControl());
                    }else{
                        mobileCrane.setSteeringAngle(0f);
                        inputManager.removeListener(mobileCrane);
                    }
                    mobileCrane.setUsing(false);
                    Control.addListener(crane.getArmControl());
                    crane.setUsing(true);
                    GameManager.setActualUnit(crane);
            }
        } else {
            if(name.equals(Control.Actions.SHOW_CURSOR.toString()) 
                    && Shop.getDisplayedShop() == null && !BirdsEyeView.isActive()
                    && CleaningDialogWindow.getDisplayedCleaningDialogWindow() == null){
                flyCam.setDragToRotate(false);
            }
        }
    }
    /**
     * Pobiera tekstową reprezentację liczby klatek na sekundę. 
     * @return FPS w postaci tekstu 
     */
    public static String getFPSString(){
        return BuildingSimulator.game.fpsText.getText();
    }
}
