package org.oreon.examples.gl.oreonworlds.plants;

import java.nio.FloatBuffer;
import java.util.List;
import org.oreon.common.terrain.TerrainHelper;
import org.oreon.core.context.ContextHolder;
import org.oreon.core.gl.instanced.GLInstancedCluster;
import org.oreon.core.gl.memory.GLMeshVBO;
import org.oreon.core.gl.memory.GLUniformBuffer;
import org.oreon.core.gl.scenegraph.GLRenderInfo;
import org.oreon.core.math.Matrix4f;
import org.oreon.core.math.Vec3f;
import org.oreon.core.scenegraph.NodeComponentType;
import org.oreon.core.scenegraph.Renderable;
import org.oreon.core.util.BufferUtil;
import org.oreon.core.util.IntegerReference;
import org.oreon.gl.components.terrain.GLTerrain;

public class Tree01Cluster extends GLInstancedCluster {

  public Tree01Cluster(int instances, Vec3f pos, List<Renderable> objects) {

    setCenter(pos);
    setHighPolyInstances(new IntegerReference(0));
    setLowPolyInstances(new IntegerReference(instances));
    int buffersize = Float.BYTES * 16 * instances;

    for (int i = 0; i < instances; i++) {

      float s = (float) (Math.random() * 2 + 4);
      Vec3f translation = new Vec3f((float) (Math.random() * 100) - 50 + getCenter().getX(), 0,
          (float) (Math.random() * 100) - 50 + getCenter().getZ());
      Vec3f scaling = new Vec3f(s, s, s);
      Vec3f rotation = new Vec3f(0, (float) Math.random() * 360f, 0);

      float terrainHeight = TerrainHelper.getTerrainHeight(GLTerrain.getConfig(), translation.getX(),
          translation.getZ());
      terrainHeight -= 1;
      translation.setY(terrainHeight);

      Matrix4f translationMatrix = new Matrix4f().Translation(translation);
      Matrix4f rotationMatrix = new Matrix4f().Rotation(rotation);
      Matrix4f scalingMatrix = new Matrix4f().Scaling(scaling);

      getWorldMatrices().add(translationMatrix.mul(scalingMatrix.mul(rotationMatrix)));
      getModelMatrices().add(rotationMatrix);
      getLowPolyIndices().add(i);
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

    ((GLMeshVBO) ((GLRenderInfo) ((Renderable) getChildren().get(0)).getComponent(
        NodeComponentType.MAIN_RENDERINFO)).getVbo()).setInstances(getHighPolyInstances());
    ((GLMeshVBO) ((GLRenderInfo) ((Renderable) getChildren().get(1)).getComponent(
        NodeComponentType.MAIN_RENDERINFO)).getVbo()).setInstances(getHighPolyInstances());
    ((GLMeshVBO) ((GLRenderInfo) ((Renderable) getChildren().get(2)).getComponent(
        NodeComponentType.MAIN_RENDERINFO)).getVbo()).setInstances(getLowPolyInstances());
  }

  public void updateUBOs() {

    getHighPolyIndices().clear();

    int index = 0;

    for (Matrix4f transform : getWorldMatrices()) {
      if (transform.getTranslation().sub(ContextHolder.getContext().getCamera().getPosition()).length() < 220) {
        getHighPolyIndices().add(index);
      }

      index++;
    }
    getHighPolyInstances().setValue(getHighPolyIndices().size());
  }

  public void renderShadows() {

    getHighPolyInstances().setValue(0);

    super.renderShadows();

    getHighPolyInstances().setValue(getHighPolyIndices().size());
  }
}