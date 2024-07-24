package org.oreon.gl.components.filter.contrast;

import static org.lwjgl.opengl.GL11.glFinish;
import static org.lwjgl.opengl.GL15.GL_READ_ONLY;
import static org.lwjgl.opengl.GL15.GL_WRITE_ONLY;
import static org.lwjgl.opengl.GL30.GL_RGBA16F;
import static org.lwjgl.opengl.GL42.glBindImageTexture;
import static org.lwjgl.opengl.GL43.glDispatchCompute;

import lombok.Getter;
import org.lwjgl.glfw.GLFW;
import org.oreon.core.context.ContextHolder;
import org.oreon.core.gl.pipeline.GLShaderProgram;
import org.oreon.core.gl.texture.GLTexture;
import org.oreon.core.gl.wrapper.texture.TextureImage2D;
import org.oreon.core.image.Image.ImageFormat;
import org.oreon.core.image.Image.SamplerFilter;
import org.oreon.core.image.Image.TextureWrapMode;

public class ContrastController {

  @Getter
  private GLTexture contrastTexture;
  private GLShaderProgram contrastShader;

  private float contrastFactor = 1.0f;
  private float brightnessFactor = 2.0f;

  public ContrastController() {

    contrastShader = ContrastShader.getInstance();

    contrastTexture = new TextureImage2D(
        ContextHolder.getContext().getWindow().getWidth(),
        ContextHolder.getContext().getWindow().getHeight(),
        ImageFormat.RGBA16FLOAT, SamplerFilter.Bilinear, TextureWrapMode.ClampToEdge);
  }

  public void render(GLTexture sceneSampler) {

    contrastShader.bind();
    glBindImageTexture(0, sceneSampler.getHandle(), 0, false, 0, GL_READ_ONLY, GL_RGBA16F);
    glBindImageTexture(1, contrastTexture.getHandle(), 0, false, 0, GL_WRITE_ONLY, GL_RGBA16F);
    contrastShader.updateUniforms(contrastFactor, brightnessFactor);
    glDispatchCompute(
        ContextHolder.getContext().getWindow().getWidth() / 16,
        ContextHolder.getContext().getWindow().getHeight() / 16, 1);
    glFinish();
  }

  public void update() {
    if (ContextHolder.getContext().getInput().isKeyPushed(GLFW.GLFW_KEY_KP_ADD)) {
      contrastFactor += 0.01f;
    }
    if (ContextHolder.getContext().getInput().isKeyPushed(GLFW.GLFW_KEY_KP_SUBTRACT)) {
      contrastFactor -= 0.01f;
    }
    if (ContextHolder.getContext().getInput().isKeyPushed(GLFW.GLFW_KEY_KP_MULTIPLY)) {
      brightnessFactor += 1f;
    }
    if (ContextHolder.getContext().getInput().isKeyPushed(GLFW.GLFW_KEY_KP_DIVIDE)) {
      brightnessFactor -= 1f;
    }
  }

}
