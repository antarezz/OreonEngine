package org.oreon.gl.components.terrain;

import lombok.Getter;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

import org.oreon.common.quadtree.QuadtreeCache;
import org.oreon.common.quadtree.QuadtreeConfig;
import org.oreon.common.quadtree.QuadtreeNode;
import org.oreon.common.terrain.TerrainQuadtree;
import org.oreon.core.context.ContextHolder;
import org.oreon.core.gl.memory.GLPatchVBO;
import org.oreon.core.gl.memory.GLShaderStorageBuffer;
import org.oreon.core.gl.pipeline.GLShaderProgram;
import org.oreon.core.gl.scenegraph.GLRenderInfo;
import org.oreon.core.gl.wrapper.parameter.DefaultRenderParams;
import org.oreon.core.math.Transform;
import org.oreon.core.math.Vec2f;
import org.oreon.core.scenegraph.Node;
import org.oreon.core.scenegraph.NodeComponent;
import org.oreon.core.scenegraph.NodeComponentType;
import org.oreon.core.util.MeshGenerator;

import static org.lwjgl.system.MemoryUtil.memAlloc;

public class GLTerrain extends Node {

  @Getter
  private static QuadtreeConfig config;
  @Getter
  private GLTerrainQuadtree quadtree;

  private GLShaderStorageBuffer ssbo0;
  private GLShaderStorageBuffer ssbo1;

  public GLTerrain(GLShaderProgram shader, GLShaderProgram wireframeShader, GLShaderProgram shadowShader) {
    config = new GLTerrainConfig();

    GLPatchVBO buffer = new GLPatchVBO();
    buffer.addData(MeshGenerator.TerrainChunkMesh(), 16);

    GLRenderInfo renderInfo = new GLRenderInfo(shader,
        new DefaultRenderParams(),
        buffer);

    GLRenderInfo wireframeRenderInfo = new GLRenderInfo(wireframeShader,
        new DefaultRenderParams(),
        buffer);

    HashMap<NodeComponentType, NodeComponent> components = new HashMap<NodeComponentType, NodeComponent>();

    components.put(NodeComponentType.MAIN_RENDERINFO, renderInfo);
    components.put(NodeComponentType.WIREFRAME_RENDERINFO, wireframeRenderInfo);
    components.put(NodeComponentType.CONFIGURATION, config);

    if (shadowShader != null) {
      GLRenderInfo shadowRenderInfo = new GLRenderInfo(shadowShader,
          new DefaultRenderParams(),
          buffer);

      components.put(NodeComponentType.SHADOW_RENDERINFO, shadowRenderInfo);
    }

    initShaderBuffer();

    quadtree = new GLTerrainQuadtree(components, config.getRootChunkCount(), config.getHorizontalScaling());

    addChild(quadtree);

    quadtree.start();

  }

  public void initShaderBuffer() {
    ssbo0 = new GLShaderStorageBuffer();
    ByteBuffer byteBuffer = memAlloc(Float.BYTES * 8 + Integer.BYTES * 4);
    byteBuffer.putFloat(ContextHolder.getContext().getConfig().getFogColor().getX());
    byteBuffer.putFloat(ContextHolder.getContext().getConfig().getFogColor().getY());
    byteBuffer.putFloat(ContextHolder.getContext().getConfig().getFogColor().getZ());
    byteBuffer.putFloat(ContextHolder.getContext().getConfig().getSightRange());
    byteBuffer.putInt(config.isDiamond_square() ? 1 : 0);
    byteBuffer.putInt(config.getTessellationFactor());
    byteBuffer.putFloat(config.getTessellationSlope());
    byteBuffer.putFloat(config.getTessellationShift());
    byteBuffer.putFloat(config.getHorizontalScaling());
    byteBuffer.putInt(config.getBezier());
    byteBuffer.putFloat(config.getUvScaling());
    byteBuffer.putInt(config.getHighDetailRange());
    byteBuffer.flip();
    ssbo0.addData(byteBuffer);

    ssbo1 = new GLShaderStorageBuffer();
    ByteBuffer byteBuffer2 = memAlloc(Integer.BYTES * 8);
    for (int i = 0; i < 8; i++) {
      byteBuffer2.putInt(config.getLod_morphing_area()[i]);
    }
    byteBuffer2.flip();
    ssbo1.addData(byteBuffer2);
  }

  public class GLTerrainQuadtree extends TerrainQuadtree {

    public GLTerrainQuadtree(Map<NodeComponentType, NodeComponent> components, int rootChunkCount,
        float horizontalScaling) {
      super(components, rootChunkCount, horizontalScaling);
    }

    @Override
    public QuadtreeNode createChildChunk(Map<NodeComponentType, NodeComponent> components,
        QuadtreeCache quadtreeCache,
        Transform worldTransform, Vec2f location, int levelOfDetail, Vec2f index) {
      return new TerrainChunk(components, quadtreeCache, worldTransform, location, levelOfDetail, index);
    }
  }

  public class TerrainChunk extends QuadtreeNode {

    public TerrainChunk(Map<NodeComponentType, NodeComponent> components, QuadtreeCache quadtreeCache,
        Transform worldTransform, Vec2f location, int lod, Vec2f index) {

      super(components, quadtreeCache, worldTransform, location, lod, index);
    }

    @Override
    public QuadtreeNode createChildChunk(Map<NodeComponentType, NodeComponent> components, QuadtreeCache quadtreeCache,
        Transform worldTransform, Vec2f location, int levelOfDetail, Vec2f index) {
      return new TerrainChunk(components, quadtreeCache,
          worldTransform, location, levelOfDetail, index);
    }

    @Override
    public void render() {
      ssbo0.bindBufferBase(1);
      ssbo1.bindBufferBase(2);
      super.render();
    }

    @Override
    public void renderWireframe() {
      ssbo0.bindBufferBase(1);
      ssbo1.bindBufferBase(2);
      super.renderWireframe();
    }

    @Override
    public void renderShadows() {
      ssbo0.bindBufferBase(1);
      ssbo1.bindBufferBase(2);
      super.renderShadows();
    }
  }
}
