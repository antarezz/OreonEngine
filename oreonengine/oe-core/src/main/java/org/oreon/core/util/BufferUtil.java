package org.oreon.core.util;

import static org.lwjgl.system.MemoryUtil.memAlloc;

import java.nio.ByteBuffer;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.List;

import org.lwjgl.BufferUtils;
import org.oreon.core.math.Matrix4f;
import org.oreon.core.math.Vec2f;
import org.oreon.core.math.Vec3f;
import org.oreon.core.math.Vec4f;
import org.oreon.core.model.Vertex;
import org.oreon.core.model.Vertex.VertexLayout;

public class BufferUtil {

  public static FloatBuffer createFloatBuffer(int size) {
    return BufferUtils.createFloatBuffer(size);
  }

  public static IntBuffer createIntBuffer(int size) {
    return BufferUtils.createIntBuffer(size);
  }

  public static DoubleBuffer createDoubleBuffer(int size) {
    return BufferUtils.createDoubleBuffer(size);
  }

  public static IntBuffer createFlippedBuffer(int... values) {
    IntBuffer buffer = createIntBuffer(values.length);
    buffer.put(values);
    buffer.flip();

    return buffer;
  }

  public static FloatBuffer createFlippedBuffer(float... values) {
    FloatBuffer buffer = createFloatBuffer(values.length);
    buffer.put(values);
    buffer.flip();

    return buffer;
  }

  public static DoubleBuffer createFlippedBuffer(double... values) {
    DoubleBuffer buffer = createDoubleBuffer(values.length);
    buffer.put(values);
    buffer.flip();

    return buffer;
  }

  public static FloatBuffer createFlippedBufferAOS(Vertex[] vertices) {
    FloatBuffer buffer = createFloatBuffer(vertices.length * Vertex.FLOATS);

    for (int i = 0; i < vertices.length; i++) {
      buffer.put(vertices[i].getPosition().getX());
      buffer.put(vertices[i].getPosition().getY());
      buffer.put(vertices[i].getPosition().getZ());
      buffer.put(vertices[i].getNormal().getX());
      buffer.put(vertices[i].getNormal().getY());
      buffer.put(vertices[i].getNormal().getZ());
      buffer.put(vertices[i].getUVCoord().getX());
      buffer.put(vertices[i].getUVCoord().getY());

      if (vertices[i].getTangent() != null && vertices[i].getBitangent() != null) {
        buffer.put(vertices[i].getTangent().getX());
        buffer.put(vertices[i].getTangent().getY());
        buffer.put(vertices[i].getTangent().getZ());
        buffer.put(vertices[i].getBitangent().getX());
        buffer.put(vertices[i].getBitangent().getY());
        buffer.put(vertices[i].getBitangent().getZ());
      }
    }

    buffer.flip();

    return buffer;
  }

  public static FloatBuffer createFlippedBufferSOA(Vertex[] vertices) {
    FloatBuffer buffer = createFloatBuffer(vertices.length * Vertex.FLOATS);

    for (int i = 0; i < vertices.length; i++) {
      buffer.put(vertices[i].getPosition().getX());
      buffer.put(vertices[i].getPosition().getY());
      buffer.put(vertices[i].getPosition().getZ());
    }

    for (int i = 0; i < vertices.length; i++) {
      buffer.put(vertices[i].getNormal().getX());
      buffer.put(vertices[i].getNormal().getY());
      buffer.put(vertices[i].getNormal().getZ());
    }

    for (int i = 0; i < vertices.length; i++) {
      buffer.put(vertices[i].getUVCoord().getX());
      buffer.put(vertices[i].getUVCoord().getY());
    }

    buffer.flip();

    return buffer;
  }

  public static FloatBuffer createFlippedBuffer(Vec3f[] vector) {
    FloatBuffer buffer = createFloatBuffer(vector.length * Float.BYTES * 3);

    for (int i = 0; i < vector.length; i++) {
      buffer.put(vector[i].getX());
      buffer.put(vector[i].getY());
      buffer.put(vector[i].getZ());
    }

    buffer.flip();

    return buffer;
  }

  public static FloatBuffer createFlippedBuffer(Vec4f[] vector) {
    FloatBuffer buffer = createFloatBuffer(vector.length * Float.BYTES * 4);

    for (int i = 0; i < vector.length; i++) {
      buffer.put(vector[i].getX());
      buffer.put(vector[i].getY());
      buffer.put(vector[i].getZ());
      buffer.put(vector[i].getW());
    }

    buffer.flip();

    return buffer;
  }

  public static FloatBuffer createFlippedBuffer(Vec3f vector) {
    FloatBuffer buffer = createFloatBuffer(Float.BYTES * 3);

    buffer.put(vector.getX());
    buffer.put(vector.getY());
    buffer.put(vector.getZ());

    buffer.flip();

    return buffer;
  }

  public static FloatBuffer createFlippedBuffer(Vec2f vector) {
    FloatBuffer buffer = createFloatBuffer(Float.BYTES * 2);

    buffer.put(vector.getX());
    buffer.put(vector.getY());

    buffer.flip();

    return buffer;
  }

  public static FloatBuffer createFlippedBuffer(Vec4f vector) {
    FloatBuffer buffer = createFloatBuffer(Float.BYTES * 4);

    buffer.put(vector.getX());
    buffer.put(vector.getY());
    buffer.put(vector.getZ());
    buffer.put(vector.getW());

    buffer.flip();

    return buffer;
  }

  public static FloatBuffer createFlippedBuffer(Vec2f[] vector) {
    FloatBuffer buffer = createFloatBuffer(vector.length * Float.BYTES * 2);

    for (int i = 0; i < vector.length; i++) {
      buffer.put(vector[i].getX());
      buffer.put(vector[i].getY());
    }

    buffer.flip();

    return buffer;
  }

  public static FloatBuffer createFlippedBuffer(List<Vec2f> vector) {
    FloatBuffer buffer = createFloatBuffer(vector.size() * Float.BYTES * 2);

    for (Vec2f v : vector) {
      buffer.put(v.getX());
      buffer.put(v.getY());
    }

    buffer.flip();

    return buffer;
  }

  public static FloatBuffer createFlippedBuffer(Matrix4f matrix) {
    FloatBuffer buffer = createFloatBuffer(4 * 4);

    for (int i = 0; i < 4; i++) {
      for (int j = 0; j < 4; j++) {
        buffer.put(matrix.get(i, j));
      }
    }

    buffer.flip();

    return buffer;
  }

  public static FloatBuffer createFlippedBuffer(Matrix4f[] matrices) {
    FloatBuffer buffer = createFloatBuffer(4 * 4 * matrices.length);

    for (Matrix4f matrix : matrices) {
      for (int i = 0; i < 4; i++) {
        for (int j = 0; j < 4; j++) {
          buffer.put(matrix.get(i, j));
        }
      }
    }

    buffer.flip();

    return buffer;
  }

  public static ByteBuffer createByteBuffer(Matrix4f matrix) {

    ByteBuffer byteBuffer = memAlloc(Float.BYTES * 16);
    FloatBuffer floatBuffer = byteBuffer.asFloatBuffer();
    floatBuffer.put(BufferUtil.createFlippedBuffer(matrix));

    return byteBuffer;
  }

  public static ByteBuffer createByteBuffer(Vec3f vector) {

    ByteBuffer byteBuffer = memAlloc(Float.BYTES * 3);
    FloatBuffer floatBuffer = byteBuffer.asFloatBuffer();
    floatBuffer.put(BufferUtil.createFlippedBuffer(vector));

    return byteBuffer;
  }

  public static ByteBuffer createByteBuffer(Vec2f[] vertices) {

    ByteBuffer byteBuffer = memAlloc(Float.BYTES * 2 * vertices.length);
    FloatBuffer floatBuffer = byteBuffer.asFloatBuffer();

    for (int i = 0; i < vertices.length; i++) {
      floatBuffer.put(vertices[i].getX());
      floatBuffer.put(vertices[i].getY());
    }

    return byteBuffer;
  }

  public static ByteBuffer createByteBuffer(Vec3f[] vertices) {

    ByteBuffer byteBuffer = memAlloc(Float.BYTES * 3 * vertices.length);
    FloatBuffer floatBuffer = byteBuffer.asFloatBuffer();

    for (int i = 0; i < vertices.length; i++) {
      floatBuffer.put(vertices[i].getX());
      floatBuffer.put(vertices[i].getY());
      floatBuffer.put(vertices[i].getZ());
    }

    return byteBuffer;
  }

  public static ByteBuffer createByteBuffer(Vec4f[] vertices) {

    ByteBuffer byteBuffer = memAlloc(Float.BYTES * 4 * vertices.length);
    FloatBuffer floatBuffer = byteBuffer.asFloatBuffer();

    for (int i = 0; i < vertices.length; i++) {
      floatBuffer.put(vertices[i].getX());
      floatBuffer.put(vertices[i].getY());
      floatBuffer.put(vertices[i].getZ());
      floatBuffer.put(vertices[i].getW());
    }

    return byteBuffer;
  }

  public static ByteBuffer createByteBuffer(Vertex[] vertices, VertexLayout layout) {

    ByteBuffer byteBuffer = allocateVertexByteBuffer(layout, vertices.length);
    FloatBuffer floatBuffer = byteBuffer.asFloatBuffer();

    for (int i = 0; i < vertices.length; i++) {
      if (layout == VertexLayout.POS2D ||
          layout == VertexLayout.POS2D_UV) {
        floatBuffer.put(vertices[i].getPosition().getX());
        floatBuffer.put(vertices[i].getPosition().getY());
      } else {
        floatBuffer.put(vertices[i].getPosition().getX());
        floatBuffer.put(vertices[i].getPosition().getY());
        floatBuffer.put(vertices[i].getPosition().getZ());
      }

      if (layout == VertexLayout.POS_NORMAL ||
          layout == VertexLayout.POS_NORMAL_UV ||
          layout == VertexLayout.POS_NORMAL_UV_TAN_BITAN) {

        floatBuffer.put(vertices[i].getNormal().getX());
        floatBuffer.put(vertices[i].getNormal().getY());
        floatBuffer.put(vertices[i].getNormal().getZ());
      }

      if (layout == VertexLayout.POS_NORMAL_UV ||
          layout == VertexLayout.POS_UV ||
          layout == VertexLayout.POS_NORMAL_UV_TAN_BITAN ||
          layout == VertexLayout.POS2D_UV) {
        floatBuffer.put(vertices[i].getUVCoord().getX());
        floatBuffer.put(vertices[i].getUVCoord().getY());
      }

      if (layout == VertexLayout.POS_NORMAL_UV_TAN_BITAN) {

        floatBuffer.put(vertices[i].getTangent().getX());
        floatBuffer.put(vertices[i].getTangent().getY());
        floatBuffer.put(vertices[i].getTangent().getZ());
        floatBuffer.put(vertices[i].getBitangent().getX());
        floatBuffer.put(vertices[i].getBitangent().getY());
        floatBuffer.put(vertices[i].getBitangent().getZ());
      }
    }

    return byteBuffer;
  }

  public static ByteBuffer createByteBuffer(int... values) {

    ByteBuffer byteBuffer = memAlloc(Integer.BYTES * values.length);
    IntBuffer intBuffer = byteBuffer.asIntBuffer();
    intBuffer.put(values);
    intBuffer.flip();

    return byteBuffer;
  }

  public static ByteBuffer createByteBuffer(float... values) {

    ByteBuffer byteBuffer = memAlloc(Float.BYTES * values.length);
    FloatBuffer intBuffer = byteBuffer.asFloatBuffer();
    intBuffer.put(values);
    intBuffer.flip();

    return byteBuffer;
  }

  public static ByteBuffer createByteBuffer(FloatBuffer floatBuffer) {

    ByteBuffer byteBuffer = memAlloc(Float.BYTES * floatBuffer.limit());
    FloatBuffer intBuffer = byteBuffer.asFloatBuffer();
    intBuffer.put(floatBuffer);
    intBuffer.flip();

    return byteBuffer;
  }

  public static ByteBuffer createByteBuffer(Vec2f vector) {

    return createByteBuffer(createFlippedBuffer(vector));
  }

  public static ByteBuffer allocateVertexByteBuffer(VertexLayout layout, int vertexCount) {

    ByteBuffer byteBuffer;

    switch (layout) {
      case POS2D:
        byteBuffer = memAlloc(Float.BYTES * 2 * vertexCount);
        break;
      case POS:
        byteBuffer = memAlloc(Float.BYTES * 3 * vertexCount);
        break;
      case POS_UV:
        byteBuffer = memAlloc(Float.BYTES * 5 * vertexCount);
        break;
      case POS2D_UV:
        byteBuffer = memAlloc(Float.BYTES * 4 * vertexCount);
        break;
      case POS_NORMAL:
        byteBuffer = memAlloc(Float.BYTES * 6 * vertexCount);
        break;
      case POS_NORMAL_UV:
        byteBuffer = memAlloc(Float.BYTES * 8 * vertexCount);
        break;
      case POS_NORMAL_UV_TAN_BITAN:
        byteBuffer = memAlloc(Float.BYTES * 14 * vertexCount);
        break;
      default:
        byteBuffer = memAlloc(0);
    }
    return byteBuffer;
  }

  public static ByteBuffer resizeBuffer(ByteBuffer buffer, int newCapacity) {
    ByteBuffer newBuffer = BufferUtils.createByteBuffer(newCapacity);
    buffer.flip();
    newBuffer.put(buffer);
    return newBuffer;
  }

}
