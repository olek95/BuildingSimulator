package buildingsimulator;

import settings.Control;
import eyeview.VisibleFromAbove;
import eyeview.BirdsEyeView;
import listeners.DummyCollisionListener;
import building.Construction;
import building.BuildingCreator;
import cranes.mobileCrane.MobileCraneArmControl;
import cranes.mobileCrane.MobileCrane;
import cranes.crane.Crane;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.AppStateManager;
import com.jme3.asset.AssetManager;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.input.FlyByCamera;
import com.jme3.input.InputManager;
import com.jme3.input.controls.ActionListener;
import com.jme3.light.DirectionalLight;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.Renderer;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import java.util.List;
import menu.CleaningDialogWindow;
import menu.HUD;
import menu.MainMenu;
import menu.MenuFactory;
import menu.MenuTypes;
import menu.Options;
import menu.Shop;
import texts.Translator;
        
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
            HUD.updateTime();
            MobileCrane unit = GameManager.getMobileCrane();
            unit.updateState();
            List<Spatial> gameObjects = rootNode.getChildren();
            int gameObjectsNumber = gameObjects.size();
            for(int i = 0; i < gameObjectsNumber; i++){
                Spatial gameObject = gameObjects.get(i); 
                if(gameObject.getName().startsWith(ElementName.BUILDING_BASE_NAME)){
                    Construction building = (Construction)gameObject; 
                    if(building.isHit()){
                        building.setResetWalls(false);
                        building.updateState();
                        if(!building.isResetWalls()) {
                            building.setHit(false);
                            building.renovateBuilding();
                        }
                    }
                }
            }
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
    
    @Override
    public void onAction(String name, boolean isPressed, float tpf){
        MobileCrane mobileCrane = GameManager.getMobileCrane();
        Crane crane = GameManager.getCrane();
        BirdsEyeView view;
        boolean buildingCloning = false; 
        if(isPressed) {
            switch(Control.Actions.valueOf(name)) {
                case PAUSE:
                    if(!GameManager.isPausedGame()) {
                        Shop shop = Shop.getDisplayedShop();
                        if(shop != null) shop.cancel(null, true);
                        else {
                            CleaningDialogWindow cleaningWindow = CleaningDialogWindow
                                    .getDisplayedCleaningDialogWindow();
                            if(cleaningWindow != null) cleaningWindow.cancel(null, true);
                        }
                        GameManager.pauseGame();
                        HUD.setControlsLabelVisibilityBeforeHiding(HUD.isControlsLabelVisible());
                        GameManager.removeHUD();
                    } else {
                        MainMenu.start();
                        GameManager.continueGame();
                    }
                    break;
                case SHOW_CURSOR:
                    flyCam.setDragToRotate(true);
                    break;
                case MOVE_CRANE:
                    view = crane.getView();
                    if(view == null) {
                        if(!BirdsEyeView.isActive()) {
                            if(crane.getArmControl().getHook()
                                    .getAttachedObject() == null) {
                                GameManager.getActualUnit().getCamera().setOff();
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
                    if(BirdsEyeView.isActive() || GameManager.getActualUnit().equals(mobileCrane))
                        break;
                    inputManager.removeListener(crane.getArmControl());
                    crane.setUsing(false);
                    Control.addListener(((MobileCraneArmControl)mobileCrane
                            .getArmControl()).isUsing() ? mobileCrane.getArmControl()
                            : mobileCrane);
                    mobileCrane.setUsing(true);
                    crane.getCamera().setOff();
                    GameManager.setActualUnit(mobileCrane);
                    break;
                case SECOND:
                    if(BirdsEyeView.isActive() || GameManager.getActualUnit().equals(crane))
                        break;
                    if(((MobileCraneArmControl)mobileCrane.getArmControl()).isUsing()){
                        inputManager.removeListener(mobileCrane.getArmControl());
                    }else{
                        mobileCrane.setSteeringAngle(0f);
                        inputManager.removeListener(mobileCrane);
                    }
                    mobileCrane.setUsing(false);
                    Control.addListener(crane.getArmControl());
                    crane.setUsing(true);
                    mobileCrane.getCamera().setOff();
                    GameManager.setActualUnit(crane);
                    break;
                case COPY_BUILDING: 
                    buildingCloning = true; 
                case BUY_BUILDING: 
                    if(buildingCloning != true) buildingCloning = false; 
                    BuildingCreator creator = new BuildingCreator(buildingCloning); 
                    view = creator.getView();
                    if(view == null) {
                        if(!BirdsEyeView.isActive()) {
                            GameManager.getActualUnit().getCamera().setOff();
                            creator.start();
                        }
                    }
                    break;
                case CHANGING_CONTROLS_HUD_VISIBILITY:
                    HUD.switchControlsVisibility();
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
    public static String getFPSString(){ return game.fpsText.getText(); }
    
    /**
     * Ustawia poruszającą się kamerę. 
     * @param camera poruszająca się kamera
     */
    public static void setFlyByCamera(FlyByCamera camera) { game.flyCam = camera; }
    
    /**
     * Zwraca poruszającą się kamerę. 
     * @return poruszająca się kamera 
     */
    public static FlyByCamera getGameFlyByCamera() {  return game.flyCam; }
    
    /** 
     * Metoda zwracająca aktualną grę. 
     * @return aktualna gra
     */
    public static BuildingSimulator getBuildingSimulator(){ return game; }
    /**
     * Zwraca główną przestrzeń fizyki. 
     * @return główna przestrzeń fizyki
     */
    public static PhysicsSpace getPhysicsSpace(){
        return game.bulletAppState.getPhysicsSpace();
    }
    
    /**
     * Zwraca obiekt zezwalający na stosowanie fizyki w grze. 
     * @return obiekt zezwalający na stosowanie fizyki w grze 
     */
    public BulletAppState getBulletAppState() { return game.bulletAppState; }
    
    /**
     * Zwraca zarządcę zasobami. 
     * @return zarządca zasobami 
     */
    public static AssetManager getGameAssetManager() {  return game.assetManager; }
    
    /**
     * Zwraca korzeń (główny węzeł) gry. Do niego należą wszystkie obiekty świata 
     * gry. 
     * @return główny węzeł gry
     */
    public static Node getGameRootNode() { return game.rootNode; }
    
    /**
     * Zwraca węzeł przeznaczony na elementy GUI. 
     * @return węzeł przeznaczony na elementy GUI 
     */
    public static Node getGameGuiNode() { return game.guiNode;  }
    
    /**
     * Zwraca zarządcę stanów gry. 
     * @return zarządca stanów gry 
     */
    public static AppStateManager getGameStateManager() { return game.stateManager; }
    
    /**
     * Zwraca obiekt odpowiedzialny za zdarzenia wejścia. 
     * @return obiekt odpowiedzialny za zdarzenia wejścia
     */
    public static InputManager getGameInputManager() { return game.inputManager; }
    
    /**
     * Zwraca kamerę 
     * @return kamera
     */
    public static Camera getCam() { return game.cam; }
}
