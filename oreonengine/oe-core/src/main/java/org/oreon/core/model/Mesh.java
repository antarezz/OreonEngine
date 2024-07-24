package org.oreon.core.model;

import java.util.ArrayList;
import java.util.List;

import org.oreon.core.math.Vec2f;
import org.oreon.core.model.Vertex.VertexLayout;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class Mesh {

  private Vertex[] vertices;
  private int[] indices;
  private VertexLayout vertexLayout;
  private boolean tangentSpace = false;

  public Mesh(Vertex[] vertices, int[] indices) {
    this.vertices = vertices;
    this.indices = indices;
  }

  public List<Vec2f> getUvCoords() {

    ArrayList<Vec2f> uvCoords = new ArrayList<Vec2f>();

    for (Vertex v : vertices) {
      uvCoords.add(v.getUVCoord());
    }

    return uvCoords;
  }

  public Vertex[] getVertices() {
    return vertices;
  }

  public void setVertices(Vertex[] vertices) {
    this.vertices = vertices;
  }

  public int[] getIndices() {
    return indices;
  }

  public void setIndices(int[] indices) {
    this.indices = indices;
  }

  public boolean isTangentSpace() {
    return tangentSpace;
  }

  public void setTangentSpace(boolean tangentSpace) {
    this.tangentSpace = tangentSpace;
  }

  public VertexLayout getVertexLayout() {
    return vertexLayout;
  }

  public void setVertexLayout(VertexLayout vertexLayout) {
    this.vertexLayout = vertexLayout;
  }
}
