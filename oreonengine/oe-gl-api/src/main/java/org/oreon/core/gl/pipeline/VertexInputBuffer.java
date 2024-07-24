package org.oreon.core.gl.pipeline;

import static org.lwjgl.opengl.GL15.glBindBuffer;
import static org.lwjgl.opengl.GL15.glBufferData;
import static org.lwjgl.opengl.GL15.glDeleteBuffers;
import static org.lwjgl.opengl.GL15.glGenBuffers;
import static org.lwjgl.opengl.GL30.glBindBufferBase;

import java.nio.ByteBuffer;

import lombok.Getter;

public abstract class VertexInputBuffer {

  @Getter
  private int handle;
  private int target;

  public VertexInputBuffer(int target) {

    handle = glGenBuffers();
  }

  public void bind() {

    glBindBuffer(target, handle);
  }

  public void unbind() {

    glBindBuffer(target, 0);
  }

  public void bindBufferBase(int binding) {

    glBindBufferBase(target, binding, handle);
  }

  public void allocate(ByteBuffer data, int usage) {

    glBufferData(target, data, usage);
  }

  public void delete() {

    glDeleteBuffers(handle);
  }
}
