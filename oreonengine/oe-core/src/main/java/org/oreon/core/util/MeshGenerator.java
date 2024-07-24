package org.oreon.core.util;

import org.oreon.core.math.Vec2f;
import org.oreon.core.math.Vec3f;
import org.oreon.core.model.Mesh;
import org.oreon.core.model.Vertex;
import org.oreon.core.model.Vertex.VertexLayout;

public class MeshGenerator {

  public static Mesh NDCQuad2D() {
    Vertex[] vertices = new Vertex[4];
    int[] indices = {0, 2, 1, 1, 2, 3};
    vertices[0] = new Vertex(new Vec3f(-1, -1, 0), new Vec2f(0, 0));
    vertices[1] = new Vertex(new Vec3f(1, -1, 0), new Vec2f(1, 0));
    vertices[2] = new Vertex(new Vec3f(-1, 1, 0), new Vec2f(0, 1));
    vertices[3] = new Vertex(new Vec3f(1, 1, 0), new Vec2f(1, 1));
    Mesh quad = new Mesh(vertices, indices);
    quad.setVertexLayout(VertexLayout.POS_UV);
    return quad;
  }

  public static Mesh NDCQuad2Drot180() {
    Vertex[] vertices = new Vertex[4];
    int[] indices = {0, 2, 1, 1, 2, 3};
    vertices[0] = new Vertex(new Vec3f(-1, -1, 0), new Vec2f(0, 1));
    vertices[1] = new Vertex(new Vec3f(1, -1, 0), new Vec2f(1, 1));
    vertices[2] = new Vertex(new Vec3f(-1, 1, 0), new Vec2f(0, 0));
    vertices[3] = new Vertex(new Vec3f(1, 1, 0), new Vec2f(1, 0));
    Mesh quad = new Mesh(vertices, indices);
    quad.setVertexLayout(VertexLayout.POS_UV);
    return quad;
  }


  public static Mesh Cube() {
    Vertex[] vertices = new Vertex[24];
    int[] indices = {0, 1, 2, 0, 2, 3, 4, 5, 6, 4, 6, 7, 8, 9, 10, 8, 10, 11, 12, 13,
        14, 12, 14, 15, 16, 17, 18, 16, 18, 19, 20, 21, 22, 20, 22, 23};

    vertices[0] = new Vertex(new Vec3f(-1, -1, -1), new Vec2f(0, 1));
    vertices[1] = new Vertex(new Vec3f(-1, 1, -1), new Vec2f(0, 0));
    vertices[2] = new Vertex(new Vec3f(1, 1, -1), new Vec2f(1, 0));
    vertices[3] = new Vertex(new Vec3f(1, -1, -1), new Vec2f(1, 1));

    vertices[4] = new Vertex(new Vec3f(-1, -1, 1), new Vec2f(0, 1));
    vertices[5] = new Vertex(new Vec3f(-1, 1, 1), new Vec2f(0, 0));
    vertices[6] = new Vertex(new Vec3f(-1, 1, -1), new Vec2f(1, 0));
    vertices[7] = new Vertex(new Vec3f(-1, -1, -1), new Vec2f(1, 1));

    vertices[8] = new Vertex(new Vec3f(1, -1, 1), new Vec2f(0, 1));
    vertices[9] = new Vertex(new Vec3f(1, 1, 1), new Vec2f(0, 0));
    vertices[10] = new Vertex(new Vec3f(-1, 1, 1), new Vec2f(1, 0));
    vertices[11] = new Vertex(new Vec3f(-1, -1, 1), new Vec2f(1, 1));

    vertices[12] = new Vertex(new Vec3f(1, -1, -1), new Vec2f(0, 1));
    vertices[13] = new Vertex(new Vec3f(1, 1, -1), new Vec2f(0, 0));
    vertices[14] = new Vertex(new Vec3f(1, 1, 1), new Vec2f(1, 0));
    vertices[15] = new Vertex(new Vec3f(1, -1, 1), new Vec2f(1, 1));

    vertices[16] = new Vertex(new Vec3f(-1, 1, -1), new Vec2f(0, 1));
    vertices[17] = new Vertex(new Vec3f(-1, 1, 1), new Vec2f(0, 0));
    vertices[18] = new Vertex(new Vec3f(1, 1, 1), new Vec2f(1, 0));
    vertices[19] = new Vertex(new Vec3f(1, 1, -1), new Vec2f(1, 1));

    vertices[20] = new Vertex(new Vec3f(1, -1, -1), new Vec2f(0, 1));
    vertices[21] = new Vertex(new Vec3f(1, -1, 1), new Vec2f(0, 0));
    vertices[22] = new Vertex(new Vec3f(-1, -1, 1), new Vec2f(1, 0));
    vertices[23] = new Vertex(new Vec3f(-1, -1, -1), new Vec2f(1, 1));

    return new Mesh(vertices, indices);
  }

  public static Vec2f[] TerrainChunkMesh() {

    // 16 vertices for each patch
    Vec2f[] vertices = new Vec2f[16];

    int index = 0;

    vertices[index++] = new Vec2f(0, 0);
    vertices[index++] = new Vec2f(0.333f, 0);
    vertices[index++] = new Vec2f(0.666f, 0);
    vertices[index++] = new Vec2f(1, 0);

    vertices[index++] = new Vec2f(0, 0.333f);
    vertices[index++] = new Vec2f(0.333f, 0.333f);
    vertices[index++] = new Vec2f(0.666f, 0.333f);
    vertices[index++] = new Vec2f(1, 0.333f);

    vertices[index++] = new Vec2f(0, 0.666f);
    vertices[index++] = new Vec2f(0.333f, 0.666f);
    vertices[index++] = new Vec2f(0.666f, 0.666f);
    vertices[index++] = new Vec2f(1, 0.666f);

    vertices[index++] = new Vec2f(0, 1);
    vertices[index++] = new Vec2f(0.333f, 1);
    vertices[index++] = new Vec2f(0.666f, 1);
    vertices[index++] = new Vec2f(1, 1);

    return vertices;
  }

  public static Vec2f[] generatePatch2D4x4(int patches) {

    int amountx = patches;
    int amounty = patches;

    // 16 vertices for each patch
    Vec2f[] vertices = new Vec2f[amountx * amounty * 16];

    int index = 0;
    float dx = 1f / amountx;
    float dy = 1f / amounty;

    for (float i = 0; i < 1; i += dx) {
      for (float j = 0; j < 1; j += dy) {
        vertices[index++] = new Vec2f(i, j);
        vertices[index++] = new Vec2f(i + dx * 0.33f, j);
        vertices[index++] = new Vec2f(i + dx * 0.66f, j);
        vertices[index++] = new Vec2f(i + dx, j);

        vertices[index++] = new Vec2f(i, j + dy * 0.33f);
        vertices[index++] = new Vec2f(i + dx * 0.33f, j + dy * 0.33f);
        vertices[index++] = new Vec2f(i + dx * 0.66f, j + dy * 0.33f);
        vertices[index++] = new Vec2f(i + dx, j + dy * 0.33f);

        vertices[index++] = new Vec2f(i, j + dy * 0.66f);
        vertices[index++] = new Vec2f(i + dx * 0.33f, j + dy * 0.66f);
        vertices[index++] = new Vec2f(i + dx * 0.66f, j + dy * 0.66f);
        vertices[index++] = new Vec2f(i + dx, j + dy * 0.66f);

        vertices[index++] = new Vec2f(i, j + dy);
        vertices[index++] = new Vec2f(i + dx * 0.33f, j + dy);
        vertices[index++] = new Vec2f(i + dx * 0.66f, j + dy);
        vertices[index++] = new Vec2f(i + dx, j + dy);
      }
    }

    return vertices;
  }
}
