package org.oreon.gl.components.filter.ssao;

import org.oreon.core.gl.pipeline.GLShaderProgram;
import org.oreon.core.math.Matrix4f;
import org.oreon.core.math.Vec3f;
import org.oreon.core.util.ResourceLoader;

public class SSAOShader extends GLShaderProgram {

  private static SSAOShader instance = null;

  public static SSAOShader getInstance() {
    if (instance == null) {

      instance = new SSAOShader();
    }
    return instance;
  }

  protected SSAOShader() {

    super();

    addComputeShader(ResourceLoader.loadShader("shaders/filter/ssao/ssao.comp"));
    compileShader();

    addUniform("m_View");
    addUniform("m_Proj");
    addUniform("kernelSize");
    addUniform("uRadius");
    addUniform("threshold");
    addUniform("width");
    addUniform("height");

    for (int i = 0; i < 64; i++) {
      addUniform("kernel[" + i + "]");
    }
  }

  public void updateUniforms(Matrix4f viewMatrix, Matrix4f projectionMatrix,
      int width, int height, Vec3f[] kernel) {

    setUniform("m_View", viewMatrix);
    setUniform("m_Proj", projectionMatrix);
    setUniformi("kernelSize", 64);
    setUniformf("uRadius", 2f);
    setUniformf("threshold", 0.02f);
    setUniformi("width", width);
    setUniformi("height", height);

    for (int i = 0; i < 64; i++) {
      setUniform("kernel[" + i + "]", kernel[i]);
    }
  }
}
