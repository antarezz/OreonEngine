package org.oreon.gl.engine.antialiasing;

import org.oreon.core.context.ContextHolder;
import org.oreon.core.gl.pipeline.GLShaderProgram;
import org.oreon.core.gl.texture.GLTexture;
import org.oreon.core.util.ResourceLoader;

import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.glActiveTexture;

public class FXAAShader extends GLShaderProgram {

  private static FXAAShader instance = null;

  public static FXAAShader getInstance() {
    if (instance == null) {
      instance = new FXAAShader();
    }
    return instance;
  }

  protected FXAAShader() {
    super();

    addComputeShader(ResourceLoader.loadShader("shaders/fxaa.comp"));

    compileShader();

    addUniform("sceneSampler");
    addUniform("width");
    addUniform("height");
  }

  public void updateUniforms(GLTexture sceneTexture) {

    glActiveTexture(GL_TEXTURE0);
    sceneTexture.bind();
    sceneTexture.bilinearFilter();
    setUniformi("sceneSampler", 0);
    setUniformf("width", (float) ContextHolder.getContext().getWindow().getWidth());
    setUniformf("height", (float) ContextHolder.getContext().getWindow().getHeight());
  }
}
