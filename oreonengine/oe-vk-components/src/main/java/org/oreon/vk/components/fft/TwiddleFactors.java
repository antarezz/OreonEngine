package org.oreon.vk.components.fft;

import static org.lwjgl.system.MemoryUtil.memAlloc;
import static org.lwjgl.system.MemoryUtil.memFree;
import static org.lwjgl.vulkan.VK10.VK_BUFFER_USAGE_STORAGE_BUFFER_BIT;
import static org.lwjgl.vulkan.VK10.VK_DESCRIPTOR_TYPE_STORAGE_BUFFER;
import static org.lwjgl.vulkan.VK10.VK_DESCRIPTOR_TYPE_STORAGE_IMAGE;
import static org.lwjgl.vulkan.VK10.VK_FORMAT_R32G32B32A32_SFLOAT;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_ASPECT_COLOR_BIT;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_LAYOUT_GENERAL;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_USAGE_SAMPLED_BIT;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_USAGE_STORAGE_BIT;
import static org.lwjgl.vulkan.VK10.VK_SHADER_STAGE_COMPUTE_BIT;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkPhysicalDeviceMemoryProperties;
import org.lwjgl.vulkan.VkQueue;
import org.oreon.core.util.BufferUtil;
import org.oreon.core.util.Util;
import org.oreon.core.vk.command.CommandBuffer;
import org.oreon.core.vk.command.SubmitInfo;
import org.oreon.core.vk.descriptor.DescriptorPool;
import org.oreon.core.vk.descriptor.DescriptorSet;
import org.oreon.core.vk.descriptor.DescriptorSetLayout;
import org.oreon.core.vk.device.VkDeviceBundle;
import org.oreon.core.vk.image.VkImage;
import org.oreon.core.vk.image.VkImageView;
import org.oreon.core.vk.memory.VkBuffer;
import org.oreon.core.vk.pipeline.ShaderModule;
import org.oreon.core.vk.pipeline.VkPipeline;
import org.oreon.core.vk.synchronization.Fence;
import org.oreon.core.vk.util.VkUtil;
import org.oreon.core.vk.wrapper.buffer.VkBufferHelper;
import org.oreon.core.vk.wrapper.command.ComputeCmdBuffer;
import org.oreon.core.vk.wrapper.descriptor.VkDescriptor;
import org.oreon.core.vk.wrapper.image.Image2DDeviceLocal;

import lombok.Getter;

public class TwiddleFactors {

  @Getter
  private VkImageView imageView;

  private VkImage image;

  public TwiddleFactors(VkDeviceBundle deviceBundle, int n) {

    VkDevice device = deviceBundle.getLogicalDevice().getHandle();
    VkPhysicalDeviceMemoryProperties memoryProperties = deviceBundle.getPhysicalDevice().getMemoryProperties();
    DescriptorPool descriptorPool = deviceBundle.getLogicalDevice().getDescriptorPool(Thread.currentThread().getId());
    VkQueue queue = deviceBundle.getLogicalDevice().getComputeQueue();

    int log_2_n = (int) (Math.log(n) / Math.log(2));

    image = new Image2DDeviceLocal(device, memoryProperties, log_2_n, n,
        VK_FORMAT_R32G32B32A32_SFLOAT,
        VK_IMAGE_USAGE_SAMPLED_BIT | VK_IMAGE_USAGE_STORAGE_BIT);

    imageView = new VkImageView(device,
        VK_FORMAT_R32G32B32A32_SFLOAT, image.getHandle(), VK_IMAGE_ASPECT_COLOR_BIT);

    VkBuffer bitReversedIndicesBuffer = VkBufferHelper.createDeviceLocalBuffer(device,
        memoryProperties,
        deviceBundle.getLogicalDevice().getTransferCommandPool(Thread.currentThread().getId()).getHandle(),
        deviceBundle.getLogicalDevice().getTransferQueue(),
        BufferUtil.createByteBuffer(Util.initBitReversedIndices(n)),
        VK_BUFFER_USAGE_STORAGE_BUFFER_BIT);

    ByteBuffer pushConstants = memAlloc(Integer.BYTES * 1);
    IntBuffer intBuffer = pushConstants.asIntBuffer();
    intBuffer.put(n);

    VkDescriptor descriptor = new TwiddleDescriptor(device, descriptorPool,
        imageView, bitReversedIndicesBuffer, n);

    ShaderModule computeShader = new ShaderModule(device,
        "shaders/fft/twiddleFactors.comp.spv", VK_SHADER_STAGE_COMPUTE_BIT);

    VkPipeline pipeline = new VkPipeline(device);
    pipeline.setPushConstantsRange(VK_SHADER_STAGE_COMPUTE_BIT, Integer.BYTES * 1);
    pipeline.setLayout(descriptor.getDescriptorSetLayout().getHandlePointer());
    pipeline.createComputePipeline(computeShader);

    CommandBuffer commandBuffer = new ComputeCmdBuffer(device,
        deviceBundle.getLogicalDevice().getComputeCommandPool(Thread.currentThread().getId()).getHandle(),
        pipeline.getHandle(), pipeline.getLayoutHandle(),
        VkUtil.createLongArray(descriptor.getDescriptorSet()),
        log_2_n, n / 16, 1,
        pushConstants, VK_SHADER_STAGE_COMPUTE_BIT);

    Fence fence = new Fence(device);

    SubmitInfo submitInfo = new SubmitInfo();
    submitInfo.setCommandBuffers(commandBuffer.getHandlePointer());
    submitInfo.setFence(fence);
    submitInfo.submit(queue);

    fence.waitForFence();

    computeShader.destroy();
    pipeline.destroy();
    commandBuffer.destroy();
    fence.destroy();
    descriptor.destroy();
    bitReversedIndicesBuffer.destroy();
    memFree(pushConstants);
  }

  private class TwiddleDescriptor extends VkDescriptor {

    public TwiddleDescriptor(VkDevice device, DescriptorPool descriptorPool,
        VkImageView imageView, VkBuffer buffer, int n) {

      descriptorSetLayout = new DescriptorSetLayout(device, 2);
      descriptorSetLayout.addLayoutBinding(0, VK_DESCRIPTOR_TYPE_STORAGE_IMAGE,
          VK_SHADER_STAGE_COMPUTE_BIT);
      descriptorSetLayout.addLayoutBinding(1, VK_DESCRIPTOR_TYPE_STORAGE_BUFFER,
          VK_SHADER_STAGE_COMPUTE_BIT);
      descriptorSetLayout.create();

      descriptorSet = new DescriptorSet(device, descriptorPool.getHandle(),
          descriptorSetLayout.getHandlePointer());
      descriptorSet.updateDescriptorImageBuffer(imageView.getHandle(),
          VK_IMAGE_LAYOUT_GENERAL, -1,
          0, VK_DESCRIPTOR_TYPE_STORAGE_IMAGE);
      descriptorSet.updateDescriptorBuffer(buffer.getHandle(), Integer.BYTES * n, 0, 1,
          VK_DESCRIPTOR_TYPE_STORAGE_BUFFER);
    }
  }

  public void destroy() {

    image.destroy();
    imageView.destroy();
  }

}
