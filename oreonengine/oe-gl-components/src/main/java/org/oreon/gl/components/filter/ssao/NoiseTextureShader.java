package org.oreon.gl.components.filter.ssao;

import org.oreon.core.gl.pipeline.GLShaderProgram;
import org.oreon.core.util.ResourceLoader;

public class NoiseTextureShader extends GLShaderProgram {

  private static NoiseTextureShader instance = null;

  public static NoiseTextureShader getInstance() {
    if (instance == null) {

      instance = new NoiseTextureShader();
    }
    return instance;
  }

  protected NoiseTextureShader() {

    super();

    addComputeShader(ResourceLoader.loadShader("shaders/filter/ssao/noise.comp"));
    compileShader();

    for (int i = 0; i < 16; i++) {
      addUniform("randomx[" + i + "]");
    }

    for (int i = 0; i < 16; i++) {
      addUniform("randomy[" + i + "]");
    }
  }

  public void updateUniforms(float[] randomx, float[] randomy) {

    for (int i = 0; i < 16; i++) {
      setUniformf("randomx[" + i + "]", randomx[i]);
    }

    for (int i = 0; i < 16; i++) {
      setUniformf("randomy[" + i + "]", randomy[i]);
    }
  }

}
