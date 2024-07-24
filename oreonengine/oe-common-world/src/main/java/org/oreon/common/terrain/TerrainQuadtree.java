package org.oreon.common.terrain;

import java.util.Map;

import org.oreon.common.quadtree.Quadtree;
import org.oreon.core.math.Transform;
import org.oreon.core.math.Vec2f;
import org.oreon.core.scenegraph.NodeComponent;
import org.oreon.core.scenegraph.NodeComponentType;

public abstract class TerrainQuadtree extends Quadtree {

  public TerrainQuadtree(Map<NodeComponentType, NodeComponent> components,
      int rootChunkCount, float horizontalScaling) {

    super();

    Transform worldTransform = new Transform();
    worldTransform.setTranslation(-0.5f * horizontalScaling,
        0, -0.5f * horizontalScaling);
    worldTransform.setScaling(horizontalScaling, 0, horizontalScaling);

    for (int i = 0; i < rootChunkCount; i++) {
      for (int j = 0; j < rootChunkCount; j++) {
        addChild(createChildChunk(components, quadtreeCache, worldTransform,
            new Vec2f(1f * i / (float) rootChunkCount, 1f * j / (float) rootChunkCount),
            0, new Vec2f(i, j)));
      }
    }
  }

}
