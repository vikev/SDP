
// Needs proper files imported - NOT been debugged or tested
// author s1143704

import java.lang.Math.*;

public class RelativeLocation{
    private static final double FRAME_ANGLE = 1.0; //To be calculated using AngleCalculator
    private static final double FRAME_DIST = 1.0;  //To be calculated using DistanceCalculator
    private static final double DEGTORAD = Math.PI/180.0;
    private static final double RADTODEG = 180.0/Math.PI;
    
    public static double x = 0.0, y = 0.0, facing = 0.0;
    
    public static void addAng(double deg){
        facing+=deg;
    }
    
    public static void addAng(){ //this method adds the ang turned in one FRAME, we'll have to
                                 //calculate this ourselves
        addAng(FRAME_ANGLE);
    }
    
    public static void subAng(){
        addAng(-FRAME_ANGLE);
    }
    
    public static void addDist(double cm){ // Travel distance method will need to factor in gear ratio
        x += cm * Math.cos(facing*DEGTORAD);
        y += cm * Math.sin(facing*DEGTORAD);
    }
    
    public static void addDist(){
        addDist(FRAME_DIST);
    }
    
    public static double returnAng(){
        return Math.atan2(y,x)*RADTODEG + 180.0;
    }
    
    public static double returnDist(){
        return Math.sqrt(x*x + y*y);
    }
}
