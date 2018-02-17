package cranes;

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
    
    public String getStart() { return start; }
}
