package menu;

import com.jme3.app.state.AbstractAppState;
import com.jme3.input.event.MouseButtonEvent;
import com.jme3.math.Vector2f;
import tonegod.gui.controls.windows.DialogBox;
import tonegod.gui.controls.windows.Window;
import tonegod.gui.core.Screen;

/**
 * Klasa abstrakcyjna <code>Menu</code> służy jako nadklasa wszystkich rodzai menu 
 * w grze. 
 * @author AleksanderSklorz
 */
public abstract class Menu extends AbstractAppState{
    protected Window window;
    
    /**
     * Zwraca okno aktualnie wybranego menu. 
     * @return okno menu 
     */
    public Window getWindow() { return window; }
    
    /**
     * Tworzy okno alarmowe ostrzegające przed próbą zamknięcia aktualnego okna, 
     * gdy wykonało się wcześniej jakieś zmiany. 
     * @param screen ekran dla którego chce się wyświetlić okienko alarmowe
     * @return okno alarmowe ostrzegające przed niezapisanymi zmianami 
     */
    public DialogBox createNotSavedChangesAlert(final Screen screen) {
        DialogBox alert = new DialogBox(screen, "closing_alert", new Vector2f(0f, 0f), 
                    new Vector2f(400, 190)
            ) {
                @Override
                public void onButtonCancelPressed(MouseButtonEvent mbe, boolean bln) {
                    hide();
                    screen.removeElement(this);
                }

                @Override
                public void onButtonOkPressed(MouseButtonEvent mbe, boolean bln) {
                    closeWindow();
                    screen.removeElement(this);
                }
            };
            alert.centerToParent();
            alert.getDragBar().setIsMovable(false);
            alert.setIsModal(true);
            alert.showAsModal(true);
            alert.setMsg("Changes aren't saved. Are you sure you want to leave?");
            alert.setButtonOkText("Yes");
            return alert; 
    }
    
    /**
     * Zamyka aktualne okno. 
     */
    public abstract void closeWindow();
}
