package cranes;

/**
 * Typ wyliczeniowy <code>CameraType</code> przechowuje różne typy kamer - kamera
 * ze środka kabiny dźwigu, kamera za pojazdem, kamera z kabiny sterowania ramieniem, 
 * kamera za ramieniem, kamera z lotu ptaka oraz luźna kamera. 
 * @author AleksanderSklorz 
 */
public enum CameraType {
    CABIN("cabinCamStart"),
    BEHIND("behindCamStart"),
    ARM_CABIN("armCabinCamStart"),
    BEHIND_ARM("behindArmCamStart"),
    BIRDS_EYE_VIEW("birdsEyeViewCamStart"),
    LOOSE(null);

    private String start; 

    private CameraType(String start) {
        this.start = start;
    }
    
    /**
     * Zwraca nazwę węzła na pozycji którego znajduje się kamera. 
     * @return nazwa węzła na pozycji którego znajduje się kamera
     */
    public String getStart() { return start; }
}
