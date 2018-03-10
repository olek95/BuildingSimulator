package menu;

import building.CatchNode;
import building.Construction;
import buildingsimulator.BuildingSimulator;
import buildingsimulator.ElementName;
import buildingsimulator.GameManager;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.control.RigidBodyControl;
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
        BuildingSimulator game = BuildingSimulator.getBuildingSimulator();
        screen = new Screen(game);
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
        game.getGuiNode().addControl(screen);
        displayedCleaningDialogWindow = this; 
    }
    
    /**
     * Usuwa wszystkie budowle - zarówno te ukończone, jak i w trakcie budowy. 
     * @param evt
     * @param isToggled 
     */
    public void deleteAllBuildings(MouseButtonEvent evt, boolean isToggled) {
        deleteBuildings(false);
        HUD.setControlsVisibility(HUD.isControlsLabelVisibilityBeforeHiding());
    }
    
    /**
     * Usuwa tylko nieukończone budynki. 
     * @param evt
     * @param isToggled 
     */
    public void deleteInfiniteBuildings(MouseButtonEvent evt, boolean isToggled) {
        deleteBuildings(true);
        HUD.setControlsVisibility(HUD.isControlsLabelVisibilityBeforeHiding());
    }
    
    /**
     * Wyłacza okienko oczyszczania mapy. 
     * @param evt
     * @param isToggled 
     */
    public void cancel(MouseButtonEvent evt, boolean isToggled) {
        displayedCleaningDialogWindow = null; 
        BuildingSimulator.getGameFlyByCamera().setDragToRotate(false);
        goNextMenu(screen, null);
        HUD.setControlsVisibility(HUD.isControlsLabelVisibilityBeforeHiding());
    }
    
    /**
     * Zwraca okienko oczyszczania mapy. 
     * @return okienko oczyszczania mapy 
     */
    public static CleaningDialogWindow getDisplayedCleaningDialogWindow() {
        return displayedCleaningDialogWindow; 
    }
    
    private void deleteBuildings(boolean onlyConstructionsDuringBuilding) {
        List<Spatial> gameObjects = GameManager.getGameObjects(); 
        int objectsCount = gameObjects.size();
        for(int i = objectsCount - 1; i >= 0; i--) {
            Spatial object = gameObjects.get(i);
            if(object.getName().startsWith(ElementName.BUILDING_BASE_NAME)) {
                Construction building = (Construction)object; 
                Node firstWall = (Node)building.getChild(0);
                if(onlyConstructionsDuringBuilding) {
                    if(!building.isSold()) {
                        deleteWallsControl(firstWall);
                        object.removeFromParent();
                    }
                } else {
                    deleteWallsControl(firstWall);
                    object.removeFromParent();
                }
            }
        }
        cancel(null, true);
    }
    
    private static int deleteWallsControl(Node element) {
        List<Spatial> wallElements = element.getChildren();
        int points = 0, end = CatchNode.values().length;
        for(int i = 1; i <= end; i++){ 
            List<Spatial> sideChildren  = ((Node)wallElements.get(i)).getChildren();
            int sideChildrenCount = sideChildren.size(); 
            for(int k = 0; k < sideChildrenCount; k++) {
                deleteWallsControl((Node)sideChildren.get(k));
            }
        }
        deleteWallControl(element);
        return points;
    }
    
    private static void deleteWallControl(Node wall) {
        PhysicsSpace physics = BuildingSimulator.getPhysicsSpace();
        for(int i = 0; i < 3; i++) {
            physics.remove(wall.getControl(0));
        }
    }
}
