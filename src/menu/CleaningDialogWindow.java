package menu;

import building.CatchNode;
import buildingsimulator.BuildingSimulator;
import buildingsimulator.ElementName;
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
        BuildingSimulator.getBuildingSimulator().getFlyByCamera().setDragToRotate(false);
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
    
    private void deleteBuildings(boolean onlyInfiniteBuildings) {
        BuildingSimulator game = BuildingSimulator.getBuildingSimulator();
        List<Spatial> gameObjects = game.getRootNode().getChildren(); 
        int objectsCount = gameObjects.size();
        for(int i = 0; i < objectsCount; i++) {
            Spatial object = gameObjects.get(i);
            if(object.getName().startsWith(ElementName.BUILDING_BASE_NAME)) {
                Node firstWall = (Node)((Node)object).getChild(0);
                if(onlyInfiniteBuildings) {
                    if(firstWall.getControl(RigidBodyControl.class).getMass() != 0) {
                        //                deleteMainPartsControls((Node)object);
                        deleteWallsControl((Node)((Node)object).getChild(0));
                        object.removeFromParent();
                    }
                } else {
                    //                deleteMainPartsControls((Node)object);
                    deleteWallsControl((Node)((Node)object).getChild(0));
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
    
//    private static int deleteMainPartsControls(Node building) {
//        List<Spatial> parts = building.getChildren(); 
//        int partsAmount = parts.size(), points = 0; 
//        for(int i = 0; i < partsAmount; i++) {
//            Node part = (Node)parts.get(i);
//            deleteWallsControl(part);
//            deleteWallControl(part);
//        }
//        return points; 
//    }
    
    private static void deleteWallControl(Node wall) {
        PhysicsSpace physics = BuildingSimulator.getBuildingSimulator().getBulletAppState()
                        .getPhysicsSpace();
        for(int i = 0; i < 3; i++) {
            physics.remove(wall.getControl(0));
        }
    }
}
