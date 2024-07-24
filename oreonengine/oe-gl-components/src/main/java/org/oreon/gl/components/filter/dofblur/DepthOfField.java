package org.oreon.gl.components.filter.dofblur;

import lombok.Getter;

import org.oreon.core.context.ContextHolder;
import org.oreon.core.gl.framebuffer.GLFramebuffer;
import org.oreon.core.gl.surface.FullScreenQuad;
import org.oreon.core.gl.texture.GLTexture;
import org.oreon.core.gl.wrapper.texture.TextureImage2D;
import org.oreon.core.gl.wrapper.texture.TextureStorage2D;
import org.oreon.core.image.Image.ImageFormat;
import org.oreon.core.image.Image.SamplerFilter;
import org.oreon.core.image.Image.TextureWrapMode;

import static org.lwjgl.opengl.GL11.glFinish;
import static org.lwjgl.opengl.GL15.GL_READ_ONLY;
import static org.lwjgl.opengl.GL15.GL_WRITE_ONLY;
import static org.lwjgl.opengl.GL30.GL_RGBA16F;
import static org.lwjgl.opengl.GL42.glBindImageTexture;
import static org.lwjgl.opengl.GL43.glDispatchCompute;

public class DepthOfField {

  @Getter
  private GLTexture horizontalBlurSceneTexture;
  @Getter
  private GLTexture verticalBlurSceneTexture;

  private DepthOfFieldHorizontalBlurShader horizontalBlurShader;
  private DepthOfFieldVerticalBlurShader verticalBlurShader;

  private GLFramebuffer lowResFbo;
  @SuppressWarnings("unused")
  private FullScreenQuad fullScreenQuad;
  @Getter
  private GLTexture downsampledSceneSampler;

  public DepthOfField() {

    horizontalBlurShader = DepthOfFieldHorizontalBlurShader.getInstance();
    verticalBlurShader = DepthOfFieldVerticalBlurShader.getInstance();

    horizontalBlurSceneTexture = new TextureImage2D(ContextHolder.getContext().getConfig().getFrameWidth(),
        ContextHolder.getContext().getConfig().getFrameHeight(), ImageFormat.RGBA16FLOAT, SamplerFilter.Bilinear,
        TextureWrapMode.ClampToEdge);

    verticalBlurSceneTexture = new TextureImage2D(ContextHolder.getContext().getConfig().getFrameWidth(),
        ContextHolder.getContext().getConfig().getFrameHeight(), ImageFormat.RGBA16FLOAT, SamplerFilter.Bilinear,
        TextureWrapMode.ClampToEdge);

    new TextureStorage2D(ContextHolder.getContext().getConfig().getFrameWidth(),
        ContextHolder.getContext().getConfig().getFrameHeight(), 1, ImageFormat.RGBA16FLOAT);

    downsampledSceneSampler = new TextureImage2D((int) (ContextHolder.getContext().getConfig().getFrameWidth() / 2f),
        (int) (ContextHolder.getContext().getConfig().getFrameWidth() / 2f), ImageFormat.RGBA16FLOAT,
        SamplerFilter.Bilinear, TextureWrapMode.ClampToEdge);

    fullScreenQuad = new FullScreenQuad();
    lowResFbo = new GLFramebuffer();
    lowResFbo.bind();
    lowResFbo.createColorTextureAttachment(downsampledSceneSampler.getHandle(), 0);
    lowResFbo.checkStatus();
    lowResFbo.unbind();
  }

  public void render(GLTexture depthmap, GLTexture sceneSampler) {

//		glFinish();
//		lowResFbo.bind();
//		fullScreenQuad.setTexture(sceneSampler);
//		glViewport(0,0,(int)(BaseContext.getConfig().getX_ScreenResolution()/2f),
//				(int)(BaseContext.getConfig().getY_ScreenResolution()/2f));
//		fullScreenQuad.render();
//		lowResFbo.unbind();
//		glViewport(0,0, BaseContext.getConfig().getX_ScreenResolution(), BaseContext.getConfig().getY_ScreenResolution());
    glFinish();

    horizontalBlurShader.bind();
    glBindImageTexture(0, horizontalBlurSceneTexture.getHandle(), 0, false, 0, GL_WRITE_ONLY, GL_RGBA16F);
    horizontalBlurShader.updateUniforms(depthmap, sceneSampler, downsampledSceneSampler);
    glDispatchCompute(ContextHolder.getContext().getConfig().getFrameWidth() / 8,
        ContextHolder.getContext().getConfig().getFrameHeight() / 8, 1);
    glFinish();

    verticalBlurShader.bind();
    glBindImageTexture(0, horizontalBlurSceneTexture.getHandle(), 0, false, 0, GL_READ_ONLY, GL_RGBA16F);
    glBindImageTexture(1, verticalBlurSceneTexture.getHandle(), 0, false, 0, GL_WRITE_ONLY, GL_RGBA16F);
    verticalBlurShader.updateUniforms(depthmap);
    glDispatchCompute(ContextHolder.getContext().getConfig().getFrameWidth() / 8,
        ContextHolder.getContext().getConfig().getFrameHeight() / 8, 1);
    glFinish();
  }

}
