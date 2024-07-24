package org.oreon.examples.gl.oreonworlds.plants;

import java.nio.FloatBuffer;
import java.util.List;

import org.oreon.common.terrain.TerrainHelper;
import org.oreon.core.gl.instanced.GLInstancedCluster;
import org.oreon.core.gl.memory.GLUniformBuffer;
import org.oreon.core.math.Matrix4f;
import org.oreon.core.math.Vec3f;
import org.oreon.core.scenegraph.Renderable;
import org.oreon.core.util.BufferUtil;
import org.oreon.gl.components.terrain.GLTerrain;

public class Grass01Cluster extends GLInstancedCluster {

  public Grass01Cluster(int instances, Vec3f pos, List<Renderable> objects) {

    setCenter(pos);
    int buffersize = Float.BYTES * 16 * instances;

    for (int i = 0; i < instances; i++) {

      float s = (float) (Math.random() * 2 + 500);
      Vec3f translation = new Vec3f((float) (Math.random() * 50) - 25 + getCenter().getX(), 0,
          (float) (Math.random() * 50) - 25 + getCenter().getZ());
      Vec3f scaling = new Vec3f(s, s, s);
      Vec3f rotation = new Vec3f(0, (float) Math.random() * 360f, 0);

      float terrainHeight = TerrainHelper.getTerrainHeight(GLTerrain.getConfig(), translation.getX(),
          translation.getZ());
      System.out.println(terrainHeight);
      terrainHeight -= 1.2f;
      translation.setY(terrainHeight);

      Matrix4f translationMatrix = new Matrix4f().Translation(translation);
      Matrix4f rotationMatrix = new Matrix4f().Rotation(rotation);
      Matrix4f scalingMatrix = new Matrix4f().Scaling(scaling);

      getWorldMatrices().add(translationMatrix.mul(scalingMatrix.mul(rotationMatrix)));
      getModelMatrices().add(rotationMatrix);
      getHighPolyIndices().add(i);
    }

    setModelMatricesBuffer(new GLUniformBuffer());
    getModelMatricesBuffer().allocate(buffersize);

    setWorldMatricesBuffer(new GLUniformBuffer());
    getWorldMatricesBuffer().allocate(buffersize);

    /**
     * init matrices UBO's
     */
    int size = Float.BYTES * 16 * instances;

    FloatBuffer worldMatricesFloatBuffer = BufferUtil.createFloatBuffer(size);
    FloatBuffer modelMatricesFloatBuffer = BufferUtil.createFloatBuffer(size);

    for (Matrix4f matrix : getWorldMatrices()) {
      worldMatricesFloatBuffer.put(BufferUtil.createFlippedBuffer(matrix));
    }
    for (Matrix4f matrix : getModelMatrices()) {
      modelMatricesFloatBuffer.put(BufferUtil.createFlippedBuffer(matrix));
    }

    getWorldMatricesBuffer().updateData(worldMatricesFloatBuffer, size);
    getModelMatricesBuffer().updateData(modelMatricesFloatBuffer, size);

    for (Renderable object : objects) {
      addChild(object);
    }
  }
}
