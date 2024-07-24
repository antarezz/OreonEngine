package org.oreon.core.gl.wrapper.parameter;

import static org.lwjgl.opengl.GL11.GL_DEPTH_TEST;
import static org.lwjgl.opengl.GL11.GL_BLEND;
import static org.lwjgl.opengl.GL11.GL_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_ONE;
import static org.lwjgl.opengl.GL11.glBlendFunc;
import static org.lwjgl.opengl.GL11.glDisable;
import static org.lwjgl.opengl.GL11.glEnable;

import org.oreon.core.gl.pipeline.RenderParameter;


public class AdditiveBlending implements RenderParameter {

  public AdditiveBlending() {
  }

  public void enable() {
    glDisable(GL_DEPTH_TEST);
    glEnable(GL_BLEND);
    glBlendFunc(GL_SRC_ALPHA, GL_ONE);
  }

  public void disable() {
    glEnable(GL_DEPTH_TEST);
    glDisable(GL_BLEND);
  }
}
