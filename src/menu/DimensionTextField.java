package menu;

import com.jme3.input.event.KeyInputEvent;
import com.jme3.math.Vector2f;
import tonegod.gui.controls.text.TextField;
import tonegod.gui.core.ElementManager;

public class DimensionTextField extends TextField{
    private Shop shop; 
    public DimensionTextField(Shop shop, String id, Vector2f position) {
        super(shop.getScreen(), id, position); 
        this.shop = shop; 
    }
    
    @Override
    public void onKeyRelease(KeyInputEvent evt) {
        super.onKeyRelease(evt);
        shop.prepareOrder(-1, null);
    }
}
