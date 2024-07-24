package org.oreon.gl.components.filter.motionblur;

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
import static org.lwjgl.opengl.GL30.GL_RGBA16F;
import static org.lwjgl.opengl.GL42.glBindImageTexture;
import static org.lwjgl.opengl.GL43.glDispatchCompute;

public class MotionBlur {

  @Getter
  private GLTexture motionBlurSceneTexture;
  private GLTexture pixelVelocityTexture;
  private PixelVelocityShader pixelVelocityShader;
  private MotionBlurShader motionBlurShader;

  public MotionBlur() {

    pixelVelocityTexture = new TextureImage2D(
        ContextHolder.getContext().getConfig().getFrameWidth(),
        ContextHolder.getContext().getConfig().getFrameHeight(),
        ImageFormat.RGBA16FLOAT, SamplerFilter.Bilinear, TextureWrapMode.ClampToEdge);
    motionBlurSceneTexture = new TextureImage2D(
        ContextHolder.getContext().getConfig().getFrameWidth(),
        ContextHolder.getContext().getConfig().getFrameHeight(),
        ImageFormat.RGBA16FLOAT, SamplerFilter.Bilinear, TextureWrapMode.ClampToEdge);

    pixelVelocityShader = PixelVelocityShader.getInstance();
    motionBlurShader = MotionBlurShader.getInstance();
  }

  public void render(GLTexture sceneSampler, GLTexture depthmap) {

    glFinish();
    pixelVelocityShader.bind();
    glBindImageTexture(0, pixelVelocityTexture.getHandle(), 0, false, 0, GL_WRITE_ONLY, GL_RGBA16F);
    pixelVelocityShader.updateUniforms(
        ContextHolder.getContext().getCamera().getProjectionMatrix(),
        ContextHolder.getContext().getCamera().getViewProjectionMatrix().invert(),
        ContextHolder.getContext().getCamera().getPreviousViewProjectionMatrix(),
        depthmap);
    glDispatchCompute(ContextHolder.getContext().getConfig().getFrameWidth() / 8,
        ContextHolder.getContext().getConfig().getFrameHeight() / 8, 1);
    glFinish();

    motionBlurShader.bind();
    glBindImageTexture(0, motionBlurSceneTexture.getHandle(), 0, false, 0, GL_WRITE_ONLY, GL_RGBA16F);
    glBindImageTexture(2, pixelVelocityTexture.getHandle(), 0, false, 0, GL_READ_ONLY, GL_RGBA16F);
    motionBlurShader.updateUniforms(
        ContextHolder.getContext().getConfig().getFrameWidth(),
        ContextHolder.getContext().getConfig().getFrameHeight(),
        sceneSampler
    );
    glDispatchCompute(
        ContextHolder.getContext().getConfig().getFrameWidth() / 8,
        ContextHolder.getContext().getConfig().getFrameHeight() / 8,
        1
    );
    glFinish();
  }

}
