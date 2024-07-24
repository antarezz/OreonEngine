package org.oreon.gl.components.filter.bloom;

import org.oreon.core.gl.pipeline.GLShaderProgram;
import org.oreon.core.util.ResourceLoader;

public class BloomVerticalBlurShader extends GLShaderProgram {

  private static BloomVerticalBlurShader instance = null;

  public static BloomVerticalBlurShader getInstance() {
    if (instance == null) {
      instance = new BloomVerticalBlurShader();
    }
    return instance;
  }

  protected BloomVerticalBlurShader() {
    super();

    addComputeShader(ResourceLoader.loadShader("shaders/filter/bloom/bloom_verticalGaussianBlur.comp"));
    compileShader();
  }
}
