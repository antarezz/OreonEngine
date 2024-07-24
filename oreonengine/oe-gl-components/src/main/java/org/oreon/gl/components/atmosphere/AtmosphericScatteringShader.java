package org.oreon.gl.components.atmosphere;

import org.oreon.core.context.BaseOreonContext;
import org.oreon.core.gl.pipeline.GLShaderProgram;
import org.oreon.core.scenegraph.Renderable;
import org.oreon.core.util.ResourceLoader;

public class AtmosphericScatteringShader extends GLShaderProgram{

	private static AtmosphericScatteringShader instance = null;
	
	public static AtmosphericScatteringShader getInstance() 
	{
		if(instance == null) 
		{
			instance = new AtmosphericScatteringShader();
		}
		return instance;
	}
		
	protected AtmosphericScatteringShader()
	{
		super();
		
		addVertexShader(ResourceLoader.loadShader("shaders/atmosphere/atmospheric_scattering.vert"));
		addFragmentShader(ResourceLoader.loadShader("shaders/atmosphere/atmospheric_scattering.frag"));
		compileShader();
			
		addUniform("m_MVP");
		addUniform("m_Projection");
		addUniform("m_View");
		addUniform("v_Sun");
		addUniform("r_Sun");
		addUniform("width");
		addUniform("height");
		addUniform("isReflection");
		addUniform("horizonVerticalShift");
		addUniform("reflectionVerticalShift");
		addUniform("bloom");
	}
	
	public void updateUniforms(Renderable object)
	{
		setUniform("m_MVP", object.getWorldTransform().getModelViewProjectionMatrix());
		setUniform("m_Projection", BaseOreonContext.getCamera().getProjectionMatrix());
		setUniform("m_View", BaseOreonContext.getCamera().getViewMatrix());
		setUniform("v_Sun", BaseOreonContext.getConfig().getSunPosition().mul(-1));
		setUniformf("horizonVerticalShift", BaseOreonContext.getConfig().getHorizonVerticalShift());
		setUniformf("reflectionVerticalShift", BaseOreonContext.getConfig().getHorizonReflectionVerticalShift());
		setUniformf("r_Sun", BaseOreonContext.getConfig().getSunRadius());
		setUniformi("width", BaseOreonContext.getConfig().getFrameWidth());
		setUniformi("height", BaseOreonContext.getConfig().getFrameHeight());
		setUniformi("isReflection", BaseOreonContext.getConfig().isRenderReflection() ? 1 : 0);
		setUniformf("bloom", BaseOreonContext.getConfig().getAtmosphereBloomFactor());
	}

}
