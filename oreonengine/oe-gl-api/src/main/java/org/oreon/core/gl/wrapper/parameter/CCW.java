package org.oreon.core.gl.wrapper.parameter;

import static org.lwjgl.opengl.GL11.GL_CCW;
import static org.lwjgl.opengl.GL11.GL_CW;
import static org.lwjgl.opengl.GL11.glFrontFace;

import org.oreon.core.gl.pipeline.RenderParameter;

public class CCW implements RenderParameter {

  public void enable() {
    glFrontFace(GL_CCW);
  }

  public void disable() {
    glFrontFace(GL_CW);
  }
}
