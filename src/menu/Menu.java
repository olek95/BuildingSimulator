package menu;

import buildingsimulator.BuildingSimulator;
import com.jme3.app.state.AbstractAppState;
import com.jme3.input.event.MouseButtonEvent;
import com.jme3.math.Vector2f;
import tonegod.gui.controls.windows.DialogBox;
import tonegod.gui.controls.windows.Window;
import tonegod.gui.core.Element;
import tonegod.gui.core.Screen;

public abstract class Menu extends AbstractAppState{
    protected Window window;
    
    public Window getWindow() { return window; }
    
    public DialogBox createNotSavedChangesAlert(final Screen screen) {
        DialogBox alert = new DialogBox(screen, "closing_alert", new Vector2f(0f, 0f), 
                    new Vector2f(400, 190)
            ) {
                @Override
                public void onButtonCancelPressed(MouseButtonEvent mbe, boolean bln) {
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
    
    public abstract void closeWindow();
}
