package org.oreon.gl.components.water;

import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.glActiveTexture;

import org.oreon.core.gl.context.GLOreonContext;
import org.oreon.core.gl.pipeline.GLShaderProgram;
import org.oreon.core.gl.texture.GLTexture;
import org.oreon.core.util.ResourceLoader;

public class UnderWaterShader extends GLShaderProgram{

	private static UnderWaterShader instance = null;
	
	public static UnderWaterShader getInstance() 
	{
	    if(instance == null) 
	    {
	    	instance = new UnderWaterShader();
	    }
	      return instance;
	}
	
	protected UnderWaterShader()
	{
		super();
		
		addComputeShader(ResourceLoader.loadShader("shaders/water/underwater.comp"));
		
		compileShader();
		
		addUniform("sceneDepthMap");
		addUniform("waterRefractionColor");
	}
	
	public void updateUniforms(GLTexture sceneDepthMap) {
		glActiveTexture(GL_TEXTURE0);
		sceneDepthMap.bind();
		setUniformi("sceneDepthMap", 0);
		setUniform("waterRefractionColor", GLOreonContext.getResources().getWaterConfig().getBaseColor());
	}
}
