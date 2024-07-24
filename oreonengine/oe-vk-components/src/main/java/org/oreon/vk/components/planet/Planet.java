package org.oreon.vk.components.planet;

import static org.lwjgl.vulkan.VK10.VK_BUFFER_USAGE_VERTEX_BUFFER_BIT;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import lombok.Getter;
import org.lwjgl.vulkan.VkPhysicalDeviceMemoryProperties;
import org.oreon.common.quadtree.Quadtree;
import org.oreon.common.quadtree.QuadtreeConfig;
import org.oreon.core.context.ContextHolder;
import org.oreon.core.math.Vec2f;
import org.oreon.core.model.Vertex.VertexLayout;
import org.oreon.core.scenegraph.Node;
import org.oreon.core.scenegraph.NodeComponent;
import org.oreon.core.scenegraph.NodeComponentType;
import org.oreon.core.util.BufferUtil;
import org.oreon.core.util.MeshGenerator;
import org.oreon.core.vk.context.DeviceManager.DeviceType;
import org.oreon.core.vk.context.VkOreonContext;
import org.oreon.core.vk.descriptor.DescriptorSet;
import org.oreon.core.vk.descriptor.DescriptorSetLayout;
import org.oreon.core.vk.device.LogicalDevice;
import org.oreon.core.vk.memory.VkBuffer;
import org.oreon.core.vk.pipeline.ShaderPipeline;
import org.oreon.core.vk.pipeline.VkVertexInput;
import org.oreon.core.vk.scenegraph.VkMeshData;
import org.oreon.core.vk.scenegraph.VkRenderInfo;
import org.oreon.core.vk.wrapper.buffer.VkBufferHelper;

public class Planet extends Node {

  @Getter
  private Quadtree quadtree;

  public Planet() {
    final VkOreonContext context = (VkOreonContext) ContextHolder.getContext();
    LogicalDevice device = context.getDeviceManager().getLogicalDevice(DeviceType.MAJOR_GRAPHICS_DEVICE);
    VkPhysicalDeviceMemoryProperties memoryProperties =
        context.getDeviceManager().getPhysicalDevice(DeviceType.MAJOR_GRAPHICS_DEVICE).getMemoryProperties();

    Vec2f[] mesh = MeshGenerator.TerrainChunkMesh();
    ByteBuffer vertexBuffer = BufferUtil.createByteBuffer(mesh);
    VkBuffer vertexBufferObject = VkBufferHelper.createDeviceLocalBuffer(
        device.getHandle(), memoryProperties,
        device.getTransferCommandPool(Thread.currentThread().getId()).getHandle(),
        device.getTransferQueue(),
        vertexBuffer, VK_BUFFER_USAGE_VERTEX_BUFFER_BIT);

    VkMeshData meshData = VkMeshData.builder().vertexBufferObject(vertexBufferObject)
        .vertexBuffer(vertexBuffer).vertexCount(mesh.length).build();

    HashMap<NodeComponentType, NodeComponent> components =
        new HashMap<NodeComponentType, NodeComponent>();

    QuadtreeConfig config = new QuadtreeConfig();

    VkVertexInput vertexInput = new VkVertexInput(VertexLayout.POS2D);

    ShaderPipeline shaderPipeline = new ShaderPipeline(device.getHandle());
    shaderPipeline.createVertexShader("shaders/planet/planet.vert.spv");
    shaderPipeline.createTessellationControlShader("shaders/planet/planet.tesc.spv");
    shaderPipeline.createTessellationEvaluationShader("shaders/planet/planet.tese.spv");
    shaderPipeline.createGeometryShader("shaders/planet/planetWireframe.geom.spv");
    shaderPipeline.createFragmentShader("shaders/planet/planet.frag.spv");
    shaderPipeline.createShaderPipeline();

    List<DescriptorSet> descriptorSets = new ArrayList<DescriptorSet>();
    List<DescriptorSetLayout> descriptorSetLayouts = new ArrayList<DescriptorSetLayout>();
    descriptorSets.add(context.getCamera().getDescriptorSet());
    descriptorSetLayouts.add(context.getCamera().getDescriptorSetLayout());

    VkRenderInfo renderInfo = VkRenderInfo.builder().vertexInput(vertexInput)
        .shaderPipeline(shaderPipeline).descriptorSets(descriptorSets)
        .descriptorSetLayouts(descriptorSetLayouts).build();

    components.put(NodeComponentType.CONFIGURATION, config);
    components.put(NodeComponentType.MAIN_RENDERINFO, renderInfo);
    components.put(NodeComponentType.MESH_DATA, meshData);

    PlanetQuadtree planetQuadtree = new PlanetQuadtree(components, config,
        config.getRootChunkCount(), config.getHorizontalScaling());

    quadtree = planetQuadtree;
    addChild(planetQuadtree);

    planetQuadtree.start();
  }

  public void render() {
    return;
  }

}
