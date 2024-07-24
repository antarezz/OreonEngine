package org.oreon.core.gl.surface;

import org.oreon.core.gl.memory.GLMeshVBO;
import org.oreon.core.gl.pipeline.GLShaderProgram;
import org.oreon.core.gl.pipeline.RenderParameter;
import org.oreon.core.gl.texture.GLTexture;
import org.oreon.core.gl.wrapper.parameter.DefaultRenderParams;
import org.oreon.core.math.Vec2f;
import org.oreon.core.util.MeshGenerator;

import lombok.Getter;
import lombok.Setter;

public class FullScreenQuad {

  @Getter
  @Setter
  private GLTexture texture;
  private GLShaderProgram shader;
  private GLMeshVBO vao;
  private RenderParameter config;
  protected Vec2f[] texCoords;

  public FullScreenQuad() {

    shader = FullScreenQuadShader.getInstance();
    config = new DefaultRenderParams();
    vao = new GLMeshVBO();
    vao.addData(MeshGenerator.NDCQuad2D());
  }


  public void render() {
    getConfig().enable();
    getShader().bind();
    getShader().updateUniforms(texture);
    getVao().draw();
    getConfig().disable();
  }

  public RenderParameter getConfig() {
    return config;
  }

  public void setConfig(RenderParameter config) {
    this.config = config;
  }

  public GLShaderProgram getShader() {
    return shader;
  }

  public void setShader(GLShaderProgram shader) {
    this.shader = shader;
  }

  public GLMeshVBO getVao() {
    return vao;
  }

  public void setVao(GLMeshVBO vao) {
    this.vao = vao;
  }

}
