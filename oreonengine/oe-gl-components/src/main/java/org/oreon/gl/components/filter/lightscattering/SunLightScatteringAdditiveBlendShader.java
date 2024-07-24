package org.oreon.gl.components.filter.lightscattering;

import org.oreon.core.gl.pipeline.GLShaderProgram;
import org.oreon.core.util.ResourceLoader;

public class SunLightScatteringAdditiveBlendShader extends GLShaderProgram {

  private static SunLightScatteringAdditiveBlendShader instance = null;

  public static SunLightScatteringAdditiveBlendShader getInstance() {
    if (instance == null) {
      instance = new SunLightScatteringAdditiveBlendShader();
    }
    return instance;
  }

  protected SunLightScatteringAdditiveBlendShader() {
    super();

    addComputeShader(ResourceLoader.loadShader("shaders/filter/light_scattering/lightScattering_additiveBlend.comp"));

    compileShader();
  }
}
