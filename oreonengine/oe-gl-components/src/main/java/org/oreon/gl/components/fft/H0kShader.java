package org.oreon.gl.components.fft;

import org.oreon.core.gl.pipeline.GLShaderProgram;
import org.oreon.core.math.Vec2f;
import org.oreon.core.util.ResourceLoader;

public class H0kShader extends GLShaderProgram {

  private static H0kShader instance = null;

  public static H0kShader getInstance() {
    if (instance == null) {
      instance = new H0kShader();
    }
    return instance;
  }

  protected H0kShader() {
    super();

    addComputeShader(ResourceLoader.loadShader("shaders/fft/h0k.comp"));
    compileShader();

    addUniform("N");
    addUniform("L");
    addUniform("amplitude");
    addUniform("direction");
    addUniform("intensity");
    addUniform("l");
//		addUniform("alignment");
    addUniform("noise_r0");
    addUniform("noise_i0");
    addUniform("noise_r1");
    addUniform("noise_i1");
  }

  public void updateUniforms(int N, int L, float amplitude, Vec2f direction, float alignment,
      float intensity, float capillarSupressFactor) {
    setUniformi("N", N);
    setUniformi("L", L);
    setUniformf("amplitude", amplitude);
    setUniformf("intensity", intensity);
    setUniform("direction", direction);
    setUniformf("l", capillarSupressFactor);
//		setUniformf("alignment", alignment);
  }

  public void updateUniforms(int texture0, int texture1, int texture2, int texture3) {
    setUniformi("noise_r0", texture0);
    setUniformi("noise_i0", texture1);
    setUniformi("noise_r1", texture2);
    setUniformi("noise_i1", texture3);
  }

}
