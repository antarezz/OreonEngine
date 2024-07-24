package org.oreon.vk.engine;

import static org.lwjgl.system.MemoryUtil.memAlloc;
import static org.lwjgl.system.MemoryUtil.memAllocInt;
import static org.lwjgl.vulkan.VK10.VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER;
import static org.lwjgl.vulkan.VK10.VK_DESCRIPTOR_TYPE_STORAGE_IMAGE;
import static org.lwjgl.vulkan.VK10.VK_FILTER_LINEAR;
import static org.lwjgl.vulkan.VK10.VK_FORMAT_R16G16B16A16_SFLOAT;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_ASPECT_COLOR_BIT;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_LAYOUT_GENERAL;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_USAGE_SAMPLED_BIT;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_USAGE_STORAGE_BIT;
import static org.lwjgl.vulkan.VK10.VK_PIPELINE_STAGE_COMPUTE_SHADER_BIT;
import static org.lwjgl.vulkan.VK10.VK_SAMPLER_ADDRESS_MODE_MIRRORED_REPEAT;
import static org.lwjgl.vulkan.VK10.VK_SAMPLER_MIPMAP_MODE_LINEAR;
import static org.lwjgl.vulkan.VK10.VK_SHADER_STAGE_COMPUTE_BIT;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.util.ArrayList;
import java.util.List;

import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkPhysicalDeviceMemoryProperties;
import org.lwjgl.vulkan.VkQueue;
import org.oreon.core.vk.command.CommandBuffer;
import org.oreon.core.vk.command.SubmitInfo;
import org.oreon.core.vk.descriptor.DescriptorPool;
import org.oreon.core.vk.descriptor.DescriptorSet;
import org.oreon.core.vk.descriptor.DescriptorSetLayout;
import org.oreon.core.vk.device.VkDeviceBundle;
import org.oreon.core.vk.image.VkImage;
import org.oreon.core.vk.image.VkImageView;
import org.oreon.core.vk.image.VkSampler;
import org.oreon.core.vk.pipeline.ShaderModule;
import org.oreon.core.vk.pipeline.VkPipeline;
import org.oreon.core.vk.synchronization.VkSemaphore;
import org.oreon.core.vk.util.VkUtil;
import org.oreon.core.vk.wrapper.command.ComputeCmdBuffer;
import org.oreon.core.vk.wrapper.image.Image2DDeviceLocal;
import org.oreon.core.vk.wrapper.shader.ComputeShader;

import lombok.Getter;

public class OpaqueTransparencyBlending {

  private VkQueue queue;

  private VkImage blendedSceneImage;
  @Getter
  private VkImageView blendedSceneImageView;
  private VkImage blendedLightScatteringImage;
  @Getter
  private VkImageView blendedLightScatteringImageView;

  private VkPipeline computePipeline;
  private DescriptorSet descriptorSet;
  private DescriptorSetLayout descriptorSetLayout;
  private CommandBuffer cmdBuffer;
  private SubmitInfo submitInfo;

  // sampler
  private VkSampler opaqueSceneSampler;
  private VkSampler opaqueSceneDepthSampler;
  private VkSampler opaqueSceneLightScatteringSampler;
  private VkSampler transparencySceneSampler;
  private VkSampler transparencySceneDepthSampler;
  private VkSampler transparencyAlphaSampler;
  private VkSampler transparencyLightScatteringSampler;

  @Getter
  private VkSemaphore signalSemaphore;

  public OpaqueTransparencyBlending(VkDeviceBundle deviceBundle,
      int width, int height, VkImageView opaqueSceneImageView,
      VkImageView opaqueSceneDepthMap, VkImageView opaqueSceneLightScatteringImageView,
      VkImageView transparencySceneImageView, VkImageView transparencySceneDepthMap,
      VkImageView transparencyAlphaMap, VkImageView transparencyLightScatteringImageView,
      LongBuffer waitSemaphores) {

    VkDevice device = deviceBundle.getLogicalDevice().getHandle();
    VkPhysicalDeviceMemoryProperties memoryProperties = deviceBundle.getPhysicalDevice().getMemoryProperties();
    DescriptorPool descriptorPool = deviceBundle.getLogicalDevice().getDescriptorPool(Thread.currentThread().getId());
    queue = deviceBundle.getLogicalDevice().getComputeQueue();

    blendedSceneImage = new Image2DDeviceLocal(device, memoryProperties, width, height,
        VK_FORMAT_R16G16B16A16_SFLOAT, VK_IMAGE_USAGE_STORAGE_BIT | VK_IMAGE_USAGE_SAMPLED_BIT);
    blendedSceneImageView = new VkImageView(device, blendedSceneImage.getFormat(),
        blendedSceneImage.getHandle(), VK_IMAGE_ASPECT_COLOR_BIT);

    blendedLightScatteringImage = new Image2DDeviceLocal(device, memoryProperties, width, height,
        VK_FORMAT_R16G16B16A16_SFLOAT, VK_IMAGE_USAGE_STORAGE_BIT | VK_IMAGE_USAGE_SAMPLED_BIT);
    blendedLightScatteringImageView = new VkImageView(device, blendedLightScatteringImage.getFormat(),
        blendedLightScatteringImage.getHandle(), VK_IMAGE_ASPECT_COLOR_BIT);

    opaqueSceneSampler = new VkSampler(device, VK_FILTER_LINEAR,
        false, 0, VK_SAMPLER_MIPMAP_MODE_LINEAR, 0, VK_SAMPLER_ADDRESS_MODE_MIRRORED_REPEAT);
    opaqueSceneDepthSampler = new VkSampler(device, VK_FILTER_LINEAR,
        false, 0, VK_SAMPLER_MIPMAP_MODE_LINEAR, 0, VK_SAMPLER_ADDRESS_MODE_MIRRORED_REPEAT);
    opaqueSceneLightScatteringSampler = new VkSampler(device, VK_FILTER_LINEAR,
        false, 0, VK_SAMPLER_MIPMAP_MODE_LINEAR, 0, VK_SAMPLER_ADDRESS_MODE_MIRRORED_REPEAT);
    transparencySceneSampler = new VkSampler(device, VK_FILTER_LINEAR,
        false, 0, VK_SAMPLER_MIPMAP_MODE_LINEAR, 0, VK_SAMPLER_ADDRESS_MODE_MIRRORED_REPEAT);
    transparencySceneDepthSampler = new VkSampler(device, VK_FILTER_LINEAR,
        false, 0, VK_SAMPLER_MIPMAP_MODE_LINEAR, 0, VK_SAMPLER_ADDRESS_MODE_MIRRORED_REPEAT);
    transparencyAlphaSampler = new VkSampler(device, VK_FILTER_LINEAR,
        false, 0, VK_SAMPLER_MIPMAP_MODE_LINEAR, 0, VK_SAMPLER_ADDRESS_MODE_MIRRORED_REPEAT);
    transparencyLightScatteringSampler = new VkSampler(device, VK_FILTER_LINEAR,
        false, 0, VK_SAMPLER_MIPMAP_MODE_LINEAR, 0, VK_SAMPLER_ADDRESS_MODE_MIRRORED_REPEAT);

    descriptorSetLayout = new DescriptorSetLayout(device, 9);
    descriptorSetLayout.addLayoutBinding(0, VK_DESCRIPTOR_TYPE_STORAGE_IMAGE,
        VK_SHADER_STAGE_COMPUTE_BIT);
    descriptorSetLayout.addLayoutBinding(1, VK_DESCRIPTOR_TYPE_STORAGE_IMAGE,
        VK_SHADER_STAGE_COMPUTE_BIT);
    descriptorSetLayout.addLayoutBinding(2, VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER,
        VK_SHADER_STAGE_COMPUTE_BIT);
    descriptorSetLayout.addLayoutBinding(3, VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER,
        VK_SHADER_STAGE_COMPUTE_BIT);
    descriptorSetLayout.addLayoutBinding(4, VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER,
        VK_SHADER_STAGE_COMPUTE_BIT);
    descriptorSetLayout.addLayoutBinding(5, VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER,
        VK_SHADER_STAGE_COMPUTE_BIT);
    descriptorSetLayout.addLayoutBinding(6, VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER,
        VK_SHADER_STAGE_COMPUTE_BIT);
    descriptorSetLayout.addLayoutBinding(7, VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER,
        VK_SHADER_STAGE_COMPUTE_BIT);
    descriptorSetLayout.addLayoutBinding(8, VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER,
        VK_SHADER_STAGE_COMPUTE_BIT);
    descriptorSetLayout.create();

    descriptorSet = new DescriptorSet(device, descriptorPool.getHandle(),
        descriptorSetLayout.getHandlePointer());
    descriptorSet.updateDescriptorImageBuffer(blendedSceneImageView.getHandle(),
        VK_IMAGE_LAYOUT_GENERAL, -1, 0,
        VK_DESCRIPTOR_TYPE_STORAGE_IMAGE);
    descriptorSet.updateDescriptorImageBuffer(blendedLightScatteringImageView.getHandle(),
        VK_IMAGE_LAYOUT_GENERAL, -1, 1,
        VK_DESCRIPTOR_TYPE_STORAGE_IMAGE);
    descriptorSet.updateDescriptorImageBuffer(
        opaqueSceneImageView.getHandle(),
        VK_IMAGE_LAYOUT_GENERAL, opaqueSceneSampler.getHandle(),
        2, VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER);
    descriptorSet.updateDescriptorImageBuffer(
        opaqueSceneLightScatteringImageView.getHandle(),
        VK_IMAGE_LAYOUT_GENERAL, opaqueSceneLightScatteringSampler.getHandle(),
        3, VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER);
    descriptorSet.updateDescriptorImageBuffer(
        opaqueSceneDepthMap.getHandle(),
        VK_IMAGE_LAYOUT_GENERAL, opaqueSceneDepthSampler.getHandle(),
        4, VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER);
    descriptorSet.updateDescriptorImageBuffer(
        transparencySceneImageView.getHandle(),
        VK_IMAGE_LAYOUT_GENERAL, transparencySceneSampler.getHandle(),
        5, VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER);
    descriptorSet.updateDescriptorImageBuffer(
        transparencyLightScatteringImageView.getHandle(),
        VK_IMAGE_LAYOUT_GENERAL, transparencyLightScatteringSampler.getHandle(),
        6, VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER);
    descriptorSet.updateDescriptorImageBuffer(
        transparencySceneDepthMap.getHandle(),
        VK_IMAGE_LAYOUT_GENERAL, transparencySceneDepthSampler.getHandle(),
        7, VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER);
    descriptorSet.updateDescriptorImageBuffer(
        transparencyAlphaMap.getHandle(),
        VK_IMAGE_LAYOUT_GENERAL, transparencyAlphaSampler.getHandle(),
        8, VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER);

    List<DescriptorSet> descriptorSets = new ArrayList<DescriptorSet>();
    List<DescriptorSetLayout> descriptorSetLayouts = new ArrayList<DescriptorSetLayout>();

    descriptorSets.add(descriptorSet);
    descriptorSetLayouts.add(descriptorSetLayout);

    int pushConstantRange = Float.BYTES * 2;
    ByteBuffer pushConstants = memAlloc(pushConstantRange);
    pushConstants.putFloat(width);
    pushConstants.putFloat(height);
    pushConstants.flip();

    ShaderModule shader = new ComputeShader(device, "shaders/opaqueTransparencyBlend.comp.spv");

    computePipeline = new VkPipeline(device);
    computePipeline.setPushConstantsRange(VK_SHADER_STAGE_COMPUTE_BIT, pushConstantRange);
    computePipeline.setLayout(VkUtil.createLongBuffer(descriptorSetLayouts));
    computePipeline.createComputePipeline(shader);

    cmdBuffer = new ComputeCmdBuffer(device,
        deviceBundle.getLogicalDevice().getComputeCommandPool(Thread.currentThread().getId()).getHandle(),
        computePipeline.getHandle(), computePipeline.getLayoutHandle(),
        VkUtil.createLongArray(descriptorSets), width / 16, height / 16, 1,
        pushConstants, VK_SHADER_STAGE_COMPUTE_BIT);

    signalSemaphore = new VkSemaphore(device);

    IntBuffer pWaitDstStageMask = memAllocInt(2);
    pWaitDstStageMask.put(0, VK_PIPELINE_STAGE_COMPUTE_SHADER_BIT);
    pWaitDstStageMask.put(1, VK_PIPELINE_STAGE_COMPUTE_SHADER_BIT);
    submitInfo = new SubmitInfo();
    submitInfo.setCommandBuffers(cmdBuffer.getHandlePointer());
    submitInfo.setWaitSemaphores(waitSemaphores);
    submitInfo.setWaitDstStageMask(pWaitDstStageMask);
    submitInfo.setSignalSemaphores(signalSemaphore.getHandlePointer());

    shader.destroy();
  }

  public void render() {

    submitInfo.submit(queue);
  }

  public void shutdown() {
    computePipeline.destroy();
    descriptorSet.destroy();
    descriptorSetLayout.destroy();
    cmdBuffer.destroy();
    opaqueSceneSampler.destroy();
    opaqueSceneDepthSampler.destroy();
    opaqueSceneLightScatteringSampler.destroy();
    transparencySceneSampler.destroy();
    transparencySceneDepthSampler.destroy();
    transparencyAlphaSampler.destroy();
    transparencyLightScatteringSampler.destroy();
    signalSemaphore.destroy();
  }

}
