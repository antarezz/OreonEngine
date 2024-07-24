package org.oreon.gl.components.atmosphere;

import org.oreon.core.context.ContextHolder;
import org.oreon.core.gl.pipeline.GLShaderProgram;
import org.oreon.core.scenegraph.Renderable;
import org.oreon.core.util.Constants;
import org.oreon.core.util.ResourceLoader;

public class AtmosphereShader extends GLShaderProgram {

  private static AtmosphereShader instance = null;

  public static AtmosphereShader getInstance() {
    if (instance == null) {
      instance = new AtmosphereShader();
    }
    return instance;
  }

  protected AtmosphereShader() {
    super();

    addVertexShader(ResourceLoader.loadShader("shaders/atmosphere/atmosphere.vert"));
    addFragmentShader(ResourceLoader.loadShader("shaders/atmosphere/atmosphere.frag"));
    compileShader();

    addUniform("modelViewProjectionMatrix");
    addUniform("modelMatrix");
    addUniform("m_ViewProjection");
    addUniform("v_SunWorld");
    addUniform("r_Sun");
    addUniform("width");
    addUniform("height");
    addUniform("isReflection");
    addUniform("bloom");
  }

  public void updateUniforms(Renderable object) {
    setUniform("modelViewProjectionMatrix", object.getWorldTransform().getModelViewProjectionMatrix());
    setUniform("modelMatrix", object.getWorldTransform().getWorldMatrix());
    setUniform("m_ViewProjection", ContextHolder.getContext().getCamera().getOriginViewProjectionMatrix());
    setUniform("v_SunWorld", ContextHolder.getContext().getConfig().getSunPosition().mul(-Constants.ZFAR));
    setUniformf("r_Sun", ContextHolder.getContext().getConfig().getSunRadius());
    setUniformi("width", ContextHolder.getContext().getConfig().getFrameWidth());
    setUniformi("height", ContextHolder.getContext().getConfig().getFrameHeight());
    setUniformi("isReflection", ContextHolder.getContext().getConfig().isRenderReflection() ? 1 : 0);
    setUniformf("bloom", ContextHolder.getContext().getConfig().getAtmosphereBloomFactor());
  }
}
