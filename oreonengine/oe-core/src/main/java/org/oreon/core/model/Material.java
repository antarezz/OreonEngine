package org.oreon.core.model;

import org.oreon.core.image.Image;
import org.oreon.core.math.Vec3f;
import org.oreon.core.scenegraph.NodeComponent;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Material extends NodeComponent {

  private String name;
  private Image diffusemap;
  private Image normalmap;
  private Image heightmap;
  private Image ambientmap;
  private Image specularmap;
  private Image alphamap;
  private Vec3f color;
  private float heightScaling;
  private float horizontalScaling;
  private float emission;
  private float shininess;

}