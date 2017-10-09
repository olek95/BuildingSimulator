package menu;

import buildingsimulator.BuildingSimulator;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.font.BitmapFont;
import com.jme3.input.event.MouseButtonEvent;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import java.util.List;
import texts.Translator;
import tonegod.gui.controls.windows.Window;
import tonegod.gui.core.Element;
import tonegod.gui.core.Screen;

/**
 * Obiekt klasy <code>CleaningDialogWindow</code> reprezentuje okienko umożliwiające 
 * oczyszczenie mapy ze wszystkich budowli lub tylko z budowli, które nie zostały 
 * ukończone. 
 * @author AleksanderSklorz
 */
public class CleaningDialogWindow extends Menu{
    private static Screen screen;
    private static CleaningDialogWindow displayedCleaningDialogWindow = null;
    public CleaningDialogWindow() {
        screen = new Screen(BuildingSimulator.getBuildingSimulator());
        screen.parseLayout("Interface/cleaning_dialog.gui.xml", this);
        Window window = (Window)screen.getElementById("cleaning_window");
        window.setWindowTitle(Translator.CLEANING_MAP.getValue());
        Element dragBar = window.getDragBar();
        dragBar.setTextAlign(BitmapFont.Align.Center);
        dragBar.setIsMovable(false);
        setWindow(window); 
        Translator.setTexts(new String[]{"cleaning_message", "entire_map_button",
            "infinite_buildings_button", "cancellation_button"}, new Translator[]{Translator
                .CLEANING_MAP_MESSAGE, Translator.ENTIRE_MAP, Translator.INFINITE_BUILDINGS,
                Translator.CANCELLATION}, screen);
        BuildingSimulator.getBuildingSimulator().getGuiNode().addControl(screen);
        displayedCleaningDialogWindow = this; 
    }
    
    /**
     * Usuwa wszystkie budowle - zarówno te ukończone, jak i w trakcie budowy. 
     * @param evt
     * @param isToggled 
     */
    public void deleteAllBuildings(MouseButtonEvent evt, boolean isToggled) {
        BuildingSimulator game = BuildingSimulator.getBuildingSimulator();
        List<Spatial> gameObjects = game.getRootNode().getChildren(); 
        int objectsCount = gameObjects.size();
        for(int i = 0; i < objectsCount; i++) {
            Spatial object = gameObjects.get(i);
            if(object.getName().startsWith("Building")) {
                deleteMainPartsControls((Node)object);
                object.removeFromParent();
            }
        }
        cancel(null, true);
    }
    
    /**
     * Wyłacza okienko oczyszczania mapy. 
     * @param evt
     * @param isToggled 
     */
    public void cancel(MouseButtonEvent evt, boolean isToggled) {
        displayedCleaningDialogWindow = null; 
        BuildingSimulator.getBuildingSimulator().getFlyByCamera().setDragToRotate(false);
        goNextMenu(screen, null);
    }
    
    /**
     * Zwraca okienko oczyszczania mapy. 
     * @return okienko oczyszczania mapy 
     */
    public static CleaningDialogWindow getDisplayedCleaningDialogWindow() {
        return displayedCleaningDialogWindow; 
    }
    
    private static int deleteWallsControl(Node element) {
        List<Spatial> walls = element.getChildren();
        int points = 0;
        for(int i = 1; i < 14; i++){ // od 1 do 13 bo w tym przedziale są strony 
            Node catchNode = (Node)walls.get(i);
            if(!catchNode.getChildren().isEmpty()){
                Spatial spatialWall = catchNode.getChild(0);
                if(spatialWall.getName().startsWith("Wall")) {
                    deleteWallControl((Node)spatialWall);
                }
            }
        }
        return points;
    }
    
    private static int deleteMainPartsControls(Node building) {
        List<Spatial> parts = building.getChildren(); 
        int partsAmount = parts.size(), points = 0; 
        for(int i = 0; i < partsAmount; i++) {
            Node part = (Node)parts.get(i);
            deleteWallsControl(part);
            deleteWallControl(part);
        }
        return points; 
    }
    
    private static void deleteWallControl(Node wall) {
        PhysicsSpace physics = BuildingSimulator.getBuildingSimulator().getBulletAppState()
                        .getPhysicsSpace();
        for(int i = 0; i < 3; i++) {
            physics.remove(wall.getControl(0));
        }
    }
}
