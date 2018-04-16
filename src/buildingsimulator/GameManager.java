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
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import cranes.AbstractCraneCamera;
import cranes.CameraType;
import cranes.crane.Crane;
import cranes.mobileCrane.MobileCrane;
import cranes.mobileCrane.MobileCraneArmControl;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import listeners.ArmCollisionListener;
import menu.HUD;
import menu.MenuFactory;
import menu.MenuTypes;
import settings.Control.Actions;
import texts.Translator;

/**
 * Klasa <code>GameManager</code> reprezentuje zarządcę gry, posiadającego 
 * metody ułatwiające sterowanie grą. 
 * @author AleksanderSklorz
 */
public class GameManager {
    private static Crane crane; 
    private static MobileCrane mobileCrane;
    private static User user; 
    private static boolean startedGame = false, pausedGame = false, godmode; 
    private static float gameSoundVolume;
    private static Billboard billboard; 
    private static CraneAbstract actualUnit;
    
    /**
     * Uruchamia grę. 
     */
    public static void runGame(SavedData loadedData){
        BuildingSimulator game = BuildingSimulator.getBuildingSimulator(); 
        addHUD();
        FlyByCamera cam = game.getFlyByCamera();
        cam.setDragToRotate(false);
        cam.unregisterInput();
        LimitedFlyByCamera limitedCamera = new LimitedFlyByCamera(game.getCamera());
        BuildingSimulator.setFlyByCamera(limitedCamera);
        limitedCamera.registerWithInput(game.getInputManager());
        BulletAppState bas = game.getBulletAppState(); 
        game.getStateManager().attach(bas);
        addToScene(new Map(9).getScene());
        if(loadedData != null) {
            WallsFactory.restoreWalls(loadedData.getWalls());
            mobileCrane =  new MobileCrane(loadedData.getMobileCrane());
            crane = new Crane(loadedData.getCrane());
            List<Construction> buildings = loadedData.getBuildings();
            buildings.addAll(loadedData.getSampleBuildings());
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
                AbstractCraneCamera mobileCraneCam = mobileCrane.getCamera();
                changeHUDColorDependingOnMobileCraneState();
                crane.getCamera().setOff();
                mobileCraneCam.restore();
            } else {
                actualUnit = crane;
                Control.addListener(crane.getArmControl(), true);
                crane.setUsing(true);
                mobileCrane.getCamera().setOff();
                crane.getCamera().restore();
                HUD.changeHUDColor(true);
            }
        } else {
            mobileCrane = new MobileCrane(); 
            crane = new Crane();
            actualUnit = mobileCrane;
            crane.getCamera().setOff();
            changeHUDColorDependingOnMobileCraneState();
        }
        addToScene(mobileCrane.getCrane());
        addToScene(crane.getCrane());
        billboard = new Billboard(80, 0);
        addToScene(billboard.getBillboard());
        Control.addListener(game, false);
        PhysicsSpace physics = BuildingSimulator.getPhysicsSpace();
        physics.addCollisionListener(ArmCollisionListener.createRotateAfterImpactListener());
        physics.addCollisionListener(BuildingCollisionListener.createBuildingCollisionListener());
        displayActualUnitControlsInHUD();
        HUD.fillGeneralControlsLabel(false);
        Control.addListener(limitedCamera, false);
        startedGame = true; 
    }
    
    /**
     * Kontynuuje grę od stanu z przed zatrzymania. 
     */
    public static void continueGame() {
        addHUD();
        if(!BirdsEyeView.isActive()) {
            BuildingSimulator game = BuildingSimulator.getBuildingSimulator();
            game.getFlyByCamera().setDragToRotate(false);
            game.getInputManager().setCursorVisible(false);
            Control.addListener(Control.getActualListener(), true);
            if(actualUnit.equals(mobileCrane)) changeHUDColorDependingOnMobileCraneState();
            else HUD.changeButtonsIcon(true);
        } else {
            HUD.changeShopButtonVisibility(false);
            HUD.changeButtonsIcon(true);
        }
        user.resetTimer();
        billboard.resumeAdvertisement();
        HUD.updateControlsLabel();
        HUD.fillGeneralControlsLabel(HUD.isControlsLabelVisibilityBeforeHiding());
        HUD.setControlsVisibility(HUD.isControlsLabelVisibilityBeforeHiding());
        HUD.setCranePreviewVisibility(HUD.getRememberedCranePreviewVisibility());
        resetGameControls();
        startedGame = true;
        pausedGame = false; 
    }
    
    /**
     * Zatrzymuje grę. 
     */
    public static void pauseGame() {
        BuildingSimulator game = BuildingSimulator.getBuildingSimulator();
        game.getFlyByCamera().setDragToRotate(true);
        game.getInputManager().setCursorVisible(true);
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
        Construction.setCounter(0);
        Wall.setCounter(0);
    }
    
    /**
     * Usuwa HUD. 
     */
    public static void removeHUD() {
        HUD.hideElements();
        GameManager.removeControlFromGui(HUD.getScreen());
    }
    
    /**
     * Uruchamia animację. 
     * @param animation animacja 
     */
    public static void startAnimation(Cinematic animation) {
        BuildingSimulator.getGameStateManager().attach(animation);
        animation.play();
    }
    
    /**
     * Ładuje model 3D z pliku z rozszerzeniem j3o. 
     * @param path ściezka do pliku 
     * @return węzeł reprezentujący załadowany model 
     */
    public static Node loadModel(String path) {
        return (Node)BuildingSimulator.getGameAssetManager().loadModel(path);
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
        displayActualUnitControlsInHUD();
    }
    
    /**
     * Dodaje obiekt do świata gry. 
     * @param object dodawany obiekt 
     */
    public static void addToScene(Spatial object){
        BuildingSimulator.getGameRootNode().attachChild(object);
    }
    
    /**
     * Usuwa obiekt ze świata gry. 
     * @param object usuwany obiekt 
     */
    public static void removeFromScene(Spatial object) {
        BuildingSimulator.getGameRootNode().detachChild(object);
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
        if(autoDetaching) GameManager.removeFromScene(sound);
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
     * Wybiera HUD do wyświetlenia pomiędzy sterowaniem pojazdu dźwigu mobilnego 
     * a jego ramieniem. 
     */
    public static void displayProperMobileCraneHUD() {
        MobileCraneArmControl arm = (MobileCraneArmControl)mobileCrane.getArmControl();
        if(arm.isUsing()) {
            HUD.fillControlInformation(arm.getAvailableActions(), mobileCrane.hasLooseCamera()
                    ? new String[] {Translator.MOUSE_MOVEMENT.getValue()} : null,
                    0, 0, 1, 1);
        } else {
            HUD.fillControlInformation(mobileCrane.getAvailableActions(), mobileCrane.hasLooseCamera()
                    ? new String[] {Translator.MOUSE_MOVEMENT.getValue()} : null,
                    0, 0);
        }
    }
    
    /**
     * Wyświetla sterowanie w HUD dla aktualnej jednostki (żuraw lub dźwig mobilny).
     */
    public static void displayActualUnitControlsInHUD() {
        if(actualUnit.getCrane().getName().contains(ElementName.CRANE)) {
            displayProperMobileCraneHUD();
            HUD.updateCranePreview(true);
        } else {
            Actions[] actions = actualUnit.getArmControl().getAvailableActions();
            HUD.fillControlInformation(Arrays.copyOf(actions, actions.length - 3),
                    actualUnit.hasLooseCamera() ? new String[] {Translator.MOUSE_MOVEMENT
                    .getValue()} : null, 1, 1);
            HUD.updateCranePreview(false);
        }
    }
    
    /**
     * Zmiena kolor HUD w zależności od tego w jakim miejscu w dźwigu mobilnym 
     * znajduje się gracz (HUD jest biały dla przebywania w środku w kabinie). 
     */
    public static void changeHUDColorDependingOnMobileCraneState() {
        MobileCraneArmControl armControl = (MobileCraneArmControl)mobileCrane
                    .getArmControl();
        if(!armControl.isUsing()) {
            Control.addListener(mobileCrane, true);
            HUD.changeHUDColor(!mobileCrane.getCamera().getType().equals(CameraType.CABIN));
        } else {
            HUD.changeHUDColor(!armControl.getCamera().getType()
                    .equals(CameraType.ARM_CABIN));
        }
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
    
    /**
     * Okresla czy gra jest zatrzymana. 
     * @return true jesli gra jest zatrzymana, false w przeciwnym przypadku 
     */
    public static boolean isPausedGame() { return pausedGame; }
    
    /**
     * Ustawia czy gracz posiada tryb godmode (nieskończoną liczbę funduszy). 
     * @return true włącza tryb godmode, false wyłącza 
     */
    public static void setGodmode(boolean godmode) { GameManager.godmode = godmode; }
    
    /**
     * Określa czy gracz posiada tryb godmode (nieskończoną liczbę funduszy). 
     * @return true jeśli tryb godmode jest włączaony, false w przeciwnym przypadku 
     */
    public static boolean isGodmode() { return godmode; }
    
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
     * Zwraca listę wszystkich obiektów istniejących w świecie gry. 
     * @return lista obiektów ze świata gry 
     */
    public static List<Spatial> getGameObjects() {
        return BuildingSimulator.getGameRootNode().getChildren();
    }
    
    /**
     * Dodaje obiekt kontroli dla GUI. 
     * @param control obiekt kontroli 
     */
    public static void addControlToGui(com.jme3.scene.control.Control control) {
        BuildingSimulator.getGameGuiNode().addControl(control);
    }
    
    /**
     * Usuwa obiekt kontroli z GUI. 
     * @param control obiekt kontroli 
     */
    public static void removeControlFromGui(com.jme3.scene.control.Control control) {
        BuildingSimulator.getGameGuiNode().removeControl(control);
    }
    
    private static void addHUD() {
        GameManager.addControlToGui(new HUD().getScreen());
    }
    
    private static void resetGameControls() {
        BuildingSimulator game = BuildingSimulator.getBuildingSimulator();
        Control.removeListener(game);
        Control.addListener(game, false);
        Control.addListener(game.getFlyByCamera(), false);
    }
    
    private static void removeAllUnits() { 
        mobileCrane = null; 
        crane = null;
    }
}
