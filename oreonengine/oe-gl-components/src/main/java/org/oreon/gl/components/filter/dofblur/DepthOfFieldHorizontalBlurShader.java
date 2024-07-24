package org.oreon.gl.components.filter.dofblur;

import static org.lwjgl.opengl.GL13.GL_TEXTURE2;
import static org.lwjgl.opengl.GL13.GL_TEXTURE3;
import static org.lwjgl.opengl.GL13.glActiveTexture;

import org.oreon.core.gl.pipeline.GLShaderProgram;
import org.oreon.core.gl.texture.GLTexture;
import org.oreon.core.util.ResourceLoader;

public class DepthOfFieldHorizontalBlurShader extends GLShaderProgram {

  private static DepthOfFieldHorizontalBlurShader instance = null;

  public static DepthOfFieldHorizontalBlurShader getInstance() {
    if (instance == null) {
      instance = new DepthOfFieldHorizontalBlurShader();
    }
    return instance;
  }

  protected DepthOfFieldHorizontalBlurShader() {
    super();

    addComputeShader(
        ResourceLoader.loadShader("shaders/filter/depth_of_field/depthOfField_horizontalGaussianBlur.comp"));

    compileShader();

    addUniform("depthmap");
    addUniform("sceneSampler");
//		addUniform("sceneSamplerDownsampled");
  }

  public void updateUniforms(GLTexture depthmap, GLTexture sceneSampler, GLTexture sceneSamplerDownsampled) {

    glActiveTexture(GL_TEXTURE2);
    depthmap.bind();
    setUniformi("depthmap", 2);

    glActiveTexture(GL_TEXTURE3);
    sceneSampler.bind();
    setUniformi("sceneSampler", 3);

//		glActiveTexture(GL_TEXTURE4);
//		sceneSamplerDownsampled.bind();
//		setUniformi("sceneSamplerDownsampled", 4);
  }
}
