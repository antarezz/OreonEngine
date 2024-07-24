package org.oreon.examples.gl.oreonworlds.shaders.plants;

import java.util.List;
import org.oreon.core.context.ContextHolder;
import org.oreon.core.gl.instanced.GLInstancedCluster;
import org.oreon.core.gl.pipeline.GLShaderProgram;
import org.oreon.core.instanced.InstancedCluster;
import org.oreon.core.math.Matrix4f;
import org.oreon.core.model.Material;
import org.oreon.core.scenegraph.NodeComponentType;
import org.oreon.core.scenegraph.Renderable;
import org.oreon.core.util.Constants;
import org.oreon.core.util.ResourceLoader;

public class PalmShader extends GLShaderProgram {

  private static PalmShader instance = null;

  public static PalmShader getInstance() {
    if (instance == null) {
      instance = new PalmShader();
    }
    return instance;
  }

  protected PalmShader() {
    super();

    addVertexShader(ResourceLoader.loadShader("oreonworlds/shaders/assets/Palm_Shader/Palm01_VS.glsl"));
    addGeometryShader(ResourceLoader.loadShader("oreonworlds/shaders/assets/Palm_Shader/Palm01_GS.glsl"));
    addFragmentShader(ResourceLoader.loadShader("oreonworlds/shaders/assets/Palm_Shader/Palm01_FS.glsl"));
    compileShader();

    addUniform("material.color");
    addUniform("clipplane");
    addUniform("scalingMatrix");
    addUniform("isReflection");

    addUniformBlock("worldMatrices");
    addUniformBlock("modelMatrices");
    addUniformBlock("Camera");

    for (int i = 0; i < 100; i++) {
      addUniform("matrixIndices[" + i + "]");
    }
  }

  public void updateUniforms(Renderable object) {
    bindUniformBlock("Camera", Constants.CameraUniformBlockBinding);
    setUniformi("isReflection", ContextHolder.getContext().getConfig().isRenderReflection() ? 1 : 0);

    ((GLInstancedCluster) object.getParentNode()).getWorldMatricesBuffer().bindBufferBase(0);
    bindUniformBlock("worldMatrices", 0);
    ((GLInstancedCluster) object.getParentNode()).getModelMatricesBuffer().bindBufferBase(1);
    bindUniformBlock("modelMatrices", 1);

    setUniform("clipplane", ContextHolder.getContext().getConfig().getClipplane());
    setUniform("scalingMatrix", new Matrix4f().Scaling(object.getWorldTransform().getScaling()));

    Material material = (Material) object.getComponent(NodeComponentType.MATERIAL0);
    setUniform("material.color", material.getColor());
//		setUniformf("material.emission", material.getEmission());
//		setUniformf("material.shininess", material.getShininess());

    List<Integer> indices = ((InstancedCluster) object.getParentNode()).getHighPolyIndices();

    for (int i = 0; i < indices.size(); i++) {
      setUniformi("matrixIndices[" + i + "]", indices.get(i));
    }
  }
}
