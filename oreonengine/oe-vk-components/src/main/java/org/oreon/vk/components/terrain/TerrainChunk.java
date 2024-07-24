package org.oreon.vk.components.terrain;

import static org.lwjgl.system.MemoryUtil.memAlloc;
import static org.lwjgl.vulkan.VK10.VK_SHADER_STAGE_ALL_GRAPHICS;

import java.nio.ByteBuffer;
import java.util.Map;
import org.oreon.common.quadtree.QuadtreeCache;
import org.oreon.common.quadtree.QuadtreeNode;
import org.oreon.core.context.ContextHolder;
import org.oreon.core.math.Transform;
import org.oreon.core.math.Vec2f;
import org.oreon.core.scenegraph.NodeComponent;
import org.oreon.core.scenegraph.NodeComponentType;
import org.oreon.core.util.BufferUtil;
import org.oreon.core.vk.command.CommandBuffer;
import org.oreon.core.vk.context.DeviceManager.DeviceType;
import org.oreon.core.vk.context.VkOreonContext;
import org.oreon.core.vk.device.LogicalDevice;
import org.oreon.core.vk.pipeline.VkPipeline;
import org.oreon.core.vk.scenegraph.VkMeshData;
import org.oreon.core.vk.scenegraph.VkRenderInfo;
import org.oreon.core.vk.util.VkUtil;
import org.oreon.core.vk.wrapper.command.SecondaryDrawCmdBuffer;
import org.oreon.core.vk.wrapper.pipeline.GraphicsTessellationPipeline;

public class TerrainChunk extends QuadtreeNode {

  public TerrainChunk(Map<NodeComponentType, NodeComponent> components, QuadtreeCache quadtreeCache,
      Transform worldTransform, Vec2f location, int levelOfDetail, Vec2f index) {

    super(components, quadtreeCache, worldTransform, location, levelOfDetail, index);

    try {
      addComponent(NodeComponentType.MAIN_RENDERINFO, components.get(NodeComponentType.MAIN_RENDERINFO).clone());
      addComponent(NodeComponentType.MESH_DATA, components.get(NodeComponentType.MESH_DATA).clone());
    } catch (CloneNotSupportedException e) {
      e.printStackTrace();
    }
    final VkOreonContext context = (VkOreonContext) ContextHolder.getContext();
    LogicalDevice device = context.getDeviceManager().getLogicalDevice(DeviceType.MAJOR_GRAPHICS_DEVICE);

    VkRenderInfo renderInfo = getComponent(NodeComponentType.MAIN_RENDERINFO);
    VkMeshData meshData = getComponent(NodeComponentType.MESH_DATA);

    int pushConstantsRange = Float.BYTES * 42 + Integer.BYTES * 11;

    ByteBuffer pushConstants = memAlloc(pushConstantsRange);
    pushConstants.put(BufferUtil.createByteBuffer(getLocalTransform().getWorldMatrix()));
    pushConstants.put(BufferUtil.createByteBuffer(getWorldTransform().getWorldMatrixRTS()));
    pushConstants.putFloat(quadtreeConfig.getVerticalScaling());
    pushConstants.putFloat(quadtreeConfig.getHorizontalScaling());
    pushConstants.putInt(chunkConfig.getLod());
    pushConstants.putFloat(chunkConfig.getGap());
    pushConstants.put(BufferUtil.createByteBuffer(location));
    pushConstants.put(BufferUtil.createByteBuffer(index));
    for (int morphArea : quadtreeConfig.getLod_morphing_area()) {
      pushConstants.putInt(morphArea);
    }
    pushConstants.putInt(quadtreeConfig.getTessellationFactor());
    pushConstants.putFloat(quadtreeConfig.getTessellationSlope());
    pushConstants.putFloat(quadtreeConfig.getTessellationShift());
    pushConstants.putFloat(quadtreeConfig.getUvScaling());
    pushConstants.putInt(quadtreeConfig.getHighDetailRange());
    pushConstants.flip();

    VkPipeline graphicsPipeline = new GraphicsTessellationPipeline(device.getHandle(),
        renderInfo.getShaderPipeline(), renderInfo.getVertexInput(),
        VkUtil.createLongBuffer(renderInfo.getDescriptorSetLayouts()),
        context.getConfig().getFrameWidth(),
        context.getConfig().getFrameHeight(),
        context.getResources().getOffScreenFbo().getRenderPass().getHandle(),
        context.getResources().getOffScreenFbo().getColorAttachmentCount(),
        context.getConfig().getMultisampling_sampleCount(),
        pushConstantsRange, VK_SHADER_STAGE_ALL_GRAPHICS,
        16);

    CommandBuffer commandBuffer = new SecondaryDrawCmdBuffer(
        device.getHandle(),
        device.getGraphicsCommandPool(Thread.currentThread().getId()).getHandle(),
        graphicsPipeline.getHandle(), graphicsPipeline.getLayoutHandle(),
        context.getResources().getOffScreenFbo().getFrameBuffer().getHandle(),
        context.getResources().getOffScreenFbo().getRenderPass().getHandle(),
        0,
        VkUtil.createLongArray(renderInfo.getDescriptorSets()),
        meshData.getVertexBufferObject().getHandle(),
        meshData.getVertexCount(),
        pushConstants, VK_SHADER_STAGE_ALL_GRAPHICS);

    renderInfo.setCommandBuffer(commandBuffer);
    renderInfo.setPipeline(graphicsPipeline);
  }

  @Override
  public QuadtreeNode createChildChunk(Map<NodeComponentType, NodeComponent> components, QuadtreeCache quadtreeCache,
      Transform worldTransform, Vec2f location, int levelOfDetail, Vec2f index) {
    return new TerrainChunk(components, quadtreeCache, worldTransform, location, levelOfDetail, index);
  }

}
