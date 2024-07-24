package org.oreon.gl.components.util;

import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.glActiveTexture;

import org.oreon.core.gl.pipeline.GLShaderProgram;
import org.oreon.core.gl.texture.GLTexture;
import org.oreon.core.util.ResourceLoader;

public class NormalMapShader extends GLShaderProgram {

  private static NormalMapShader instance = null;

  public static NormalMapShader getInstance() {
    if (instance == null) {
      instance = new NormalMapShader();
    }
    return instance;
  }

  protected NormalMapShader() {
    super();

    addComputeShader(ResourceLoader.loadShader("shaders/util/normals.comp"));
    compileShader();

    addUniform("heightmap");
    addUniform("N");
    addUniform("normalStrength");
  }

  public void updateUniforms(GLTexture heightmap, int N, float strength) {
    glActiveTexture(GL_TEXTURE0);
    heightmap.bind();
    setUniformi("heightmap", 0);
    setUniformi("N", N);
    setUniformf("normalStrength", strength);
  }
}
