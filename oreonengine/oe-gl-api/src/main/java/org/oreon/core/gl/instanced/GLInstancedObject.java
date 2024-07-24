package org.oreon.core.gl.instanced;

import lombok.Getter;
import lombok.Setter;

import java.nio.FloatBuffer;

import org.oreon.core.context.ContextHolder;
import org.oreon.core.gl.memory.GLUniformBuffer;
import org.oreon.core.instanced.InstancedObject;
import org.oreon.core.math.Matrix4f;
import org.oreon.core.util.BufferUtil;

@Getter
@Setter
public class GLInstancedObject extends InstancedObject {

  private GLUniformBuffer modelMatricesBuffer;
  private GLUniformBuffer worldMatricesBuffer;

  public void initMatricesBuffers() {

    int buffersize = Float.BYTES * 16 * getInstanceCount();

    setModelMatricesBuffer(new GLUniformBuffer());
    getModelMatricesBuffer().allocate(buffersize);

    setWorldMatricesBuffer(new GLUniformBuffer());
    getWorldMatricesBuffer().allocate(buffersize);

    int size = Float.BYTES * 16 * getInstanceCount();

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
  }

  public void renderShadows() {

    // only low-poly objects are rendered into shadow maps
    getWorldMatricesBuffer().bindBufferBase(0);
    if (getHighPolyInstanceCount().getValue() == 0) {
      renderLowPolyShadows();
    } else {
      super.renderShadows();
    }
  }

  public void render() {

    getWorldMatricesBuffer().bindBufferBase(0);
    getModelMatricesBuffer().bindBufferBase(1);

    // only low-poly objects rendered when reflection or refraction rendering
    if (ContextHolder.getContext().getConfig().isRenderRefraction()) {
      renderLowPoly();
    } else if (ContextHolder.getContext().getConfig().isRenderReflection()) {
      renderLowPoly();
    } else {
      if (getHighPolyInstanceCount().getValue() == 0) {
        renderLowPoly();
      } else {
        super.render();
      }
    }
  }

  public void renderWireframe() {

    // only low-poly objects are rendered into shadow maps
    getWorldMatricesBuffer().bindBufferBase(0);
    getWorldMatricesBuffer().bindBufferBase(1);

    super.renderWireframe();
  }
}
