package org.oreon.gl.components.filter.lightscattering;

import org.oreon.core.context.ContextHolder;
import org.oreon.core.gl.pipeline.GLShaderProgram;
import org.oreon.core.math.Matrix4f;
import org.oreon.core.util.Constants;
import org.oreon.core.util.ResourceLoader;

public class SunLightScatteringShader extends GLShaderProgram {

  private static SunLightScatteringShader instance = null;

  public static SunLightScatteringShader getInstance() {
    if (instance == null) {
      instance = new SunLightScatteringShader();
    }
    return instance;
  }

  protected SunLightScatteringShader() {
    super();

    addComputeShader(ResourceLoader.loadShader("shaders/filter/light_scattering/lightScattering.comp"));

    compileShader();

    addUniform("sunWorldPosition");
    addUniform("windowWidth");
    addUniform("windowHeight");
    addUniform("viewProjectionMatrix");
    addUniform("num_samples");
    addUniform("decay");
  }

  public void updateUniforms(int windowWidth, int windowHeight, Matrix4f viewProjectionMatrix) {

    setUniformf("windowWidth", windowWidth);
    setUniformf("windowHeight", windowHeight);
    setUniform("viewProjectionMatrix", viewProjectionMatrix);
    setUniform("sunWorldPosition", ContextHolder.getContext().getConfig().getSunPosition().mul(-Constants.ZFAR));
    setUniformi("num_samples", ContextHolder.getContext().getConfig().getLightscatteringSampleCount());
    setUniformf("decay", ContextHolder.getContext().getConfig().getLightscatteringDecay());
  }
}
