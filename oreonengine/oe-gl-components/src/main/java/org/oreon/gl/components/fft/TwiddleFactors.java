package org.oreon.gl.components.fft;

import static org.lwjgl.opengl.GL15.GL_WRITE_ONLY;
import static org.lwjgl.opengl.GL30.GL_RGBA32F;
import static org.lwjgl.opengl.GL42.glBindImageTexture;
import static org.lwjgl.opengl.GL43.glDispatchCompute;

import org.oreon.core.gl.memory.GLShaderStorageBuffer;
import org.oreon.core.gl.texture.GLTexture;
import org.oreon.core.gl.wrapper.texture.TextureStorage2D;
import org.oreon.core.image.Image.ImageFormat;

import lombok.Getter;

public class TwiddleFactors {

  @Getter
  private GLTexture texture;

  private int N;
  private int log_2_N;
  private TwiddleFactorsShader shader;
  private GLShaderStorageBuffer bitReversedSSBO;

  public TwiddleFactors(int N) {
    this.N = N;

    bitReversedSSBO = new GLShaderStorageBuffer();
    bitReversedSSBO.addData(initBitReversedIndices());

    log_2_N = (int) (Math.log(N) / Math.log(2));
    shader = TwiddleFactorsShader.getInstance();
    texture = new TextureStorage2D(log_2_N, N, 1, ImageFormat.RGBA32FLOAT);
  }

  public void render() {
    shader.bind();
    bitReversedSSBO.bindBufferBase(1);
    shader.updateUniforms(N);
    glBindImageTexture(0, texture.getHandle(), 0, false, 0, GL_WRITE_ONLY, GL_RGBA32F);
    glDispatchCompute(log_2_N, N / 16, 1);
  }

  private int[] initBitReversedIndices() {
    int[] bitReversedIndices = new int[N];
    int bits = (int) (Math.log(N) / Math.log(2));

    for (int i = 0; i < N; i++) {
      int x = Integer.reverse(i);
      x = Integer.rotateLeft(x, bits);
      bitReversedIndices[i] = x;
    }

    return bitReversedIndices;
  }

}
