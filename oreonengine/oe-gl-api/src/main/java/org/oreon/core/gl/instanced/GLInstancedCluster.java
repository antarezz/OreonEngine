package org.oreon.core.gl.instanced;

import org.oreon.core.gl.memory.GLUniformBuffer;
import org.oreon.core.instanced.InstancedCluster;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GLInstancedCluster extends InstancedCluster {

  private GLUniformBuffer modelMatricesBuffer;
  private GLUniformBuffer worldMatricesBuffer;
}
