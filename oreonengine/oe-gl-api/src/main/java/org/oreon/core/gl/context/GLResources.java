package org.oreon.core.gl.context;

import lombok.Getter;
import lombok.Setter;

import org.oreon.common.water.WaterConfig;
import org.oreon.core.context.OreonResource;
import org.oreon.core.gl.framebuffer.GLFrameBufferObject;
import org.oreon.core.gl.memory.GLUniformBuffer;
import org.oreon.core.gl.texture.GLTexture;

@Getter
@Setter
public class GLResources implements OreonResource {

  private GLFrameBufferObject primaryFbo;
  private GLTexture sceneDepthMap;

  private GLTexture underwaterDudvMap;
  private GLTexture underwaterCausticsMap;

  private WaterConfig waterConfig;
  private GLUniformBuffer GlobalShaderParameters;
}
