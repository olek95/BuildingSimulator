package buildingsimulator;

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
import menu.HUD;
import menu.MainMenu;
import menu.MenuFactory;
import menu.MenuTypes;
import menu.Options;
import menu.Shop;
import texts.Translator;
import tonegod.gui.controls.windows.Window;
import tonegod.gui.core.Screen;

public class BuildingSimulator extends SimpleApplication implements ActionListener{
    private static BuildingSimulator game;
    private BulletAppState bulletAppState = new BulletAppState();
    private boolean debug = false;
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
            MobileCrane unit = (MobileCrane)GameManager.getUnit(0);
            unit.updateState();
            List<Spatial> gameObjects = rootNode.getChildren();
            int gameObjectsNumber = gameObjects.size();
            for(int i = 0; i < gameObjectsNumber; i++){
                Spatial gameObject = gameObjects.get(i); 
                if(gameObject.getName().startsWith("Building")){
                    Construction building = (Construction)gameObject; 
                    if(building.isHit()){
                        building.setResetWalls(false);
                        building.updateState((Node)building.getChild(0)); 
                        if(!building.isResetWalls()) building.setHit(false);
                        //building.setHit(false);
                    }
                }
            }
            Shop shop = Shop.getDisplayedShop(); 
            if(shop != null) {
                DummyCollisionListener shopListener = shop.getListener();
                if(shopListener != null && shopListener.isEnd() 
                        && !shopListener.isCollision())
                    Shop.getDisplayedShop().realizeOrder(); 
            }
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
        MobileCrane mobileCrane = (MobileCrane)GameManager.getUnit(0);
        CraneAbstract crane = GameManager.getUnit(1);
        if(isPressed){
            if(name.equals(Control.Actions.PAUSE.toString())){
                Shop shop = Shop.getDisplayedShop();
                if(shop != null) shop.cancel(null, true);
                GameManager.pauseGame();
                GameManager.removeHUD(); 
            }else{
                if(name.equals(Control.Actions.SHOW_CURSOR.toString())){
                    flyCam.setDragToRotate(true);
                }else{
                    if(name.equals(Control.Actions.PHYSICS.toString())){
                        if(!debug) bulletAppState.getPhysicsSpace().enableDebug(assetManager);
                        else bulletAppState.getPhysicsSpace().disableDebug();
                        debug = !debug;
                    }else{
                        if(name.equals(Control.Actions.FIRST.toString())){
                            inputManager.removeListener(crane.getArmControl());
                            crane.setUsing(false);
                            Control.addListener(((MobileCraneArmControl)mobileCrane
                                    .getArmControl()).isUsing() ? mobileCrane.getArmControl()
                                    : mobileCrane);
                            mobileCrane.setUsing(true);
                        }else{
                            if(((MobileCraneArmControl)mobileCrane.getArmControl()).isUsing()){
                                inputManager.removeListener(mobileCrane.getArmControl());
                            }else{
                                mobileCrane.setSteeringAngle(0f);
                                inputManager.removeListener(mobileCrane);
                            }
                            mobileCrane.setUsing(false);
                            Control.addListener(crane.getArmControl());
                            crane.setUsing(true);
                        }
                    }
                }
            }
        } else {
            if(name.equals(Control.Actions.SHOW_CURSOR.toString()) && Shop.getDisplayedShop() == null){
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
