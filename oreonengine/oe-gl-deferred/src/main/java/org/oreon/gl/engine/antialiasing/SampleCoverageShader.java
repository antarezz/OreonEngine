package org.oreon.gl.engine.antialiasing;

import org.oreon.core.context.ContextHolder;
import org.oreon.core.gl.pipeline.GLShaderProgram;
import org.oreon.core.util.ResourceLoader;

public class SampleCoverageShader extends GLShaderProgram {

  private static SampleCoverageShader instance = null;

  public static SampleCoverageShader getInstance() {
    if (instance == null) {
      instance = new SampleCoverageShader();
    }
    return instance;
  }

  protected SampleCoverageShader() {
    super();

    addComputeShader(ResourceLoader.loadShader("shaders/sampleCoverage.comp"));

    compileShader();

    addUniform("multisamples");
  }

  public void updateUniforms() {

    setUniformi("multisamples", ContextHolder.getContext().getConfig().getMultisampling_sampleCount());
  }

}
