package org.oreon.core.target;

import lombok.Getter;

@Getter
public abstract class FrameBufferObject {

  protected int height;
  protected int width;
  protected int colorAttachmentCount;
  protected int depthAttachmentCount;

  public enum Attachment {

    COLOR,
    ALPHA,
    NORMAL,
    POSITION,
    SPECULAR_EMISSION_DIFFUSE_SSAO_BLOOM,
    LIGHT_SCATTERING,
    DEPTH;
  }
}
