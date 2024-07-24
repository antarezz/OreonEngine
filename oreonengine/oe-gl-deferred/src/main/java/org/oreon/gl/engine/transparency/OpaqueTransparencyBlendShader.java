package org.oreon.gl.engine.transparency;

import static org.lwjgl.opengl.GL13.GL_TEXTURE1;
import static org.lwjgl.opengl.GL13.GL_TEXTURE3;
import static org.lwjgl.opengl.GL13.GL_TEXTURE4;
import static org.lwjgl.opengl.GL13.GL_TEXTURE5;
import static org.lwjgl.opengl.GL13.GL_TEXTURE6;
import static org.lwjgl.opengl.GL13.glActiveTexture;

import org.oreon.core.context.BaseOreonContext;
import org.oreon.core.gl.pipeline.GLShaderProgram;
import org.oreon.core.gl.texture.GLTexture;
import org.oreon.core.util.ResourceLoader;

public class OpaqueTransparencyBlendShader extends GLShaderProgram{
	
private static OpaqueTransparencyBlendShader instance = null;
	
	public static OpaqueTransparencyBlendShader getInstance() 
	{
		if(instance == null) 
		{
			instance = new OpaqueTransparencyBlendShader();
		}
		return instance;
	}
	
	protected OpaqueTransparencyBlendShader() {
		
		super();
		
		addComputeShader(ResourceLoader.loadShader("shaders/opaqueTransparencyBlend.comp"));
		
		compileShader();
		
		addUniform("opaqueSceneLightScatteringTexture");
		addUniform("transparencyAlphaMap");
		addUniform("transparencyLayerLightScatteringTexture");
		addUniform("opaqueSceneDepthMap");
		addUniform("transparencyLayerDepthMap");
		addUniform("width");
		addUniform("height");
	}
	
	public void updateUniforms(GLTexture opaqueSceneLightScatteringTexture,
			GLTexture alphaMap, GLTexture transparencyLayerLightScatteringTexture,
			GLTexture opaqueDepthMap, GLTexture transparencyLayerDepthMap)
	{
		setUniformf("width", BaseOreonContext.getConfig().getFrameWidth());
		setUniformf("height", BaseOreonContext.getConfig().getFrameHeight());

		glActiveTexture(GL_TEXTURE1);
		opaqueSceneLightScatteringTexture.bind();
		setUniformi("opaqueSceneLightScatteringTexture", 1);

		glActiveTexture(GL_TEXTURE3);
		alphaMap.bind();
		setUniformi("transparencyAlphaMap", 3);
		
		glActiveTexture(GL_TEXTURE4);
		transparencyLayerLightScatteringTexture.bind();
		setUniformi("transparencyLayerLightScatteringTexture", 4);
		
		glActiveTexture(GL_TEXTURE5);
		opaqueDepthMap.bind();
		setUniformi("opaqueSceneDepthMap", 5);
		
		glActiveTexture(GL_TEXTURE6);
		transparencyLayerDepthMap.bind();
		setUniformi("transparencyLayerDepthMap", 6);
		
	}

}
