package org.oreon.gl.components.ui;

import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.glActiveTexture;

import org.oreon.common.ui.UITextPanel;
import org.oreon.core.gl.pipeline.GLShaderProgram;
import org.oreon.core.gl.pipeline.RenderParameter;
import org.oreon.core.gl.texture.GLTexture;
import org.oreon.core.gl.wrapper.parameter.DefaultRenderParams;

public class GLStaticTextPanel extends UITextPanel {

  private GLShaderProgram shader;
  private RenderParameter config;
  private GUIVAO vao;
  private GLTexture texture;

  public GLStaticTextPanel(String text, int xPos, int yPos, int xScaling, int yScaling,
      GLTexture fontsTexture) {
    super(text, xPos, yPos, xScaling, yScaling);
    texture = fontsTexture;
    shader = UITextPanelShader.getInstance();
    vao = new GUIVAO();
    config = new DefaultRenderParams();
    vao.addData(panel);
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
