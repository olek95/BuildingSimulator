package cranes;

public enum CameraType {
    CABIN("cabinCamStart", "cabinCamEnd"),
    BEHIND("behindCamStart", "behindCamEnd"),
    ARM_CABIN("armCabinCamStart", "armCabinCamEnd"),
    BEHIND_ARM("behindArmCamStart", "behindArmCamEnd"),
    BIRDS_EYE_VIEW("birdsEyeViewCamStart", "birdsEyeViewCamEnd"),
    LOOSE(null, null);

    private String start; 
    private String end;

    private CameraType(String start, String end) {
        this.start = start;
        this.end = end;
    }
    
    public String getStart() { return start; }
}
