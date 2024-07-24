package org.oreon.examples.gl.oreonworlds.shaders.rocks;

import java.util.List;

import org.oreon.core.gl.instanced.GLInstancedCluster;
import org.oreon.core.gl.pipeline.GLShaderProgram;
import org.oreon.core.instanced.InstancedCluster;
import org.oreon.core.scenegraph.Renderable;
import org.oreon.core.util.Constants;
import org.oreon.core.util.ResourceLoader;

public class RockShadowShader extends GLShaderProgram {

  private static RockShadowShader instance;

  public static RockShadowShader getInstance() {
    if (instance == null) {
      instance = new RockShadowShader();
    }
    return instance;
  }

  protected RockShadowShader() {
    super();

    addVertexShader(ResourceLoader.loadShader("oreonworlds/shaders/assets/Rock_Shader/RockShadow_VS.glsl"));
    addGeometryShader(ResourceLoader.loadShader("oreonworlds/shaders/assets/Rock_Shader/RockShadow_GS.glsl"));
    addFragmentShader(ResourceLoader.loadShader("oreonworlds/shaders/assets/Rock_Shader/RockShadow_FS.glsl"));
    compileShader();

    addUniformBlock("worldMatrices");
    addUniformBlock("Camera");
    addUniformBlock("LightViewProjections");

    for (int i = 0; i < 100; i++) {
      addUniform("matrixIndices[" + i + "]");
    }
  }

  public void updateUniforms(Renderable object) {

    bindUniformBlock("Camera", Constants.CameraUniformBlockBinding);
    bindUniformBlock("LightViewProjections", Constants.LightMatricesUniformBlockBinding);
    ((GLInstancedCluster) object.getParentNode()).getWorldMatricesBuffer().bindBufferBase(0);
    bindUniformBlock("worldMatrices", 0);

    List<Integer> indices = ((InstancedCluster) object.getParentNode()).getHighPolyIndices();

    for (int i = 0; i < indices.size(); i++) {
      setUniformi("matrixIndices[" + i + "]", indices.get(i));
    }
  }
}
