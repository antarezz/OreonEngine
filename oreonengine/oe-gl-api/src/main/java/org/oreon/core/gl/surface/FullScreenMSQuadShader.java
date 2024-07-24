package org.oreon.core.gl.surface;

import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.glActiveTexture;

import org.oreon.core.context.BaseOreonContext;
import org.oreon.core.gl.pipeline.GLShaderProgram;
import org.oreon.core.gl.texture.GLTexture;
import org.oreon.core.util.ResourceLoader;

public class FullScreenMSQuadShader extends GLShaderProgram{

	private static FullScreenMSQuadShader instance = null;
	
	public static FullScreenMSQuadShader getInstance() 
	{
	    if(instance == null) 
	    {
	    	instance = new FullScreenMSQuadShader();
	    }
	    return instance;
	}
	
	protected FullScreenMSQuadShader()
	{
		super();

		addVertexShader(ResourceLoader.loadShader("shaders/quad/quad_VS.glsl"));
		addFragmentShader(ResourceLoader.loadShader("shaders/quad/quadMS_FS.glsl"));
		compileShader();
		
		addUniform("texture");
		addUniform("width");
		addUniform("height");
		addUniform("multisamples");
	}
	
	public void updateUniforms(GLTexture texture)
	{
		glActiveTexture(GL_TEXTURE0);
		texture.bind();
		setUniformi("texture", 0);
		
		setUniformi("width", BaseOreonContext.getConfig().getFrameWidth());
		setUniformi("height", BaseOreonContext.getConfig().getFrameHeight());
		setUniformi("multisamples", BaseOreonContext.getConfig().getMultisampling_sampleCount());
	}
}
