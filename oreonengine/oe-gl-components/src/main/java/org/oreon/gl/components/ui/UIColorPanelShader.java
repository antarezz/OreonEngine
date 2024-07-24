package org.oreon.gl.components.ui;

import org.oreon.core.gl.pipeline.GLShaderProgram;
import org.oreon.core.math.Matrix4f;
import org.oreon.core.math.Vec4f;
import org.oreon.core.util.ResourceLoader;

public class UIColorPanelShader extends GLShaderProgram {

  private static UIColorPanelShader instance = null;

  public static UIColorPanelShader getInstance() {
    if (instance == null) {
      instance = new UIColorPanelShader();
    }
    return instance;
  }

  protected UIColorPanelShader() {
    super();

    addVertexShader(ResourceLoader.loadShader("shaders/ui/colorPanel.vert"));
    addFragmentShader(ResourceLoader.loadShader("shaders/ui/colorPanel.frag"));
    compileShader();

    addUniform("orthographicMatrix");
    addUniform("rgba");
  }

  public void updateUniforms(Matrix4f orthographicMatrix) {
    setUniform("orthographicMatrix", orthographicMatrix);
  }

  public void updateUniforms(Vec4f rgba) {
    setUniform("rgba", rgba);
  }
}
