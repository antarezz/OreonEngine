package org.oreon.vk.components.util;

import static org.lwjgl.system.MemoryUtil.memAlloc;
import static org.lwjgl.system.MemoryUtil.memAllocInt;
import static org.lwjgl.vulkan.VK10.VK_ACCESS_SHADER_READ_BIT;
import static org.lwjgl.vulkan.VK10.VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER;
import static org.lwjgl.vulkan.VK10.VK_DESCRIPTOR_TYPE_STORAGE_IMAGE;
import static org.lwjgl.vulkan.VK10.VK_FORMAT_R32G32B32A32_SFLOAT;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_ASPECT_COLOR_BIT;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_LAYOUT_GENERAL;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_LAYOUT_UNDEFINED;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_USAGE_SAMPLED_BIT;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_USAGE_STORAGE_BIT;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_USAGE_TRANSFER_DST_BIT;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_USAGE_TRANSFER_SRC_BIT;
import static org.lwjgl.vulkan.VK10.VK_PIPELINE_STAGE_COMPUTE_SHADER_BIT;
import static org.lwjgl.vulkan.VK10.VK_PIPELINE_STAGE_FRAGMENT_SHADER_BIT;
import static org.lwjgl.vulkan.VK10.VK_SHADER_STAGE_COMPUTE_BIT;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;

import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkQueue;
import org.oreon.core.util.Util;
import org.oreon.core.vk.command.CommandBuffer;
import org.oreon.core.vk.command.CommandPool;
import org.oreon.core.vk.command.SubmitInfo;
import org.oreon.core.vk.descriptor.DescriptorSet;
import org.oreon.core.vk.descriptor.DescriptorSetLayout;
import org.oreon.core.vk.device.VkDeviceBundle;
import org.oreon.core.vk.image.VkImage;
import org.oreon.core.vk.image.VkImageView;
import org.oreon.core.vk.image.VkSampler;
import org.oreon.core.vk.pipeline.ShaderModule;
import org.oreon.core.vk.pipeline.VkPipeline;
import org.oreon.core.vk.synchronization.Fence;
import org.oreon.core.vk.util.VkUtil;
import org.oreon.core.vk.wrapper.command.ComputeCmdBuffer;
import org.oreon.core.vk.wrapper.command.MipMapGenerationCmdBuffer;
import org.oreon.core.vk.wrapper.image.Image2DDeviceLocal;

import lombok.Getter;

public class NormalRenderer {

  private CommandBuffer commandBuffer;
  private VkPipeline pipeline;
  private DescriptorSet descriptorSet;
  private DescriptorSetLayout descriptorSetLayout;
  private SubmitInfo submitInfo;
  private VkImage normalImage;
  private Fence fence;

  private CommandBuffer mipmapCmdBuffer;
  private SubmitInfo mipmapSubmitInfo;

  private VkDevice device;
  private VkQueue computeQueue;
  private VkQueue transferQueue;
  private CommandPool graphicsCommandPool;

  @Getter
  private VkImageView normalImageView;
  private int N;

  public NormalRenderer(VkDeviceBundle deviceBundle, int n, float strength,
      VkImageView heightImageView, VkSampler heightSampler) {

    N = n;

    device = deviceBundle.getLogicalDevice().getHandle();
    computeQueue = deviceBundle.getLogicalDevice().getComputeQueue();
    transferQueue = deviceBundle.getLogicalDevice().getGraphicsQueue();
    graphicsCommandPool = deviceBundle.getLogicalDevice().getGraphicsCommandPool(Thread.currentThread().getId());

    normalImage = new Image2DDeviceLocal(deviceBundle.getLogicalDevice().getHandle(),
        deviceBundle.getPhysicalDevice().getMemoryProperties(), N, N,
        VK_FORMAT_R32G32B32A32_SFLOAT,
        VK_IMAGE_USAGE_SAMPLED_BIT | VK_IMAGE_USAGE_STORAGE_BIT |
            VK_IMAGE_USAGE_TRANSFER_DST_BIT | VK_IMAGE_USAGE_TRANSFER_SRC_BIT,
        1, Util.getLog2N(N));

    normalImageView = new VkImageView(deviceBundle.getLogicalDevice().getHandle(),
        VK_FORMAT_R32G32B32A32_SFLOAT, normalImage.getHandle(), VK_IMAGE_ASPECT_COLOR_BIT,
        Util.getLog2N(N));

    descriptorSetLayout = new DescriptorSetLayout(deviceBundle.getLogicalDevice().getHandle(), 2);
    descriptorSetLayout.addLayoutBinding(0, VK_DESCRIPTOR_TYPE_STORAGE_IMAGE,
        VK_SHADER_STAGE_COMPUTE_BIT);
    descriptorSetLayout.addLayoutBinding(1, VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER,
        VK_SHADER_STAGE_COMPUTE_BIT);
    descriptorSetLayout.create();

    descriptorSet = new DescriptorSet(deviceBundle.getLogicalDevice().getHandle(),
        deviceBundle.getLogicalDevice().getDescriptorPool(Thread.currentThread().getId()).getHandle(),
        descriptorSetLayout.getHandlePointer());
    descriptorSet.updateDescriptorImageBuffer(normalImageView.getHandle(),
        VK_IMAGE_LAYOUT_GENERAL, -1,
        0, VK_DESCRIPTOR_TYPE_STORAGE_IMAGE);
    descriptorSet.updateDescriptorImageBuffer(heightImageView.getHandle(),
        VK_IMAGE_LAYOUT_GENERAL, heightSampler.getHandle(),
        1, VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER);

    ByteBuffer pushConstants = memAlloc(Integer.BYTES + Float.BYTES);
    pushConstants.putInt(N);
    pushConstants.putFloat(strength);
    pushConstants.flip();

    pipeline = new VkPipeline(deviceBundle.getLogicalDevice().getHandle());
    pipeline.setPushConstantsRange(VK_SHADER_STAGE_COMPUTE_BIT, Integer.BYTES + Float.BYTES);
    pipeline.setLayout(descriptorSetLayout.getHandlePointer());
    pipeline.createComputePipeline(new ShaderModule(deviceBundle.getLogicalDevice().getHandle(),
        "shaders/util/normals.comp.spv", VK_SHADER_STAGE_COMPUTE_BIT));

    commandBuffer = new ComputeCmdBuffer(deviceBundle.getLogicalDevice().getHandle(),
        deviceBundle.getLogicalDevice().getComputeCommandPool(Thread.currentThread().getId()).getHandle(),
        pipeline.getHandle(), pipeline.getLayoutHandle(),
        VkUtil.createLongArray(descriptorSet), N / 16, N / 16, 1,
        pushConstants, VK_SHADER_STAGE_COMPUTE_BIT);

    fence = new Fence(deviceBundle.getLogicalDevice().getHandle());

    submitInfo = new SubmitInfo();
    submitInfo.setFence(fence);
    submitInfo.setCommandBuffers(commandBuffer.getHandlePointer());

    mipmapCmdBuffer = new MipMapGenerationCmdBuffer(device,
        graphicsCommandPool.getHandle(), normalImage.getHandle(),
        N, N, Util.getLog2N(N),
        VK_IMAGE_LAYOUT_UNDEFINED, 0, VK_PIPELINE_STAGE_COMPUTE_SHADER_BIT,
        VK_IMAGE_LAYOUT_GENERAL, VK_ACCESS_SHADER_READ_BIT, VK_PIPELINE_STAGE_FRAGMENT_SHADER_BIT);

    mipmapSubmitInfo = new SubmitInfo();
    mipmapSubmitInfo.setCommandBuffers(mipmapCmdBuffer.getHandlePointer());
  }

  public void setWaitSemaphores(LongBuffer waitSemaphore) {

    IntBuffer pWaitDstStageMask = memAllocInt(1);
    pWaitDstStageMask.put(0, VK_PIPELINE_STAGE_COMPUTE_SHADER_BIT);
    submitInfo.setWaitDstStageMask(pWaitDstStageMask);
    submitInfo.setWaitSemaphores(waitSemaphore);
  }

  public void render(int dstQueueFamilyIndex) {

    submitInfo.submit(computeQueue);
    fence.waitForFence();
    mipmapSubmitInfo.submit(transferQueue);
  }

  public void destroy() {

    commandBuffer.destroy();
    pipeline.destroy();
    descriptorSet.destroy();
    descriptorSetLayout.destroy();
    fence.destroy();
    mipmapCmdBuffer.destroy();
    normalImageView.destroy();
    normalImage.destroy();
  }
}
