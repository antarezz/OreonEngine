package org.oreon.gl.engine.deferred;

import lombok.Getter;

import org.oreon.core.context.ContextHolder;
import org.oreon.core.gl.texture.GLTexture;
import org.oreon.core.gl.wrapper.texture.TextureImage2D;
import org.oreon.core.image.Image.ImageFormat;
import org.oreon.core.image.Image.SamplerFilter;
import org.oreon.core.image.Image.TextureWrapMode;

import static org.lwjgl.opengl.GL11.glFinish;
import static org.lwjgl.opengl.GL15.GL_READ_ONLY;
import static org.lwjgl.opengl.GL15.GL_WRITE_ONLY;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.opengl.GL42.glBindImageTexture;
import static org.lwjgl.opengl.GL43.glDispatchCompute;

public class DeferredLighting {

  @Getter
  private GLTexture deferredLightingSceneTexture;
  private DeferredLightingShader shader;
  private int width;
  private int height;

  public DeferredLighting(int width, int height) {

    this.width = width;
    this.height = height;

    shader = DeferredLightingShader.getInstance();

    deferredLightingSceneTexture = new TextureImage2D(width, height,
        ImageFormat.RGBA16FLOAT, SamplerFilter.Bilinear, TextureWrapMode.ClampToEdge);
  }

  public void render(GLTexture sampleCoverageMask, GLTexture ssaoBlurTexture, GLTexture shadowmap,
      GLTexture albedoTexture, GLTexture worldPositionTexture, GLTexture normalTexture,
      GLTexture specularEmissionDiffuseSsaoBloomTexture) {

    glFinish();

    shader.bind();
    glBindImageTexture(0, deferredLightingSceneTexture.getHandle(), 0, false, 0, GL_WRITE_ONLY, GL_RGBA16F);
    glBindImageTexture(2, albedoTexture.getHandle(), 0, false, 0, GL_READ_ONLY, GL_RGBA16F);
    glBindImageTexture(3, worldPositionTexture.getHandle(), 0, false, 0, GL_READ_ONLY, GL_RGBA32F);
    glBindImageTexture(4, normalTexture.getHandle(), 0, false, 0, GL_READ_ONLY, GL_RGBA16F);
    glBindImageTexture(5, specularEmissionDiffuseSsaoBloomTexture.getHandle(), 0, false, 0, GL_READ_ONLY, GL_RGBA16F);
    glBindImageTexture(6, sampleCoverageMask.getHandle(), 0, false, 0, GL_READ_ONLY, GL_R8);
    if (ContextHolder.getContext().getConfig().isSsaoEnabled()) {
      glBindImageTexture(7, ssaoBlurTexture.getHandle(), 0, false, 0, GL_READ_ONLY, GL_R16F);
    }
    shader.updateUniforms(shadowmap);
    glDispatchCompute(width / 2, height / 2, 1);
  }

}
