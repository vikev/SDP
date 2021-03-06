
// Needs proper files imported - NOT been debugged or tested
// author s1143704

import java.lang.math.*;

public class RelativeLocation{
    private static final double FRAME_ANGLE = 0.0025753285436; //To be calculated using AngleCalculator
    private static final double FRAME_DIST = 0.00085790762050; //To be calculated using DistanceCalculator
    private static final double DEGTORAD = Math.PI/180.0;
    private static final double RADTODEG = 180.0/Math.PI;
    
    public static BigDecimal x = new BigDecimal(0.0);
    public static BigDecimal y = new BigDecimal(0.0); 
    public static BigDecimal facing = new BigDecimal(0.0);
    
    private static double normalize(BigDecimal ang){
    	while(ang.compareTo(new BigDecimal(180.0))==1){
    		ang.subtract(new BigDecimal(360.0));
    	}
    	while(ang.compareTo(new BigDecimal(180.0))==-1){
    		ang.add(new BigDecimal(360.0));
    	}
    	return ang;
    }
    
    public static void reset(){
    	x = new BigDecimal(0.0);
    	y = new BigDecimal(0.0);
    	facing = new BigDecimal(0.0);
    }
    
    public static void addAng(BigDecimal deg){
        facing.add(deg);
    }
    
    public static void addAng(){ //this method adds the ang turned in one FRAME, we'll have to
                                 //calculate this ourselves
        addAng(new BigDecimal(FRAME_ANGLE));
    }
    
    public static void subAng(){
        addAng(new BigDecimal(-FRAME_ANGLE));
    }
    
    public static void addDist(BigDecimal cm){ // Travel distance method will need to factor in gear ratio
    	x.add(cm.multiply(new BigDecimal(Math.cos(facing*DEGTORAD))));
    	y.add(cm.mutliply(new BigDecimal(Math.sin(facing*DEGTORAD))));
    }
    
    public static void addDist(){
        addDist(new BigDecimal(FRAME_DIST));
    }
    
    public static double returnAng(){
    	System.out.println(x);
    	System.out.println(y);
        return normalize(Math.atan2(y,x)*RADTODEG);
    }
    
    public static double returnDist(){
    	System.out.println(x);
    	System.out.println(y);
        return Math.sqrt(x*x + y*y);
    }
}
