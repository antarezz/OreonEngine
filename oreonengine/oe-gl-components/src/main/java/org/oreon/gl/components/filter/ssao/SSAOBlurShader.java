package org.oreon.gl.components.filter.ssao;

import org.oreon.core.gl.pipeline.GLShaderProgram;
import org.oreon.core.util.ResourceLoader;

public class SSAOBlurShader extends GLShaderProgram {

  private static SSAOBlurShader instance = null;

  public static SSAOBlurShader getInstance() {
    if (instance == null) {

      instance = new SSAOBlurShader();
    }
    return instance;
  }

  protected SSAOBlurShader() {

    super();

    addComputeShader(ResourceLoader.loadShader("shaders/filter/ssao/ssao_blur.comp"));
    compileShader();
  }
}
