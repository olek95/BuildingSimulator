package buildingsimulator;

import com.jme3.app.SimpleApplication;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.light.DirectionalLight;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.scene.Spatial;

public class BuildingSimulator extends SimpleApplication implements ActionListener{
    private static BuildingSimulator game;
    private BulletAppState bulletAppState = new BulletAppState();
    private MobileCrane player;
    public static void main(String[] args) {
        game = new BuildingSimulator();
        game.start();
    }

    @Override
    public void simpleInitApp() {
        Spatial scene = assetManager.loadModel("Scenes/gameMap.j3o");
        scene.setLocalTranslation(0, -3f, 0);
        flyCam.setMoveSpeed(100);
        RigidBodyControl rgb = new RigidBodyControl(0.0f);
        scene.addControl(rgb);
        rootNode.attachChild(scene);
        stateManager.attach(bulletAppState);
        bulletAppState.getPhysicsSpace().add(rgb);
        //bulletAppState.getPhysicsSpace().enableDebug(assetManager);
        player = new MobileCrane();
        setupKeys(player);
        setupKeys(this);
        DirectionalLight sun = new DirectionalLight();
        sun.setColor(ColorRGBA.White);
        sun.setDirection(new Vector3f(-.5f,-.5f,-.5f).normalizeLocal());
        rootNode.addLight(sun);
    }

    @Override
    public void simpleUpdate(float tpf) {
        player.updateState();
    }

    @Override
    public void simpleRender(RenderManager rm) {
        //TODO: add render code
    }
    public static BuildingSimulator getBuildingSimulator(){
        return game;
    }
    public BulletAppState getBulletAppState(){
        return bulletAppState;
    }
    private void setupKeys(Object o){
        if(!inputManager.hasMapping("Left")){
            inputManager.addMapping("Left", new KeyTrigger(KeyInput.KEY_H));
            inputManager.addMapping("Right", new KeyTrigger(KeyInput.KEY_K));
            inputManager.addMapping("Up", new KeyTrigger(KeyInput.KEY_U));
            inputManager.addMapping("Down", new KeyTrigger(KeyInput.KEY_J));
            inputManager.addMapping("Action", new KeyTrigger(KeyInput.KEY_F));
        }
        if(o instanceof MobileCrane){
            inputManager.addListener((MobileCrane)o, "Left");
            inputManager.addListener((MobileCrane)o, "Right");
            inputManager.addListener((MobileCrane)o, "Up");
            inputManager.addListener((MobileCrane)o, "Down");
        }else{
            if(o instanceof CraneCabin){
                inputManager.addListener((CraneCabin)o, "Left");
                inputManager.addListener((CraneCabin)o, "Right");
            }else{
                inputManager.addListener(this, "Action");
            }
        }
    }
    public void onAction(String name, boolean isPressed, float tpf){
        if(isPressed && name.equals("Action")){
            if(player.using){
                inputManager.removeListener(player);
                setupKeys(player.getCabin());
            }
            else{
                inputManager.removeListener(player.getCabin());
                setupKeys(player);
            }
            player.using = !player.using;
        }
    }
}
