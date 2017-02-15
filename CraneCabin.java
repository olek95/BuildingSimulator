package buildingsimulator;

import com.jme3.input.controls.AnalogListener;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.Control;

public class CraneCabin implements AnalogListener{
    private Spatial craneSpatial, craneCabin;
    private float actualYRotation;
    public CraneCabin(Spatial craneSpatial){
        this.craneSpatial = craneSpatial;
        craneCabin = ((Node)craneSpatial).getChild("craneCabin");
        actualYRotation = 0;
    }
    public void onAnalog(String name, float value, float tpf) {
        actualYRotation += 0.1f;
        switch(name){
            case "Right":
                craneCabin.rotate(0, tpf, 0);
                break;
            case "Left": 
                craneCabin.rotate(0, -tpf, 0);
        }
    }
}
