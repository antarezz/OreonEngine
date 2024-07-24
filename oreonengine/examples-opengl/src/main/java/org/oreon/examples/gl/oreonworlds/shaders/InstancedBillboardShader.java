package org.oreon.examples.gl.oreonworlds.shaders;

import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.glActiveTexture;

import java.util.List;
import org.oreon.core.context.ContextHolder;
import org.oreon.core.gl.pipeline.GLShaderProgram;
import org.oreon.core.instanced.InstancedObject;
import org.oreon.core.math.Matrix4f;
import org.oreon.core.model.Material;
import org.oreon.core.scenegraph.NodeComponentType;
import org.oreon.core.scenegraph.Renderable;
import org.oreon.core.util.Constants;
import org.oreon.core.util.ResourceLoader;

public class InstancedBillboardShader extends GLShaderProgram {

  private static InstancedBillboardShader instance = null;

  public static InstancedBillboardShader getInstance() {
    if (instance == null) {
      instance = new InstancedBillboardShader();
    }
    return instance;
  }

  protected InstancedBillboardShader() {
    super();

    addVertexShader(ResourceLoader.loadShader("oreonworlds/shaders/assets/Billboard_Shader/billboard.vert"));
    addGeometryShader(ResourceLoader.loadShader("oreonworlds/shaders/assets/Billboard_Shader/billboard.geom"));
    addFragmentShader(ResourceLoader.loadShader("oreonworlds/shaders/assets/Billboard_Shader/billboard.frag"));
    compileShader();

    addUniform("clipplane");
    addUniformBlock("worldMatrices");
    addUniformBlock("modelMatrices");
    addUniform("scalingMatrix");
    addUniform("isReflection");
    addUniform("isRefraction");

    addUniformBlock("Camera");
    addUniform("material.diffusemap");

    for (int i = 0; i < 100; i++) {
      addUniform("matrixIndices[" + i + "]");
    }
  }

  public void updateUniforms(Renderable object) {

    setUniform("clipplane", ContextHolder.getContext().getConfig().getClipplane());
    bindUniformBlock("Camera", Constants.CameraUniformBlockBinding);
    setUniformi("isReflection", ContextHolder.getContext().getConfig().isRenderReflection() ? 1 : 0);
    setUniformi("isRefraction", ContextHolder.getContext().getConfig().isRenderRefraction() ? 1 : 0);
    setUniform("scalingMatrix", new Matrix4f().Scaling(object.getWorldTransform().getScaling()));

    bindUniformBlock("worldMatrices", 0);
    bindUniformBlock("modelMatrices", 1);

    Material material = object.getComponent(NodeComponentType.MATERIAL0);

    glActiveTexture(GL_TEXTURE0);
    material.getDiffusemap().bind();
    setUniformi("material.diffusemap", 0);

    InstancedObject vParentNode = object.getParentObject();
    List<Integer> indices = vParentNode.getLowPolyIndices();

    for (int i = 0; i < indices.size(); i++) {
      setUniformi("matrixIndices[" + i + "]", indices.get(i));
    }
  }
}
