package org.oreon.examples.gl.oreonworlds.shaders.plants;

import java.util.List;

import org.oreon.core.context.BaseOreonContext;
import org.oreon.core.gl.instanced.GLInstancedCluster;
import org.oreon.core.gl.pipeline.GLShaderProgram;
import org.oreon.core.instanced.InstancedCluster;
import org.oreon.core.scenegraph.Renderable;
import org.oreon.core.util.Constants;
import org.oreon.core.util.ResourceLoader;

public class PalmShadowShader extends GLShaderProgram{

	private static PalmShadowShader instance;

	public static PalmShadowShader getInstance() 
	{
	    if(instance == null) 
	    {
	    	instance = new PalmShadowShader();
	    }
	     return instance;
	}
	
	protected PalmShadowShader()
	{
		super();
		
		addVertexShader(ResourceLoader.loadShader("oreonworlds/shaders/assets/Palm_Shader/Palm01_VS.glsl"));
		addGeometryShader(ResourceLoader.loadShader("oreonworlds/shaders/assets/Palm_Shader/Palm01Shadow_GS.glsl"));
		addFragmentShader(ResourceLoader.loadShader("oreonworlds/shaders/assets/Palm_Shader/Palm01Shadow_FS.glsl"));
		compileShader();
		
		addUniform("clipplane");
		addUniformBlock("worldMatrices");
		addUniformBlock("Camera");
		addUniformBlock("LightViewProjections");
		
		for (int i=0; i<100; i++)
		{
			addUniform("matrixIndices[" + i + "]");
		}
	}
	
	public void updateUniforms(Renderable object){
		
		setUniform("clipplane", BaseOreonContext.getConfig().getClipplane());
		bindUniformBlock("Camera", Constants.CameraUniformBlockBinding);
		bindUniformBlock("LightViewProjections", Constants.LightMatricesUniformBlockBinding);
		
		((GLInstancedCluster) object.getParentNode()).getWorldMatricesBuffer().bindBufferBase(0);
		bindUniformBlock("worldMatrices", 0);
		
		List<Integer> indices = ((InstancedCluster) object.getParentNode()).getHighPolyIndices();
		
		for (int i=0; i<indices.size(); i++)
		{
			setUniformi("matrixIndices[" + i +"]", indices.get(i));	
		}
	}
}
