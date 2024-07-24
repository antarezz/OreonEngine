package org.oreon.gl.components.fft;

import static org.lwjgl.opengl.GL11.glFinish;
import static org.lwjgl.opengl.GL15.GL_READ_WRITE;
import static org.lwjgl.opengl.GL30.GL_RGBA32F;
import static org.lwjgl.opengl.GL42.glBindImageTexture;
import static org.lwjgl.opengl.GL43.glDispatchCompute;

import org.oreon.core.gl.pipeline.GLShaderProgram;
import org.oreon.core.gl.texture.GLTexture;
import org.oreon.core.gl.wrapper.texture.TextureStorage2D;
import org.oreon.core.image.Image.ImageFormat;
import org.oreon.core.math.Vec2f;

import lombok.Getter;
import lombok.Setter;

public class FFT {

  @Getter
  private GLTexture Dy;
  @Getter
  private GLTexture Dx;
  @Getter
  private GLTexture Dz;
  @Setter
  private boolean choppy;

  @Getter
  @Setter
  protected GLTexture pingpongTexture;

  private int log_2_N;
  private int pingpong;
  private int N;
  @Setter
  private float t;
  private long systemTime = System.currentTimeMillis();
  @Setter
  private float t_delta;
  private FFTButterflyShader butterflyShader;
  protected GLShaderProgram inversionShader;
  private TwiddleFactors twiddleFactors;

  protected H0k h0k;
  protected Hkt hkt;

  public FFT(int N, int L, float amplitude, Vec2f direction, float alignment,
      float intensity, float capillarSupressFactor) {
    this.N = N;
    log_2_N = (int) (Math.log(N) / Math.log(2));
    twiddleFactors = new TwiddleFactors(N);
    h0k = new H0k(N, L, amplitude, direction, alignment, intensity, capillarSupressFactor);
    hkt = new Hkt(N, L);

    butterflyShader = FFTButterflyShader.getInstance();
    inversionShader = FFTInversionShader.getInstance();

    pingpongTexture = new TextureStorage2D(N, N, 1, ImageFormat.RGBA32FLOAT);
    Dy = new TextureStorage2D(N, N, 1, ImageFormat.RGBA32FLOAT);
    Dx = new TextureStorage2D(N, N, 1, ImageFormat.RGBA32FLOAT);
    Dz = new TextureStorage2D(N, N, 1, ImageFormat.RGBA32FLOAT);
  }

  public void init() {
    h0k.render();
    twiddleFactors.render();
    hkt.setImageH0k(h0k.getImageH0k());
    hkt.setImageH0minusK(h0k.getImageH0minusK());
  }

  public void render() {
    hkt.render(t);

    // Dy-FFT

    pingpong = 0;

    butterflyShader.bind();

    glBindImageTexture(0, twiddleFactors.getTexture().getHandle(), 0, false, 0, GL_READ_WRITE, GL_RGBA32F);
    glBindImageTexture(1, hkt.getImageDyCoeficcients().getHandle(), 0, false, 0, GL_READ_WRITE, GL_RGBA32F);
    glBindImageTexture(2, getPingpongTexture().getHandle(), 0, false, 0, GL_READ_WRITE, GL_RGBA32F);

    // 1D FFT horizontal
    for (int i = 0; i < log_2_N; i++) {
      butterflyShader.updateUniforms(pingpong, 0, i);
      glDispatchCompute(N / 16, N / 16, 1);
      glFinish();
      pingpong++;
      pingpong %= 2;
    }

    //1D FFT vertical
    for (int j = 0; j < log_2_N; j++) {
      butterflyShader.updateUniforms(pingpong, 1, j);
      glDispatchCompute(N / 16, N / 16, 1);
      glFinish();
      pingpong++;
      pingpong %= 2;
    }

    inversionShader.bind();
    inversionShader.updateUniforms(N, pingpong);
    glBindImageTexture(0, Dy.getHandle(), 0, false, 0, GL_READ_WRITE, GL_RGBA32F);
    glDispatchCompute(N / 16, N / 16, 1);
    glFinish();

    if (choppy) {

      // Dx-FFT

      pingpong = 0;

      butterflyShader.bind();

      glBindImageTexture(0, twiddleFactors.getTexture().getHandle(), 0, false, 0, GL_READ_WRITE, GL_RGBA32F);
      glBindImageTexture(1, hkt.getImageDxCoefficients().getHandle(), 0, false, 0, GL_READ_WRITE, GL_RGBA32F);
      glBindImageTexture(2, getPingpongTexture().getHandle(), 0, false, 0, GL_READ_WRITE, GL_RGBA32F);

      // 1D FFT horizontal
      for (int i = 0; i < log_2_N; i++) {
        butterflyShader.updateUniforms(pingpong, 0, i);
        glDispatchCompute(N / 16, N / 16, 1);
        glFinish();
        pingpong++;
        pingpong %= 2;
      }

      //1D FFT vertical
      for (int j = 0; j < log_2_N; j++) {
        butterflyShader.updateUniforms(pingpong, 1, j);
        glDispatchCompute(N / 16, N / 16, 1);
        glFinish();
        pingpong++;
        pingpong %= 2;
      }

      inversionShader.bind();
      inversionShader.updateUniforms(N, pingpong);
      glBindImageTexture(0, Dx.getHandle(), 0, false, 0, GL_READ_WRITE, GL_RGBA32F);
      glDispatchCompute(N / 16, N / 16, 1);
      glFinish();

      // Dz-FFT

      pingpong = 0;

      butterflyShader.bind();

      glBindImageTexture(0, twiddleFactors.getTexture().getHandle(), 0, false, 0, GL_READ_WRITE, GL_RGBA32F);
      glBindImageTexture(1, hkt.getImageDzCoefficients().getHandle(), 0, false, 0, GL_READ_WRITE, GL_RGBA32F);
      glBindImageTexture(2, getPingpongTexture().getHandle(), 0, false, 0, GL_READ_WRITE, GL_RGBA32F);

      // 1D FFT horizontal
      for (int i = 0; i < log_2_N; i++) {
        butterflyShader.updateUniforms(pingpong, 0, i);
        glDispatchCompute(N / 16, N / 16, 1);
        glFinish();
        pingpong++;
        pingpong %= 2;
      }

      //1D FFT vertical
      for (int j = 0; j < log_2_N; j++) {
        butterflyShader.updateUniforms(pingpong, 1, j);
        glDispatchCompute(N / 16, N / 16, 1);
        glFinish();
        pingpong++;
        pingpong %= 2;
      }

      inversionShader.bind();
      inversionShader.updateUniforms(N, pingpong);
      glBindImageTexture(0, Dz.getHandle(), 0, false, 0, GL_READ_WRITE, GL_RGBA32F);
      glDispatchCompute(N / 16, N / 16, 1);
      glFinish();
    }

    t += (System.currentTimeMillis() - systemTime) * t_delta;
    systemTime = System.currentTimeMillis();
  }
}
