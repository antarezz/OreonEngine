package org.oreon.gl.components.ui;

import org.oreon.core.gl.pipeline.GLShaderProgram;
import org.oreon.core.math.Matrix4f;
import org.oreon.core.util.ResourceLoader;

public class UITextPanelShader extends GLShaderProgram {

  private static UITextPanelShader instance = null;

  public static UITextPanelShader getInstance() {
    if (instance == null) {
      instance = new UITextPanelShader();
    }
    return instance;
  }

  protected UITextPanelShader() {
    super();

    addVertexShader(ResourceLoader.loadShader("shaders/ui/textPanel.vert"));
    addFragmentShader(ResourceLoader.loadShader("shaders/ui/textPanel.frag"));
    compileShader();

    addUniform("orthographicMatrix");
    addUniform("texture");
  }

  public void updateUniforms(Matrix4f orthographicMatrix) {
    setUniform("orthographicMatrix", orthographicMatrix);
  }

  public void updateUniforms(int texture) {
    setUniformi("texture", texture);
  }
}
