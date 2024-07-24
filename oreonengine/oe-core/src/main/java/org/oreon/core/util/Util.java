package org.oreon.core.util;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;
import org.oreon.core.image.ImageMetaData;
import org.oreon.core.math.Vec2f;
import org.oreon.core.math.Vec3f;
import org.oreon.core.math.Vec4f;
import org.oreon.core.model.Mesh;
import org.oreon.core.model.Vertex;

public class Util {

  public static String[] removeEmptyStrings(String[] data) {
    ArrayList<String> result = new ArrayList<String>();

    for (int i = 0; i < data.length; i++) {
      if (!data[i].equals("")) {
        result.add(data[i]);
      }
    }

    String[] res = new String[result.size()];
    result.toArray(res);

    return res;
  }

  public static int[] toIntArray(Integer[] data) {
    int[] result = new int[data.length];

    for (int i = 0; i < data.length; i++) {
      result[i] = data[i].intValue();
    }

    return result;
  }

  public static int[] toIntArray(List<Integer> data) {
    int[] result = new int[data.size()];

    for (int i = 0; i < data.size(); i++) {
      result[i] = data.get(i).intValue();
    }

    return result;
  }

  public static Vertex[] toVertexArray(FloatBuffer data) {
    Vertex[] vertices = new Vertex[data.limit() / Vertex.FLOATS];

    for (int i = 0; i < vertices.length; i++) {
      vertices[i] = new Vertex();
      vertices[i].setPosition(new Vec3f(data.get(), data.get(), data.get()));
      vertices[i].setUVCoord(new Vec2f(data.get(), data.get()));
      vertices[i].setNormal(new Vec3f(data.get(), data.get(), data.get()));
    }

    return vertices;
  }

  public static Vertex[] toVertexArray(List<Vertex> data) {
    Vertex[] vertices = new Vertex[data.size()];

    for (int i = 0; i < vertices.length; i++) {
      vertices[i] = new Vertex();
      vertices[i].setPosition(data.get(i).getPosition());
      vertices[i].setUVCoord(data.get(i).getUVCoord());
      vertices[i].setNormal(data.get(i).getNormal());
      vertices[i].setTangent(data.get(i).getTangent());
      vertices[i].setBitangent(data.get(i).getBitangent());
    }

    return vertices;
  }

  public static void generateNormalsCW(Vertex[] vertices, int[] indices) {
    for (int i = 0; i < indices.length; i += 3) {
      Vec3f v0 = vertices[indices[i]].getPosition();
      Vec3f v1 = vertices[indices[i + 1]].getPosition();
      Vec3f v2 = vertices[indices[i + 2]].getPosition();

      Vec3f normal = v1.sub(v0).cross(v2.sub(v0)).normalize();

      vertices[indices[i]].setNormal(vertices[indices[i]].getNormal().add(normal));
      vertices[indices[i + 1]].setNormal(vertices[indices[i + 1]].getNormal().add(normal));
      vertices[indices[i + 2]].setNormal(vertices[indices[i + 2]].getNormal().add(normal));
    }

    for (int i = 0; i < vertices.length; ++i) {
      vertices[i].setNormal(vertices[i].getNormal().normalize());
    }
  }

  public static void generateNormalsCCW(Vertex[] vertices, int[] indices) {
    for (int i = 0; i < indices.length; i += 3) {
      Vec3f v0 = vertices[indices[i]].getPosition();
      Vec3f v1 = vertices[indices[i + 1]].getPosition();
      Vec3f v2 = vertices[indices[i + 2]].getPosition();

      Vec3f normal = v2.sub(v0).cross(v1.sub(v0)).normalize();

      vertices[indices[i]].setNormal(vertices[indices[i]].getNormal().add(normal));
      vertices[indices[i + 1]].setNormal(vertices[indices[i + 1]].getNormal().add(normal));
      vertices[indices[i + 2]].setNormal(vertices[indices[i + 2]].getNormal().add(normal));
    }

    for (int i = 0; i < vertices.length; ++i) {
      vertices[i].setNormal(vertices[i].getNormal().normalize());
    }
  }

  public static void generateNormalsCW(ArrayList<Vertex> vertices, ArrayList<Integer> indices) {
    for (int i = 0; i < indices.size(); i += 3) {
      Vec3f v0 = vertices.get(indices.get(i)).getPosition();
      Vec3f v1 = vertices.get(indices.get(i + 1)).getPosition();
      Vec3f v2 = vertices.get(indices.get(i + 2)).getPosition();

      Vec3f normal = v1.sub(v0).cross(v2.sub(v0)).normalize();

      vertices.get(indices.get(i)).setNormal(vertices.get(indices.get(i)).getNormal().add(normal));
      vertices.get(indices.get(i + 1)).setNormal(vertices.get(indices.get(i + 1)).getNormal().add(normal));
      vertices.get(indices.get(i + 2)).setNormal(vertices.get(indices.get(i + 2)).getNormal().add(normal));
    }

    for (int i = 0; i < vertices.size(); ++i) {
      vertices.get(i).setNormal(vertices.get(i).getNormal().normalize());
    }
  }

  public static void generateNormalsCCW(ArrayList<Vertex> vertices, ArrayList<Integer> indices) {
    for (int i = 0; i < indices.size(); i += 3) {
      Vec3f v0 = vertices.get(indices.get(i)).getPosition();
      Vec3f v1 = vertices.get(indices.get(i + 1)).getPosition();
      Vec3f v2 = vertices.get(indices.get(i + 2)).getPosition();

      Vec3f normal = v2.sub(v0).cross(v1.sub(v0)).normalize();

      vertices.get(indices.get(i)).setNormal(vertices.get(indices.get(i)).getNormal().add(normal));
      vertices.get(indices.get(i + 1)).setNormal(vertices.get(indices.get(i + 1)).getNormal().add(normal));
      vertices.get(indices.get(i + 2)).setNormal(vertices.get(indices.get(i + 2)).getNormal().add(normal));
    }

    for (int i = 0; i < vertices.size(); ++i) {
      vertices.get(i).setNormal(vertices.get(i).getNormal().normalize());
    }
  }

  public static void generateTangentsBitangents(Mesh mesh) {
    for (int i = 0; i < mesh.getIndices().length; i += 3) {
      Vec3f v0 = mesh.getVertices()[mesh.getIndices()[i]].getPosition();
      Vec3f v1 = mesh.getVertices()[mesh.getIndices()[i + 1]].getPosition();
      Vec3f v2 = mesh.getVertices()[mesh.getIndices()[i + 2]].getPosition();

      Vec2f uv0 = mesh.getVertices()[mesh.getIndices()[i]].getUVCoord();
      Vec2f uv1 = mesh.getVertices()[mesh.getIndices()[i + 1]].getUVCoord();
      Vec2f uv2 = mesh.getVertices()[mesh.getIndices()[i + 2]].getUVCoord();

      Vec3f e1 = v1.sub(v0);
      Vec3f e2 = v2.sub(v0);

      Vec2f deltaUV1 = uv1.sub(uv0);
      Vec2f deltaUV2 = uv2.sub(uv0);

      float r = (1.0f / (deltaUV1.getX() * deltaUV2.getY() - deltaUV1.getY() * deltaUV2.getX()));

      Vec3f tangent = new Vec3f();
      tangent.setX(r * deltaUV2.getY() * e1.getX() - deltaUV1.getY() * e2.getX());
      tangent.setY(r * deltaUV2.getY() * e1.getY() - deltaUV1.getY() * e2.getY());
      tangent.setZ(r * deltaUV2.getY() * e1.getZ() - deltaUV1.getY() * e2.getZ());
      Vec3f bitangent = new Vec3f();
      Vec3f normal = mesh.getVertices()[mesh.getIndices()[i]].getNormal().add(
          mesh.getVertices()[mesh.getIndices()[i + 1]].getNormal()).add(
          mesh.getVertices()[mesh.getIndices()[i + 2]].getNormal());
      normal = normal.normalize();

      bitangent = tangent.cross(normal);

      tangent = tangent.normalize();
      bitangent = bitangent.normalize();

      if (mesh.getVertices()[mesh.getIndices()[i]].getTangent() == null) {
        mesh.getVertices()[mesh.getIndices()[i]].setTangent(new Vec3f(0, 0, 0));
      }
      if (mesh.getVertices()[mesh.getIndices()[i]].getBitangent() == null) {
        mesh.getVertices()[mesh.getIndices()[i]].setBitangent(new Vec3f(0, 0, 0));
      }
      if (mesh.getVertices()[mesh.getIndices()[i + 1]].getTangent() == null) {
        mesh.getVertices()[mesh.getIndices()[i + 1]].setTangent(new Vec3f(0, 0, 0));
      }
      if (mesh.getVertices()[mesh.getIndices()[i + 1]].getBitangent() == null) {
        mesh.getVertices()[mesh.getIndices()[i + 1]].setBitangent(new Vec3f(0, 0, 0));
      }
      if (mesh.getVertices()[mesh.getIndices()[i + 2]].getTangent() == null) {
        mesh.getVertices()[mesh.getIndices()[i + 2]].setTangent(new Vec3f(0, 0, 0));
      }
      if (mesh.getVertices()[mesh.getIndices()[i + 2]].getBitangent() == null) {
        mesh.getVertices()[mesh.getIndices()[i + 2]].setBitangent(new Vec3f(0, 0, 0));
      }

      mesh.getVertices()[mesh.getIndices()[i]].setTangent(
          mesh.getVertices()[mesh.getIndices()[i]].getTangent().add(tangent));
      mesh.getVertices()[mesh.getIndices()[i]].setBitangent(
          mesh.getVertices()[mesh.getIndices()[i]].getBitangent().add(bitangent));
      mesh.getVertices()[mesh.getIndices()[i + 1]].setTangent(
          mesh.getVertices()[mesh.getIndices()[i + 1]].getTangent().add(tangent));
      mesh.getVertices()[mesh.getIndices()[i + 1]].setBitangent(
          mesh.getVertices()[mesh.getIndices()[i + 1]].getBitangent().add(bitangent));
      mesh.getVertices()[mesh.getIndices()[i + 2]].setTangent(
          mesh.getVertices()[mesh.getIndices()[i + 2]].getTangent().add(tangent));
      mesh.getVertices()[mesh.getIndices()[i + 2]].setBitangent(
          mesh.getVertices()[mesh.getIndices()[i + 2]].getBitangent().add(bitangent));
    }

    for (Vertex vertex : mesh.getVertices()) {
      vertex.setTangent(vertex.getTangent().normalize());
      vertex.setBitangent(vertex.getBitangent().normalize());
    }
  }

  public static Vec4f normalizePlane(Vec4f plane) {
    float mag;
    mag = (float) Math.sqrt(plane.getX() * plane.getX() + plane.getY() * plane.getY() + plane.getZ() * plane.getZ());
    plane.setX(plane.getX() / mag);
    plane.setY(plane.getY() / mag);
    plane.setZ(plane.getZ() / mag);
    plane.setW(plane.getW() / mag);

    return plane;
  }

  public static Vec2f[] texCoordsFromFontMap(char x) {
    float x_ = (x % 16) / 16.0f;
    float y_ = (x / 16) / 16.0f;
    Vec2f[] texCoords = new Vec2f[4];
    texCoords[0] = new Vec2f(x_, y_ + 1.0f / 16.0f);
    texCoords[1] = new Vec2f(x_, y_);
    texCoords[2] = new Vec2f(x_ + 1.0f / 16.0f, y_ + 1.0f / 16.0f);
    texCoords[3] = new Vec2f(x_ + 1.0f / 16.0f, y_);

    return texCoords;
  }

  public static int[] initBitReversedIndices(int n) {
    int[] bitReversedIndices = new int[n];
    int bits = (int) (Math.log(n) / Math.log(2));

    for (int i = 0; i < n; i++) {
      int x = Integer.reverse(i);
      x = Integer.rotateLeft(x, bits);
      bitReversedIndices[i] = x;
    }

    return bitReversedIndices;
  }

  public static int getLog2N(int n) {

    return (int) (Math.log(n) / Math.log(2));
  }

  public static int getMipLevelCount(ImageMetaData metaData) {
    return getLog2N(
        metaData.getHeight() < metaData.getWidth()
            ? metaData.getWidth()
            : metaData.getHeight()
    );
  }

  public static Vec3f[] generateRandomKernel3D(int kernelSize) {

    Vec3f[] kernel = new Vec3f[kernelSize];

    for (int i = 0; i < kernelSize; i++) {
      kernel[i] = new Vec3f((float) Math.random() * 2 - 1,
          (float) Math.random() * 2 - 1,
          (float) Math.random());
      kernel[i].normalize();

      float scale = (float) i / (float) kernelSize;

      scale = (float) Math.min(Math.max(0.01, scale * scale), 1.0);

      kernel[i] = kernel[i].mul(scale).mul(-1);
    }

    return kernel;
  }

  public static Vec4f[] generateRandomKernel4D(int kernelSize) {

    Vec4f[] kernel = new Vec4f[kernelSize];

    for (int i = 0; i < kernelSize; i++) {
      kernel[i] = new Vec4f((float) Math.random() * 2 - 1,
          (float) Math.random() * 2 - 1,
          (float) Math.random(),
          0);
      kernel[i].normalize();

      float scale = (float) i / (float) kernelSize;

      scale = (float) Math.min(Math.max(0.01, scale * scale), 1.0);

      kernel[i] = kernel[i].mul(scale).mul(-1);
    }

    return kernel;
  }
}
