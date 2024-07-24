package org.oreon.core.gl.shadow;

import lombok.Getter;

import org.oreon.core.context.ContextHolder;
import org.oreon.core.gl.framebuffer.GLFramebuffer;
import org.oreon.core.gl.pipeline.RenderParameter;
import org.oreon.core.gl.texture.GLTexture;
import org.oreon.core.gl.wrapper.parameter.ShadowConfig;
import org.oreon.core.gl.wrapper.texture.TextureImage2DArrray;
import org.oreon.core.image.Image.ImageFormat;
import org.oreon.core.image.Image.SamplerFilter;
import org.oreon.core.image.Image.TextureWrapMode;
import org.oreon.core.util.Constants;

import static org.lwjgl.opengl.GL11.GL_NONE;
import static org.lwjgl.opengl.GL20.glDrawBuffers;
import static org.lwjgl.opengl.GL30.GL_DEPTH_ATTACHMENT;
import static org.lwjgl.opengl.GL30.GL_FRAMEBUFFER;
import static org.lwjgl.opengl.GL32.glFramebufferTexture;

@Getter
public class ParallelSplitShadowMapsFbo {

  private GLFramebuffer fbo;
  private GLTexture depthMap;
  private RenderParameter config;

  public ParallelSplitShadowMapsFbo() {

    config = new ShadowConfig();

    depthMap = new TextureImage2DArrray(
        ContextHolder.getContext().getConfig().getShadowMapResolution(),
        ContextHolder.getContext().getConfig().getShadowMapResolution(),
        Constants.PSSM_SPLITS, ImageFormat.DEPTH32FLOAT, SamplerFilter.Bilinear, TextureWrapMode.ClampToEdge);

    fbo = new GLFramebuffer();
    fbo.bind();
    glFramebufferTexture(GL_FRAMEBUFFER,
        GL_DEPTH_ATTACHMENT,
        depthMap.getHandle(),
        0);
    glDrawBuffers(GL_NONE);
    fbo.checkStatus();
    fbo.unbind();
  }

}
