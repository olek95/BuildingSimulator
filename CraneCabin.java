package buildingsimulator;

import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.input.controls.AnalogListener;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;

public class CraneCabin implements AnalogListener{
    private Spatial craneCabin, lift;
    public CraneCabin(Spatial craneSpatial){
        //c.setMass(0f);
        //c.setKinematicSpatial(true);
        craneCabin = ((Node)craneSpatial).getChild("crane");
        lift = ((Node)craneCabin).getChild("lift");
    }
    public void onAnalog(String name, float value, float tpf) {
        switch(name){
            case "Right":
                craneCabin.rotate(0, tpf, 0);
                //c.update(tpf);
                break;
            case "Left": 
                craneCabin.rotate(0, -tpf, 0);
                //c.update(-tpf);
                break;
           case "Up":
                lift.rotate(-tpf, 0, 0);
                //c.update(-tpf);
                break;
            case "Down":
                lift.rotate(tpf, 0, 0);
                //c.update(tpf);
        }
    }
}
