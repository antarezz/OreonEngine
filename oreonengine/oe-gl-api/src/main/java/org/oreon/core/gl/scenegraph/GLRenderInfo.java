package org.oreon.core.gl.scenegraph;

import org.oreon.core.gl.memory.VBO;
import org.oreon.core.gl.pipeline.GLShaderProgram;
import org.oreon.core.gl.pipeline.RenderParameter;
import org.oreon.core.scenegraph.NodeComponent;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class GLRenderInfo extends NodeComponent {

  private GLShaderProgram shader;
  private RenderParameter config;
  private VBO vbo;

  @Override
  public void render() {

    config.enable();
    shader.bind();
    shader.updateUniforms(getParent());
    vbo.draw();
    config.disable();
  }

}
