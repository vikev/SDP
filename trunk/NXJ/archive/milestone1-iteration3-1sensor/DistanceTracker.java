import java.lang.Math.*;

import lejos.nxt.LCD;
import lejos.robotics.navigation.DifferentialPilot;
import lejos.util.Delay;

public class DistanceTracker {
	
	public static final double FRAME_DIST =  0.13673655;
	
	private static final double DEGTORAD = Math.PI / 180.0;
    private static final double RADTODEG = 180.0 / Math.PI;
    public static double dist = 0.0;
    public static boolean distReach = false;
    
    public static void reset(){
    	dist = 0.0;
    }
    
    public static void addDist(double cm){
        dist+=cm;
    }
    
    public static void addDist(){
    	addDist(FRAME_DIST);
    }
}

