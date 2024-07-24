package org.oreon.core.image;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Image {

  protected ImageMetaData metaData;

  public enum ImageFormat {

    RGBA8_SNORM,
    RGBA32FLOAT,
    RGB32FLOAT,
    RGBA16FLOAT,
    DEPTH32FLOAT,
    R16FLOAT,
    R32FLOAT,
    R8
  }

  public enum SamplerFilter {

    Nearest,
    Bilinear,
    Trilinear,
    Anistropic
  }

  public enum TextureWrapMode {

    ClampToEdge,
    ClampToBorder,
    Repeat,
    MirrorRepeat
  }

  public void bind() {
  }

  ;

  public void unbind() {
  }

  ;
}
