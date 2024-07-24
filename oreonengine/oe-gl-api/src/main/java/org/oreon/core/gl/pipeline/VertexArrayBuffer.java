package org.oreon.core.gl.pipeline;

import static org.lwjgl.opengl.GL11.GL_UNSIGNED_INT;
import static org.lwjgl.opengl.GL11.glDrawArrays;
import static org.lwjgl.opengl.GL11.glDrawElements;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL30.glDeleteVertexArrays;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;
import static org.lwjgl.opengl.GL31.glDrawElementsInstanced;

import lombok.Getter;

public abstract class VertexArrayBuffer {

  @Getter
  private int handle;

  public VertexArrayBuffer() {

    handle = glGenVertexArrays();
  }

  public void bind() {

    glBindVertexArray(handle);
  }

  public void unbind() {

    glBindVertexArray(0);
  }

  public void setAttributePointer(int index, int size, int type, boolean normalized, int stride, int offset) {

    glVertexAttribPointer(index, size, type, normalized, stride, offset);
  }

  public void drawElements(int mode, int size) {

    glDrawElements(mode, size, GL_UNSIGNED_INT, 0);
  }

  public void drawElementsInstanced(int mode, int size, int count) {

    glDrawElementsInstanced(mode, size, GL_UNSIGNED_INT, 0, count);
  }

  public void drawArrays(int mode, int count) {

    glDrawArrays(mode, 0, count);
  }

  public void delete() {

    glDeleteVertexArrays(handle);
  }
}
