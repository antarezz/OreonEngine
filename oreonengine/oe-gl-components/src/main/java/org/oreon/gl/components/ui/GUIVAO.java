package org.oreon.gl.components.ui;

import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL11.GL_TRIANGLES;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_INT;
import static org.lwjgl.opengl.GL11.glDrawElements;
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.GL_DYNAMIC_DRAW;
import static org.lwjgl.opengl.GL15.GL_ELEMENT_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.GL_STATIC_DRAW;
import static org.lwjgl.opengl.GL15.glBindBuffer;
import static org.lwjgl.opengl.GL15.glBufferData;
import static org.lwjgl.opengl.GL15.glBufferSubData;
import static org.lwjgl.opengl.GL15.glGenBuffers;
import static org.lwjgl.opengl.GL20.glDisableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;

import java.util.List;

import org.oreon.core.math.Vec2f;
import org.oreon.core.model.Mesh;
import org.oreon.core.util.BufferUtil;

public class GUIVAO {

  private int vbo;
  private int ibo;
  private int vaoId;
  private int indices;
  private int vertices;
  private int offset;

  public GUIVAO() {
    vbo = glGenBuffers();
    ibo = glGenBuffers();
    vaoId = glGenVertexArrays();
    indices = 0;
    vertices = 0;
    offset = 24;
  }

  public void addData(Mesh mesh) {
    indices = mesh.getIndices().length;
    vertices = mesh.getVertices().length;

    glBindVertexArray(vaoId);

    glBindBuffer(GL_ARRAY_BUFFER, vbo);
    glBufferData(GL_ARRAY_BUFFER, BufferUtil.createFlippedBufferSOA(mesh.getVertices()), GL_DYNAMIC_DRAW);

    glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ibo);
    glBufferData(GL_ELEMENT_ARRAY_BUFFER, BufferUtil.createFlippedBuffer(mesh.getIndices()), GL_STATIC_DRAW);

    glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
    glVertexAttribPointer(1, 2, GL_FLOAT, false, 0, vertices * offset);

    glBindVertexArray(0);
  }

  public void draw() {

    glBindVertexArray(vaoId);

    glEnableVertexAttribArray(0);
    glEnableVertexAttribArray(1);

    glDrawElements(GL_TRIANGLES, indices, GL_UNSIGNED_INT, 0);

    glDisableVertexAttribArray(0);
    glDisableVertexAttribArray(1);

    glBindVertexArray(0);
  }

  public void update(Vec2f[] texCoords) {
    glBindVertexArray(vaoId);
    glBindBuffer(GL_ARRAY_BUFFER, vbo);
    glBufferSubData(GL_ARRAY_BUFFER, vertices * offset, BufferUtil.createFlippedBuffer(texCoords));
    glBindVertexArray(0);
  }

  public void update(List<Vec2f> texCoords) {
    glBindVertexArray(vaoId);
    glBindBuffer(GL_ARRAY_BUFFER, vbo);
    glBufferSubData(GL_ARRAY_BUFFER, vertices * offset, BufferUtil.createFlippedBuffer(texCoords));
    glBindVertexArray(0);
  }
}
