package cranes;

import buildingsimulator.BuildingSimulator;
import buildingsimulator.GameManager;
import static buildingsimulator.GameManager.addNewCollisionShapeToCompound;
import static buildingsimulator.GameManager.createPhysics;
import static buildingsimulator.GameManager.joinsElementToOtherElement;
import static buildingsimulator.GameManager.moveWithScallingObject;
import com.jme3.bounding.BoundingBox;
import com.jme3.bullet.collision.PhysicsCollisionGroupListener;
import com.jme3.bullet.collision.PhysicsCollisionObject;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.collision.shapes.CompoundCollisionShape;
import com.jme3.bullet.collision.shapes.infos.ChildCollisionShape;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.bullet.joints.HingeJoint;
import com.jme3.bullet.util.CollisionShapeFactory;
import com.jme3.collision.CollisionResults;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Cylinder;

/**
 * Klasa <code>Hook</code> jest klasą abstrakcji dla wszystkich haków w grze. 
 * @author AleksanderSklorz
 */
public abstract class Hook {
    private Node ropeHook;
    private Spatial hook, hookHandle, recentlyHitObject, attachedObject = null;
    private float actualLowering = 1f, speed;
    private Vector3f hookDisplacement;
    private HingeJoint lineAndHookHandleJoint = null, buildingMaterialJoint;
    public Hook(Node ropeHook, Spatial hookHandle, float speed){
        this.ropeHook = ropeHook;
        hook = ropeHook.getChild("hook");
        this.hookHandle = hookHandle;
        this.speed = speed;
    } 
    
    /**
     * Tworzy obiekt kolizji dla haków. Ustawia on ostatnio dotkniety przez hak 
     * obiekt. 
     * @return obiekt kolizji dla haków 
     */
    public static PhysicsCollisionGroupListener createCollisionListener(){
        return new PhysicsCollisionGroupListener(){
            @Override
            public boolean collide(PhysicsCollisionObject nodeA, PhysicsCollisionObject nodeB){
                Object a = nodeA.getUserObject(), b = nodeB.getUserObject();
                Spatial aSpatial = (Spatial)a, bSpatial = (Spatial)b;
                String aName = aSpatial.getName(), bName = bSpatial.getName();
                if(!isProperCollisionGroup(bSpatial)) return false;
                if(aName.equals("ropeHook") && !bName.equals("hookHandle")){
                    setCollision(bSpatial);
                }
                else if(bName.equals("ropeHook") && !aName.equals("hookHandle")){
                    setCollision(aSpatial);
                }
                return true;
            }
        };
    }
    
    /**
     * Podnosi hak. 
     */
    public void heighten(){
        changeHookPosition(new Vector3f(1f, actualLowering -= speed, 1f),
                true);
    }
    
    /**
     * Łączy hak z dotknieym obiektem. 
     */
    public void attach(){
        if(buildingMaterialJoint == null){
            attachedObject = recentlyHitObject;
            addSafetyRopes(hook, attachedObject);
            buildingMaterialJoint = joinsElementToOtherElement(buildingMaterialJoint,
                    hook, attachedObject, Vector3f.ZERO, new Vector3f(0, 
                    ((BoundingBox)attachedObject.getWorldBound()).getYExtent()
                    + ((BoundingBox)hook.getWorldBound()).getYExtent() + 0.2f, 0)); // 1.5 mobil, 1.2 zuraw
            //addSafetyRopes(hook, attachedObject);
        }
    }
    
    public void addSafetyRopes(Spatial hook, Spatial attachedObject){
        Vector3f max = ((BoundingBox)attachedObject.getWorldBound()).getMax(null),
                end = hook.getWorldTranslation();
        float length = max.distance(end);
        //end.subtract(0f, 0.5f, 0f);
        BuildingSimulator game = BuildingSimulator.getBuildingSimulator();
        Geometry[] ropes = new Geometry[4];
        Vector3f[] ropesLocations = new Vector3f[4];
        for(int i = 0; i < ropes.length; i++){
            ropes[i] = new Geometry("Cylinder", new Cylinder(4, 8, 0.02f, length));
            ropesLocations[i] = FastMath.interpolateLinear(0.5f, max, end);
        }
        Geometry rope = new Geometry("Cylinder", new Cylinder(4, 8, 0.02f, 
                max.distance(end)));  
        Vector3f ropeLocation = FastMath.interpolateLinear(0.5f, max, end);
        Material mat = new Material(game.getAssetManager(),
                "Common/MatDefs/Misc/Unshaded.j3md");  
        mat.setColor("Color", ColorRGBA.Black);   
        rope.setMaterial(mat);         
        game.getBulletAppState().getPhysicsSpace().remove(attachedObject.getControl(0));
        attachedObject.removeControl(RigidBodyControl.class);
        ((Node)attachedObject).attachChild(rope);
        CompoundCollisionShape wallRopesShape = GameManager
                .createCompound((Node)attachedObject, new String[] {"Box", "Cylinder"});
        GameManager.createPhysics(wallRopesShape, attachedObject, 0.00001f, false);
        
        
        /*CompoundCollisionShape wallRopesShape = new CompoundCollisionShape(); 
        CollisionShape wallShape = CollisionShapeFactory
                .createDynamicMeshShape(((Node)attachedObject).getChild("Box")),
                ropeShape = CollisionShapeFactory
                .createDynamicMeshShape(((Node)attachedObject).getChild("Cylinder"));
        wallRopesShape.addChildShape(wallShape, Vector3f.ZERO);
        wallRopesShape.addChildShape(ropeShape, Vector3f.ZERO);
        RigidBodyControl wallRopesControl = new RigidBodyControl(wallRopesShape, 0.00001f);
        attachedObject.addControl(wallRopesControl); 
        game.getBulletAppState().getPhysicsSpace().add(wallRopesControl);*/
        rope.setLocalTranslation(ropeLocation.subtract(attachedObject
                .getControl(RigidBodyControl.class).getPhysicsLocation()));
        rope.lookAt(end, Vector3f.UNIT_Y);
        ChildCollisionShape gotShape = wallRopesShape.getChildren().get(1);
        gotShape.location = (rope.getLocalTranslation());
        gotShape.rotation = rope.getLocalRotation().toRotationMatrix();
    }
    
    /**
     * Odłącza od haka przyczepiony obiekt. 
     */
    public void detach(){
        BuildingSimulator.getBuildingSimulator().getBulletAppState().getPhysicsSpace()
                .remove(buildingMaterialJoint);
        attachedObject = null;
    }
    
    /**
     * Zwraca ostatnio dotknięty obiekt. 
     * @return ostatnio dotknięy obiekt
     */
    public Spatial getRecentlyHitObject(){ return recentlyHitObject; }
    
    /**
     * Ustawia ostatnio dotknięty przez hak obiekt.
     * @param object dotknięty obiekt 
     */
    public void setRecentlyHitObject(Spatial object){ recentlyHitObject = object; }
    
    /**
     * Zwraca wartość określającą jak bardzo opuszczony jest hak. 
     * @return wartość opuszczenia haka 
     */
    public float getActualLowering(){ return actualLowering; }
    
    /**
     * Zwraca uchwyt do jakiego przyczepiona jest lina. 
     * @return uchwyt liny
     */
    public Spatial getHookHandle(){ return hookHandle; }
    
    /**
     * Zwraca węzeł z hakiem z liniami. 
     * @return węzeł z hakiem z liniami 
     */
    public Node getRopeHook(){ return ropeHook; }
    
    /**
     * Zwraca zaczepiony obiekt. 
     * @return zaczepiony obiekt 
     */
    public Spatial getAttachedObject() { return attachedObject; }
    
    /**
     * Zwraca przesunięcie haka po opuszczeniu liny. 
     * @return przesunięcie haka 
     */
    public Vector3f getHookDisplacement() { return hookDisplacement; }
    
    /**
     * Ustawia przesunięcie haka po opuszczeniu liny. 
     * @param displacement przesunięcie haka 
     */
    public void setHookDisplacement(Vector3f displacement){
        this.hookDisplacement = displacement;
    }
    
    /**
     * Zwraca hak. 
     * @return hak 
     */
    public Spatial getHook() { return hook; }
    
    /**
     * Opuszcza hak, jeśli nic nie znajduje się pod nim. W przeciwnym razie 
     * hak nie zmienia pozycji. 
     * @param results wyniki kolizji, dzięki którym sprawdza się w ilu punktach 
     * jest kolizja z danym obiektem 
     */
    protected void lower(CollisionResults results){
        // obniża hak, jeśli w żadnym punkcie z dołu nie dotyka jakiegoś obiektu
        if(results.size() == 0){
            changeHookPosition(new Vector3f(1f, actualLowering += speed, 1f),
                    false);
            recentlyHitObject = null;
        }
    }
    
    /**
     * Dołącza do podanego złożonego kształtu kolizji kształty kolizji innych 
     * elementów haka, wspólnych dla wszystkich haków. Ponadto dołącza fizykę 
     * dla tego obiektu do gry, ustawia odpowiednie grupy kolizji, a także 
     * łączy hak z uchwytem na hak. 
     * @param ropeHookCompound złożony kształt kolizji do którego dołącza się 
     * kształty kolizji dla haka. 
     * @param distanceHookHandleAndRopeHook wektor decydujący w jakiej odległości 
     * ma być zawieszony hak od uchwytu na hak. 
     */
    protected  void addHookPhysics(CompoundCollisionShape ropeHookCompound,
            Vector3f distanceHookHandleAndRopeHook){
        addNewCollisionShapeToCompound(ropeHookCompound, (Node)hook, ((Node)hook).getChild(0).getName(),
                hook.getLocalTranslation(), hook.getLocalRotation());
        createPhysics(ropeHookCompound, ropeHook, 4f, false);
        RigidBodyControl ropeHookControl = ropeHook.getControl(RigidBodyControl.class);
        ropeHookControl.setCollisionGroup(2); 
        ropeHookControl.addCollideWithGroup(1); // tylko mobilny???
        ropeHookControl.setCollideWithGroups(3); // tylko mobilny???
        lineAndHookHandleJoint = joinsElementToOtherElement(lineAndHookHandleJoint,
                hookHandle, ropeHook, Vector3f.ZERO, distanceHookHandleAndRopeHook);
    }
    
    /**
     * Podnosi lub opuszcza hak. 
     * @param scallingVector wektor o jaki ma być przesunięty hak 
     * @param heightening true jeśli podnosimy hak, false w przeciwnym przypadku 
     */
    protected void changeHookPosition(Vector3f scallingVector, boolean heightening){
        if(attachedObject != null){
            moveWithScallingObject(heightening, hookDisplacement, scallingVector, 
                    getRopes(), hook, attachedObject);
        }else{
            moveWithScallingObject(heightening, hookDisplacement, scallingVector, 
                    getRopes(), hook);
        }
        createRopeHookPhysics();
    }
    
    /**
     * Metoda abstrakcyjna którą musi nadpisać każda klasa haka, aby określić 
     * dodatkowe zasady sprawdzania kolizji podczas opuszczania haka. 
     */
    protected abstract void lower();
    
    /**
     * Tworzy fizykę dla całego haka wraz z linami go trzymającymi. 
     */
    protected abstract void createRopeHookPhysics();
    
    /**
     * Zwraca tablicę wszystkich lin trzymających hak. 
     * @return tablica lin 
     */
    protected abstract Node[] getRopes();
    
    private static void setCollision(Spatial b){
        Hook actualHook = GameManager.findActualUnit().getHook();
        Spatial object = actualHook.recentlyHitObject, attached = actualHook.attachedObject;
        /* zabezpiecza przypadek gdy hak dotyka jednocześnie elementu pionowego
        i poziomego*/
        if((object == null || ((BoundingBox)object.getWorldBound()).getMax(null).y 
                > ((BoundingBox)b.getWorldBound()).getMax(null).y) && attached == null)
            actualHook.recentlyHitObject = b;
    }
    
    private static boolean isProperCollisionGroup(Spatial b){
        // PhysicsCollisionObject bo control nie musi być tylko typu RigidBodyControl
        int collisionGroup = ((PhysicsCollisionObject)b.getControl(0)).getCollisionGroup();
        return collisionGroup != 4;
    }
}
