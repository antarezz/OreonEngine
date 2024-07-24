package org.oreon.gl.components.filter.motionblur;

import org.oreon.core.context.ContextHolder;
import org.oreon.core.gl.pipeline.GLShaderProgram;
import org.oreon.core.gl.texture.GLTexture;
import org.oreon.core.math.Matrix4f;
import org.oreon.core.util.ResourceLoader;

import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.glActiveTexture;

public class PixelVelocityShader extends GLShaderProgram {

  private static PixelVelocityShader instance = null;

  public static PixelVelocityShader getInstance() {
    if (instance == null) {
      instance = new PixelVelocityShader();
    }
    return instance;
  }

  protected PixelVelocityShader() {
    super();

    addComputeShader(
        ResourceLoader.loadShader(ContextHolder.getContext().getConfig().getMultisampling_sampleCount() > 1 ?
            "shaders/filter/motion_blur/pixelVelocity.comp"
            : "shaders/filter/motion_blur/pixelVelocity_singleSample.comp"));

    compileShader();

    addUniform("depthmap");
    addUniform("windowWidth");
    addUniform("windowHeight");
    addUniform("projectionMatrix");
    addUniform("inverseViewProjectionMatrix");
    addUniform("previousViewProjectionMatrix");
  }

  public void updateUniforms(Matrix4f projectionMatrix,
      Matrix4f inverseViewProjectionMatrix,
      Matrix4f previousViewProjectionMatrix,
      GLTexture depthmap) {
    glActiveTexture(GL_TEXTURE0);
    depthmap.bind();
    setUniformi("depthmap", 0);
    setUniformf("windowWidth", ContextHolder.getContext().getConfig().getFrameWidth());
    setUniformf("windowHeight", ContextHolder.getContext().getConfig().getFrameHeight());
    setUniform("projectionMatrix", projectionMatrix);
    setUniform("inverseViewProjectionMatrix", inverseViewProjectionMatrix);
    setUniform("previousViewProjectionMatrix", previousViewProjectionMatrix);
  }
}
