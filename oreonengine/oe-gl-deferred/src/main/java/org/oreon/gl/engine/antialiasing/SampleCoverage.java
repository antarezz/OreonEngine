package org.oreon.gl.engine.antialiasing;

import lombok.Getter;

import org.oreon.core.context.ContextHolder;
import org.oreon.core.gl.texture.GLTexture;
import org.oreon.core.gl.wrapper.texture.TextureImage2D;
import org.oreon.core.image.Image.ImageFormat;
import org.oreon.core.image.Image.SamplerFilter;
import org.oreon.core.image.Image.TextureWrapMode;

import static org.lwjgl.opengl.GL15.GL_READ_ONLY;
import static org.lwjgl.opengl.GL15.GL_WRITE_ONLY;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.opengl.GL42.glBindImageTexture;
import static org.lwjgl.opengl.GL43.glDispatchCompute;

public class SampleCoverage {

  @Getter
  private GLTexture sampleCoverageMask;
  @Getter
  private GLTexture lightScatteringMaskSingleSample;
  @Getter
  private GLTexture specularEmissionBloomMaskSingleSample;

  private SampleCoverageShader shader;

  public SampleCoverage(int width, int height) {

    shader = SampleCoverageShader.getInstance();

    sampleCoverageMask = new TextureImage2D(width, height,
        ImageFormat.R8, SamplerFilter.Nearest, TextureWrapMode.ClampToEdge);
    lightScatteringMaskSingleSample = new TextureImage2D(width, height,
        ImageFormat.RGBA16FLOAT, SamplerFilter.Nearest, TextureWrapMode.ClampToEdge);
    specularEmissionBloomMaskSingleSample = new TextureImage2D(width, height,
        ImageFormat.RGBA16FLOAT, SamplerFilter.Nearest, TextureWrapMode.ClampToEdge);
  }

  public void render(GLTexture worldPositionTexture, GLTexture lightScatteringMask,
      GLTexture specularEmissionBloomMask) {

    shader.bind();
    glBindImageTexture(0, sampleCoverageMask.getHandle(), 0, false, 0, GL_WRITE_ONLY, GL_R8);
    glBindImageTexture(1, worldPositionTexture.getHandle(), 0, false, 0, GL_READ_ONLY, GL_RGBA32F);
    glBindImageTexture(2, lightScatteringMaskSingleSample.getHandle(), 0, false, 0, GL_WRITE_ONLY, GL_RGBA16F);
    glBindImageTexture(3, lightScatteringMask.getHandle(), 0, false, 0, GL_READ_ONLY, GL_RGBA16F);
    glBindImageTexture(4, specularEmissionBloomMaskSingleSample.getHandle(), 0, false, 0, GL_WRITE_ONLY, GL_RGBA16F);
    glBindImageTexture(5, specularEmissionBloomMask.getHandle(), 0, false, 0, GL_READ_ONLY, GL_RGBA16F);
    shader.updateUniforms();
    glDispatchCompute(
        ContextHolder.getContext().getConfig().getFrameWidth() / 16,
        ContextHolder.getContext().getConfig().getFrameHeight() / 16,
        1);
  }

}
