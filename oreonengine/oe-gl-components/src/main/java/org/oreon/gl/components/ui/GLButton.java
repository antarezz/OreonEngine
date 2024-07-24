package org.oreon.gl.components.ui;

import static org.lwjgl.glfw.GLFW.glfwGetCursorPos;
import static org.lwjgl.opengl.GL13.GL_TEXTURE1;
import static org.lwjgl.opengl.GL13.glActiveTexture;

import java.nio.DoubleBuffer;
import org.lwjgl.BufferUtils;
import org.oreon.common.ui.UIButton;
import org.oreon.core.context.ContextHolder;
import org.oreon.core.gl.pipeline.GLShaderProgram;
import org.oreon.core.gl.pipeline.RenderParameter;
import org.oreon.core.gl.texture.GLTexture;
import org.oreon.core.gl.wrapper.parameter.DefaultRenderParams;
import org.oreon.core.math.Vec2f;
import org.oreon.core.math.Vec4f;
import org.oreon.core.model.Mesh;

public abstract class GLButton extends UIButton {

  private GLShaderProgram shader;
  private RenderParameter config;
  private GUIVAO vao;
  protected GLTexture buttonTexture;
  protected GLTexture buttonClickTexture;
  private boolean onClick = false;
  private Vec2f[] pos;

  public GLButton(int xPos, int yPos, int xScaling, int yScaling,
      Mesh panelMesh) {
    super(xPos, yPos, xScaling, yScaling);
    pos = new Vec2f[4];
    shader = UITextPanelShader.getInstance();
    vao = new GUIVAO();
    config = new DefaultRenderParams();
    vao.addData(panelMesh);
    Vec4f q0 = new Vec4f(0, 0, 0, 0);
    Vec4f q1 = new Vec4f(0, 0, 0, 0);
    Vec4f q2 = new Vec4f(0, 0, 0, 0);
    Vec4f q3 = new Vec4f(0, 0, 0, 0);
    q0 = getWorldTransform().getWorldMatrix().mul(new Vec4f(panelMesh.getVertices()[0].getPosition(), 1));
    q1 = getWorldTransform().getWorldMatrix().mul(new Vec4f(panelMesh.getVertices()[1].getPosition(), 1));
    q2 = getWorldTransform().getWorldMatrix().mul(new Vec4f(panelMesh.getVertices()[2].getPosition(), 1));
    q3 = getWorldTransform().getWorldMatrix().mul(new Vec4f(panelMesh.getVertices()[3].getPosition(), 1));
    pos[0] = new Vec2f(q0.getX(), q0.getY() + 5);
    pos[1] = new Vec2f(q1.getX(), q1.getY() + 5);
    pos[2] = new Vec2f(q2.getX() - 7, q2.getY() - 5);
    pos[3] = new Vec2f(q3.getX() - 7, q3.getY() - 5);
  }

  public void render() {
    config.enable();
    shader.bind();
    shader.updateUniforms(getOrthographicMatrix());
    glActiveTexture(GL_TEXTURE1);
    if (onClick) {
      buttonClickTexture.bind();
    } else {
      buttonTexture.bind();
    }
    shader.updateUniforms(1);
    vao.draw();
    config.disable();
  }

  public void update() {
    if (ContextHolder.getContext().getInput().isButtonPushed(0)) {
      if (onClick()) {
        onClick = true;
        onClickActionPerformed();
      }
    }

    if (ContextHolder.getContext().getInput().isButtonReleased(0)) {
      onClick = false;
    }
  }

  public boolean onClick() {
    DoubleBuffer xPos = BufferUtils.createDoubleBuffer(1);
    DoubleBuffer yPos = BufferUtils.createDoubleBuffer(1);

    glfwGetCursorPos(ContextHolder.getContext().getWindow().getId(), xPos, yPos);

    Vec2f mousePos = new Vec2f((float) xPos.get(), (float) yPos.get());

    if (pos[0].getX() < mousePos.getX() &&
        pos[1].getX() < mousePos.getX() &&
        pos[2].getX() > mousePos.getX() &&
        pos[3].getX() > mousePos.getX() &&
        pos[0].getY() < ContextHolder.getContext().getWindow().getHeight() - mousePos.getY() &&
        pos[3].getY() < ContextHolder.getContext().getWindow().getHeight() - mousePos.getY() &&
        pos[1].getY() > ContextHolder.getContext().getWindow().getHeight() - mousePos.getY() &&
        pos[2].getY() > ContextHolder.getContext().getWindow().getHeight() - mousePos.getY()) {

      return true;
    } else {
      return false;
    }
  }

  public void onClickActionPerformed() {
  }
}
