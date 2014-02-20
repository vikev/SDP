package sdp.pc.vision.settings;

import java.awt.event.ActionListener;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import sdp.pc.vision.Point2;
import sdp.pc.vision.WorldStatePainter.HighlightMode;

/**
 * Contains all settings that should be stored and read from a backing file.
 * Provides a static member defaultSettings which works with the DEFAULT_SETTINGS_FILE
 * <p>
 * You can import it using: 
 * <br>
 * {@code import static sdp.pc.vision.settings.SettingsManager.defaultSettings}
 * 
 * @author s1141301
 *
 */
@SuppressWarnings("serial")
public class SettingsManager implements java.io.Serializable {

	private static final String DEFAULT_SETTINGS_FILE = "./settings";
	
	public static final SettingsManager defaultSettings = SettingsManager.load();
	
	/**
	 * To add a new colour code do the following:
	 *  - add a new COLOR_CODE_* constant
	 *  - include the text description of the colour; 
	 *    the radio button in the settings menu should send this value as an ActionCommand
	 *  - N_SETTINGS++
	 *  - add the appropriate highlighting mode for the world painter, if any
	 */

	public static final int COLOR_CODE_BALL = 0;
	public static final int COLOR_CODE_PLATE = 1;
	public static final int COLOR_CODE_BLUE = 2;
	public static final int COLOR_CODE_YELLOW = 3;
	public static final int COLOR_CODE_GRAY = 4;

	public static final String[] COLOR_CODES = new String[] { 
		"Ball", 
		"Plate",
		"Blue", 
		"Yellow", 
		"Gray", 
	};
	
	
	public static final int N_SETTINGS = 5;

	public static final int getColorCode(String s) {
		int res = -1;
		for(int i = 0; i < N_SETTINGS; i++)
			if(COLOR_CODES[i].equals(s))
				res = i;
		return res;
	}
	
	static final HighlightMode[] colorCodeToHighlight = new HighlightMode[] {
		HighlightMode.Red,
		HighlightMode.Green,
		HighlightMode.Blue,
		HighlightMode.Yellow,
		HighlightMode.Black,
	};
	
	public static final HighlightMode getHighlightMode(int colorCode) {
		if(colorCode < 0 || colorCode >= N_SETTINGS)
			return HighlightMode.None;
		return colorCodeToHighlight[colorCode];
	}
	
	
	
	
	/* serializable members */

	/**
	 * Our team number. 0 for Yellow, 1 for Blue. TODO: Should be abstracted
	 */
	private int ourTeam = 0;

	/**
	 * Our shooting direction. 0 for Left, 1 for RIght. TODO: Should be
	 * abstracted
	 */
	private int shootingDirection = 0;

	/**
	 * The minimum and maximum values for the different sliders.
	 * colorSettings[colorCode][minOrMax][r,g,b,h,s,v]
	 * <p>
	 * Where:
	 * 		pitchId		the pitch we need the data for
	 * 		colorCode	the COLOR_CODE constant of the object to recognise
	 * 		minOrMax	0 for the minimum, 1 for the maximum
	 * 		r,g,b,h,s,v	the corresponding min/max values for this object
	 */
	private int[][][][] colorSettings = new int[2][N_SETTINGS][2][6];

	/**
	 * The minimum RGB value for a colour to be considered white.
	 */
	private int[] whiteRgbThreshold = new int[2];
	
	/**
	 * The maximum R/G/B delta for a colour to be considered white
	 */
	private int[] whiteRgbDelta = new int[2];
	
	// boundary points
	private Point2[] boundaryPoints = new Point2[] { new Point2(),
			new Point2() };
	
	/* non-serializable (transient) fields */
	
	/**
	 * The filename of this settings instance
	 */
	private transient String fileName;
	
	/**
	 * The pitch number. 0 is the main pitch, 1 is the side pitch. 
	 * ix: decided it should not be saved in the settings file
	 */
	private transient int currentPitchId = 0;

	/**
	 * Whether there are changes unsaved to the file. 
	 */
	private transient boolean hasChanges = false;
	

	public transient ActionListener changeListener;
	
	/**
	 * Registers that a change has occurred. 
	 * <p>
	 * Currently all set methods implement this automatically. 
	 * This one should use this method when updating the values of 
	 * directly exposed arrays (such as the ones returned by get-Min/Max-Values)
	 * 
	 */
	public void registerChange() {
		if(!hasChanges) {
			hasChanges = true;
			if(changeListener != null)
				changeListener.actionPerformed(null);
		}
	}
	
	
	/* getters setters for serializable fields */
	
	//should use registerChange() when using the next 2 methods
	/**
	 * Adds a boundary point for image clipping
	 * 
	 * @param p
	 *            the point to add
	 * @return whether the point was needed'n'received
	 */
	public boolean addBoundary(Point2 p) {
		if (boundaryPoints[0].equals(Point2.EMPTY)) {
			boundaryPoints[0] = p;
			registerChange();
			return true;
		}
		if (boundaryPoints[1].equals(Point2.EMPTY)) {
			boundaryPoints[1] = p;
			registerChange();
			return true;
		}
		System.out
				.println("Warning: Attempted to add boundary to a world state listener with existing boundaries.");
		return false;
	}

	/**
	 * Returns whether there is full boundary information
	 */
	public boolean hasBoundary() {
		return !(boundaryPoints[0].equals(Point2.EMPTY) || boundaryPoints[1]
				.equals(Point2.EMPTY));
	}

	/**
	 * Gets the selected point from the boundary. Makes no guarantees which
	 * point has smaller coordinates Returns an empty point if there is no
	 * boundary information for this index
	 * 
	 * @param i
	 *            the index of the point; should be in the range [0;1]
	 * @return the requested boundary point
	 */
	public Point2 getBoundary(int i) {
		return boundaryPoints[i];
	}

	/**
	 * Resets the region boundary.
	 */
	public void resetBoundary() {
		boundaryPoints[0] = new Point2();
		boundaryPoints[1] = new Point2();
	}
	
	public int[] getMinValues(int colorCode) {
		return colorSettings[currentPitchId][colorCode][0];
	}

	public int[] getMaxValues(int colorCode) {
		return colorSettings[currentPitchId][colorCode][1];
	}
	
	public int getWhiteRgbThreshold() {
		return whiteRgbThreshold[currentPitchId];
	}
	
	public void setWhiteRgbThreshold(int newThreshold) {
		if(whiteRgbThreshold[currentPitchId] != newThreshold) {
			whiteRgbThreshold[currentPitchId] = newThreshold;
			registerChange();
		}
	}
	
	public int getWhiteRgbDelta() {
		return whiteRgbDelta[currentPitchId];
	}
	
	public void setWhiteRgbDelta(int newDelta) {
		if(whiteRgbDelta[currentPitchId] != newDelta) {
			whiteRgbDelta[currentPitchId] = newDelta;
			registerChange();
		}
	}

	public int getCurrentPitchId() {
		return currentPitchId;
	}

	public void setCurrentPitchId(int currentPitchId) {
		this.currentPitchId = currentPitchId;
	}

	/**
	 * Gets our team colour
	 * @return Returns 0 if yellow, 1 if blue
	 */
	public int getOurTeam() {
		return ourTeam;
	}

	/**
	 * Sets our team colour
	 * @param ourTeam The team color where 0 is yellow and 1 is blue
	 */
	public void setOurTeam(int ourTeam) {
		if(this.ourTeam != ourTeam) {
			this.ourTeam = ourTeam;
			registerChange();
		}
	}

	/**
	 * Gets the direction our team should shoot at
	 */
	public int getShootingDirection() {
		return shootingDirection;
	}

	/**
	 * Sets the shooting direction for our team. 
	 * @param shootingDirection The direction we should attack/shoot at - 0 is left, 1 is right
	 */
	public void setShootingDirection(int shootingDirection) {
		if(this.shootingDirection != shootingDirection) {
			this.shootingDirection = shootingDirection;
			registerChange();
		}
	}
	
	/**
	 * Gets whether there are changes unsaved to the backing file
	 */
	public boolean isChanged() {
		return hasChanges;
	}

	/**
	 * Saves the current state of this settings manager to the appropriate file
	 */
	public void save() {
		if(!hasChanges) {
			System.out.println("SettingsManager: no changes to save!");
			return;
		}
		
		System.out.print("SettingsManager: saving... ");
		
		try {
			FileOutputStream fileOut = new FileOutputStream(fileName);
	        ObjectOutputStream streamOut = new ObjectOutputStream(fileOut);
	        
	        streamOut.writeObject(this);
	        
	        streamOut.close();
	        fileOut.close();
	        hasChanges = false;
	        System.out.println("success!");
			if(changeListener != null)
				changeListener.actionPerformed(null);
	        
		}
		catch(IOException i) {
			i.printStackTrace();
	        System.out.println("failed!");
	    }
	}
	
	/**
	 * Creates a new SettingsManager object with default values
	 */
	private SettingsManager() {
		for(int pitchId = 0; pitchId < 2; pitchId++) {
			
			//white values
			whiteRgbThreshold[pitchId] = 100;
			whiteRgbDelta[pitchId] = 70;
			
			//initialise other colour arrays
			colorSettings[pitchId] = new int[N_SETTINGS][][];
			for(int colorCode = 0; colorCode < N_SETTINGS; colorCode++) {
				colorSettings[pitchId][colorCode] = new int[2][];
				colorSettings[pitchId][colorCode][0] = new int[6];
				colorSettings[pitchId][colorCode][1] = new int[6];
				for(int i = 0; i < 6; i++) {
					colorSettings[pitchId][colorCode][0][i] = 0;
					colorSettings[pitchId][colorCode][1][i] = 100;
				}
			}
		}
	}
	
	/**
	 * Returns a new SettingsManager object initialized at the default file path 
	 * as foretold by DEFAULT_SETTINGS_FILE
	 * 
	 * @return 
	 * 		A new SettingsManager containing the data saved in the file, 
	 * 		or a default SettingsManager if unsuccessful 
	 */
	private static SettingsManager load() {
		return load(DEFAULT_SETTINGS_FILE);
	}
	
	/**
	 * Returns a new SettingsManager object initialized at the file path 
	 * provided as an argument
	 * 
	 * @param fileName 
	 * 		The file path for this settings manager
	 * @return 
	 * 		A new SettingsManager containing the data saved in the file, 
	 * 		or a default SettingsManager if unsuccessful 
	 */
	public static SettingsManager load(String fileName) {
		System.out.print("SettingsManager: Opening file '" + fileName + "' ... ");
		SettingsManager val;
		try {
			FileInputStream fileIn = new FileInputStream(fileName);
			ObjectInputStream streamIn = new ObjectInputStream(fileIn);
			val = (SettingsManager) streamIn.readObject();
			streamIn.close();
			fileIn.close();
			System.out.println("success!");
	    }
		catch(IOException i) {
		    System.out.println("file not found! Creating a new one instead. ");
			val = new SettingsManager();
			val.registerChange();
		}
		catch(ClassNotFoundException c) {
		    System.out.println("class info not found! Creating a new one instead. ");
			val = new SettingsManager();
			val.registerChange();
		}
		
		val.fileName = fileName;
		
		return val;
	}
	
}
