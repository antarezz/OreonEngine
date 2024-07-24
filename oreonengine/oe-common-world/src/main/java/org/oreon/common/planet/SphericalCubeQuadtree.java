package org.oreon.common.planet;

import java.util.Map;

import org.oreon.common.quadtree.Quadtree;
import org.oreon.common.quadtree.QuadtreeConfig;
import org.oreon.common.quadtree.QuadtreeNode;
import org.oreon.core.math.Transform;
import org.oreon.core.math.Vec2f;
import org.oreon.core.scenegraph.NodeComponent;
import org.oreon.core.scenegraph.NodeComponentType;

public abstract class SphericalCubeQuadtree extends Quadtree {

  public SphericalCubeQuadtree(Map<NodeComponentType, NodeComponent> components,
      QuadtreeConfig quadtreeConfig, int rootChunkCount, float horizontalScaling) {

    super();

    // cube face 0 - front face
    // x-rotation 90 degrees, z-translation -1
    Transform worldTransformFace0 = new Transform();
    worldTransformFace0.setTranslation(-0.5f * horizontalScaling,
        0.5f * horizontalScaling, -0.5f * horizontalScaling);
    worldTransformFace0.setRotation(-90f, 0f, 0f);
    worldTransformFace0.setScaling(horizontalScaling);

    for (int i = 0; i < rootChunkCount; i++) {
      for (int j = 0; j < rootChunkCount; j++) {
        QuadtreeNode newChunk = createChildChunk(components,
            quadtreeCache, worldTransformFace0,
            new Vec2f(1f * i / (float) rootChunkCount, 1f * j / (float) rootChunkCount),
            0, new Vec2f(i, j));
        addChild(newChunk);
      }
    }

    // cube face 1 - left face
    // x-rotation 90 degrees, y-rotation 90 degrees, x-translation -1
    Transform worldTransformFace1 = new Transform();
    worldTransformFace1.setTranslation(-0.5f * horizontalScaling,
        0.5f * horizontalScaling, -0.5f * horizontalScaling);
    worldTransformFace1.setRotation(-90, 90, 0);
    worldTransformFace1.setScaling(horizontalScaling);

    for (int i = 0; i < rootChunkCount; i++) {
      for (int j = 0; j < rootChunkCount; j++) {
        QuadtreeNode newChunk = createChildChunk(components,
            quadtreeCache, worldTransformFace1,
            new Vec2f(1f * i / (float) rootChunkCount, 1f * j / (float) rootChunkCount),
            0, new Vec2f(i, j));
        addChild(newChunk);
      }
    }

    // cube face 2 - back face
    // x-rotation 90 degrees, y-rotation 180 degrees, z-translation +1
    Transform worldTransformFace2 = new Transform();
    worldTransformFace2.setTranslation(-0.5f * horizontalScaling,
        0.5f * horizontalScaling, -0.5f * horizontalScaling);
    worldTransformFace2.setRotation(-90, 180, 0);
    worldTransformFace2.setScaling(horizontalScaling);

    for (int i = 0; i < rootChunkCount; i++) {
      for (int j = 0; j < rootChunkCount; j++) {
        QuadtreeNode newChunk = createChildChunk(components,
            quadtreeCache, worldTransformFace2,
            new Vec2f(1f * i / (float) rootChunkCount, 1f * j / (float) rootChunkCount),
            0, new Vec2f(i, j));
        addChild(newChunk);
      }
    }

    // cube face 3 - right face
    // x-rotation 90 degrees, y-rotation 270 degrees, x-translation +1
    Transform worldTransformFace3 = new Transform();
    worldTransformFace3.setTranslation(-0.5f * horizontalScaling,
        0.5f * horizontalScaling, -0.5f * horizontalScaling);
    worldTransformFace3.setRotation(-90, -90, 0);
    worldTransformFace3.setScaling(horizontalScaling);

    for (int i = 0; i < rootChunkCount; i++) {
      for (int j = 0; j < rootChunkCount; j++) {
        QuadtreeNode newChunk = createChildChunk(components,
            quadtreeCache, worldTransformFace3,
            new Vec2f(1f * i / (float) rootChunkCount, 1f * j / (float) rootChunkCount),
            0, new Vec2f(i, j));
        addChild(newChunk);
      }
    }

    // cube face 4 - top face
    // y-translation +1
    Transform worldTransformFace4 = new Transform();
    worldTransformFace4.setTranslation(-0.5f * horizontalScaling,
        0.5f * horizontalScaling, -0.5f * horizontalScaling);
    worldTransformFace4.setScaling(horizontalScaling);

    for (int i = 0; i < rootChunkCount; i++) {
      for (int j = 0; j < rootChunkCount; j++) {
        QuadtreeNode newChunk = createChildChunk(components,
            quadtreeCache, worldTransformFace4,
            new Vec2f(1f * i / (float) rootChunkCount, 1f * j / (float) rootChunkCount),
            0, new Vec2f(i, j));
        addChild(newChunk);
      }
    }

    // cube face 5 - bottom face
    // y-translation -1
    Transform worldTransformFace5 = new Transform();
    worldTransformFace5.setTranslation(-0.5f * horizontalScaling,
        -0.5f * horizontalScaling, -0.5f * horizontalScaling);
    worldTransformFace5.setScaling(horizontalScaling);

    for (int i = 0; i < rootChunkCount; i++) {
      for (int j = 0; j < rootChunkCount; j++) {
        QuadtreeNode newChunk = createChildChunk(components,
            quadtreeCache, worldTransformFace5,
            new Vec2f(1f * i / (float) rootChunkCount, 1f * j / (float) rootChunkCount),
            0, new Vec2f(i, j));
        addChild(newChunk);
      }
    }
  }

}
