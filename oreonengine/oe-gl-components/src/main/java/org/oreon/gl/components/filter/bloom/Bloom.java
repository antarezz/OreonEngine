package org.oreon.gl.components.filter.bloom;

import lombok.Getter;

import org.oreon.core.context.ContextHolder;
import org.oreon.core.gl.memory.GLShaderStorageBuffer;
import org.oreon.core.gl.texture.GLTexture;
import org.oreon.core.gl.wrapper.texture.TextureImage2D;
import org.oreon.core.image.Image.ImageFormat;
import org.oreon.core.image.Image.SamplerFilter;
import org.oreon.core.image.Image.TextureWrapMode;

import static org.lwjgl.opengl.GL11.glFinish;
import static org.lwjgl.opengl.GL15.GL_READ_ONLY;
import static org.lwjgl.opengl.GL15.GL_WRITE_ONLY;
import static org.lwjgl.opengl.GL30.GL_RGBA16F;
import static org.lwjgl.opengl.GL42.glBindImageTexture;
import static org.lwjgl.opengl.GL43.glDispatchCompute;

@Getter
public class Bloom {

  private GLTexture bloomSceneTexture;

  private GLTexture sceneBrightnessTexture;
  private GLTexture sceneBrightnessTextureDownsampled1;
  private GLTexture sceneBrightnessTextureDownsampled2;
  private GLTexture sceneBrightnessTextureDownsampled3;

  private GLTexture horizontalBloomBlurDownsampling0;
  private GLTexture verticalBloomBlurDownsampling0;

  private GLTexture horizontalBloomBlurDownsampling1;
  private GLTexture verticalBloomBlurDownsampling1;

  private GLTexture horizontalBloomBlurDownsampling2;
  private GLTexture verticalBloomBlurDownsampling2;

  private GLTexture horizontalBloomBlurDownsampling3;
  private GLTexture verticalBloomBlurDownsampling3;

  private GLTexture additiveBlendBloomTexture;

  private SceneBrightnessShader sceneBrightnessShader;
  private BloomHorizontalBlurShader horizontalBlurShader;
  private BloomVerticalBlurShader verticalBlurShader;
  private BloomSceneBlendingShader bloomSceneShader;
  private BloomAdditiveBlendShader additiveBlendShader;

  private GLShaderStorageBuffer ssbo;

  private final int[] downsamplingFactors = {2, 4, 8, 12};

  public Bloom() {

    sceneBrightnessShader = SceneBrightnessShader.getInstance();
    additiveBlendShader = BloomAdditiveBlendShader.getInstance();
    horizontalBlurShader = BloomHorizontalBlurShader.getInstance();
    verticalBlurShader = BloomVerticalBlurShader.getInstance();
    bloomSceneShader = BloomSceneBlendingShader.getInstance();

    ssbo = new GLShaderStorageBuffer();
    ssbo.addData(initSsbo());

    sceneBrightnessTexture = new TextureImage2D(
        ContextHolder.getContext().getConfig().getFrameWidth(),
        ContextHolder.getContext().getConfig().getFrameHeight(),
        ImageFormat.RGBA16FLOAT, SamplerFilter.Bilinear, TextureWrapMode.ClampToEdge);

    sceneBrightnessTextureDownsampled1 = new TextureImage2D(
        ContextHolder.getContext().getConfig().getFrameWidth() / downsamplingFactors[1],
        ContextHolder.getContext().getConfig().getFrameHeight() / downsamplingFactors[1],
        ImageFormat.RGBA16FLOAT, SamplerFilter.Bilinear, TextureWrapMode.ClampToEdge);

    sceneBrightnessTextureDownsampled2 = new TextureImage2D(
        ContextHolder.getContext().getConfig().getFrameWidth() / downsamplingFactors[2],
        ContextHolder.getContext().getConfig().getFrameHeight() / downsamplingFactors[2],
        ImageFormat.RGBA16FLOAT, SamplerFilter.Bilinear, TextureWrapMode.ClampToEdge);

    sceneBrightnessTextureDownsampled3 = new TextureImage2D(
        ContextHolder.getContext().getConfig().getFrameWidth() / downsamplingFactors[3],
        ContextHolder.getContext().getConfig().getFrameHeight() / downsamplingFactors[3],
        ImageFormat.RGBA16FLOAT, SamplerFilter.Bilinear, TextureWrapMode.ClampToEdge);

    additiveBlendBloomTexture = new TextureImage2D(
        ContextHolder.getContext().getConfig().getFrameWidth(),
        ContextHolder.getContext().getConfig().getFrameHeight(), ImageFormat.RGBA16FLOAT,
        SamplerFilter.Bilinear, TextureWrapMode.ClampToEdge);

    bloomSceneTexture = new TextureImage2D(ContextHolder.getContext().getConfig().getFrameWidth(),
        ContextHolder.getContext().getConfig().getFrameHeight(), ImageFormat.RGBA16FLOAT,
        SamplerFilter.Bilinear, TextureWrapMode.ClampToEdge);

    horizontalBloomBlurDownsampling0 = new TextureImage2D(
        ContextHolder.getContext().getConfig().getFrameWidth() / downsamplingFactors[0],
        ContextHolder.getContext().getConfig().getFrameHeight() / downsamplingFactors[0],
        ImageFormat.RGBA16FLOAT, SamplerFilter.Nearest, TextureWrapMode.ClampToEdge);

    verticalBloomBlurDownsampling0 = new TextureImage2D(
        ContextHolder.getContext().getConfig().getFrameWidth() / downsamplingFactors[0],
        ContextHolder.getContext().getConfig().getFrameHeight() / downsamplingFactors[0],
        ImageFormat.RGBA16FLOAT, SamplerFilter.Nearest, TextureWrapMode.ClampToEdge);

    horizontalBloomBlurDownsampling1 = new TextureImage2D(
        ContextHolder.getContext().getConfig().getFrameWidth() / downsamplingFactors[1],
        ContextHolder.getContext().getConfig().getFrameHeight() / downsamplingFactors[1],
        ImageFormat.RGBA16FLOAT, SamplerFilter.Nearest, TextureWrapMode.ClampToEdge);

    verticalBloomBlurDownsampling1 = new TextureImage2D(
        ContextHolder.getContext().getConfig().getFrameWidth() / downsamplingFactors[1],
        ContextHolder.getContext().getConfig().getFrameHeight() / downsamplingFactors[1],
        ImageFormat.RGBA16FLOAT, SamplerFilter.Nearest, TextureWrapMode.ClampToEdge);

    horizontalBloomBlurDownsampling2 = new TextureImage2D(
        ContextHolder.getContext().getConfig().getFrameWidth() / downsamplingFactors[2],
        ContextHolder.getContext().getConfig().getFrameHeight() / downsamplingFactors[2],
        ImageFormat.RGBA16FLOAT, SamplerFilter.Nearest, TextureWrapMode.ClampToEdge);

    verticalBloomBlurDownsampling2 = new TextureImage2D(
        ContextHolder.getContext().getConfig().getFrameWidth() / downsamplingFactors[2],
        ContextHolder.getContext().getConfig().getFrameHeight() / downsamplingFactors[2],
        ImageFormat.RGBA16FLOAT, SamplerFilter.Nearest, TextureWrapMode.ClampToEdge);

    horizontalBloomBlurDownsampling3 = new TextureImage2D(
        ContextHolder.getContext().getConfig().getFrameWidth() / downsamplingFactors[3],
        ContextHolder.getContext().getConfig().getFrameHeight() / downsamplingFactors[3],
        ImageFormat.RGBA16FLOAT, SamplerFilter.Bilinear, TextureWrapMode.ClampToEdge);

    verticalBloomBlurDownsampling3 = new TextureImage2D(
        ContextHolder.getContext().getConfig().getFrameWidth() / downsamplingFactors[3],
        ContextHolder.getContext().getConfig().getFrameHeight() / downsamplingFactors[3],
        ImageFormat.RGBA16FLOAT, SamplerFilter.Nearest, TextureWrapMode.ClampToEdge);
  }

  public void render(GLTexture sceneSamplerPrePostprocessing, GLTexture sceneSampler,
      GLTexture specular_emission_bloom_attachment) {

    ssbo.bindBufferBase(1);

    glFinish();
    sceneBrightnessShader.bind();
    glBindImageTexture(0, sceneSamplerPrePostprocessing.getHandle(), 0, false, 0, GL_READ_ONLY, GL_RGBA16F);
    glBindImageTexture(1, sceneBrightnessTexture.getHandle(), 0, false, 0, GL_WRITE_ONLY, GL_RGBA16F);
    glBindImageTexture(2, sceneBrightnessTextureDownsampled1.getHandle(), 0, false, 0, GL_WRITE_ONLY, GL_RGBA16F);
    glBindImageTexture(3, sceneBrightnessTextureDownsampled2.getHandle(), 0, false, 0, GL_WRITE_ONLY, GL_RGBA16F);
    glBindImageTexture(4, sceneBrightnessTextureDownsampled3.getHandle(), 0, false, 0, GL_WRITE_ONLY, GL_RGBA16F);
    glDispatchCompute(ContextHolder.getContext().getConfig().getFrameWidth() / 8,
        ContextHolder.getContext().getConfig().getFrameHeight() / 8, 1);
    glFinish();

    horizontalBlurShader.bind();
    glBindImageTexture(0, horizontalBloomBlurDownsampling0.getHandle(), 0, false, 0, GL_WRITE_ONLY, GL_RGBA16F);
    glBindImageTexture(1, horizontalBloomBlurDownsampling1.getHandle(), 0, false, 0, GL_WRITE_ONLY, GL_RGBA16F);
    glBindImageTexture(2, horizontalBloomBlurDownsampling2.getHandle(), 0, false, 0, GL_WRITE_ONLY, GL_RGBA16F);
    glBindImageTexture(3, horizontalBloomBlurDownsampling3.getHandle(), 0, false, 0, GL_WRITE_ONLY, GL_RGBA16F);
    glBindImageTexture(4, sceneBrightnessTexture.getHandle(), 0, false, 0, GL_READ_ONLY, GL_RGBA16F);
    horizontalBlurShader.updateUniforms(sceneBrightnessTexture,
        sceneBrightnessTextureDownsampled1,
        sceneBrightnessTextureDownsampled2,
        sceneBrightnessTextureDownsampled3);
    glDispatchCompute(ContextHolder.getContext().getConfig().getFrameWidth() / 16,
        ContextHolder.getContext().getConfig().getFrameHeight() / 16, 1);
    glFinish();

    verticalBlurShader.bind();
    glBindImageTexture(0, verticalBloomBlurDownsampling0.getHandle(), 0, false, 0, GL_WRITE_ONLY, GL_RGBA16F);
    glBindImageTexture(1, verticalBloomBlurDownsampling1.getHandle(), 0, false, 0, GL_WRITE_ONLY, GL_RGBA16F);
    glBindImageTexture(2, verticalBloomBlurDownsampling2.getHandle(), 0, false, 0, GL_WRITE_ONLY, GL_RGBA16F);
    glBindImageTexture(3, verticalBloomBlurDownsampling3.getHandle(), 0, false, 0, GL_WRITE_ONLY, GL_RGBA16F);
    glBindImageTexture(4, horizontalBloomBlurDownsampling0.getHandle(), 0, false, 0, GL_READ_ONLY, GL_RGBA16F);
    glBindImageTexture(5, horizontalBloomBlurDownsampling1.getHandle(), 0, false, 0, GL_READ_ONLY, GL_RGBA16F);
    glBindImageTexture(6, horizontalBloomBlurDownsampling2.getHandle(), 0, false, 0, GL_READ_ONLY, GL_RGBA16F);
    glBindImageTexture(7, horizontalBloomBlurDownsampling3.getHandle(), 0, false, 0, GL_READ_ONLY, GL_RGBA16F);
    glDispatchCompute(ContextHolder.getContext().getConfig().getFrameWidth() / 16,
        ContextHolder.getContext().getConfig().getFrameHeight() / 16, 1);
    glFinish();

    additiveBlendShader.bind();
    glBindImageTexture(0, additiveBlendBloomTexture.getHandle(), 0, false, 0, GL_WRITE_ONLY, GL_RGBA16F);
    additiveBlendShader.updateUniforms(verticalBloomBlurDownsampling0, verticalBloomBlurDownsampling1,
        verticalBloomBlurDownsampling2,
        verticalBloomBlurDownsampling3, ContextHolder.getContext().getConfig().getFrameWidth(),
        ContextHolder.getContext().getConfig().getFrameHeight());
    glDispatchCompute(ContextHolder.getContext().getConfig().getFrameWidth() / 8,
        ContextHolder.getContext().getConfig().getFrameHeight() / 8, 1);
    glFinish();

    bloomSceneShader.bind();
    glBindImageTexture(0, sceneSampler.getHandle(), 0, false, 0, GL_READ_ONLY, GL_RGBA16F);
    glBindImageTexture(1, additiveBlendBloomTexture.getHandle(), 0, false, 0, GL_READ_ONLY, GL_RGBA16F);
    glBindImageTexture(2, specular_emission_bloom_attachment.getHandle(), 0, false, 0, GL_READ_ONLY, GL_RGBA16F);
    glBindImageTexture(3, bloomSceneTexture.getHandle(), 0, false, 0, GL_WRITE_ONLY, GL_RGBA16F);
    glDispatchCompute(ContextHolder.getContext().getConfig().getFrameWidth() / 8,
        ContextHolder.getContext().getConfig().getFrameHeight() / 8, 1);
    glFinish();
  }

  private float[] initSsbo() {
    float[] buffer = new float[12];

    buffer[0] = ContextHolder.getContext().getConfig().getFrameWidth() / downsamplingFactors[0];
    buffer[1] = ContextHolder.getContext().getConfig().getFrameHeight() / downsamplingFactors[0];
    buffer[2] = ContextHolder.getContext().getConfig().getFrameWidth() / downsamplingFactors[1];
    buffer[3] = ContextHolder.getContext().getConfig().getFrameHeight() / downsamplingFactors[1];
    buffer[4] = ContextHolder.getContext().getConfig().getFrameWidth() / downsamplingFactors[2];
    buffer[5] = ContextHolder.getContext().getConfig().getFrameHeight() / downsamplingFactors[2];
    buffer[6] = ContextHolder.getContext().getConfig().getFrameWidth() / downsamplingFactors[3];
    buffer[7] = ContextHolder.getContext().getConfig().getFrameHeight() / downsamplingFactors[3];
    buffer[8] = downsamplingFactors[0];
    buffer[9] = downsamplingFactors[1];
    buffer[10] = downsamplingFactors[2];
    buffer[11] = downsamplingFactors[3];

    return buffer;
  }

}
