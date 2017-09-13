package menu;

import com.jme3.input.event.KeyInputEvent;
import com.jme3.math.Vector2f;
import tonegod.gui.controls.text.TextField;
import tonegod.gui.core.ElementManager;

public class DimensionTextField extends TextField{
    public DimensionTextField(ElementManager screen, String id, Vector2f position) {
        super(screen, id, position); 
    }
    
    @Override
    public void onKeyRelease(KeyInputEvent evt) {
        super.onKeyRelease(evt);
        Shop.setCost();
    }
}
