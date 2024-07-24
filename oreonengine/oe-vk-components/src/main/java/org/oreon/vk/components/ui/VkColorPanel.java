package org.oreon.vk.components.ui;

import static org.lwjgl.system.MemoryUtil.memAlloc;
import static org.lwjgl.vulkan.VK10.VK_PRIMITIVE_TOPOLOGY_TRIANGLE_LIST;
import static org.lwjgl.vulkan.VK10.VK_SHADER_STAGE_FRAGMENT_BIT;
import static org.lwjgl.vulkan.VK10.VK_SHADER_STAGE_VERTEX_BIT;

import java.nio.ByteBuffer;
import org.oreon.common.ui.UIElement;
import org.oreon.core.context.ContextHolder;
import org.oreon.core.math.Vec4f;
import org.oreon.core.model.Vertex.VertexLayout;
import org.oreon.core.scenegraph.NodeComponentType;
import org.oreon.core.util.BufferUtil;
import org.oreon.core.vk.command.CommandBuffer;
import org.oreon.core.vk.command.SubmitInfo;
import org.oreon.core.vk.context.DeviceManager.DeviceType;
import org.oreon.core.vk.context.VkOreonContext;
import org.oreon.core.vk.device.LogicalDevice;
import org.oreon.core.vk.device.VkDeviceBundle;
import org.oreon.core.vk.framebuffer.VkFrameBufferObject;
import org.oreon.core.vk.pipeline.ShaderPipeline;
import org.oreon.core.vk.pipeline.VkPipeline;
import org.oreon.core.vk.pipeline.VkVertexInput;
import org.oreon.core.vk.scenegraph.VkMeshData;
import org.oreon.core.vk.scenegraph.VkRenderInfo;
import org.oreon.core.vk.wrapper.command.SecondaryDrawIndexedCmdBuffer;
import org.oreon.core.vk.wrapper.pipeline.GraphicsPipelineAlphaBlend;

public class VkColorPanel extends UIElement {

  private VkPipeline graphicsPipeline;
  private CommandBuffer cmdBuffer;
  private SubmitInfo submitInfo;

  public VkColorPanel(Vec4f rgba, int xPos, int yPos, int xScaling, int yScaling,
      VkMeshData panelMeshBuffer, VkFrameBufferObject fbo) {
    super(xPos, yPos, xScaling, yScaling);

    // flip y-axxis for vulkan coordinate system
    getOrthographicMatrix().set(1, 1, -getOrthographicMatrix().get(1, 1));
    final VkOreonContext context = (VkOreonContext) ContextHolder.getContext();
    VkDeviceBundle deviceBundle = context.getDeviceManager().getDeviceBundle(DeviceType.MAJOR_GRAPHICS_DEVICE);
    LogicalDevice device = deviceBundle.getLogicalDevice();

    ShaderPipeline shaderPipeline = new ShaderPipeline(device.getHandle());
    shaderPipeline.createVertexShader("shaders/ui/colorPanel.vert.spv");
    shaderPipeline.createFragmentShader("shaders/ui/colorPanel.frag.spv");
    shaderPipeline.createShaderPipeline();

    int pushConstantRange = Float.BYTES * 20;
    ByteBuffer pushConstants = memAlloc(pushConstantRange);
    pushConstants.put(BufferUtil.createByteBuffer(getOrthographicMatrix()));
    pushConstants.putFloat(rgba.getX());
    pushConstants.putFloat(rgba.getY());
    pushConstants.putFloat(rgba.getZ());
    pushConstants.putFloat(rgba.getW());
    pushConstants.flip();

    VkVertexInput vertexInput = new VkVertexInput(VertexLayout.POS2D);

    graphicsPipeline = new GraphicsPipelineAlphaBlend(device.getHandle(),
        shaderPipeline, vertexInput, VK_PRIMITIVE_TOPOLOGY_TRIANGLE_LIST,
        null,
        fbo.getWidth(), fbo.getHeight(),
        fbo.getRenderPass().getHandle(),
        fbo.getColorAttachmentCount(),
        1, pushConstantRange,
        VK_SHADER_STAGE_FRAGMENT_BIT | VK_SHADER_STAGE_VERTEX_BIT);

    cmdBuffer = new SecondaryDrawIndexedCmdBuffer(
        device.getHandle(),
        deviceBundle.getLogicalDevice().getGraphicsCommandPool(Thread.currentThread().getId()).getHandle(),
        graphicsPipeline.getHandle(), graphicsPipeline.getLayoutHandle(),
        fbo.getFrameBuffer().getHandle(),
        fbo.getRenderPass().getHandle(),
        0,
        null,
        panelMeshBuffer.getVertexBufferObject().getHandle(),
        panelMeshBuffer.getIndexBufferObject().getHandle(),
        panelMeshBuffer.getIndexCount(),
        pushConstants,
        VK_SHADER_STAGE_FRAGMENT_BIT | VK_SHADER_STAGE_VERTEX_BIT);

    submitInfo = new SubmitInfo();
    submitInfo.setCommandBuffers(cmdBuffer.getHandlePointer());

    VkRenderInfo mainRenderInfo = VkRenderInfo.builder().commandBuffer(cmdBuffer).build();
    addComponent(NodeComponentType.MAIN_RENDERINFO, mainRenderInfo);
  }

}
