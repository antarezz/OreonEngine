package org.oreon.examples.gl.oreonworlds.shaders.plants;

import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.glActiveTexture;

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

public class BushShader extends GLShaderProgram {

  private static BushShader instance = null;

  public static BushShader getInstance() {
    if (instance == null) {
      instance = new BushShader();
    }
    return instance;
  }

  protected BushShader() {
    super();

    addVertexShader(ResourceLoader.loadShader("oreonworlds/shaders/assets/Bush_Shader/Bush01_VS.glsl"));
    addGeometryShader(ResourceLoader.loadShader("oreonworlds/shaders/assets/Bush_Shader/Bush01_GS.glsl"));
    addFragmentShader(ResourceLoader.loadShader("oreonworlds/shaders/assets/Bush_Shader/Bush01_FS.glsl"));
    compileShader();

    addUniform("material.diffusemap");
    addUniform("clipplane");
    addUniform("scalingMatrix");
    addUniform("isReflection");
    addUniform("isRefraction");
    addUniform("isCameraUnderWater");

    addUniformBlock("DirectionalLight");
    addUniformBlock("worldMatrices");
    addUniformBlock("modelMatrices");
    addUniformBlock("LightViewProjections");
    addUniformBlock("Camera");
    addUniform("shadowMaps");

    for (int i = 0; i < 100; i++) {
      addUniform("matrixIndices[" + i + "]");
    }
  }

  public void updateUniforms(Renderable object) {
    bindUniformBlock("Camera", Constants.CameraUniformBlockBinding);
    bindUniformBlock("DirectionalLight", Constants.DirectionalLightUniformBlockBinding);
    bindUniformBlock("LightViewProjections", Constants.LightMatricesUniformBlockBinding);
    setUniformi("isReflection", ContextHolder.getContext().getConfig().isRenderReflection() ? 1 : 0);
    setUniformi("isRefraction", ContextHolder.getContext().getConfig().isRenderRefraction() ? 1 : 0);
    setUniformi("isCameraUnderWater", ContextHolder.getContext().getConfig().isRenderUnderwater() ? 1 : 0);

    ((GLInstancedCluster) object.getParentNode()).getWorldMatricesBuffer().bindBufferBase(0);
    bindUniformBlock("worldMatrices", 0);
    ((GLInstancedCluster) object.getParentNode()).getModelMatricesBuffer().bindBufferBase(1);
    bindUniformBlock("modelMatrices", 1);

    setUniform("clipplane", ContextHolder.getContext().getConfig().getClipplane());
    setUniform("scalingMatrix", new Matrix4f().Scaling(object.getWorldTransform().getScaling()));

    Material material = (Material) object.getComponent(NodeComponentType.MATERIAL0);
    glActiveTexture(GL_TEXTURE0);
    material.getDiffusemap().bind();
    setUniformi("material.diffusemap", 0);

    List<Integer> indices = ((InstancedCluster) object.getParentNode()).getHighPolyIndices();

    for (int i = 0; i < indices.size(); i++) {
      setUniformi("matrixIndices[" + i + "]", indices.get(i));
    }
  }

}
