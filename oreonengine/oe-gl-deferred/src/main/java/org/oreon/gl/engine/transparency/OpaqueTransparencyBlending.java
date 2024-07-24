package org.oreon.gl.engine.transparency;

import lombok.Getter;

import org.oreon.core.context.ContextHolder;
import org.oreon.core.gl.surface.FullScreenQuad;
import org.oreon.core.gl.texture.GLTexture;
import org.oreon.core.gl.wrapper.texture.TextureImage2D;
import org.oreon.core.image.Image.ImageFormat;
import org.oreon.core.image.Image.SamplerFilter;

import static org.lwjgl.opengl.GL11.glFinish;
import static org.lwjgl.opengl.GL15.GL_READ_ONLY;
import static org.lwjgl.opengl.GL15.GL_WRITE_ONLY;
import static org.lwjgl.opengl.GL30.GL_RGBA16F;
import static org.lwjgl.opengl.GL42.glBindImageTexture;
import static org.lwjgl.opengl.GL43.glDispatchCompute;

public class OpaqueTransparencyBlending extends FullScreenQuad {

  private OpaqueTransparencyBlendShader shader;
  @Getter
  private GLTexture blendedSceneTexture;
  @Getter
  private GLTexture blendedLightScatteringTexture;

  public OpaqueTransparencyBlending(int width, int height) {

    super();
    shader = OpaqueTransparencyBlendShader.getInstance();
    blendedSceneTexture = new TextureImage2D(width, height,
        ImageFormat.RGBA16FLOAT, SamplerFilter.Nearest);
    blendedLightScatteringTexture = new TextureImage2D(width, height,
        ImageFormat.RGBA16FLOAT, SamplerFilter.Nearest);
  }

  public void render(GLTexture opaqueScene, GLTexture opaqueSceneDepthMap,
      GLTexture opaqueSceneLightScatteringTexture,
      GLTexture transparencyLayer, GLTexture transparencyLayerDepthMap,
      GLTexture alphaMap, GLTexture transparencyLayerLightScatteringTexture) {

    glFinish();

    shader.bind();
    glBindImageTexture(0, blendedSceneTexture.getHandle(), 0, false, 0, GL_WRITE_ONLY, GL_RGBA16F);
    glBindImageTexture(1, blendedLightScatteringTexture.getHandle(), 0, false, 0, GL_WRITE_ONLY, GL_RGBA16F);
    glBindImageTexture(2, opaqueScene.getHandle(), 0, false, 0, GL_READ_ONLY, GL_RGBA16F);
    glBindImageTexture(3, transparencyLayer.getHandle(), 0, false, 0, GL_READ_ONLY, GL_RGBA16F);
    shader.updateUniforms(opaqueSceneLightScatteringTexture,
        alphaMap, transparencyLayerLightScatteringTexture,
        opaqueSceneDepthMap, transparencyLayerDepthMap);
    glDispatchCompute(
        ContextHolder.getContext().getWindow().getWidth() / 16,
        ContextHolder.getContext().getWindow().getHeight() / 16,
        1
    );
  }

}
