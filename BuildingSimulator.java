package buildingsimulator;

import com.jme3.app.SimpleApplication;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.collision.shapes.PlaneCollisionShape;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.scene.Geometry;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Box;

public class BuildingSimulator extends SimpleApplication implements ActionListener{
    private static BuildingSimulator game;
    private BulletAppState bulletAppState = new BulletAppState();
    private MobileCrane player;
    private boolean debug = false;
    public static void main(String[] args) {
        game = new BuildingSimulator();
        game.start();
    }

    @Override
    public void simpleInitApp() {
        Spatial scene = assetManager.loadModel("Scenes/gameMap.j3o");
        scene.setLocalTranslation(0, -3f, 0);
        flyCam.setMoveSpeed(100);
        PlaneCollisionShape plane = new PlaneCollisionShape();
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
        
        // KOD DLA TESTU!!
        Box b = new Box(1, 1, 1); // create cube shape
        Geometry geom = new Geometry("Box", b);  // create cube geometry from the shape
        geom.setLocalTranslation(0, 0, 20);
        Material mat = new Material(assetManager,
          "Common/MatDefs/Misc/Unshaded.j3md");  // create a simple material
        mat.setColor("Color", ColorRGBA.Blue);   // set color of material to blue
        geom.setMaterial(mat);                   // set the cube's material
        rootNode.attachChild(geom);              // make the cube appear in the scene
        RigidBodyControl rg = new RigidBodyControl(10f);
        geom.addControl(rg);
        bulletAppState.getPhysicsSpace().add(rg);
        Box b2 = new Box(1, 1, 1); // create cube shape
        Geometry geom2 = new Geometry("Box", b2);  // create cube geometry from the shape
        geom2.setLocalTranslation(0, 1, 20);
        Material mat2 = new Material(assetManager,
          "Common/MatDefs/Misc/Unshaded.j3md");  // create a simple material
        mat2.setColor("Color", ColorRGBA.Blue);   // set color of material to blue
        geom2.setMaterial(mat2);                   // set the cube's material
        rootNode.attachChild(geom2);              // make the cube appear in the scene
        RigidBodyControl rg2 = new RigidBodyControl(10f);
        geom2.addControl(rg2);
        bulletAppState.getPhysicsSpace().add(rg2);
        // KONIEC KODU DLA TESTU 
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
            inputManager.addMapping("Pull out", new KeyTrigger(KeyInput.KEY_E));
            inputManager.addMapping("Pull in", new KeyTrigger(KeyInput.KEY_SPACE));
            inputManager.addMapping("Physics", new KeyTrigger(KeyInput.KEY_P));
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
                inputManager.addListener((CraneCabin)o, "Up");
                inputManager.addListener((CraneCabin)o, "Down");
                inputManager.addListener((CraneCabin)o, "Pull out");
                inputManager.addListener((CraneCabin)o, "Pull in");
            }else{
                inputManager.addListener(this, "Action");
                inputManager.addListener(this, "Physics");
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
        }else{
            if(isPressed && name.equals("Physics")){
                if(!debug) bulletAppState.getPhysicsSpace().enableDebug(assetManager);
                else bulletAppState.getPhysicsSpace().disableDebug();
                debug = !debug;
            }
        }
    }
}
