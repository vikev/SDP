package sdp.pc.gl;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;


import static org.lwjgl.opengl.GL11.*;
import org.lwjgl.opengl.ARBFragmentShader;
import org.lwjgl.opengl.ARBShaderObjects;
import org.lwjgl.opengl.ARBVertexShader;

public class Shader {
	
	int id;
	String filePath;
		
	
	public static int LinkProgram(Shader vertex, Shader fragment) {
		if(vertex.id <= 0 || fragment.id <= 0) {
			return -1;
		}
	
		int progId = ARBShaderObjects.glCreateProgramObjectARB();
		if(progId == 0) {
			return 0;
		}
		
		//add the shaders to the program
		ARBShaderObjects.glAttachObjectARB(progId, vertex.id);
		ARBShaderObjects.glAttachObjectARB(progId, fragment.id);
		
		//link
		ARBShaderObjects.glLinkProgramARB(progId);
		if (ARBShaderObjects.glGetObjectParameteriARB(progId, ARBShaderObjects.GL_OBJECT_LINK_STATUS_ARB)
				== GL_FALSE) {
			System.err.println("Unable to link the program!\n" + getLogInfo(progId));
			return -2;
		}
		
		//validate 
		ARBShaderObjects.glValidateProgramARB(progId);
		if (ARBShaderObjects.glGetObjectParameteriARB(progId, ARBShaderObjects.GL_OBJECT_VALIDATE_STATUS_ARB) 
				== GL_FALSE) {
			System.err.println("Unable to validate the program!\n" + getLogInfo(progId));
			return -3;
		}
		
		return progId;
	}
	
	public Shader(String filePath, boolean vertexShader) {
		this.filePath = filePath;
		int type = vertexShader ? ARBVertexShader.GL_VERTEX_SHADER_ARB : ARBFragmentShader.GL_FRAGMENT_SHADER_ARB;
		String sType = vertexShader ? "vertex" : "fragment";
		int id = 0;
		
		//allocate the shader
		id = ARBShaderObjects.glCreateShaderObjectARB(type);
		if(id == 0) {
			throw new RuntimeException("Error allocating shader!" + getLogInfo(id));
		}
		
		//compile it
		ARBShaderObjects.glShaderSourceARB(id, readFile());
		ARBShaderObjects.glCompileShaderARB(id);
		
		//check if successful
		if(ARBShaderObjects.glGetObjectParameteriARB(id, ARBShaderObjects.GL_OBJECT_COMPILE_STATUS_ARB) 
				== GL_FALSE) {
			throw new RuntimeException("Error creating " + sType + " shader: " + getLogInfo(id));
		}

		this.id = id;
	}
	
	private static String getLogInfo(int obj) {
		return ARBShaderObjects.glGetInfoLogARB(obj, ARBShaderObjects.glGetObjectParameteriARB(obj, ARBShaderObjects.GL_OBJECT_INFO_LOG_LENGTH_ARB));
	}
	
	
	
	private String readFile() 
	{
		try {
			List<String> ls = Files.readAllLines(Paths.get(filePath), StandardCharsets.US_ASCII);
			StringBuilder sb = new StringBuilder();
			for(String l : ls)
				sb.append(l).append('\n');
			return sb.toString();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
}
