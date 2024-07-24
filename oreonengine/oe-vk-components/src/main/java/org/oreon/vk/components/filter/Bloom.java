package org.oreon.vk.components.filter;

import static org.lwjgl.system.MemoryUtil.memAlloc;
import static org.lwjgl.vulkan.VK10.VK_ACCESS_SHADER_READ_BIT;
import static org.lwjgl.vulkan.VK10.VK_ACCESS_SHADER_WRITE_BIT;
import static org.lwjgl.vulkan.VK10.VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER;
import static org.lwjgl.vulkan.VK10.VK_DESCRIPTOR_TYPE_STORAGE_IMAGE;
import static org.lwjgl.vulkan.VK10.VK_FILTER_LINEAR;
import static org.lwjgl.vulkan.VK10.VK_FORMAT_B8G8R8A8_SRGB;
import static org.lwjgl.vulkan.VK10.VK_FORMAT_R16G16B16A16_SFLOAT;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_ASPECT_COLOR_BIT;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_LAYOUT_GENERAL;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_USAGE_SAMPLED_BIT;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_USAGE_STORAGE_BIT;
import static org.lwjgl.vulkan.VK10.VK_PIPELINE_STAGE_COMPUTE_SHADER_BIT;
import static org.lwjgl.vulkan.VK10.VK_SAMPLER_ADDRESS_MODE_CLAMP_TO_EDGE;
import static org.lwjgl.vulkan.VK10.VK_SAMPLER_MIPMAP_MODE_LINEAR;
import static org.lwjgl.vulkan.VK10.VK_SHADER_STAGE_COMPUTE_BIT;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkPhysicalDeviceMemoryProperties;
import org.lwjgl.vulkan.VkQueue;
import org.oreon.core.vk.command.CommandBuffer;
import org.oreon.core.vk.descriptor.DescriptorPool;
import org.oreon.core.vk.descriptor.DescriptorSet;
import org.oreon.core.vk.descriptor.DescriptorSetLayout;
import org.oreon.core.vk.device.VkDeviceBundle;
import org.oreon.core.vk.image.VkImage;
import org.oreon.core.vk.image.VkImageView;
import org.oreon.core.vk.image.VkSampler;
import org.oreon.core.vk.pipeline.ShaderModule;
import org.oreon.core.vk.pipeline.VkPipeline;
import org.oreon.core.vk.util.VkUtil;
import org.oreon.core.vk.wrapper.image.Image2DDeviceLocal;
import org.oreon.core.vk.wrapper.image.VkImageBundle;
import org.oreon.core.vk.wrapper.shader.ComputeShader;

import lombok.Getter;

@Getter
public class Bloom {

  private VkQueue queue;

  private VkImageBundle bloomSceneImageBundle;
  private VkImageBundle additiveBlendImageBundle;
  private VkImageBundle sceneBrightnessImageBundle;
  private VkImageBundle sceneBrightnessImageBundle_div4;
  private VkImageBundle sceneBrightnessImageBundle_div8;
  private VkImageBundle sceneBrightnessImageBundle_div16;
  private VkImageBundle horizontalBloomBlurImageBundle_div2;
  private VkImageBundle horizontalBloomBlurImageBundle_div4;
  private VkImageBundle horizontalBloomBlurImageBundle_div8;
  private VkImageBundle horizontalBloomBlurImageBundle_div16;
  private VkImageBundle verticalBloomBlurImageBundle_div2;
  private VkImageBundle verticalBloomBlurImageBundle_div4;
  private VkImageBundle verticalBloomBlurImageBundle_div8;
  private VkImageBundle verticalBloomBlurImageBundle_div16;

  // scene brightness resources
  private VkPipeline sceneBrightnessPipeline;
  private DescriptorSet sceneBrightnessDescriptorSet;
  private DescriptorSetLayout sceneBrightnessDescriptorSetLayout;
  private List<DescriptorSet> sceneBrightnessDescriptorSets;

  // horizontal blur resources
  private DescriptorSetLayout horizontalBlurDescriptorSetLayout;
  private List<DescriptorSet> horizontalBlurDescriptorSets;
  private VkPipeline horizontalBlurPipeline;
  private DescriptorSet horizontalBlurDescriptorSet;

  // vertical blur resources
  private VkPipeline verticalBlurPipeline;
  private DescriptorSet verticalBlurDescriptorSet;
  private DescriptorSetLayout verticalBlurDescriptorSetLayout;
  private List<DescriptorSet> verticalBlurDescriptorSets;

  // blend resources
  private VkPipeline blendPipeline;
  private DescriptorSet blendDescriptorSet;
  private DescriptorSetLayout blendDescriptorSetLayout;
  private VkSampler bloomBlurSampler_div2;
  private VkSampler bloomBlurSampler_div4;
  private VkSampler bloomBlurSampler_div8;
  private VkSampler bloomBlurSampler_div16;
  private List<DescriptorSet> blendDescriptorSets;

  // final bloom Scene resources
  private VkPipeline bloomScenePipeline;
  private DescriptorSet bloomSceneDescriptorSet;
  private DescriptorSetLayout bloomSceneDescriptorSetLayout;
  private List<DescriptorSet> bloomSceneDescriptorSets;

  private ByteBuffer pushConstants;
  private ByteBuffer pushConstants_blend;

  private int width;
  private int height;

  public Bloom(VkDeviceBundle deviceBundle,
      int width, int height, VkImageView sceneImageView, VkImageView specular_emission_bloom_attachment) {

    VkDevice device = deviceBundle.getLogicalDevice().getHandle();
    VkPhysicalDeviceMemoryProperties memoryProperties = deviceBundle.getPhysicalDevice().getMemoryProperties();
    DescriptorPool descriptorPool = deviceBundle.getLogicalDevice().getDescriptorPool(Thread.currentThread().getId());
    queue = deviceBundle.getLogicalDevice().getComputeQueue();
    this.width = width;
    this.height = height;

    initializeImages(device, memoryProperties, width, height);

    List<DescriptorSetLayout> descriptorSetLayouts = new ArrayList<DescriptorSetLayout>();

    ShaderModule horizontalBlurShader = new ComputeShader(device,
        "shaders/filter/bloom/horizontalGaussianBlur.comp.spv");
    ShaderModule verticalBlurShader = new ComputeShader(device,
        "shaders/filter/bloom/verticalGaussianBlur.comp.spv");
    ShaderModule sceneBrightnessShader = new ComputeShader(device,
        "shaders/filter/bloom/sceneBrightness.comp.spv");
    ShaderModule additiveBlendShader = new ComputeShader(device,
        "shaders/filter/bloom/additiveBlend.comp.spv");
    ShaderModule bloomSceneShader = new ComputeShader(device,
        "shaders/filter/bloom/bloomScene.comp.spv");

    pushConstants = memAlloc(Float.BYTES * 12);
    pushConstants.putFloat(width / 2.0f);
    pushConstants.putFloat(height / 2.0f);
    pushConstants.putFloat(width / 4.0f);
    pushConstants.putFloat(height / 4.0f);
    pushConstants.putFloat(width / 8.0f);
    pushConstants.putFloat(height / 8.0f);
    pushConstants.putFloat(width / 12.0f);
    pushConstants.putFloat(height / 12.0f);
    pushConstants.putFloat(2);
    pushConstants.putFloat(4);
    pushConstants.putFloat(8);
    pushConstants.putFloat(12);
    pushConstants.flip();

    int pushConstantRange = Float.BYTES * 2;
    pushConstants_blend = memAlloc(pushConstantRange);
    pushConstants_blend.putFloat(width);
    pushConstants_blend.putFloat(height);
    pushConstants_blend.flip();

    // scene brightness

    sceneBrightnessDescriptorSetLayout = new DescriptorSetLayout(device, 5);
    sceneBrightnessDescriptorSetLayout.addLayoutBinding(0, VK_DESCRIPTOR_TYPE_STORAGE_IMAGE,
        VK_SHADER_STAGE_COMPUTE_BIT);
    sceneBrightnessDescriptorSetLayout.addLayoutBinding(1, VK_DESCRIPTOR_TYPE_STORAGE_IMAGE,
        VK_SHADER_STAGE_COMPUTE_BIT);
    sceneBrightnessDescriptorSetLayout.addLayoutBinding(2, VK_DESCRIPTOR_TYPE_STORAGE_IMAGE,
        VK_SHADER_STAGE_COMPUTE_BIT);
    sceneBrightnessDescriptorSetLayout.addLayoutBinding(3, VK_DESCRIPTOR_TYPE_STORAGE_IMAGE,
        VK_SHADER_STAGE_COMPUTE_BIT);
    sceneBrightnessDescriptorSetLayout.addLayoutBinding(4, VK_DESCRIPTOR_TYPE_STORAGE_IMAGE,
        VK_SHADER_STAGE_COMPUTE_BIT);
    sceneBrightnessDescriptorSetLayout.create();

    sceneBrightnessDescriptorSet = new DescriptorSet(device, descriptorPool.getHandle(),
        sceneBrightnessDescriptorSetLayout.getHandlePointer());
    sceneBrightnessDescriptorSet.updateDescriptorImageBuffer(
        sceneImageView.getHandle(),
        VK_IMAGE_LAYOUT_GENERAL, -1, 0,
        VK_DESCRIPTOR_TYPE_STORAGE_IMAGE);
    sceneBrightnessDescriptorSet.updateDescriptorImageBuffer(
        sceneBrightnessImageBundle.getImageView().getHandle(),
        VK_IMAGE_LAYOUT_GENERAL, -1, 1,
        VK_DESCRIPTOR_TYPE_STORAGE_IMAGE);
    sceneBrightnessDescriptorSet.updateDescriptorImageBuffer(
        sceneBrightnessImageBundle_div4.getImageView().getHandle(),
        VK_IMAGE_LAYOUT_GENERAL, -1, 2,
        VK_DESCRIPTOR_TYPE_STORAGE_IMAGE);
    sceneBrightnessDescriptorSet.updateDescriptorImageBuffer(
        sceneBrightnessImageBundle_div8.getImageView().getHandle(),
        VK_IMAGE_LAYOUT_GENERAL, -1, 3,
        VK_DESCRIPTOR_TYPE_STORAGE_IMAGE);
    sceneBrightnessDescriptorSet.updateDescriptorImageBuffer(
        sceneBrightnessImageBundle_div16.getImageView().getHandle(),
        VK_IMAGE_LAYOUT_GENERAL, -1, 4,
        VK_DESCRIPTOR_TYPE_STORAGE_IMAGE);

    sceneBrightnessDescriptorSets = new ArrayList<DescriptorSet>();
    sceneBrightnessDescriptorSets.add(sceneBrightnessDescriptorSet);
    descriptorSetLayouts.add(sceneBrightnessDescriptorSetLayout);

    sceneBrightnessPipeline = new VkPipeline(device);
    sceneBrightnessPipeline.setPushConstantsRange(VK_SHADER_STAGE_COMPUTE_BIT, Float.BYTES * 12);
    sceneBrightnessPipeline.setLayout(VkUtil.createLongBuffer(descriptorSetLayouts));
    sceneBrightnessPipeline.createComputePipeline(sceneBrightnessShader);

    // horizontal blur

    horizontalBlurDescriptorSetLayout = new DescriptorSetLayout(device, 8);
    horizontalBlurDescriptorSetLayout.addLayoutBinding(0, VK_DESCRIPTOR_TYPE_STORAGE_IMAGE,
        VK_SHADER_STAGE_COMPUTE_BIT);
    horizontalBlurDescriptorSetLayout.addLayoutBinding(1, VK_DESCRIPTOR_TYPE_STORAGE_IMAGE,
        VK_SHADER_STAGE_COMPUTE_BIT);
    horizontalBlurDescriptorSetLayout.addLayoutBinding(2, VK_DESCRIPTOR_TYPE_STORAGE_IMAGE,
        VK_SHADER_STAGE_COMPUTE_BIT);
    horizontalBlurDescriptorSetLayout.addLayoutBinding(3, VK_DESCRIPTOR_TYPE_STORAGE_IMAGE,
        VK_SHADER_STAGE_COMPUTE_BIT);
    horizontalBlurDescriptorSetLayout.addLayoutBinding(4, VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER,
        VK_SHADER_STAGE_COMPUTE_BIT);
    horizontalBlurDescriptorSetLayout.addLayoutBinding(5, VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER,
        VK_SHADER_STAGE_COMPUTE_BIT);
    horizontalBlurDescriptorSetLayout.addLayoutBinding(6, VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER,
        VK_SHADER_STAGE_COMPUTE_BIT);
    horizontalBlurDescriptorSetLayout.addLayoutBinding(7, VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER,
        VK_SHADER_STAGE_COMPUTE_BIT);
    horizontalBlurDescriptorSetLayout.create();

    horizontalBlurDescriptorSet = new DescriptorSet(device, descriptorPool.getHandle(),
        horizontalBlurDescriptorSetLayout.getHandlePointer());
    horizontalBlurDescriptorSet.updateDescriptorImageBuffer(
        horizontalBloomBlurImageBundle_div2.getImageView().getHandle(),
        VK_IMAGE_LAYOUT_GENERAL, -1, 0,
        VK_DESCRIPTOR_TYPE_STORAGE_IMAGE);
    horizontalBlurDescriptorSet.updateDescriptorImageBuffer(
        horizontalBloomBlurImageBundle_div4.getImageView().getHandle(),
        VK_IMAGE_LAYOUT_GENERAL, -1, 1,
        VK_DESCRIPTOR_TYPE_STORAGE_IMAGE);
    horizontalBlurDescriptorSet.updateDescriptorImageBuffer(
        horizontalBloomBlurImageBundle_div8.getImageView().getHandle(),
        VK_IMAGE_LAYOUT_GENERAL, -1, 2,
        VK_DESCRIPTOR_TYPE_STORAGE_IMAGE);
    horizontalBlurDescriptorSet.updateDescriptorImageBuffer(
        horizontalBloomBlurImageBundle_div16.getImageView().getHandle(),
        VK_IMAGE_LAYOUT_GENERAL, -1, 3,
        VK_DESCRIPTOR_TYPE_STORAGE_IMAGE);
    horizontalBlurDescriptorSet.updateDescriptorImageBuffer(
        sceneBrightnessImageBundle.getImageView().getHandle(),
        VK_IMAGE_LAYOUT_GENERAL, sceneBrightnessImageBundle.getSampler().getHandle(), 4,
        VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER);
    horizontalBlurDescriptorSet.updateDescriptorImageBuffer(
        sceneBrightnessImageBundle_div4.getImageView().getHandle(),
        VK_IMAGE_LAYOUT_GENERAL, sceneBrightnessImageBundle_div4.getSampler().getHandle(), 5,
        VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER);
    horizontalBlurDescriptorSet.updateDescriptorImageBuffer(
        sceneBrightnessImageBundle_div8.getImageView().getHandle(),
        VK_IMAGE_LAYOUT_GENERAL, sceneBrightnessImageBundle_div8.getSampler().getHandle(), 6,
        VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER);
    horizontalBlurDescriptorSet.updateDescriptorImageBuffer(
        sceneBrightnessImageBundle_div16.getImageView().getHandle(),
        VK_IMAGE_LAYOUT_GENERAL, sceneBrightnessImageBundle_div16.getSampler().getHandle(), 7,
        VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER);

    descriptorSetLayouts = new ArrayList<DescriptorSetLayout>();
    horizontalBlurDescriptorSets = new ArrayList<DescriptorSet>();
    horizontalBlurDescriptorSets.add(horizontalBlurDescriptorSet);
    descriptorSetLayouts.add(horizontalBlurDescriptorSetLayout);

    horizontalBlurPipeline = new VkPipeline(device);
    horizontalBlurPipeline.setPushConstantsRange(VK_SHADER_STAGE_COMPUTE_BIT, Float.BYTES * 12);
    horizontalBlurPipeline.setLayout(VkUtil.createLongBuffer(descriptorSetLayouts));
    horizontalBlurPipeline.createComputePipeline(horizontalBlurShader);

    // vertical blur

    verticalBlurDescriptorSetLayout = new DescriptorSetLayout(device, 8);
    verticalBlurDescriptorSetLayout.addLayoutBinding(0, VK_DESCRIPTOR_TYPE_STORAGE_IMAGE,
        VK_SHADER_STAGE_COMPUTE_BIT);
    verticalBlurDescriptorSetLayout.addLayoutBinding(1, VK_DESCRIPTOR_TYPE_STORAGE_IMAGE,
        VK_SHADER_STAGE_COMPUTE_BIT);
    verticalBlurDescriptorSetLayout.addLayoutBinding(2, VK_DESCRIPTOR_TYPE_STORAGE_IMAGE,
        VK_SHADER_STAGE_COMPUTE_BIT);
    verticalBlurDescriptorSetLayout.addLayoutBinding(3, VK_DESCRIPTOR_TYPE_STORAGE_IMAGE,
        VK_SHADER_STAGE_COMPUTE_BIT);
    verticalBlurDescriptorSetLayout.addLayoutBinding(4, VK_DESCRIPTOR_TYPE_STORAGE_IMAGE,
        VK_SHADER_STAGE_COMPUTE_BIT);
    verticalBlurDescriptorSetLayout.addLayoutBinding(5, VK_DESCRIPTOR_TYPE_STORAGE_IMAGE,
        VK_SHADER_STAGE_COMPUTE_BIT);
    verticalBlurDescriptorSetLayout.addLayoutBinding(6, VK_DESCRIPTOR_TYPE_STORAGE_IMAGE,
        VK_SHADER_STAGE_COMPUTE_BIT);
    verticalBlurDescriptorSetLayout.addLayoutBinding(7, VK_DESCRIPTOR_TYPE_STORAGE_IMAGE,
        VK_SHADER_STAGE_COMPUTE_BIT);
    verticalBlurDescriptorSetLayout.create();

    verticalBlurDescriptorSet = new DescriptorSet(device, descriptorPool.getHandle(),
        verticalBlurDescriptorSetLayout.getHandlePointer());
    verticalBlurDescriptorSet.updateDescriptorImageBuffer(
        verticalBloomBlurImageBundle_div2.getImageView().getHandle(),
        VK_IMAGE_LAYOUT_GENERAL, -1, 0,
        VK_DESCRIPTOR_TYPE_STORAGE_IMAGE);
    verticalBlurDescriptorSet.updateDescriptorImageBuffer(
        verticalBloomBlurImageBundle_div4.getImageView().getHandle(),
        VK_IMAGE_LAYOUT_GENERAL, -1, 1,
        VK_DESCRIPTOR_TYPE_STORAGE_IMAGE);
    verticalBlurDescriptorSet.updateDescriptorImageBuffer(
        verticalBloomBlurImageBundle_div8.getImageView().getHandle(),
        VK_IMAGE_LAYOUT_GENERAL, -1, 2,
        VK_DESCRIPTOR_TYPE_STORAGE_IMAGE);
    verticalBlurDescriptorSet.updateDescriptorImageBuffer(
        verticalBloomBlurImageBundle_div16.getImageView().getHandle(),
        VK_IMAGE_LAYOUT_GENERAL, -1, 3,
        VK_DESCRIPTOR_TYPE_STORAGE_IMAGE);
    verticalBlurDescriptorSet.updateDescriptorImageBuffer(
        horizontalBloomBlurImageBundle_div2.getImageView().getHandle(),
        VK_IMAGE_LAYOUT_GENERAL, -1, 4,
        VK_DESCRIPTOR_TYPE_STORAGE_IMAGE);
    verticalBlurDescriptorSet.updateDescriptorImageBuffer(
        horizontalBloomBlurImageBundle_div4.getImageView().getHandle(),
        VK_IMAGE_LAYOUT_GENERAL, -1, 5,
        VK_DESCRIPTOR_TYPE_STORAGE_IMAGE);
    verticalBlurDescriptorSet.updateDescriptorImageBuffer(
        horizontalBloomBlurImageBundle_div8.getImageView().getHandle(),
        VK_IMAGE_LAYOUT_GENERAL, -1, 6,
        VK_DESCRIPTOR_TYPE_STORAGE_IMAGE);
    verticalBlurDescriptorSet.updateDescriptorImageBuffer(
        horizontalBloomBlurImageBundle_div16.getImageView().getHandle(),
        VK_IMAGE_LAYOUT_GENERAL, -1, 7,
        VK_DESCRIPTOR_TYPE_STORAGE_IMAGE);

    descriptorSetLayouts = new ArrayList<DescriptorSetLayout>();
    verticalBlurDescriptorSets = new ArrayList<DescriptorSet>();
    verticalBlurDescriptorSets.add(verticalBlurDescriptorSet);
    descriptorSetLayouts.add(verticalBlurDescriptorSetLayout);

    verticalBlurPipeline = new VkPipeline(device);
    verticalBlurPipeline.setPushConstantsRange(VK_SHADER_STAGE_COMPUTE_BIT, Float.BYTES * 12);
    verticalBlurPipeline.setLayout(VkUtil.createLongBuffer(descriptorSetLayouts));
    verticalBlurPipeline.createComputePipeline(verticalBlurShader);

    // aditive Blend

    bloomBlurSampler_div2 = new VkSampler(device, VK_FILTER_LINEAR,
        false, 0, VK_SAMPLER_MIPMAP_MODE_LINEAR, 0, VK_SAMPLER_ADDRESS_MODE_CLAMP_TO_EDGE);
    bloomBlurSampler_div4 = new VkSampler(device, VK_FILTER_LINEAR,
        false, 0, VK_SAMPLER_MIPMAP_MODE_LINEAR, 0, VK_SAMPLER_ADDRESS_MODE_CLAMP_TO_EDGE);
    bloomBlurSampler_div8 = new VkSampler(device, VK_FILTER_LINEAR,
        false, 0, VK_SAMPLER_MIPMAP_MODE_LINEAR, 0, VK_SAMPLER_ADDRESS_MODE_CLAMP_TO_EDGE);
    bloomBlurSampler_div16 = new VkSampler(device, VK_FILTER_LINEAR,
        false, 0, VK_SAMPLER_MIPMAP_MODE_LINEAR, 0, VK_SAMPLER_ADDRESS_MODE_CLAMP_TO_EDGE);

    blendDescriptorSetLayout = new DescriptorSetLayout(device, 5);
    blendDescriptorSetLayout.addLayoutBinding(0, VK_DESCRIPTOR_TYPE_STORAGE_IMAGE,
        VK_SHADER_STAGE_COMPUTE_BIT);
    blendDescriptorSetLayout.addLayoutBinding(1, VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER,
        VK_SHADER_STAGE_COMPUTE_BIT);
    blendDescriptorSetLayout.addLayoutBinding(2, VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER,
        VK_SHADER_STAGE_COMPUTE_BIT);
    blendDescriptorSetLayout.addLayoutBinding(3, VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER,
        VK_SHADER_STAGE_COMPUTE_BIT);
    blendDescriptorSetLayout.addLayoutBinding(4, VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER,
        VK_SHADER_STAGE_COMPUTE_BIT);
    blendDescriptorSetLayout.create();

    blendDescriptorSet = new DescriptorSet(device, descriptorPool.getHandle(),
        blendDescriptorSetLayout.getHandlePointer());
    blendDescriptorSet.updateDescriptorImageBuffer(
        additiveBlendImageBundle.getImageView().getHandle(),
        VK_IMAGE_LAYOUT_GENERAL, -1, 0,
        VK_DESCRIPTOR_TYPE_STORAGE_IMAGE);
    blendDescriptorSet.updateDescriptorImageBuffer(
        verticalBloomBlurImageBundle_div2.getImageView().getHandle(),
        VK_IMAGE_LAYOUT_GENERAL, bloomBlurSampler_div2.getHandle(), 1,
        VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER);
    blendDescriptorSet.updateDescriptorImageBuffer(
        verticalBloomBlurImageBundle_div4.getImageView().getHandle(),
        VK_IMAGE_LAYOUT_GENERAL, bloomBlurSampler_div4.getHandle(), 2,
        VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER);
    blendDescriptorSet.updateDescriptorImageBuffer(
        verticalBloomBlurImageBundle_div8.getImageView().getHandle(),
        VK_IMAGE_LAYOUT_GENERAL, bloomBlurSampler_div8.getHandle(), 3,
        VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER);
    blendDescriptorSet.updateDescriptorImageBuffer(
        verticalBloomBlurImageBundle_div16.getImageView().getHandle(),
        VK_IMAGE_LAYOUT_GENERAL, bloomBlurSampler_div16.getHandle(), 4,
        VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER);

    descriptorSetLayouts = new ArrayList<DescriptorSetLayout>();
    blendDescriptorSets = new ArrayList<DescriptorSet>();
    blendDescriptorSets.add(blendDescriptorSet);
    descriptorSetLayouts.add(blendDescriptorSetLayout);

    blendPipeline = new VkPipeline(device);
    blendPipeline.setPushConstantsRange(VK_SHADER_STAGE_COMPUTE_BIT, pushConstantRange);
    blendPipeline.setLayout(VkUtil.createLongBuffer(descriptorSetLayouts));
    blendPipeline.createComputePipeline(additiveBlendShader);

    // final bloom scene

    bloomSceneDescriptorSetLayout = new DescriptorSetLayout(device, 4);
    bloomSceneDescriptorSetLayout.addLayoutBinding(0, VK_DESCRIPTOR_TYPE_STORAGE_IMAGE,
        VK_SHADER_STAGE_COMPUTE_BIT);
    bloomSceneDescriptorSetLayout.addLayoutBinding(1, VK_DESCRIPTOR_TYPE_STORAGE_IMAGE,
        VK_SHADER_STAGE_COMPUTE_BIT);
    bloomSceneDescriptorSetLayout.addLayoutBinding(2, VK_DESCRIPTOR_TYPE_STORAGE_IMAGE,
        VK_SHADER_STAGE_COMPUTE_BIT);
    bloomSceneDescriptorSetLayout.addLayoutBinding(3, VK_DESCRIPTOR_TYPE_STORAGE_IMAGE,
        VK_SHADER_STAGE_COMPUTE_BIT);
    bloomSceneDescriptorSetLayout.create();

    bloomSceneDescriptorSet = new DescriptorSet(device, descriptorPool.getHandle(),
        bloomSceneDescriptorSetLayout.getHandlePointer());
    bloomSceneDescriptorSet.updateDescriptorImageBuffer(
        sceneImageView.getHandle(),
        VK_IMAGE_LAYOUT_GENERAL, -1, 0,
        VK_DESCRIPTOR_TYPE_STORAGE_IMAGE);
    bloomSceneDescriptorSet.updateDescriptorImageBuffer(
        additiveBlendImageBundle.getImageView().getHandle(),
        VK_IMAGE_LAYOUT_GENERAL, -1, 1,
        VK_DESCRIPTOR_TYPE_STORAGE_IMAGE);
    bloomSceneDescriptorSet.updateDescriptorImageBuffer(
        specular_emission_bloom_attachment.getHandle(),
        VK_IMAGE_LAYOUT_GENERAL, -1, 2,
        VK_DESCRIPTOR_TYPE_STORAGE_IMAGE);
    bloomSceneDescriptorSet.updateDescriptorImageBuffer(
        bloomSceneImageBundle.getImageView().getHandle(),
        VK_IMAGE_LAYOUT_GENERAL, -1, 3,
        VK_DESCRIPTOR_TYPE_STORAGE_IMAGE);

    descriptorSetLayouts = new ArrayList<DescriptorSetLayout>();
    bloomSceneDescriptorSets = new ArrayList<DescriptorSet>();
    bloomSceneDescriptorSets.add(bloomSceneDescriptorSet);
    descriptorSetLayouts.add(bloomSceneDescriptorSetLayout);

    bloomScenePipeline = new VkPipeline(device);
    bloomScenePipeline.setLayout(VkUtil.createLongBuffer(descriptorSetLayouts));
    bloomScenePipeline.createComputePipeline(bloomSceneShader);

    horizontalBlurShader.destroy();
    verticalBlurShader.destroy();
    additiveBlendShader.destroy();
    sceneBrightnessShader.destroy();
    bloomSceneShader.destroy();
  }

  public void record(CommandBuffer commandBuffer) {

    // scene luminance
    commandBuffer.pushConstantsCmd(sceneBrightnessPipeline.getLayoutHandle(),
        VK_SHADER_STAGE_COMPUTE_BIT, pushConstants);
    commandBuffer.bindComputePipelineCmd(sceneBrightnessPipeline.getHandle());
    commandBuffer.bindComputeDescriptorSetsCmd(sceneBrightnessPipeline.getLayoutHandle(),
        VkUtil.createLongArray(sceneBrightnessDescriptorSets));
    commandBuffer.dispatchCmd(width / 8, height / 8, 1);

    // barrier
    commandBuffer.pipelineMemoryBarrierCmd(
        VK_ACCESS_SHADER_WRITE_BIT, VK_ACCESS_SHADER_READ_BIT,
        VK_PIPELINE_STAGE_COMPUTE_SHADER_BIT,
        VK_PIPELINE_STAGE_COMPUTE_SHADER_BIT);

    // horizontal blur
    commandBuffer.pushConstantsCmd(horizontalBlurPipeline.getLayoutHandle(),
        VK_SHADER_STAGE_COMPUTE_BIT, pushConstants);
    commandBuffer.bindComputePipelineCmd(horizontalBlurPipeline.getHandle());
    commandBuffer.bindComputeDescriptorSetsCmd(horizontalBlurPipeline.getLayoutHandle(),
        VkUtil.createLongArray(horizontalBlurDescriptorSets));
    commandBuffer.dispatchCmd(width / 16, height / 16, 1);

    // barrier
    commandBuffer.pipelineMemoryBarrierCmd(
        VK_ACCESS_SHADER_WRITE_BIT, VK_ACCESS_SHADER_READ_BIT,
        VK_PIPELINE_STAGE_COMPUTE_SHADER_BIT,
        VK_PIPELINE_STAGE_COMPUTE_SHADER_BIT);

    // vertical blur
    commandBuffer.pushConstantsCmd(verticalBlurPipeline.getLayoutHandle(),
        VK_SHADER_STAGE_COMPUTE_BIT, pushConstants);
    commandBuffer.bindComputePipelineCmd(verticalBlurPipeline.getHandle());
    commandBuffer.bindComputeDescriptorSetsCmd(verticalBlurPipeline.getLayoutHandle(),
        VkUtil.createLongArray(verticalBlurDescriptorSets));
    commandBuffer.dispatchCmd(width / 16, height / 16, 1);

    // barrier
    commandBuffer.pipelineMemoryBarrierCmd(
        VK_ACCESS_SHADER_WRITE_BIT, VK_ACCESS_SHADER_READ_BIT,
        VK_PIPELINE_STAGE_COMPUTE_SHADER_BIT,
        VK_PIPELINE_STAGE_COMPUTE_SHADER_BIT);

    // additive blend
    commandBuffer.pushConstantsCmd(blendPipeline.getLayoutHandle(),
        VK_SHADER_STAGE_COMPUTE_BIT, pushConstants_blend);
    commandBuffer.bindComputePipelineCmd(blendPipeline.getHandle());
    commandBuffer.bindComputeDescriptorSetsCmd(blendPipeline.getLayoutHandle(),
        VkUtil.createLongArray(blendDescriptorSets));
    commandBuffer.dispatchCmd(width / 8, height / 8, 1);

    // barrier
    commandBuffer.pipelineMemoryBarrierCmd(
        VK_ACCESS_SHADER_WRITE_BIT, VK_ACCESS_SHADER_READ_BIT,
        VK_PIPELINE_STAGE_COMPUTE_SHADER_BIT,
        VK_PIPELINE_STAGE_COMPUTE_SHADER_BIT);

    // bloom Scene
    commandBuffer.bindComputePipelineCmd(bloomScenePipeline.getHandle());
    commandBuffer.bindComputeDescriptorSetsCmd(bloomScenePipeline.getLayoutHandle(),
        VkUtil.createLongArray(bloomSceneDescriptorSets));
    commandBuffer.dispatchCmd(width / 8, height / 8, 1);
  }

  public void initializeImages(VkDevice device,
      VkPhysicalDeviceMemoryProperties memoryProperties, int width, int height) {

    VkImage bloomSceneImage = new Image2DDeviceLocal(device, memoryProperties,
        width, height, VK_FORMAT_B8G8R8A8_SRGB, VK_IMAGE_USAGE_STORAGE_BIT
        | VK_IMAGE_USAGE_SAMPLED_BIT);
    VkImageView bloomSceneImageView = new VkImageView(device,
        VK_FORMAT_B8G8R8A8_SRGB, bloomSceneImage.getHandle(), VK_IMAGE_ASPECT_COLOR_BIT);

    bloomSceneImageBundle = new VkImageBundle(bloomSceneImage, bloomSceneImageView);

    VkImage additiveBlendImage = new Image2DDeviceLocal(device, memoryProperties,
        width, height, VK_FORMAT_R16G16B16A16_SFLOAT, VK_IMAGE_USAGE_STORAGE_BIT
        | VK_IMAGE_USAGE_SAMPLED_BIT);
    VkImageView additiveBlendImageView = new VkImageView(device,
        VK_FORMAT_R16G16B16A16_SFLOAT, additiveBlendImage.getHandle(), VK_IMAGE_ASPECT_COLOR_BIT);

    additiveBlendImageBundle = new VkImageBundle(additiveBlendImage, additiveBlendImageView);

    // brightness images

    VkImage sceneBrightnessImage = new Image2DDeviceLocal(device, memoryProperties,
        width, height, VK_FORMAT_R16G16B16A16_SFLOAT, VK_IMAGE_USAGE_STORAGE_BIT
        | VK_IMAGE_USAGE_SAMPLED_BIT);
    VkImageView sceneBrightnessImageView = new VkImageView(device,
        VK_FORMAT_R16G16B16A16_SFLOAT, sceneBrightnessImage.getHandle(), VK_IMAGE_ASPECT_COLOR_BIT);

    VkSampler brightnessSampler = new VkSampler(device, VK_FILTER_LINEAR,
        false, 0, VK_SAMPLER_MIPMAP_MODE_LINEAR, 0, VK_SAMPLER_ADDRESS_MODE_CLAMP_TO_EDGE);

    sceneBrightnessImageBundle = new VkImageBundle(sceneBrightnessImage, sceneBrightnessImageView,
        brightnessSampler);

    VkImage sceneBrightnessImage_div4 = new Image2DDeviceLocal(device, memoryProperties,
        (int) (width / 4.0f), (int) (height / 4.0f), VK_FORMAT_R16G16B16A16_SFLOAT, VK_IMAGE_USAGE_STORAGE_BIT
        | VK_IMAGE_USAGE_SAMPLED_BIT);
    VkImageView sceneBrightnessImageView_div4 = new VkImageView(device,
        VK_FORMAT_R16G16B16A16_SFLOAT, sceneBrightnessImage_div4.getHandle(), VK_IMAGE_ASPECT_COLOR_BIT);

    VkSampler brightnessSampler_div4 = new VkSampler(device, VK_FILTER_LINEAR,
        false, 0, VK_SAMPLER_MIPMAP_MODE_LINEAR, 0, VK_SAMPLER_ADDRESS_MODE_CLAMP_TO_EDGE);

    sceneBrightnessImageBundle_div4 = new VkImageBundle(sceneBrightnessImage_div4, sceneBrightnessImageView_div4,
        brightnessSampler_div4);

    VkImage sceneBrightnessImage_div8 = new Image2DDeviceLocal(device, memoryProperties,
        (int) (width / 8.0f), (int) (height / 8.0f), VK_FORMAT_R16G16B16A16_SFLOAT, VK_IMAGE_USAGE_STORAGE_BIT
        | VK_IMAGE_USAGE_SAMPLED_BIT);
    VkImageView sceneBrightnessImageView_div8 = new VkImageView(device,
        VK_FORMAT_R16G16B16A16_SFLOAT, sceneBrightnessImage_div8.getHandle(), VK_IMAGE_ASPECT_COLOR_BIT);

    VkSampler brightnessSampler_div8 = new VkSampler(device, VK_FILTER_LINEAR,
        false, 0, VK_SAMPLER_MIPMAP_MODE_LINEAR, 0, VK_SAMPLER_ADDRESS_MODE_CLAMP_TO_EDGE);

    sceneBrightnessImageBundle_div8 = new VkImageBundle(sceneBrightnessImage_div8, sceneBrightnessImageView_div8,
        brightnessSampler_div8);

    VkImage sceneBrightnessImage_div16 = new Image2DDeviceLocal(device, memoryProperties,
        (int) (width / 12.0f), (int) (height / 12.0f), VK_FORMAT_R16G16B16A16_SFLOAT, VK_IMAGE_USAGE_STORAGE_BIT
        | VK_IMAGE_USAGE_SAMPLED_BIT);
    VkImageView sceneBrightnessImageView_div16 = new VkImageView(device,
        VK_FORMAT_R16G16B16A16_SFLOAT, sceneBrightnessImage_div16.getHandle(), VK_IMAGE_ASPECT_COLOR_BIT);

    VkSampler brightnessSampler_div16 = new VkSampler(device, VK_FILTER_LINEAR,
        false, 0, VK_SAMPLER_MIPMAP_MODE_LINEAR, 0, VK_SAMPLER_ADDRESS_MODE_CLAMP_TO_EDGE);

    sceneBrightnessImageBundle_div16 = new VkImageBundle(sceneBrightnessImage_div16, sceneBrightnessImageView_div16,
        brightnessSampler_div16);

    // horizontal Bloom Blur images

    VkImage horizontalBloomBlurImage_div2 = new Image2DDeviceLocal(device, memoryProperties,
        (int) (width / 2.0f), (int) (height / 2.0f), VK_FORMAT_R16G16B16A16_SFLOAT,
        VK_IMAGE_USAGE_STORAGE_BIT | VK_IMAGE_USAGE_SAMPLED_BIT);
    VkImageView horizontalBloomBlurImageView_div2 = new VkImageView(device,
        VK_FORMAT_R16G16B16A16_SFLOAT, horizontalBloomBlurImage_div2.getHandle(), VK_IMAGE_ASPECT_COLOR_BIT);

    horizontalBloomBlurImageBundle_div2 = new VkImageBundle(horizontalBloomBlurImage_div2,
        horizontalBloomBlurImageView_div2);

    VkImage horizontalBloomBlurImage_div4 = new Image2DDeviceLocal(device, memoryProperties,
        (int) (width / 4.0f), (int) (height / 4.0f), VK_FORMAT_R16G16B16A16_SFLOAT,
        VK_IMAGE_USAGE_STORAGE_BIT | VK_IMAGE_USAGE_SAMPLED_BIT);
    VkImageView horizontalBloomBlurImageView_div4 = new VkImageView(device,
        VK_FORMAT_R16G16B16A16_SFLOAT, horizontalBloomBlurImage_div4.getHandle(), VK_IMAGE_ASPECT_COLOR_BIT);

    horizontalBloomBlurImageBundle_div4 = new VkImageBundle(horizontalBloomBlurImage_div4,
        horizontalBloomBlurImageView_div4);

    VkImage horizontalBloomBlurImage_div8 = new Image2DDeviceLocal(device, memoryProperties,
        (int) (width / 8.0f), (int) (height / 8.0f), VK_FORMAT_R16G16B16A16_SFLOAT,
        VK_IMAGE_USAGE_STORAGE_BIT | VK_IMAGE_USAGE_SAMPLED_BIT);
    VkImageView horizontalBloomBlurImageView_div8 = new VkImageView(device,
        VK_FORMAT_R16G16B16A16_SFLOAT, horizontalBloomBlurImage_div8.getHandle(), VK_IMAGE_ASPECT_COLOR_BIT);

    horizontalBloomBlurImageBundle_div8 = new VkImageBundle(horizontalBloomBlurImage_div8,
        horizontalBloomBlurImageView_div8);

    VkImage horizontalBloomBlurImage_div16 = new Image2DDeviceLocal(device, memoryProperties,
        (int) (width / 12.0f), (int) (height / 12.0f), VK_FORMAT_R16G16B16A16_SFLOAT,
        VK_IMAGE_USAGE_STORAGE_BIT | VK_IMAGE_USAGE_SAMPLED_BIT);
    VkImageView horizontalBloomBlurImageView_div16 = new VkImageView(device,
        VK_FORMAT_R16G16B16A16_SFLOAT, horizontalBloomBlurImage_div16.getHandle(), VK_IMAGE_ASPECT_COLOR_BIT);

    horizontalBloomBlurImageBundle_div16 = new VkImageBundle(horizontalBloomBlurImage_div16,
        horizontalBloomBlurImageView_div16);

    // vertical Bloom Blur images

    VkImage verticalBloomBlurImage_div2 = new Image2DDeviceLocal(device, memoryProperties,
        (int) (width / 2.0f), (int) (height / 2.0f), VK_FORMAT_R16G16B16A16_SFLOAT,
        VK_IMAGE_USAGE_STORAGE_BIT | VK_IMAGE_USAGE_SAMPLED_BIT);
    VkImageView verticalBloomBlurImageView_div2 = new VkImageView(device,
        VK_FORMAT_R16G16B16A16_SFLOAT, verticalBloomBlurImage_div2.getHandle(), VK_IMAGE_ASPECT_COLOR_BIT);

    verticalBloomBlurImageBundle_div2 = new VkImageBundle(verticalBloomBlurImage_div2,
        verticalBloomBlurImageView_div2);

    VkImage verticalBloomBlurImage_div4 = new Image2DDeviceLocal(device, memoryProperties,
        (int) (width / 4.0f), (int) (height / 4.0f), VK_FORMAT_R16G16B16A16_SFLOAT,
        VK_IMAGE_USAGE_STORAGE_BIT | VK_IMAGE_USAGE_SAMPLED_BIT);
    VkImageView verticalBloomBlurImageView_div4 = new VkImageView(device,
        VK_FORMAT_R16G16B16A16_SFLOAT, verticalBloomBlurImage_div4.getHandle(), VK_IMAGE_ASPECT_COLOR_BIT);

    verticalBloomBlurImageBundle_div4 = new VkImageBundle(verticalBloomBlurImage_div4,
        verticalBloomBlurImageView_div4);

    VkImage verticalBloomBlurImage_div8 = new Image2DDeviceLocal(device, memoryProperties,
        (int) (width / 8.0f), (int) (height / 8.0f), VK_FORMAT_R16G16B16A16_SFLOAT,
        VK_IMAGE_USAGE_STORAGE_BIT | VK_IMAGE_USAGE_SAMPLED_BIT);
    VkImageView verticalBloomBlurImageView_div8 = new VkImageView(device,
        VK_FORMAT_R16G16B16A16_SFLOAT, verticalBloomBlurImage_div8.getHandle(), VK_IMAGE_ASPECT_COLOR_BIT);

    verticalBloomBlurImageBundle_div8 = new VkImageBundle(verticalBloomBlurImage_div8,
        verticalBloomBlurImageView_div8);

    VkImage verticalBloomBlurImage_div16 = new Image2DDeviceLocal(device, memoryProperties,
        (int) (width / 12.0f), (int) (height / 12.0f), VK_FORMAT_R16G16B16A16_SFLOAT,
        VK_IMAGE_USAGE_STORAGE_BIT | VK_IMAGE_USAGE_SAMPLED_BIT);
    VkImageView verticalBloomBlurImageView_div16 = new VkImageView(device,
        VK_FORMAT_R16G16B16A16_SFLOAT, verticalBloomBlurImage_div16.getHandle(), VK_IMAGE_ASPECT_COLOR_BIT);

    verticalBloomBlurImageBundle_div16 = new VkImageBundle(verticalBloomBlurImage_div16,
        verticalBloomBlurImageView_div16);
  }

  public void shutdown() {
    bloomSceneImageBundle.destroy();
    additiveBlendImageBundle.destroy();
    sceneBrightnessImageBundle.destroy();
    horizontalBloomBlurImageBundle_div2.destroy();
    horizontalBloomBlurImageBundle_div4.destroy();
    horizontalBloomBlurImageBundle_div8.destroy();
    horizontalBloomBlurImageBundle_div16.destroy();
    verticalBloomBlurImageBundle_div2.destroy();
    verticalBloomBlurImageBundle_div4.destroy();
    verticalBloomBlurImageBundle_div8.destroy();
    verticalBloomBlurImageBundle_div16.destroy();
    sceneBrightnessPipeline.destroy();
    sceneBrightnessDescriptorSet.destroy();
    sceneBrightnessDescriptorSetLayout.destroy();
    horizontalBlurPipeline.destroy();
    horizontalBlurDescriptorSet.destroy();
    horizontalBlurDescriptorSetLayout.destroy();
    verticalBlurPipeline.destroy();
    verticalBlurDescriptorSet.destroy();
    verticalBlurDescriptorSetLayout.destroy();
    blendPipeline.destroy();
    blendDescriptorSet.destroy();
    blendDescriptorSetLayout.destroy();
    bloomBlurSampler_div2.destroy();
    bloomBlurSampler_div4.destroy();
    bloomBlurSampler_div8.destroy();
    bloomBlurSampler_div16.destroy();
    bloomScenePipeline.destroy();
    bloomSceneDescriptorSet.destroy();
    bloomSceneDescriptorSetLayout.destroy();
  }

  public VkImageView getBloomSceneImageView() {
    return bloomSceneImageBundle.getImageView();
  }

}
