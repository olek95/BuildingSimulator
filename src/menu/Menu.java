package menu;

import buildingsimulator.BuildingSimulator;
import com.jme3.app.state.AbstractAppState;
import com.jme3.input.event.MouseButtonEvent;
import com.jme3.math.Vector2f;
import texts.Translator;
import tonegod.gui.controls.windows.DialogBox;
import tonegod.gui.controls.windows.Window;
import tonegod.gui.core.Screen;

/**
 * Klasa abstrakcyjna <code>Menu</code> służy jako nadklasa wszystkich rodzai menu 
 * w grze. 
 * @author AleksanderSklorz
 */
public abstract class Menu extends AbstractAppState{
    private Window window;
    
    /**
     * Zwraca okno aktualnie wybranego menu. 
     * @return okno menu 
     */
    protected Window getWindow() { return window; }
    
    /**
     * Ustawia okno. 
     * @param window okno 
     */
    protected void setWindow(Window window) { this.window = window; }
    
    /**
     * Tworzy okno alarmowe ostrzegające przed daną czynnością. 
     * @param screen ekran dla którego chce się wyświetlić okienko alarmowe
     * @param message wyświetlana informacja
     * @param nextMenu menu do którego chcemy wyjść 
     * @return okno alarmowe ostrzegające przed niezapisanymi zmianami 
     */
    protected DialogBox createNotSavedChangesAlert(final Screen ownerScreen, String message,
            final MenuTypes nextMenu) {
        DialogBox alert = new DialogBox(ownerScreen, "closing_alert", new Vector2f(0f, 0f), 
                new Vector2f(400, 190)) {
                    @Override
                    public void onButtonCancelPressed(MouseButtonEvent mbe, boolean bln) {
                        hide();
                        ownerScreen.removeElement(this);
                    }

                    @Override
                    public void onButtonOkPressed(MouseButtonEvent mbe, boolean bln) {
                        hide();
                        ownerScreen.removeElement(this);
                        doWhenAcceptedExit(ownerScreen, nextMenu);
                    }
                };
        alert.centerToParent();
        alert.getDragBar().setIsMovable(false);
        alert.setIsResizable(false);
        alert.setIsModal(true);
        alert.showAsModal(true);
        alert.setMsg(message);
        alert.setButtonCancelText(Translator.CANCELLATION.getValue());
        alert.setButtonOkText(Translator.CONFIRMATION.getValue());
        return alert; 
    }
    
    /**
     * Wykonuje się w chwili gdy wyjście z obecnego okna jest potwierdzone. 
     * @param screen ekran dla którego akceptujemy wyjście 
     * @param menuType typ menu do którego przechodzimy po akceptacji wyjścia. Jesli null, 
     * to po prostu tylko zamyka się obecne okno
     */
    protected void doWhenAcceptedExit(Screen screen, MenuTypes menuType){
        goNextMenu(screen, menuType); 
    }
    
    /**
     * Przechodzi do kolejnego menu. 
     * @param screen ekran aktualnego menu 
     * @param menuType typ menu do którego przechodzimy 
     */
    protected void goNextMenu(Screen screen, MenuTypes menuType) {
        window.hide();
        BuildingSimulator.getBuildingSimulator().getGuiNode().removeControl(screen);
        if(menuType != null) MenuFactory.showMenu(menuType);
    }
}
