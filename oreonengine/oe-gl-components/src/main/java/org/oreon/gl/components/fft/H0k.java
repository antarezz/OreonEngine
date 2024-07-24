package org.oreon.gl.components.fft;

import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.GL_TEXTURE1;
import static org.lwjgl.opengl.GL13.GL_TEXTURE2;
import static org.lwjgl.opengl.GL13.GL_TEXTURE3;
import static org.lwjgl.opengl.GL13.glActiveTexture;
import static org.lwjgl.opengl.GL15.GL_WRITE_ONLY;
import static org.lwjgl.opengl.GL30.GL_RGBA32F;
import static org.lwjgl.opengl.GL42.glBindImageTexture;
import static org.lwjgl.opengl.GL43.glDispatchCompute;

import org.oreon.core.gl.pipeline.GLShaderProgram;
import org.oreon.core.gl.texture.GLTexture;
import org.oreon.core.gl.wrapper.texture.TextureImage2D;
import org.oreon.core.gl.wrapper.texture.TextureStorage2D;
import org.oreon.core.image.Image.ImageFormat;
import org.oreon.core.image.Image.SamplerFilter;
import org.oreon.core.math.Vec2f;

import lombok.Getter;

public class H0k {

  @Getter
  private GLTexture imageH0k;
  @Getter
  private GLTexture imageH0minusK;

  private int N;
  private int L;
  private Vec2f direction;
  private float intensity;
  private float amplitude;
  private float alignment;
  private float capillarSupressFactor;

  private GLTexture noise0;
  private GLTexture noise1;
  private GLTexture noise2;
  private GLTexture noise3;

  protected GLShaderProgram shader;

  public H0k(int N, int L, float amplitude, Vec2f direction, float alignment,
      float intensity, float capillarSupressFactor) {

    this.N = N;
    this.L = L;
    this.direction = direction;
    this.amplitude = amplitude;
    this.intensity = intensity;
    this.capillarSupressFactor = capillarSupressFactor;
    this.alignment = alignment;

    shader = H0kShader.getInstance();

    imageH0k = new TextureStorage2D(N, N, 1, ImageFormat.RGBA32FLOAT);
    imageH0minusK = new TextureStorage2D(N, N, 1, ImageFormat.RGBA32FLOAT);
    noise0 = new TextureImage2D("textures/noise/Noise256_0.jpg", SamplerFilter.Nearest);
    noise1 = new TextureImage2D("textures/noise/Noise256_1.jpg", SamplerFilter.Nearest);
    noise2 = new TextureImage2D("textures/noise/Noise256_2.jpg", SamplerFilter.Nearest);
    noise3 = new TextureImage2D("textures/noise/Noise256_3.jpg", SamplerFilter.Nearest);
  }

  public void render() {

    shader.bind();
    shader.updateUniforms(N, L, amplitude, direction, alignment, intensity, capillarSupressFactor);

    glActiveTexture(GL_TEXTURE0);
    noise0.bind();

    glActiveTexture(GL_TEXTURE1);
    noise1.bind();

    glActiveTexture(GL_TEXTURE2);
    noise2.bind();

    glActiveTexture(GL_TEXTURE3);
    noise3.bind();

    shader.updateUniforms(0, 1, 2, 3);

    glBindImageTexture(0, imageH0k.getHandle(), 0, false, 0, GL_WRITE_ONLY, GL_RGBA32F);

    glBindImageTexture(1, imageH0minusK.getHandle(), 0, false, 0, GL_WRITE_ONLY, GL_RGBA32F);

    glDispatchCompute(N / 16, N / 16, 1);
  }

}
