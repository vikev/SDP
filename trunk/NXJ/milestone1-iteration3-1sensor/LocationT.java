import java.lang.Math.*;

import lejos.nxt.LCD;
import lejos.robotics.navigation.DifferentialPilot;
import lejos.util.Delay;

public class LocationT {
	
	public static final double FRAME_DIST =  0.13673655;
	
	private static final double DEGTORAD = Math.PI / 180.0;
    private static final double RADTODEG = 180.0 / Math.PI;
    public static double dist = 0.0;
    public static boolean distReach = false;
    
    
    public static void addDist(double cm){ // Travel distance method will need to factor in gear ratio
        dist+=cm;
    }
    
	
}