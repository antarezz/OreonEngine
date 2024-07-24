package org.oreon.gl.components.util;

import static org.lwjgl.opengl.GL11.glFinish;
import static org.lwjgl.opengl.GL15.GL_WRITE_ONLY;
import static org.lwjgl.opengl.GL30.GL_RGBA32F;
import static org.lwjgl.opengl.GL42.glBindImageTexture;
import static org.lwjgl.opengl.GL43.glDispatchCompute;

import org.oreon.core.gl.pipeline.GLShaderProgram;
import org.oreon.core.gl.texture.GLTexture;
import org.oreon.core.gl.wrapper.texture.TextureStorage2D;
import org.oreon.core.image.Image.ImageFormat;

import lombok.Getter;

public class NormalRenderer {

  @Getter
  private GLTexture normalmap;

  private float strength;
  private GLShaderProgram computeShader;
  private int N;

  public NormalRenderer(int N) {
    this.N = N;
    computeShader = NormalMapShader.getInstance();
    normalmap = new TextureStorage2D(N, N, (int) (Math.log(N) / Math.log(2)), ImageFormat.RGBA32FLOAT);
    normalmap.bind();
    normalmap.trilinearFilter();
    normalmap.unbind();
  }

  public void render(GLTexture heightmap) {
    computeShader.bind();
    computeShader.updateUniforms(heightmap, N, strength);
    glBindImageTexture(0, normalmap.getHandle(), 0, false, 0, GL_WRITE_ONLY, GL_RGBA32F);
    glDispatchCompute(N / 16, N / 16, 1);
    glFinish();
    normalmap.bind();
    normalmap.trilinearFilter();
  }

  public float getStrength() {
    return strength;
  }

  public void setStrength(float strength) {
    this.strength = strength;
  }

}
