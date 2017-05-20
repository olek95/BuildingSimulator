package buildingsimulator;

import com.jme3.app.SimpleApplication;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.input.controls.ActionListener;
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
    private boolean debug = false;
    boolean kabina;
    public static void main(String[] args) {
        game = new BuildingSimulator();
        game.start();
    }

    @Override
    public void simpleInitApp() {
        Spatial scene = assetManager.loadModel("Scenes/gameMap.j3o");
        scene.setLocalTranslation(0, -1, 0);
        flyCam.setMoveSpeed(100);
        RigidBodyControl rgc = new RigidBodyControl(0.0f);
        scene.addControl(rgc);
        rootNode.attachChild(scene);
        stateManager.attach(bulletAppState);
        bulletAppState.getPhysicsSpace().add(rgc);
        MobileCrane crane = new MobileCrane();
        GameManager.addUnit(crane);
        GameManager.addUnit(new Crane());
        crane.setUsing(true);
        GameManager.initHookCollisionListener();
        Control.setupKeys(crane);
        Control.setupKeys(this);
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
        MobileCrane unit = (MobileCrane)GameManager.getUnit(0);
        unit.updateState();
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
        MobileCrane unit = (MobileCrane)GameManager.getUnit(0);
            if(isPressed){
                if(name.equals(Control.Actions.PHYSICS.toString())){
                    if(!debug) bulletAppState.getPhysicsSpace().enableDebug(assetManager);
                    else bulletAppState.getPhysicsSpace().disableDebug();
                    debug = !debug;
                }else{
                    if(name.equals(Control.Actions.FIRST.toString())){
                        CraneAbstract crane = GameManager.getUnit(1);
                        inputManager.removeListener(crane.getArmControl());
                        crane.setUsing(false);
                        Control.setupKeys(unit);
                        unit.setUsing(true);
                    }else{
                        inputManager.removeListener(unit);
                        unit.setUsing(false);
                        CraneAbstract crane = GameManager.getUnit(1);
                        Control.setupKeys(crane.getArmControl());
                        crane.setUsing(true);
                    }
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
