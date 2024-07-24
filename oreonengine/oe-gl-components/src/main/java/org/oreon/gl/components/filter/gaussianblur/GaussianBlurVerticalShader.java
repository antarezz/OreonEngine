package org.oreon.gl.components.filter.gaussianblur;

import org.oreon.core.gl.pipeline.GLShaderProgram;
import org.oreon.core.util.ResourceLoader;

public class GaussianBlurVerticalShader extends GLShaderProgram {

  private static GaussianBlurVerticalShader instance = null;

  public static GaussianBlurVerticalShader getInstance() {
    if (instance == null) {
      instance = new GaussianBlurVerticalShader();
    }
    return instance;
  }

  protected GaussianBlurVerticalShader() {
    super();

    addComputeShader(ResourceLoader.loadShader("shaders/filter/gaussian_blur/vertical_gaussian_blur.comp"));

    compileShader();
  }
}
