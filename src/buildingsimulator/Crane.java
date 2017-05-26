package buildingsimulator;

import com.jme3.bounding.BoundingBox;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import java.util.List;

/**
 * Obiekt klasy <code>Crane</code> reprezentuje żuraw. 
 * @author AleksanderSklorz
 */
public class Crane extends CraneAbstract{
    private BuildingSimulator game = BuildingSimulator.getBuildingSimulator();
    private Node crane;
    private Spatial rack;
    private int heightLevel = 4;
    public Crane(){
        initCraneElements((Node)game.getAssetManager().loadModel("Models/zuraw/zuraw.j3o"));
        GameManager.setCraneRack(rack);
        setArmControl(new CraneArmControl(crane));
        game.getRootNode().attachChild(crane);
    }
    public Crane(int heightLevel){
        this();
        this.heightLevel = heightLevel;
    }
    private RigidBodyControl setProperLocation(Spatial object, Vector3f displacement){
        RigidBodyControl control = object.getControl(RigidBodyControl.class);
        control.setPhysicsLocation(object.getLocalTranslation().add(displacement));
        return control;
    }
    private void initCraneElements(Node parent){
        crane = parent;
        Vector3f craneLocation = new Vector3f(10f, -1f, 0f);
        crane.setLocalTranslation(craneLocation);
        PhysicsSpace physics = game.getBulletAppState().getPhysicsSpace();
        int floorsCount = 0;
        physics.add(setProperLocation(crane.getChild("prop0"), craneLocation));
        physics.add(setProperLocation(crane.getChild("prop1"), craneLocation));
        physics.add(setProperLocation(rack = crane.getChild("rack0"), craneLocation));
        Spatial rack1 = crane.getChild("rack1"),
                rack2 = crane.getChild("rack2"), copyingRack = rack2.clone();
        copyingRack.removeControl(RigidBodyControl.class);
        Vector3f firstRackLocation = rack1.getLocalTranslation(), 
                secondRackLocation = copyingRack.getLocalTranslation();
        float distanceBetweenRacks = secondRackLocation.y - firstRackLocation.y;
        physics.add(setProperLocation(rack1, craneLocation));
        physics.add(setProperLocation(rack2, craneLocation));
        //firstRackLocation.addLocal(-5, distanceBetweenRacks * 3, 0);
        //crane.attachChild(copyingRack);
        //firstRackLocation.addLocal(-5, distanceBetweenRacks * 2, 0);
        for(int i = 3; i < 4; i++){
            //firstRackLocation.addLocal(-5, distanceBetweenRacks * (i - 1), 0);
            secondRackLocation.addLocal(-5, distanceBetweenRacks, 0);
            //copyingRack.setLocalTranslation(firstRackLocation);
            crane.attachChild(copyingRack);
            if(i != heightLevel - 1)
            copyingRack = copyingRack.clone();
            //firstRackLocation.addLocal(-5, distanceBetweenRacks, 0);
        }
        
        
        
        
        //actualTranslation.y += actualY;
        //System.out.println(actualTranslation.y);
        //setProperLocation(copyingRack, actualTranslation);
        /*for(int i = 0; i < craneChildren.size(); i++){
            Spatial child = craneChildren.get(i);
            if(child.getName().startsWith("prop")) 
                physics.add(setProperLocation(child, craneLocation));
            else if(child.getName().startsWith("rack")){
                physics.add(setProperLocation(child, craneLocation));
                if(rack == null) rack = child;
            }
        }*/
    }
}
