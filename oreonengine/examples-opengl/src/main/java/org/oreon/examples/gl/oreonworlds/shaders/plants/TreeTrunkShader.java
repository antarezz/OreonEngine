package org.oreon.examples.gl.oreonworlds.shaders.plants;

import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.GL_TEXTURE1;
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

public class TreeTrunkShader extends GLShaderProgram {

  private static TreeTrunkShader instance = null;

  public static TreeTrunkShader getInstance() {
    if (instance == null) {
      instance = new TreeTrunkShader();
    }
    return instance;
  }

  protected TreeTrunkShader() {
    super();

    addVertexShader(ResourceLoader.loadShader("oreonworlds/shaders/assets/Tree_Shader/TreeTrunk_VS.glsl"));
    addGeometryShader(ResourceLoader.loadShader("oreonworlds/shaders/assets/Tree_Shader/TreeTrunk_GS.glsl"));
    addFragmentShader(ResourceLoader.loadShader("oreonworlds/shaders/assets/Tree_Shader/TreeTrunk_FS.glsl"));
    compileShader();

    addUniform("material.diffusemap");
    addUniform("material.normalmap");
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

    bindUniformBlock("worldMatrices", 0);
    bindUniformBlock("modelMatrices", 1);

    setUniform("clipplane", ContextHolder.getContext().getConfig().getClipplane());
    setUniform("scalingMatrix", new Matrix4f().Scaling(object.getWorldTransform().getScaling()));

    Material material = object.getComponent(NodeComponentType.MATERIAL0);

    glActiveTexture(GL_TEXTURE0);
    material.getDiffusemap().bind();
    setUniformi("material.diffusemap", 0);

    glActiveTexture(GL_TEXTURE1);
    material.getNormalmap().bind();
    setUniformi("material.normalmap", 1);

    InstancedObject vParentNode = object.getParentObject();
    List<Integer> indices = vParentNode.getHighPolyIndices();

    for (int i = 0; i < indices.size(); i++) {
      setUniformi("matrixIndices[" + i + "]", indices.get(i));
    }
  }
}
