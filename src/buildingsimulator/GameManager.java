package buildingsimulator;

import settings.Control;
import eyeview.BirdsEyeView;
import listeners.BuildingCollisionListener;
import authorization.User;
import billboard.Billboard;
import building.Construction;
import building.Wall;
import building.WallsFactory;
import com.jme3.audio.AudioNode;
import com.jme3.audio.AudioSource;
import cranes.CraneAbstract;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.cinematic.Cinematic;
import com.jme3.input.FlyByCamera;
import com.jme3.input.InputManager;
import com.jme3.renderer.Camera;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import cranes.crane.Crane;
import cranes.mobileCrane.MobileCrane;
import cranes.mobileCrane.MobileCraneArmControl;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Paths;
import java.util.List;
import listeners.ArmCollisionListener;
import menu.HUD;
import menu.MenuFactory;
import menu.MenuTypes;

/**
 * Klasa <code>GameManager</code> reprezentuje zarządcę gry, posiadającego 
 * metody ułatwiające sterowanie grą. 
 * @author AleksanderSklorz
 */
public class GameManager {
    private static Crane crane; 
    private static MobileCrane mobileCrane;
    private static User user; 
    private static boolean startedGame = false;
    private static boolean pausedGame = false; 
    private static float gameSoundVolume;
    private static Billboard billboard; 
    private static CraneAbstract actualUnit;
    
    /**
     * Uruchamia grę. 
     */
    public static void runGame(SavedData loadedData){
        BuildingSimulator game = BuildingSimulator.getBuildingSimulator(); 
        addHUD();
        game.getFlyByCamera().setDragToRotate(false);
        BulletAppState bas = game.getBulletAppState(); 
        game.getStateManager().attach(bas);
        addToScene(new Map(9).getScene());
        if(loadedData != null) {
            WallsFactory.restoreWalls(loadedData.getWalls());
            mobileCrane =  new MobileCrane(loadedData.getMobileCrane());
            crane = new Crane(loadedData.getCrane());
            List<Construction> buildings = loadedData.getBuildings();
            int buildingsNumber = buildings.size();
            for(int i = 0; i < buildingsNumber; i++) {
                Construction building = buildings.get(i);
                addToScene(building);
                Construction.restoreConstruction(building);
                ((Wall)building.getChild(0)).initCollisionListener();
            }
            if(loadedData.getActualUnit().equals(ElementName.MOBILE_CRANE)) {
                actualUnit = mobileCrane;
                mobileCrane.setUsing(true);
                if(!((MobileCraneArmControl)mobileCrane.getArmControl()).isUsing())
                    Control.addListener(mobileCrane);
                crane.getCamera().setOff();
                mobileCrane.getCamera().restore();
            } else {
                actualUnit = crane;
                Control.addListener(crane.getArmControl());
                crane.setUsing(true);
                mobileCrane.getCamera().setOff();
                crane.getCamera().restore();
            }
        } else {
            mobileCrane = new MobileCrane(); 
            crane = new Crane();
            actualUnit = mobileCrane;
            crane.getCamera().setOff();
            if(!((MobileCraneArmControl)mobileCrane.getArmControl()).isUsing())
                Control.addListener(mobileCrane);
        }
        addToScene(mobileCrane.getCrane());
        addToScene(crane.getCrane());
        billboard = new Billboard(80, 0);
        addToScene(billboard.getBillboard());
        Control.addListener(game);
        PhysicsSpace physics = bas.getPhysicsSpace();
        physics.addCollisionListener(ArmCollisionListener.createRotateAfterImpactListener());
        physics.addCollisionListener(BuildingCollisionListener.createBuildingCollisionListener());
        startedGame = true; 
    }
    
    /**
     * Kontynuuje grę od stanu z przed zatrzymania. 
     */
    public static void continueGame() {
        addHUD();
        if(!BirdsEyeView.isActive()) {
            BuildingSimulator.getBuildingSimulator().getFlyByCamera().setDragToRotate(false);
            Control.addListener(Control.getActualListener());
        } else {
            HUD.changeShopButtonVisibility(false);
        }
        user.resetTimer();
        billboard.resumeAdvertisement();
        startedGame = true;
        pausedGame = false; 
    }
    
    /**
     * Zatrzymuje grę. 
     */
    public static void pauseGame() {
        BuildingSimulator.getBuildingSimulator().getFlyByCamera().setDragToRotate(true);
        Control.removeListener(Control.getActualListener());
        mobileCrane.stop();
        user.rememberTime();
        startedGame = false;
        pausedGame = true; 
        billboard.pauseAdvertisement();
        MenuFactory.showMenu(MenuTypes.PAUSE_MENU);
    }
    
    /**
     * Usuwa aktualną grę. 
     */
    public static void deleteGame() {
        BuildingSimulator game = BuildingSimulator.getBuildingSimulator();
        game.getRenderer().cleanup();
        Node rootNode = game.getRootNode(); 
        game.getBulletAppState().getPhysicsSpace().removeAll(rootNode);
        rootNode.detachAllChildren();
        pausedGame = false; 
        removeAllUnits();
    }
    
    private static void addHUD() {
        BuildingSimulator.getBuildingSimulator().getGuiNode()
                .addControl(new HUD().getScreen());
    }
    
    /**
     * Usuwa HUD. 
     */
    public static void removeHUD() {
        HUD.hideElements();
        BuildingSimulator.getBuildingSimulator().getGuiNode().removeControl(HUD.getScreen());
    }
    
    /**
     * Uruchamia animację. 
     * @param animation animacja 
     */
    public static void startAnimation(Cinematic animation) {
        BuildingSimulator.getBuildingSimulator().getStateManager().attach(animation);
        animation.play();
    }
    
    public static Node loadModel(String path) {
        return (Node)BuildingSimulator.getBuildingSimulator().getAssetManager().loadModel(path);
    }
    
    /**
     * Zwraca aktualną liczbę klatek na sekundę w postaci liczby. 
     * @return FPS w postaci liczby 
     */
    public static int getFPS(){
        String fpsString = BuildingSimulator.getFPSString(), tempFPSString = "";
        int length = fpsString.length(), i = length - 1;  
        do{
            i--; // bo na początku może być minimum jedna cyfra 
        }while(fpsString.charAt(i) != ' ');
        i++;
        for(; i < length; i++) tempFPSString += fpsString.charAt(i);
        return Integer.parseInt(tempFPSString);
    }
    
    /**
     * Określa czy dany plik istnieje. 
     * @param path ścieżka pliku 
     * @return true jeśli taki plik istnieje, false w przeciwnym przypadku 
     */
    public static boolean checkIfFileExists(String path) {
        return Files.exists(Paths.get(path), new LinkOption[]{});
    }
    
    /**
     * Zwraca dźwig mobilny. 
     * @return dźwig mobilny 
     */
    public static MobileCrane getMobileCrane() { return mobileCrane; }
    
    /**
     * Zwraca żuraw. 
     * @return żuraw 
     */
    public static Crane getCrane() { return crane; }
    
    /**
     * Zwraca aktualnie sterowaną jednostkę. 
     * @return aktualnie sterowana jednostka 
     */
    public static CraneAbstract getActualUnit() { return actualUnit; }
    
    /**
     * Ustawia aktualnie sterowaną jednostkę. 
     * @param actualUnit aktualnie sterowana jednostka 
     */
    public static void setActualUnit(CraneAbstract actualUnit) { 
        GameManager.actualUnit = actualUnit;
        actualUnit.getCamera().restore();
    }
    
    /**
     * Usuwa wszystkie jednostki (dźwigi mobilne i zurawie), które występują w grze. 
     */
    public static void removeAllUnits() { 
        mobileCrane = null; 
        crane = null;
    }
    
    
    /**
     * Dodaje obiekt do świata gry. 
     * @param object dodawany obiekt 
     */
    public static void addToScene(Spatial object){
        BuildingSimulator.getBuildingSimulator().getRootNode().attachChild(object);
    }
    
    /**
     * Tworzy dźwięk. 
     * @param path ścieżka
     * @param volume głośność
     * @param looping true jeśli ma się powtarzać, false w przeciwnym przypadku 
     * @param owner właściciel dźwięku 
     * @return węzeł reprezentujący dźwięk 
     */
    public static AudioNode createSound(String path, float volume, boolean looping, Node owner) {
        BuildingSimulator game = BuildingSimulator.getBuildingSimulator();
        AudioNode sound = new AudioNode(game.getAssetManager(),
                path, false, true);
        sound.setPositional(false);
        sound.setVolume(volume);
        sound.setLooping(looping);
        if(owner == null) game.getRootNode().attachChild(sound);
        else owner.attachChild(sound);
        return sound;
    }
    
    /**
     * Zatrzymuje dźwięk. 
     * @param sound węzeł reprezentujący dźwięk 
     * @param autoDetaching true jeśli ma się odłączyć od właściciela, false 
     * jeśli ma pozostać w drzewie 
     */
    public static void stopSound(AudioNode sound, boolean autoDetaching) {
        sound.stop();
        if(autoDetaching)
            BuildingSimulator.getBuildingSimulator().getRootNode().detachChild(sound);
    }
    
    /**
     * Określa czy dźwięk jest zatrzymany lub skończony. 
     * @param sound węzeł reprezentujący dźwięk 
     * @return true jeśli dźwięk jest zatrzymany lub skończony, false w przeciwnym przypadku
     */
    public static boolean isSoundStopped(AudioNode sound) {
        return sound.getStatus().equals(AudioSource.Status.Stopped);
    }
    
    /**
     * Zwraca aktualnego użytkownika. 
     * @return aktualny użytkownik 
     */
    public static User getUser() { return user; }
    
    /**
     * Ustawia aktualnego użytkownika. 
     * @param user aktualny użytkownik 
     */
    public static void setUser(User user) { GameManager.user = user; }
    
    /**
     * Określa czy gra się już rozpoczeła. 
     * @return true jeśli gra się rozpoczęła, false w przeciwnym przypadku 
     */
    public static boolean isStartedGame() { return startedGame; }
    
    public static void setStarted(boolean startedGame) { GameManager.startedGame = startedGame; }
    
    /**
     * Okresla czy gra jest zatrzymana. 
     * @return true jesli gra jest zatrzymana, false w przeciwnym przypadku 
     */
    public static boolean isPausedGame() { return pausedGame; }
    
    /**
     * Ustawia głośność dźwięków w grze.
     * @param volume głośność dźwięków 
     */
    public static void setGameSoundVolume(float volume) { gameSoundVolume = volume; }
    
    /**
     * Zwraca głośność dźwięków w grze. 
     * @return głosność dźwięków
     */
    public static float getGameSoundVolume() { return gameSoundVolume; }
    
    /**
     * Zwraca kamerę gry.
     * @return kamera gry 
     */
    public static Camera getCamera() {
        return BuildingSimulator.getBuildingSimulator().getCamera();
    }
    
    public static FlyByCamera getFlyByCamera() {
        return BuildingSimulator.getBuildingSimulator().getFlyByCamera();
    }
    
    public static InputManager getInputManager() {
        return BuildingSimulator.getBuildingSimulator().getInputManager();
    }
}
