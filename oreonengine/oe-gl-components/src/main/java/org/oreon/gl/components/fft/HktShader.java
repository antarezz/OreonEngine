package org.oreon.gl.components.fft;

import org.oreon.core.gl.pipeline.GLShaderProgram;
import org.oreon.core.util.ResourceLoader;

public class HktShader extends GLShaderProgram {

  private static HktShader instance = null;

  public static HktShader getInstance() {
    if (instance == null) {
      instance = new HktShader();
    }
    return instance;
  }

  protected HktShader() {
    super();

    addComputeShader(ResourceLoader.loadShader("shaders/fft/hkt.comp"));
    compileShader();

    addUniform("N");
    addUniform("L");
    addUniform("t");
  }

  public void updateUniforms(int L, int N, float t) {
    setUniformi("N", N);
    setUniformi("L", L);
    setUniformf("t", t);
  }

}
