package org.oreon.core.gl.memory;

import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL11.GL_TRIANGLES;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_INT;
import static org.lwjgl.opengl.GL11.glDrawElements;
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.GL_ELEMENT_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.GL_STATIC_DRAW;
import static org.lwjgl.opengl.GL15.glBindBuffer;
import static org.lwjgl.opengl.GL15.glBufferData;
import static org.lwjgl.opengl.GL15.glDeleteBuffers;
import static org.lwjgl.opengl.GL15.glGenBuffers;
import static org.lwjgl.opengl.GL20.glDisableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL30.glDeleteVertexArrays;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;
import static org.lwjgl.opengl.GL31.glDrawElementsInstanced;

import org.oreon.core.model.Mesh;
import org.oreon.core.model.Vertex;
import org.oreon.core.util.BufferUtil;
import org.oreon.core.util.IntegerReference;

public class GLMeshVBO implements VBO {

  private int vbo;
  private int ibo;
  private int vaoId;
  private int size;
  private boolean hasTangentsBitangents;
  private IntegerReference instances;
  private boolean drawInstanced;

  public GLMeshVBO() {
    vbo = glGenBuffers();
    ibo = glGenBuffers();
    vaoId = glGenVertexArrays();
    size = 0;
    instances = new IntegerReference();
  }

  public void addData(Mesh mesh) {
    size = mesh.getIndices().length;
    hasTangentsBitangents = mesh.isTangentSpace();

    glBindVertexArray(vaoId);

    glBindBuffer(GL_ARRAY_BUFFER, vbo);
    glBufferData(GL_ARRAY_BUFFER, BufferUtil.createFlippedBufferAOS(mesh.getVertices()), GL_STATIC_DRAW);

    glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ibo);
    glBufferData(GL_ELEMENT_ARRAY_BUFFER, BufferUtil.createFlippedBuffer(mesh.getIndices()), GL_STATIC_DRAW);

    if (mesh.isTangentSpace()) {
      glVertexAttribPointer(0, 3, GL_FLOAT, false, Vertex.BYTES, 0);
      glVertexAttribPointer(1, 3, GL_FLOAT, false, Vertex.BYTES, Float.BYTES * 3);
      glVertexAttribPointer(2, 2, GL_FLOAT, false, Vertex.BYTES, Float.BYTES * 6);
      glVertexAttribPointer(3, 3, GL_FLOAT, false, Vertex.BYTES, Float.BYTES * 8);
      glVertexAttribPointer(4, 3, GL_FLOAT, false, Vertex.BYTES, Float.BYTES * 11);
    } else {
      glVertexAttribPointer(0, 3, GL_FLOAT, false, Float.BYTES * 8, 0);
      glVertexAttribPointer(1, 3, GL_FLOAT, false, Float.BYTES * 8, Float.BYTES * 3);
      glVertexAttribPointer(2, 2, GL_FLOAT, false, Float.BYTES * 8, Float.BYTES * 6);
    }

    glBindVertexArray(0);
  }


  public void draw() {
    glBindVertexArray(vaoId);

    glEnableVertexAttribArray(0);
    glEnableVertexAttribArray(1);
    glEnableVertexAttribArray(2);
    if (hasTangentsBitangents) {
      glEnableVertexAttribArray(3);
      glEnableVertexAttribArray(4);
    }

    if (drawInstanced) {
      if (instances.getValue() > 0) {
        glDrawElementsInstanced(GL_TRIANGLES, size, GL_UNSIGNED_INT, 0, instances.getValue());
      }
    } else {
      glDrawElements(GL_TRIANGLES, size, GL_UNSIGNED_INT, 0);
    }

    glDisableVertexAttribArray(0);
    glDisableVertexAttribArray(1);
    glDisableVertexAttribArray(2);
    if (hasTangentsBitangents) {
      glDisableVertexAttribArray(3);
      glDisableVertexAttribArray(4);
    }

    glBindVertexArray(0);
  }

  public void delete() {
    glBindVertexArray(vaoId);
    glDeleteBuffers(vbo);
    glDeleteVertexArrays(vaoId);
    glBindVertexArray(0);
  }

  public void setInstances(IntegerReference instances) {
    this.instances = instances;
  }

  public void setInstances(int instances) {
    this.instances.setValue(instances);
  }

  public void setDrawInstanced(boolean vDrawInstanced) {
    drawInstanced = vDrawInstanced;
  }
}
