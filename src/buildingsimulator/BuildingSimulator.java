package buildingsimulator;

import com.jme3.app.SimpleApplication;
import com.jme3.bullet.BulletAppState;
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
    private Crane player2;
    private boolean debug = false;
    boolean kabina;
    private static Spatial scene;
    public static void main(String[] args) {
        game = new BuildingSimulator();
        game.start();
    }

    @Override
    public void simpleInitApp() {
        scene = assetManager.loadModel("Scenes/gameMap.j3o");
        scene.setLocalTranslation(0, -1, 0);
        flyCam.setMoveSpeed(100);
        RigidBodyControl rgc = new RigidBodyControl(0.0f);
        scene.addControl(rgc);
        rootNode.attachChild(scene);
        stateManager.attach(bulletAppState);
        bulletAppState.getPhysicsSpace().add(rgc);
        player2 = new Crane();
        //player = new MobileCrane();
        setupKeys(player2);
        setupKeys(this);
        DirectionalLight sun = new DirectionalLight();
        sun.setColor(ColorRGBA.White);
        sun.setDirection(new Vector3f(-.5f,-.5f,-.5f).normalizeLocal());
        rootNode.addLight(sun);
        
        // KOD DLA TESTU!!
        Box b = new Box(1, 1, 1); // create cube shape
        Geometry geom = new Geometry("Box", b);  // create cube geometry from the shape
        geom.setLocalTranslation(0, 0f, 20);
        Material mat = new Material(assetManager,
          "Common/MatDefs/Misc/Unshaded.j3md");  // create a simple material
        mat.setColor("Color", ColorRGBA.Blue);   // set color of material to blue
        geom.setMaterial(mat);                   // set the cube's material
        rootNode.attachChild(geom);              // make the cube appear in the scene
        RigidBodyControl rg = new RigidBodyControl(0.1f);
        geom.addControl(rg);
        bulletAppState.getPhysicsSpace().add(rg);
        Box b2 = new Box(1, 1, 1); // create cube shape
        Geometry geom2 = new Geometry("Box", b2);  // create cube geometry from the shape
        geom2.setLocalTranslation(0, 2.1f, 20);
        Material mat2 = new Material(assetManager,
          "Common/MatDefs/Misc/Unshaded.j3md");  // create a simple material
        mat2.setColor("Color", ColorRGBA.Blue);   // set color of material to blue
        geom2.setMaterial(mat2);                   // set the cube's material
        rootNode.attachChild(geom2);              // make the cube appear in the scene
        RigidBodyControl rg2 = new RigidBodyControl(0.1f);
        geom2.addControl(rg2);
        bulletAppState.getPhysicsSpace().add(rg2);
        /*Box b3 = new Box(1, 1, 1); // create cube shape
        Geometry geom3 = new Geometry("Box", b3);  // create cube geometry from the shape
        geom3.setLocalTranslation(0, 4.1f, 20);
        Material mat3 = new Material(assetManager,
          "Common/MatDefs/Misc/Unshaded.j3md");  // create a simple material
        mat3.setColor("Color", ColorRGBA.Blue);   // set color of material to blue
        geom3.setMaterial(mat3);                   // set the cube's material
        rootNode.attachChild(geom3);              // make the cube appear in the scene
        RigidBodyControl rg3 = new RigidBodyControl(0.1f);
        geom3.addControl(rg3);
        bulletAppState.getPhysicsSpace().add(rg3);*/
        // KONIEC KODU DLA TESTU 
    }

    @Override
    public void simpleUpdate(float tpf) {
        /*player.updateState();
        CraneCabin cabin = player.getCabin();
        if(!player.using){
            if(cabin.getPropsLowering() <= CraneCabin.MAX_PROP_PROTRUSION)
                cabin.controlProps(true);
            else{
                /* metoda wywołana na łańcuchu "Action", gdyż ostatnia akcja może być nullem.
                Może być bez tego ifa, ale dodany w celu optymalizacji aby nie powtarzać
                dodawania listenerów klawiszy*/
                /*if("Action".equals(GameManager.getLastAction())){
                    setupKeys(cabin);
                    GameManager.setLastAction(null);
                }
            }
        }else{
            if(cabin.getPropsLowering() > CraneCabin.MIN_PROP_PROTRUSION)
                cabin.controlProps(false);
            else{
                if("Action".equals(GameManager.getLastAction())){
                    setupKeys(player);
                    GameManager.setLastAction(null);
                }
            }
        }*/
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
    private void setupKeys(Object o){
        if(!inputManager.hasMapping("Left")){
            inputManager.addMapping("Left", new KeyTrigger(KeyInput.KEY_H));
            inputManager.addMapping("Right", new KeyTrigger(KeyInput.KEY_K));
            inputManager.addMapping("Up", new KeyTrigger(KeyInput.KEY_U));
            inputManager.addMapping("Down", new KeyTrigger(KeyInput.KEY_J));
            inputManager.addMapping("Action", new KeyTrigger(KeyInput.KEY_F));
            inputManager.addMapping("Pull out", new KeyTrigger(KeyInput.KEY_E));
            inputManager.addMapping("Pull in", new KeyTrigger(KeyInput.KEY_SPACE));
            inputManager.addMapping("Lower hook", new KeyTrigger(KeyInput.KEY_R));
            inputManager.addMapping("Highten hook", new KeyTrigger(KeyInput.KEY_T));
            inputManager.addMapping("Physics", new KeyTrigger(KeyInput.KEY_P));
        }
        if(o instanceof MobileCrane){
            inputManager.addListener((MobileCrane)o, "Left");
            inputManager.addListener((MobileCrane)o, "Right");
            inputManager.addListener((MobileCrane)o, "Up");
            inputManager.addListener((MobileCrane)o, "Down");
        }else{
            //if(o instanceof CraneCabin){
            if(o instanceof Crane){
                inputManager.addListener((Crane)o, "Left");
                inputManager.addListener((Crane)o, "Right");
                inputManager.addListener((Crane)o, "Up");
                inputManager.addListener((Crane)o, "Down");
                inputManager.addListener((Crane)o, "Pull out");
                inputManager.addListener((Crane)o, "Pull in");
                inputManager.addListener((Crane)o, "Lower hook");
                inputManager.addListener((Crane)o, "Highten hook");
            }else{
                inputManager.addListener(this, "Action");
                inputManager.addListener(this, "Physics");
            }
        }
    }
    public void onAction(String name, boolean isPressed, float tpf){
        /*boolean craneStop = true;
        if(isPressed && name.equals("Action")){
            if(player.using){
                float craneSpeed = player.getSpeed();
                craneStop = Math.floor(craneSpeed) == 0f && craneSpeed >= 0f 
                        || Math.ceil(craneSpeed) == 0f && craneSpeed < 0f;
                // zezwala na opuszczenie podpór tylko gdy dźwig nie porusza się
                if(craneStop) inputManager.removeListener(player);
            }
            else{
                inputManager.removeListener(player.getCabin());
            }
            if(craneStop){
                player.using = !player.using;
                GameManager.setLastAction(name);
            }
        }else{
            if(isPressed && name.equals("Physics")){
                if(!debug) bulletAppState.getPhysicsSpace().enableDebug(assetManager);
                else bulletAppState.getPhysicsSpace().disableDebug();
                debug = !debug;
            }
        }*/
        if(isPressed && name.equals("Physics")){
                if(!debug) bulletAppState.getPhysicsSpace().enableDebug(assetManager);
                else bulletAppState.getPhysicsSpace().disableDebug();
                debug = !debug;
            }
    }
    /**
     * Pobiera tekstową reprezentację liczby klatek na sekundę. 
     * @return FPS w postaci tekstu 
     */
    public static String getFPSString(){
        return BuildingSimulator.game.fpsText.getText();
    }
    
    public static Spatial getScene(){
        return scene;
    }
}
