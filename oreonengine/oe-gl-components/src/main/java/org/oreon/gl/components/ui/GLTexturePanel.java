package org.oreon.gl.components.ui;

import static org.lwjgl.opengl.GL11.GL_ONE_MINUS_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_SRC_ALPHA;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.glActiveTexture;

import org.oreon.common.ui.UIElement;
import org.oreon.core.gl.pipeline.GLShaderProgram;
import org.oreon.core.gl.pipeline.RenderParameter;
import org.oreon.core.gl.texture.GLTexture;
import org.oreon.core.gl.wrapper.parameter.AlphaBlending;
import org.oreon.core.gl.wrapper.texture.TextureImage2D;
import org.oreon.core.image.Image.SamplerFilter;

public class GLTexturePanel extends UIElement {

  private GLShaderProgram shader;
  private RenderParameter config;
  private GUIVAO vao;
  private GLTexture texture;

  public GLTexturePanel(String imageFile, int xPos, int yPos, int xScaling, int yScaling,
      GUIVAO panelMeshBuffer) {
    super(xPos, yPos, xScaling, yScaling);
    shader = UITexturePanelShader.getInstance();
    vao = panelMeshBuffer;
    config = new AlphaBlending(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
    texture = new TextureImage2D(imageFile, SamplerFilter.Bilinear);
  }

  public void render() {
    config.enable();
    shader.bind();
    shader.updateUniforms(getOrthographicMatrix());
    glActiveTexture(GL_TEXTURE0);
    texture.bind();
    shader.updateUniforms(0);
    vao.draw();
    config.disable();
  }
}
