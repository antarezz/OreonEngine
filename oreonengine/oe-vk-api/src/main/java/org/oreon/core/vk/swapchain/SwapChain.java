package org.oreon.core.vk.swapchain;

import static org.lwjgl.system.MemoryUtil.memAllocInt;
import static org.lwjgl.system.MemoryUtil.memAllocLong;
import static org.lwjgl.system.MemoryUtil.memFree;
import static org.lwjgl.vulkan.KHRSurface.VK_COLOR_SPACE_SRGB_NONLINEAR_KHR;
import static org.lwjgl.vulkan.KHRSurface.VK_COMPOSITE_ALPHA_OPAQUE_BIT_KHR;
import static org.lwjgl.vulkan.KHRSurface.VK_PRESENT_MODE_FIFO_KHR;
import static org.lwjgl.vulkan.KHRSurface.VK_PRESENT_MODE_IMMEDIATE_KHR;
import static org.lwjgl.vulkan.KHRSurface.VK_PRESENT_MODE_MAILBOX_KHR;
import static org.lwjgl.vulkan.KHRSurface.VK_SURFACE_TRANSFORM_IDENTITY_BIT_KHR;
import static org.lwjgl.vulkan.KHRSwapchain.VK_IMAGE_LAYOUT_PRESENT_SRC_KHR;
import static org.lwjgl.vulkan.KHRSwapchain.VK_STRUCTURE_TYPE_PRESENT_INFO_KHR;
import static org.lwjgl.vulkan.KHRSwapchain.VK_STRUCTURE_TYPE_SWAPCHAIN_CREATE_INFO_KHR;
import static org.lwjgl.vulkan.KHRSwapchain.vkAcquireNextImageKHR;
import static org.lwjgl.vulkan.KHRSwapchain.vkCreateSwapchainKHR;
import static org.lwjgl.vulkan.KHRSwapchain.vkDestroySwapchainKHR;
import static org.lwjgl.vulkan.KHRSwapchain.vkGetSwapchainImagesKHR;
import static org.lwjgl.vulkan.KHRSwapchain.vkQueuePresentKHR;
import static org.lwjgl.vulkan.VK10.VK_ACCESS_COLOR_ATTACHMENT_READ_BIT;
import static org.lwjgl.vulkan.VK10.VK_ACCESS_COLOR_ATTACHMENT_WRITE_BIT;
import static org.lwjgl.vulkan.VK10.VK_BUFFER_USAGE_INDEX_BUFFER_BIT;
import static org.lwjgl.vulkan.VK10.VK_BUFFER_USAGE_VERTEX_BUFFER_BIT;
import static org.lwjgl.vulkan.VK10.VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER;
import static org.lwjgl.vulkan.VK10.VK_FILTER_NEAREST;
import static org.lwjgl.vulkan.VK10.VK_FORMAT_B8G8R8A8_SRGB;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_ASPECT_COLOR_BIT;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_LAYOUT_UNDEFINED;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_USAGE_COLOR_ATTACHMENT_BIT;
import static org.lwjgl.vulkan.VK10.VK_NULL_HANDLE;
import static org.lwjgl.vulkan.VK10.VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT;
import static org.lwjgl.vulkan.VK10.VK_PIPELINE_STAGE_FRAGMENT_SHADER_BIT;
import static org.lwjgl.vulkan.VK10.VK_PRIMITIVE_TOPOLOGY_TRIANGLE_LIST;
import static org.lwjgl.vulkan.VK10.VK_SAMPLER_ADDRESS_MODE_REPEAT;
import static org.lwjgl.vulkan.VK10.VK_SAMPLER_MIPMAP_MODE_NEAREST;
import static org.lwjgl.vulkan.VK10.VK_SHADER_STAGE_FRAGMENT_BIT;
import static org.lwjgl.vulkan.VK10.VK_SHARING_MODE_EXCLUSIVE;
import static org.lwjgl.vulkan.VK10.VK_SUBPASS_EXTERNAL;
import static org.lwjgl.vulkan.VK10.VK_SUCCESS;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkExtent2D;
import org.lwjgl.vulkan.VkPresentInfoKHR;
import org.lwjgl.vulkan.VkQueue;
import org.lwjgl.vulkan.VkSwapchainCreateInfoKHR;
import org.oreon.core.context.ContextHolder;
import org.oreon.core.model.Mesh;
import org.oreon.core.model.Vertex.VertexLayout;
import org.oreon.core.util.BufferUtil;
import org.oreon.core.util.MeshGenerator;
import org.oreon.core.vk.command.CommandBuffer;
import org.oreon.core.vk.command.SubmitInfo;
import org.oreon.core.vk.descriptor.DescriptorSet;
import org.oreon.core.vk.descriptor.DescriptorSetLayout;
import org.oreon.core.vk.device.LogicalDevice;
import org.oreon.core.vk.device.PhysicalDevice;
import org.oreon.core.vk.framebuffer.VkFrameBuffer;
import org.oreon.core.vk.image.VkImageView;
import org.oreon.core.vk.image.VkSampler;
import org.oreon.core.vk.memory.VkBuffer;
import org.oreon.core.vk.pipeline.RenderPass;
import org.oreon.core.vk.pipeline.ShaderPipeline;
import org.oreon.core.vk.pipeline.VkPipeline;
import org.oreon.core.vk.pipeline.VkVertexInput;
import org.oreon.core.vk.synchronization.Fence;
import org.oreon.core.vk.synchronization.VkSemaphore;
import org.oreon.core.vk.util.VkUtil;
import org.oreon.core.vk.wrapper.buffer.VkBufferHelper;
import org.oreon.core.vk.wrapper.command.DrawCmdBuffer;

public class SwapChain {

  @Getter
  private long handle;

  @Getter
  private Fence drawFence;

  private LongBuffer pHandle;
  private VkExtent2D extent;
  private List<Long> swapChainImages;
  private List<VkImageView> swapChainImageViews;
  private List<VkFrameBuffer> frameBuffers;
  private List<CommandBuffer> renderCommandBuffers;
  private VkPresentInfoKHR presentInfo;
  private IntBuffer pAcquiredImageIndex;
  private VkSemaphore renderCompleteSemaphore;
  private VkSemaphore imageAcquiredSemaphore;
  private SubmitInfo submitInfo;
  private VkBuffer vertexBufferObject;
  private VkBuffer indexBufferObject;
  private VkPipeline pipeline;
  private RenderPass renderPass;
  private VkSampler sampler;
  private DescriptorSet descriptorSet;
  private DescriptorSetLayout descriptorSetLayout;

  private VkDevice device;

  private final long UINT64_MAX = 0xFFFFFFFFFFFFFFFFL;

  public SwapChain(LogicalDevice logicalDevice,
      PhysicalDevice physicalDevice,
      long surface,
      long imageView) {

    this.device = logicalDevice.getHandle();

    extent = physicalDevice.getSwapChainCapabilities().getSurfaceCapabilities().currentExtent();
    extent.width(ContextHolder.getContext().getWindow().getWidth());
    extent.height(ContextHolder.getContext().getWindow().getHeight());

    int imageFormat = VK_FORMAT_B8G8R8A8_SRGB;
    int colorSpace = VK_COLOR_SPACE_SRGB_NONLINEAR_KHR;

    physicalDevice.checkDeviceFormatAndColorSpaceSupport(imageFormat, colorSpace);

    int presentMode = VK_PRESENT_MODE_MAILBOX_KHR;

    if (!physicalDevice.checkDevicePresentationModeSupport(presentMode)) {

      if (physicalDevice.checkDevicePresentationModeSupport(VK_PRESENT_MODE_FIFO_KHR)) {
        presentMode = VK_PRESENT_MODE_FIFO_KHR;
      } else {
        presentMode = VK_PRESENT_MODE_IMMEDIATE_KHR;
      }
    }

    int minImageCount = physicalDevice.getDeviceMinImageCount4TripleBuffering();

    createDescriptor(logicalDevice.getDescriptorPool(Thread.currentThread().getId()).getHandle(),
        imageView);

    createRenderPass(imageFormat);
    createPipeline(renderPass.getHandle(),
        descriptorSetLayout.getHandlePointer());

    VkSwapchainCreateInfoKHR swapchainCreateInfo = VkSwapchainCreateInfoKHR.calloc()
        .sType(VK_STRUCTURE_TYPE_SWAPCHAIN_CREATE_INFO_KHR)
        .pNext(0)
        .surface(surface)
        .oldSwapchain(VK_NULL_HANDLE)
        .compositeAlpha(VK_COMPOSITE_ALPHA_OPAQUE_BIT_KHR)
        .imageUsage(VK_IMAGE_USAGE_COLOR_ATTACHMENT_BIT)
        .preTransform(VK_SURFACE_TRANSFORM_IDENTITY_BIT_KHR)
        .minImageCount(minImageCount)
        .imageFormat(imageFormat)
        .imageColorSpace(colorSpace)
        .imageExtent(extent)
        .presentMode(presentMode)
        .imageArrayLayers(1)
        .clipped(true)
        // presentation queue family and graphics queue family are the same
        .imageSharingMode(VK_SHARING_MODE_EXCLUSIVE)
        .pQueueFamilyIndices(null);

    pHandle = memAllocLong(1);
    int err = vkCreateSwapchainKHR(device, swapchainCreateInfo, null, pHandle);
    handle = pHandle.get(0);

    if (err != VK_SUCCESS) {
      throw new AssertionError("Failed to create swap chain: " + VkUtil.translateVulkanResult(err));
    }

    createImages();
    createImageViews(imageFormat);
    createFrameBuffers(renderPass.getHandle());

    renderCompleteSemaphore = new VkSemaphore(device);
    imageAcquiredSemaphore = new VkSemaphore(device);
    pAcquiredImageIndex = memAllocInt(1);

    presentInfo = VkPresentInfoKHR.calloc()
        .sType(VK_STRUCTURE_TYPE_PRESENT_INFO_KHR)
        .pNext(0)
        .pWaitSemaphores(renderCompleteSemaphore.getHandlePointer())
        .swapchainCount(1)
        .pSwapchains(pHandle)
        .pImageIndices(pAcquiredImageIndex)
        .pResults(null);

    swapchainCreateInfo.free();

    Mesh fullScreenQuad = MeshGenerator.NDCQuad2D();
    ByteBuffer vertexBuffer = BufferUtil.createByteBuffer(fullScreenQuad.getVertices(), VertexLayout.POS_UV);
    ByteBuffer indexBuffer = BufferUtil.createByteBuffer(fullScreenQuad.getIndices());

    vertexBufferObject = VkBufferHelper.createDeviceLocalBuffer(device,
        physicalDevice.getMemoryProperties(),
        logicalDevice.getTransferCommandPool(Thread.currentThread().getId()).getHandle(),
        logicalDevice.getTransferQueue(),
        vertexBuffer, VK_BUFFER_USAGE_VERTEX_BUFFER_BIT);

    indexBufferObject = VkBufferHelper.createDeviceLocalBuffer(device,
        physicalDevice.getMemoryProperties(),
        logicalDevice.getTransferCommandPool(Thread.currentThread().getId()).getHandle(),
        logicalDevice.getTransferQueue(),
        indexBuffer, VK_BUFFER_USAGE_INDEX_BUFFER_BIT);

    createRenderCommandBuffers(logicalDevice.getGraphicsCommandPool(Thread.currentThread().getId()).getHandle(),
        renderPass.getHandle(),
        vertexBufferObject.getHandle(),
        indexBufferObject.getHandle(),
        fullScreenQuad.getIndices().length,
        VkUtil.createLongArray(descriptorSet));

    drawFence = new Fence(device);

    submitInfo = new SubmitInfo();
    submitInfo.setSignalSemaphores(renderCompleteSemaphore.getHandlePointer());
    submitInfo.setFence(drawFence);
  }

  public void createDescriptor(long descriptorPool, long imageView) {

    descriptorSetLayout = new DescriptorSetLayout(device, 1);
    descriptorSetLayout.addLayoutBinding(0, VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER,
        VK_SHADER_STAGE_FRAGMENT_BIT);
    descriptorSetLayout.create();

    sampler = new VkSampler(device, VK_FILTER_NEAREST, false, 0,
        VK_SAMPLER_MIPMAP_MODE_NEAREST, 0, VK_SAMPLER_ADDRESS_MODE_REPEAT);

    descriptorSet = new DescriptorSet(device, descriptorPool, descriptorSetLayout.getHandlePointer());
    descriptorSet.updateDescriptorImageBuffer(imageView, VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL,
        sampler.getHandle(), 0, VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER);
  }

  public void createRenderPass(int imageFormat) {

    renderPass = new RenderPass(device);
    renderPass.addColorAttachment(0, VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL, imageFormat, 1,
        VK_IMAGE_LAYOUT_UNDEFINED, VK_IMAGE_LAYOUT_PRESENT_SRC_KHR);
    renderPass.addSubpassDependency(VK_SUBPASS_EXTERNAL, 0, VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT,
        VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT, 0,
        VK_ACCESS_COLOR_ATTACHMENT_READ_BIT | VK_ACCESS_COLOR_ATTACHMENT_WRITE_BIT, 0);
    renderPass.createSubpass();
    renderPass.createRenderPass();
  }

  public void createPipeline(long renderPass, LongBuffer layouts) {

    ShaderPipeline shaderPipeline = new ShaderPipeline(device);
    shaderPipeline.createVertexShader("shaders/quad/quad.vert.spv");
    shaderPipeline.createFragmentShader("shaders/quad/quad.frag.spv");
    shaderPipeline.createShaderPipeline();

    VkVertexInput vertexInputInfo = new VkVertexInput(VertexLayout.POS_UV);

    pipeline = new VkPipeline(device);
    pipeline.setVertexInput(vertexInputInfo);
    pipeline.setInputAssembly(VK_PRIMITIVE_TOPOLOGY_TRIANGLE_LIST);
    pipeline.setViewportAndScissor(extent.width(), extent.height());
    pipeline.setRasterizer();
    pipeline.setMultisamplingState(1);
    pipeline.addColorBlendAttachment();
    pipeline.setColorBlendState();
    pipeline.setDepthAndStencilTest(false);
    pipeline.setDynamicState();
    pipeline.setLayout(layouts);
    pipeline.createGraphicsPipeline(shaderPipeline, renderPass);

    shaderPipeline.destroy();
  }

  public void createImages() {

    IntBuffer pImageCount = memAllocInt(1);
    int err = vkGetSwapchainImagesKHR(device, handle, pImageCount, null);
    int imageCount = pImageCount.get(0);
    if (err != VK_SUCCESS) {
      throw new AssertionError("Failed to get number of swapchain images: " + VkUtil.translateVulkanResult(err));
    }

    LongBuffer pSwapchainImages = memAllocLong(imageCount);
    err = vkGetSwapchainImagesKHR(device, handle, pImageCount, pSwapchainImages);
    if (err != VK_SUCCESS) {
      throw new AssertionError("Failed to get swapchain images: " + VkUtil.translateVulkanResult(err));
    }

    swapChainImages = new ArrayList<>(imageCount);
    for (int i = 0; i < imageCount; i++) {
      swapChainImages.add(pSwapchainImages.get(i));
    }

    memFree(pImageCount);
    memFree(pSwapchainImages);
  }

  public void createImageViews(int imageFormat) {

    swapChainImageViews = new ArrayList<>(swapChainImages.size());
    for (long swapChainImage : swapChainImages) {

      VkImageView imageView = new VkImageView(device, imageFormat, swapChainImage,
          VK_IMAGE_ASPECT_COLOR_BIT);

      swapChainImageViews.add(imageView);
    }
  }

  public void createFrameBuffers(long renderPass) {

    frameBuffers = new ArrayList<>(swapChainImages.size());
    for (VkImageView imageView : swapChainImageViews) {

      LongBuffer pAttachments = memAllocLong(1);
      pAttachments.put(0, imageView.getHandle());

      VkFrameBuffer frameBuffer = new VkFrameBuffer(device,
          extent.width(), extent.height(), 1, pAttachments, renderPass);
      frameBuffers.add(frameBuffer);
    }
  }

  public void createRenderCommandBuffers(long commandPool, long renderPass,
      long vertexBuffer, long indexBuffer, int indexCount,
      long[] descriptorSets) {

    renderCommandBuffers = new ArrayList<>();

    for (VkFrameBuffer frameBuffer : frameBuffers) {

      CommandBuffer commandBuffer = new DrawCmdBuffer(
          device, commandPool, pipeline.getHandle(),
          pipeline.getLayoutHandle(), renderPass,
          frameBuffer.getHandle(), extent.width(), extent.height(),
          1, 0, descriptorSets, vertexBuffer, indexBuffer, indexCount);

      renderCommandBuffers.add(commandBuffer);
    }
  }

  public void draw(VkQueue queue, VkSemaphore waitSemaphore) {

    int err = vkAcquireNextImageKHR(device, handle, UINT64_MAX, imageAcquiredSemaphore.getHandle(), VK_NULL_HANDLE,
        pAcquiredImageIndex);
    if (err != VK_SUCCESS) {
      throw new AssertionError("Failed to acquire next swapchain image: " + VkUtil.translateVulkanResult(err));
    }

    CommandBuffer currentRenderCommandBuffer = renderCommandBuffers.get(pAcquiredImageIndex.get(0));

    IntBuffer pWaitDstStageMask = memAllocInt(2);
    pWaitDstStageMask.put(0, VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT);
    pWaitDstStageMask.put(1, VK_PIPELINE_STAGE_FRAGMENT_SHADER_BIT);

    LongBuffer pWaitSemaphores = memAllocLong(2);
    pWaitSemaphores.put(0, imageAcquiredSemaphore.getHandle());
    pWaitSemaphores.put(1, waitSemaphore.getHandle());

    submitInfo.setCommandBuffers(currentRenderCommandBuffer.getHandlePointer());
    submitInfo.setWaitDstStageMask(pWaitDstStageMask);
    submitInfo.setWaitSemaphores(pWaitSemaphores);
    submitInfo.submit(queue);

    VkUtil.vkCheckResult(vkQueuePresentKHR(queue, presentInfo));
  }

  public void destroy() {

    for (VkImageView imageView : swapChainImageViews) {
      imageView.destroy();
    }
    for (VkFrameBuffer framebuffer : frameBuffers) {
      framebuffer.destroy();
    }
    for (CommandBuffer commandbuffer : renderCommandBuffers) {
      commandbuffer.destroy();
    }
    vertexBufferObject.destroy();
    indexBufferObject.destroy();
    renderCompleteSemaphore.destroy();
    imageAcquiredSemaphore.destroy();
    descriptorSet.destroy();
    descriptorSetLayout.destroy();
    sampler.destroy();
    pipeline.destroy();
    renderPass.destroy();
    drawFence.destroy();
    vkDestroySwapchainKHR(device, handle, null);
  }

}
