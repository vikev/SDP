package sdp.pc.gl;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.color.ColorSpace;

import java.awt.image.BufferedImage;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Hashtable;

import org.lwjgl.opengl.ARBShaderObjects;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL20;

import static org.lwjgl.opengl.GL11.*;

public class GLObjectTracker {
	
	private int width, height;
	private boolean running = false;
	
	private BufferedImage lastFrame;
	private ComponentColorModel glColorModel;
	private ComponentColorModel glAlphaColorModel;
	
	private int shaderProgram = 0;

	private Shader vertexShader;
	private Shader fragmentShader;
	
	public GLObjectTracker() {
		this(640, 480);
	}
	
	public GLObjectTracker(int width, int height) {

		
		this.width = width;
		this.height = height;
		glAlphaColorModel = new ComponentColorModel(ColorSpace.getInstance(ColorSpace.CS_sRGB),
                new int[] {8,8,8,8},
                true,
                false,
                ComponentColorModel.TRANSLUCENT,
                DataBuffer.TYPE_BYTE);

		glColorModel = new ComponentColorModel(ColorSpace.getInstance(ColorSpace.CS_sRGB),
                new int[] {8,8,8,0},
                false,
                false,
                ComponentColorModel.OPAQUE,
                DataBuffer.TYPE_BYTE);
	}
	
	private void loadShaders() {
		vertexShader = new Shader("src/sdp/pc/gl/vertex.glsl", true);
		fragmentShader = new Shader("src/sdp/pc/gl/pixel.glsl", false);
		shaderProgram = Shader.LinkProgram(vertexShader, fragmentShader);
		
		if(shaderProgram <= 0)
			System.out.println("Shaders disabled.");
	}
	
	public void run() {
		if(running)
			return;
		running = true;
		
		System.out.print("Showing window..");
		initialize();
		loadShaders();
		System.out.println("done!");
		while(running) {
			if(Display.isCloseRequested())
				running = false;
			if(Display.wasResized()) {
				glViewport(0, 0, Display.getWidth(), Display.getHeight());
			}
			if(!draw())
				Thread.yield();
			else
				Display.update();
		}
		
		Display.destroy();
	}
	
	
	private void initialize() {
	    try{
	        Display.setDisplayMode(new DisplayMode(width, height));
	        Display.setVSyncEnabled(true);
	        Display.setTitle("ObjectTracker");
	        Display.setResizable(true);
	        Display.create();
	    }catch(Exception e){
	        System.out.println("Error setting up display");
	        System.exit(0);
	    }
	    glViewport(0,0,width,height);
	    
	    glMatrixMode(GL_PROJECTION);
	    glLoadIdentity();
	    glOrtho(0, 1, 0, 1, -1, 1);
	    
	    glMatrixMode(GL_MODELVIEW);
	    glLoadIdentity();
	    
	    GL13.glActiveTexture(0);
	    GL13.glClientActiveTexture(0);
	    glShadeModel(GL_SMOOTH);
	    glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
	    glClearDepth(1.0f);
	    glDepthFunc(GL_LEQUAL);
	    glEnable(GL_DEPTH_TEST);
	    glEnable(GL_TEXTURE_2D);
	    glHint(GL_PERSPECTIVE_CORRECTION_HINT, GL_NICEST);
		System.out.println("Object Tracker @ OpenGL " + glGetString(GL_VERSION));
	}

	
	private boolean frameChanged = false;

	public void setLastFrame(BufferedImage lastFrame) {
		if(!lastFrame.equals(this.lastFrame)) {
			this.lastFrame = lastFrame;
			frameChanged = true;
		}
	}

	private boolean draw() {
		if(!frameChanged)
			return false;
		frameChanged = false;
		
		//clear the screen
		glMatrixMode(GL_MODELVIEW);
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
		glClearColor(0.8f, 0.4f, 0.4f, 1f);
		glLoadIdentity();

		//get the texture
		int texId = loadTexture(lastFrame);
		
		//use the shaders
		if(shaderProgram > 0) {
			ARBShaderObjects.glUseProgramObjectARB(shaderProgram);
			
			//set uniforms
			int texLoc = GL20.glGetUniformLocation(shaderProgram, "tex");
			GL20.glUniform1i(texLoc, 0);
		}

		glBindTexture(GL_TEXTURE_2D, texId);
		
		//draw quad w/ texture on it
		glBegin(GL_QUADS);
		glTexCoord2f(0, 0); glVertex3f(0, 0, 0);
		glTexCoord2f(0, 1); glVertex3f(0, 1, 0);
		glTexCoord2f(1, 1); glVertex3f(1, 1, 0);
		glTexCoord2f(1, 0); glVertex3f(1, 0, 0);
		glEnd();
		
		//release textures
		unloadTexture(texId);
		
		//release shaders
		if(shaderProgram > 0)
			ARBShaderObjects.glUseProgramObjectARB(0);
		
		return true;
	}
	
	private int loadTexture(BufferedImage img) {
        int srcPixelFormat;		
		
		int id = glGenTextures();	
		glBindTexture(GL_TEXTURE_2D, id);

        
        int texWidth = img.getWidth();
        int texHeight = img.getHeight();
        
        ByteBuffer imageBuffer;
        WritableRaster raster;
        BufferedImage texImage;
        


        // create a raster that can be used by OpenGL as a source
        // for a texture
        if (img.getColorModel().hasAlpha()) {
            srcPixelFormat = GL_RGBA;
            raster = Raster.createInterleavedRaster(DataBuffer.TYPE_BYTE,texWidth,texHeight,4,null);
            texImage = new BufferedImage(glAlphaColorModel,raster,false,new Hashtable());
        } else {
            srcPixelFormat = GL_RGB;
            raster = Raster.createInterleavedRaster(DataBuffer.TYPE_BYTE,texWidth,texHeight,3,null);
            texImage = new BufferedImage(glColorModel,raster,false,new Hashtable());
        }

        // copy the source image into the produced image
        Graphics g = texImage.getGraphics();
        g.setColor(new Color(0f,0f,0f,0f));
        g.fillRect(0,0,texWidth,texHeight);
        g.drawImage(img,0,0,null);

        // build a byte buffer from the temporary image
        // that be used by OpenGL to produce a texture.
        byte[] data = ((DataBufferByte) texImage.getRaster().getDataBuffer()).getData();

        imageBuffer = ByteBuffer.allocateDirect(data.length);
        imageBuffer.order(ByteOrder.nativeOrder());
        imageBuffer.put(data, 0, data.length);
        imageBuffer.flip();

        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
		glTexImage2D(GL_TEXTURE_2D, 0, 
				GL_RGB, texWidth, texHeight, 0, srcPixelFormat, GL_UNSIGNED_BYTE, imageBuffer);
	    
		return id;
	}
	
	private void unloadTexture(int id) {
		glDeleteTextures(id);
	}
	
}
