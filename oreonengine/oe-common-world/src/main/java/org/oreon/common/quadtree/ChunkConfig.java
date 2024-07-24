package org.oreon.common.quadtree;

import org.oreon.core.math.Vec2f;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ChunkConfig {

  private int lod;
  private Vec2f location;
  private Vec2f index;
  private float gap;
}
