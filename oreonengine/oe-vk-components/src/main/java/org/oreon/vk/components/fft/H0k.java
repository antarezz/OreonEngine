package org.oreon.vk.components.fft;

import static org.lwjgl.system.MemoryUtil.memAlloc;
import static org.lwjgl.system.MemoryUtil.memFree;
import static org.lwjgl.vulkan.VK10.VK_ACCESS_SHADER_READ_BIT;
import static org.lwjgl.vulkan.VK10.VK_DESCRIPTOR_TYPE_STORAGE_IMAGE;
import static org.lwjgl.vulkan.VK10.VK_FORMAT_R32G32B32A32_SFLOAT;
import static org.lwjgl.vulkan.VK10.VK_FORMAT_R8G8B8A8_UNORM;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_ASPECT_COLOR_BIT;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_LAYOUT_GENERAL;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_USAGE_STORAGE_BIT;
import static org.lwjgl.vulkan.VK10.VK_PIPELINE_STAGE_COMPUTE_SHADER_BIT;
import static org.lwjgl.vulkan.VK10.VK_QUEUE_FAMILY_IGNORED;
import static org.lwjgl.vulkan.VK10.VK_SHADER_STAGE_COMPUTE_BIT;

import java.nio.ByteBuffer;

import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkPhysicalDeviceMemoryProperties;
import org.lwjgl.vulkan.VkQueue;
import org.oreon.core.math.Vec2f;
import org.oreon.core.vk.command.CommandBuffer;
import org.oreon.core.vk.command.SubmitInfo;
import org.oreon.core.vk.descriptor.DescriptorPool;
import org.oreon.core.vk.descriptor.DescriptorSet;
import org.oreon.core.vk.descriptor.DescriptorSetLayout;
import org.oreon.core.vk.device.VkDeviceBundle;
import org.oreon.core.vk.image.VkImage;
import org.oreon.core.vk.image.VkImageView;
import org.oreon.core.vk.pipeline.VkPipeline;
import org.oreon.core.vk.synchronization.Fence;
import org.oreon.core.vk.util.VkUtil;
import org.oreon.core.vk.wrapper.command.ComputeCmdBuffer;
import org.oreon.core.vk.wrapper.descriptor.VkDescriptor;
import org.oreon.core.vk.wrapper.image.Image2DDeviceLocal;
import org.oreon.core.vk.wrapper.image.VkImageHelper;
import org.oreon.core.vk.wrapper.shader.ComputeShader;

import lombok.Getter;

public class H0k {

  @Getter
  private VkImageView h0k_imageView;
  @Getter
  private VkImageView h0minusk_imageView;

  private VkImage h0k_image;
  private VkImage h0minusk_image;

  public H0k(VkDeviceBundle deviceBundle, int N, int L,
      float amplitude, Vec2f windDirection, float windSpeed, float capillarSuppressFactor) {

    VkDevice device = deviceBundle.getLogicalDevice().getHandle();
    VkPhysicalDeviceMemoryProperties memoryProperties = deviceBundle.getPhysicalDevice().getMemoryProperties();
    DescriptorPool descriptorPool = deviceBundle.getLogicalDevice().getDescriptorPool(Thread.currentThread().getId());
    VkQueue queue = deviceBundle.getLogicalDevice().getComputeQueue();

    h0k_image = new Image2DDeviceLocal(device, memoryProperties, N, N,
        VK_FORMAT_R32G32B32A32_SFLOAT, VK_IMAGE_USAGE_STORAGE_BIT);

    h0k_imageView = new VkImageView(device,
        VK_FORMAT_R32G32B32A32_SFLOAT, h0k_image.getHandle(), VK_IMAGE_ASPECT_COLOR_BIT);

    h0minusk_image = new Image2DDeviceLocal(device, memoryProperties, N, N,
        VK_FORMAT_R32G32B32A32_SFLOAT, VK_IMAGE_USAGE_STORAGE_BIT);

    h0minusk_imageView = new VkImageView(device,
        VK_FORMAT_R32G32B32A32_SFLOAT, h0minusk_image.getHandle(), VK_IMAGE_ASPECT_COLOR_BIT);

    VkImage noise0Image = VkImageHelper.loadImageFromFile(
        device, memoryProperties,
        deviceBundle.getLogicalDevice().getTransferCommandPool(Thread.currentThread().getId()).getHandle(),
        deviceBundle.getLogicalDevice().getTransferQueue(),
        "textures/noise/Noise" + N + "_0.jpg",
        VK_IMAGE_USAGE_STORAGE_BIT,
        VK_IMAGE_LAYOUT_GENERAL,
        VK_ACCESS_SHADER_READ_BIT,
        VK_PIPELINE_STAGE_COMPUTE_SHADER_BIT,
        VK_QUEUE_FAMILY_IGNORED);

    VkImageView noise0ImageView = new VkImageView(device,
        VK_FORMAT_R8G8B8A8_UNORM, noise0Image.getHandle(), VK_IMAGE_ASPECT_COLOR_BIT);

    VkImage noise1Image = VkImageHelper.loadImageFromFile(
        device, memoryProperties,
        deviceBundle.getLogicalDevice().getTransferCommandPool(Thread.currentThread().getId()).getHandle(),
        deviceBundle.getLogicalDevice().getTransferQueue(),
        "textures/noise/Noise" + N + "_1.jpg",
        VK_IMAGE_USAGE_STORAGE_BIT,
        VK_IMAGE_LAYOUT_GENERAL,
        VK_ACCESS_SHADER_READ_BIT,
        VK_PIPELINE_STAGE_COMPUTE_SHADER_BIT,
        VK_QUEUE_FAMILY_IGNORED);

    VkImageView noise1ImageView = new VkImageView(device,
        VK_FORMAT_R8G8B8A8_UNORM, noise1Image.getHandle(), VK_IMAGE_ASPECT_COLOR_BIT);

    VkImage noise2Image = VkImageHelper.loadImageFromFile(
        device, memoryProperties,
        deviceBundle.getLogicalDevice().getTransferCommandPool(Thread.currentThread().getId()).getHandle(),
        deviceBundle.getLogicalDevice().getTransferQueue(),
        "textures/noise/Noise" + N + "_2.jpg",
        VK_IMAGE_USAGE_STORAGE_BIT,
        VK_IMAGE_LAYOUT_GENERAL,
        VK_ACCESS_SHADER_READ_BIT,
        VK_PIPELINE_STAGE_COMPUTE_SHADER_BIT,
        VK_QUEUE_FAMILY_IGNORED);

    VkImageView noise2ImageView = new VkImageView(device,
        VK_FORMAT_R8G8B8A8_UNORM, noise2Image.getHandle(), VK_IMAGE_ASPECT_COLOR_BIT);

    VkImage noise3Image = VkImageHelper.loadImageFromFile(
        device, memoryProperties,
        deviceBundle.getLogicalDevice().getTransferCommandPool(Thread.currentThread().getId()).getHandle(),
        deviceBundle.getLogicalDevice().getTransferQueue(),
        "textures/noise/Noise" + N + "_3.jpg",
        VK_IMAGE_USAGE_STORAGE_BIT,
        VK_IMAGE_LAYOUT_GENERAL,
        VK_ACCESS_SHADER_READ_BIT,
        VK_PIPELINE_STAGE_COMPUTE_SHADER_BIT,
        VK_QUEUE_FAMILY_IGNORED);

    VkImageView noise3ImageView = new VkImageView(device,
        VK_FORMAT_R8G8B8A8_UNORM, noise3Image.getHandle(), VK_IMAGE_ASPECT_COLOR_BIT);

    int pushConstantRange = Integer.BYTES * 2 + Float.BYTES * 6;

    ByteBuffer pushConstants = memAlloc(pushConstantRange);
    pushConstants.putInt(N);
    pushConstants.putInt(L);
    pushConstants.putFloat(amplitude);
    pushConstants.putFloat(windSpeed);
    pushConstants.putFloat(windDirection.getX());
    pushConstants.putFloat(windDirection.getY());
    pushConstants.putFloat(capillarSuppressFactor);
    pushConstants.putFloat(0);
    pushConstants.flip();

    VkDescriptor descriptor = new SpectrumDescriptor(device, descriptorPool,
        noise0ImageView, noise1ImageView, noise2ImageView, noise3ImageView);

    VkPipeline pipeline = new VkPipeline(device);
    pipeline.setPushConstantsRange(VK_SHADER_STAGE_COMPUTE_BIT, pushConstantRange);
    pipeline.setLayout(descriptor.getDescriptorSetLayout().getHandlePointer());
    pipeline.createComputePipeline(new ComputeShader(device, "shaders/fft/h0k.comp.spv"));

    CommandBuffer commandBuffer = new ComputeCmdBuffer(device,
        deviceBundle.getLogicalDevice().getComputeCommandPool(Thread.currentThread().getId()).getHandle(),
        pipeline.getHandle(), pipeline.getLayoutHandle(),
        VkUtil.createLongArray(descriptor.getDescriptorSet()), N / 16, N / 16, 1,
        pushConstants, VK_SHADER_STAGE_COMPUTE_BIT);

    Fence fence = new Fence(device);

    SubmitInfo submitInfo = new SubmitInfo();
    submitInfo.setCommandBuffers(commandBuffer.getHandlePointer());
    submitInfo.setFence(fence);
    submitInfo.submit(queue);

    fence.waitForFence();

    pipeline.destroy();
    commandBuffer.destroy();
    fence.destroy();
    descriptor.destroy();
    memFree(pushConstants);

    noise0Image.destroy();
    noise1Image.destroy();
    noise2Image.destroy();
    noise3Image.destroy();
    noise0ImageView.destroy();
    noise1ImageView.destroy();
    noise2ImageView.destroy();
    noise3ImageView.destroy();
  }

  private class SpectrumDescriptor extends VkDescriptor {

    public SpectrumDescriptor(VkDevice device, DescriptorPool descriptorPool,
        VkImageView noise0, VkImageView noise1,
        VkImageView noise2, VkImageView noise3) {

      descriptorSetLayout = new DescriptorSetLayout(device, 6);
      descriptorSetLayout.addLayoutBinding(0, VK_DESCRIPTOR_TYPE_STORAGE_IMAGE,
          VK_SHADER_STAGE_COMPUTE_BIT);
      descriptorSetLayout.addLayoutBinding(1, VK_DESCRIPTOR_TYPE_STORAGE_IMAGE,
          VK_SHADER_STAGE_COMPUTE_BIT);
      descriptorSetLayout.addLayoutBinding(2, VK_DESCRIPTOR_TYPE_STORAGE_IMAGE,
          VK_SHADER_STAGE_COMPUTE_BIT);
      descriptorSetLayout.addLayoutBinding(3, VK_DESCRIPTOR_TYPE_STORAGE_IMAGE,
          VK_SHADER_STAGE_COMPUTE_BIT);
      descriptorSetLayout.addLayoutBinding(4, VK_DESCRIPTOR_TYPE_STORAGE_IMAGE,
          VK_SHADER_STAGE_COMPUTE_BIT);
      descriptorSetLayout.addLayoutBinding(5, VK_DESCRIPTOR_TYPE_STORAGE_IMAGE,
          VK_SHADER_STAGE_COMPUTE_BIT);
      descriptorSetLayout.create();

      descriptorSet = new DescriptorSet(device, descriptorPool.getHandle(),
          descriptorSetLayout.getHandlePointer());
      descriptorSet.updateDescriptorImageBuffer(h0k_imageView.getHandle(),
          VK_IMAGE_LAYOUT_GENERAL, -1, 0, VK_DESCRIPTOR_TYPE_STORAGE_IMAGE);
      descriptorSet.updateDescriptorImageBuffer(h0minusk_imageView.getHandle(),
          VK_IMAGE_LAYOUT_GENERAL, -1, 1, VK_DESCRIPTOR_TYPE_STORAGE_IMAGE);
      descriptorSet.updateDescriptorImageBuffer(noise0.getHandle(),
          VK_IMAGE_LAYOUT_GENERAL, -1, 2, VK_DESCRIPTOR_TYPE_STORAGE_IMAGE);
      descriptorSet.updateDescriptorImageBuffer(noise1.getHandle(),
          VK_IMAGE_LAYOUT_GENERAL, -1, 3, VK_DESCRIPTOR_TYPE_STORAGE_IMAGE);
      descriptorSet.updateDescriptorImageBuffer(noise2.getHandle(),
          VK_IMAGE_LAYOUT_GENERAL, -1, 4, VK_DESCRIPTOR_TYPE_STORAGE_IMAGE);
      descriptorSet.updateDescriptorImageBuffer(noise3.getHandle(),
          VK_IMAGE_LAYOUT_GENERAL, -1, 5, VK_DESCRIPTOR_TYPE_STORAGE_IMAGE);
    }
  }

  public void destroy() {

    h0k_image.destroy();
    h0k_imageView.destroy();
    h0minusk_image.destroy();
    h0minusk_imageView.destroy();
  }
}
