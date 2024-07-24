package org.oreon.gl.components.atmosphere;

import org.oreon.core.context.ContextHolder;
import org.oreon.core.gl.pipeline.GLShaderProgram;
import org.oreon.core.scenegraph.Renderable;
import org.oreon.core.util.ResourceLoader;

public class AtmosphericScatteringShader extends GLShaderProgram {

  private static AtmosphericScatteringShader instance = null;

  public static AtmosphericScatteringShader getInstance() {
    if (instance == null) {
      instance = new AtmosphericScatteringShader();
    }
    return instance;
  }

  protected AtmosphericScatteringShader() {
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

  public void updateUniforms(Renderable object) {
    setUniform("m_MVP", object.getWorldTransform().getModelViewProjectionMatrix());
    setUniform("m_Projection", ContextHolder.getContext().getCamera().getProjectionMatrix());
    setUniform("m_View", ContextHolder.getContext().getCamera().getViewMatrix());
    setUniform("v_Sun", ContextHolder.getContext().getConfig().getSunPosition().mul(-1));
    setUniformf("horizonVerticalShift", ContextHolder.getContext().getConfig().getHorizonVerticalShift());
    setUniformf("reflectionVerticalShift", ContextHolder.getContext().getConfig().getHorizonReflectionVerticalShift());
    setUniformf("r_Sun", ContextHolder.getContext().getConfig().getSunRadius());
    setUniformi("width", ContextHolder.getContext().getConfig().getFrameWidth());
    setUniformi("height", ContextHolder.getContext().getConfig().getFrameHeight());
    setUniformi("isReflection", ContextHolder.getContext().getConfig().isRenderReflection() ? 1 : 0);
    setUniformf("bloom", ContextHolder.getContext().getConfig().getAtmosphereBloomFactor());
  }

}
