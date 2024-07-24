package org.oreon.vk.components.fft;

import static org.lwjgl.system.MemoryUtil.memAlloc;
import static org.lwjgl.system.MemoryUtil.memAllocInt;
import static org.lwjgl.vulkan.VK10.VK_ACCESS_SHADER_READ_BIT;
import static org.lwjgl.vulkan.VK10.VK_ACCESS_SHADER_WRITE_BIT;
import static org.lwjgl.vulkan.VK10.VK_COMMAND_BUFFER_LEVEL_PRIMARY;
import static org.lwjgl.vulkan.VK10.VK_COMMAND_BUFFER_USAGE_RENDER_PASS_CONTINUE_BIT;
import static org.lwjgl.vulkan.VK10.VK_DESCRIPTOR_TYPE_STORAGE_IMAGE;
import static org.lwjgl.vulkan.VK10.VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER;
import static org.lwjgl.vulkan.VK10.VK_FORMAT_R32G32B32A32_SFLOAT;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_ASPECT_COLOR_BIT;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_LAYOUT_GENERAL;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_USAGE_SAMPLED_BIT;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_USAGE_STORAGE_BIT;
import static org.lwjgl.vulkan.VK10.VK_PIPELINE_STAGE_ALL_COMMANDS_BIT;
import static org.lwjgl.vulkan.VK10.VK_PIPELINE_STAGE_COMPUTE_SHADER_BIT;
import static org.lwjgl.vulkan.VK10.VK_SHADER_STAGE_COMPUTE_BIT;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

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
import org.oreon.core.vk.pipeline.ShaderModule;
import org.oreon.core.vk.pipeline.VkPipeline;
import org.oreon.core.vk.synchronization.VkSemaphore;
import org.oreon.core.vk.util.VkUtil;
import org.oreon.core.vk.wrapper.image.Image2DDeviceLocal;

import lombok.Getter;

public class FFT {

  private VkQueue computeQueue;

  @Getter
  private VkImageView dxImageView;
  @Getter
  private VkImageView dyImageView;
  @Getter
  private VkImageView dzImageView;

  private VkImage dxImage;
  private VkImage dyImage;
  private VkImage dzImage;

  private TwiddleFactors twiddleFactors;
  private H0k h0k;
  private Hkt hkt;

  private DescriptorSetLayout descriptorLayout;
  private VkPipeline butterflyPipeline;
  private VkPipeline inversionPipeline;
  private ShaderModule butterflyShader;
  private ShaderModule inversionShader;

  // dy fft resources
  private DescriptorSet dyButterflyDescriptorSet;
  private DescriptorSet dyInversionDescriptorSet;
  private VkImage dyPingpongImage;
  private VkImageView dyPingpongImageView;

  // dx fft resources
  private DescriptorSet dxButterflyDescriptorSet;
  private DescriptorSet dxInversionDescriptorSet;
  private VkImage dxPingpongImage;
  private VkImageView dxPingpongImageView;

  // dz fft resources
  private DescriptorSet dzButterflyDescriptorSet;
  private DescriptorSet dzInversionDescriptorSet;
  private VkImage dzPingpongImage;
  private VkImageView dzPingpongImageView;

  private ByteBuffer[] horizontalPushConstants;
  private ByteBuffer[] verticalPushConstants;
  private ByteBuffer inversionPushConstants;
  private CommandBuffer fftCommandBuffer;
  private SubmitInfo fftSubmitInfo;

  @Getter
  private VkSemaphore fftSignalSemaphore;

  public FFT(VkDeviceBundle deviceBundle, int N, int L, float t_delta,
      float amplitude, Vec2f direction, float intensity, float capillarSupressFactor) {

    VkDevice device = deviceBundle.getLogicalDevice().getHandle();
    VkPhysicalDeviceMemoryProperties memoryProperties = deviceBundle.getPhysicalDevice().getMemoryProperties();
    DescriptorPool descriptorPool = deviceBundle.getLogicalDevice().getDescriptorPool(Thread.currentThread().getId());
    computeQueue = deviceBundle.getLogicalDevice().getComputeQueue();

    int stages = (int) (Math.log(N) / Math.log(2));

    dyPingpongImage = new Image2DDeviceLocal(device, memoryProperties, N, N,
        VK_FORMAT_R32G32B32A32_SFLOAT,
        VK_IMAGE_USAGE_SAMPLED_BIT | VK_IMAGE_USAGE_STORAGE_BIT);

    dyPingpongImageView = new VkImageView(device,
        VK_FORMAT_R32G32B32A32_SFLOAT, dyPingpongImage.getHandle(), VK_IMAGE_ASPECT_COLOR_BIT);

    dxPingpongImage = new Image2DDeviceLocal(device, memoryProperties, N, N,
        VK_FORMAT_R32G32B32A32_SFLOAT,
        VK_IMAGE_USAGE_SAMPLED_BIT | VK_IMAGE_USAGE_STORAGE_BIT);

    dxPingpongImageView = new VkImageView(device,
        VK_FORMAT_R32G32B32A32_SFLOAT, dxPingpongImage.getHandle(), VK_IMAGE_ASPECT_COLOR_BIT);

    dzPingpongImage = new Image2DDeviceLocal(device, memoryProperties, N, N,
        VK_FORMAT_R32G32B32A32_SFLOAT,
        VK_IMAGE_USAGE_SAMPLED_BIT | VK_IMAGE_USAGE_STORAGE_BIT);

    dzPingpongImageView = new VkImageView(device,
        VK_FORMAT_R32G32B32A32_SFLOAT, dzPingpongImage.getHandle(), VK_IMAGE_ASPECT_COLOR_BIT);

    dyImage = new Image2DDeviceLocal(device, memoryProperties, N, N,
        VK_FORMAT_R32G32B32A32_SFLOAT,
        VK_IMAGE_USAGE_SAMPLED_BIT | VK_IMAGE_USAGE_STORAGE_BIT);

    dyImageView = new VkImageView(device,
        VK_FORMAT_R32G32B32A32_SFLOAT, dyImage.getHandle(), VK_IMAGE_ASPECT_COLOR_BIT);

    dxImage = new Image2DDeviceLocal(device, memoryProperties, N, N,
        VK_FORMAT_R32G32B32A32_SFLOAT,
        VK_IMAGE_USAGE_SAMPLED_BIT | VK_IMAGE_USAGE_STORAGE_BIT);

    dxImageView = new VkImageView(device,
        VK_FORMAT_R32G32B32A32_SFLOAT, dxImage.getHandle(), VK_IMAGE_ASPECT_COLOR_BIT);

    dzImage = new Image2DDeviceLocal(device, memoryProperties, N, N,
        VK_FORMAT_R32G32B32A32_SFLOAT,
        VK_IMAGE_USAGE_SAMPLED_BIT | VK_IMAGE_USAGE_STORAGE_BIT);

    dzImageView = new VkImageView(device,
        VK_FORMAT_R32G32B32A32_SFLOAT, dzImage.getHandle(), VK_IMAGE_ASPECT_COLOR_BIT);

    twiddleFactors = new TwiddleFactors(deviceBundle, N);
    h0k = new H0k(deviceBundle, N, L, amplitude, direction, intensity, capillarSupressFactor);
    hkt = new Hkt(deviceBundle, N, L, t_delta, h0k.getH0k_imageView(), h0k.getH0minusk_imageView());

    horizontalPushConstants = new ByteBuffer[stages];
    verticalPushConstants = new ByteBuffer[stages];
    int pingpong = 0;

    for (int i = 0; i < stages; i++) {
      horizontalPushConstants[i] = memAlloc(Integer.BYTES * 4);
      horizontalPushConstants[i].putInt(i);
      horizontalPushConstants[i].putInt(pingpong);
      horizontalPushConstants[i].putInt(0);
      horizontalPushConstants[i].flip();

      pingpong++;
      pingpong %= 2;
    }

    for (int i = 0; i < stages; i++) {
      verticalPushConstants[i] = memAlloc(Integer.BYTES * 4);
      verticalPushConstants[i].putInt(i);
      verticalPushConstants[i].putInt(pingpong);
      verticalPushConstants[i].putInt(1);
      verticalPushConstants[i].flip();

      pingpong++;
      pingpong %= 2;
    }

    inversionPushConstants = memAlloc(Integer.BYTES * 2);
    inversionPushConstants.putInt(N);
    inversionPushConstants.putInt(pingpong);
    inversionPushConstants.flip();

    ByteBuffer pushConstants = memAlloc(Integer.BYTES * 1);
    IntBuffer intBuffer = pushConstants.asIntBuffer();
    intBuffer.put(N);

    descriptorLayout = new DescriptorSetLayout(device, 4);
    descriptorLayout.addLayoutBinding(0, VK_DESCRIPTOR_TYPE_STORAGE_IMAGE,
        VK_SHADER_STAGE_COMPUTE_BIT);
    descriptorLayout.addLayoutBinding(1, VK_DESCRIPTOR_TYPE_STORAGE_IMAGE,
        VK_SHADER_STAGE_COMPUTE_BIT);
    descriptorLayout.addLayoutBinding(2, VK_DESCRIPTOR_TYPE_STORAGE_IMAGE,
        VK_SHADER_STAGE_COMPUTE_BIT);
    descriptorLayout.addLayoutBinding(3, VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER,
        VK_SHADER_STAGE_COMPUTE_BIT);
    descriptorLayout.create();

    dyButterflyDescriptorSet = new ButterflyDescriptorSet(device, descriptorPool,
        descriptorLayout, twiddleFactors.getImageView(),
        hkt.getDyCoefficients_imageView(), dyPingpongImageView);
    dyInversionDescriptorSet = new InversionDescriptorSet(device, descriptorPool,
        descriptorLayout, dyImageView, hkt.getDyCoefficients_imageView(),
        dyPingpongImageView);

    dxButterflyDescriptorSet = new ButterflyDescriptorSet(device, descriptorPool,
        descriptorLayout, twiddleFactors.getImageView(),
        hkt.getDxCoefficients_imageView(),
        dxPingpongImageView);
    dxInversionDescriptorSet = new InversionDescriptorSet(device, descriptorPool,
        descriptorLayout, dxImageView, hkt.getDxCoefficients_imageView(),
        dxPingpongImageView);

    dzButterflyDescriptorSet = new ButterflyDescriptorSet(device, descriptorPool,
        descriptorLayout, twiddleFactors.getImageView(),
        hkt.getDzCoefficients_imageView(),
        dzPingpongImageView);
    dzInversionDescriptorSet = new InversionDescriptorSet(device, descriptorPool,
        descriptorLayout, dzImageView, hkt.getDzCoefficients_imageView(),
        dzPingpongImageView);

    butterflyShader = new ShaderModule(device, "shaders/fft/Butterfly.comp.spv", VK_SHADER_STAGE_COMPUTE_BIT);
    inversionShader = new ShaderModule(device, "shaders/fft/Inversion.comp.spv", VK_SHADER_STAGE_COMPUTE_BIT);

    butterflyPipeline = new VkPipeline(device);
    butterflyPipeline.setPushConstantsRange(VK_SHADER_STAGE_COMPUTE_BIT, Integer.BYTES * 4);
    butterflyPipeline.setLayout(descriptorLayout.getHandlePointer());
    butterflyPipeline.createComputePipeline(butterflyShader);

    inversionPipeline = new VkPipeline(device);
    inversionPipeline.setPushConstantsRange(VK_SHADER_STAGE_COMPUTE_BIT, Integer.BYTES * 2);
    inversionPipeline.setLayout(descriptorLayout.getHandlePointer());
    inversionPipeline.createComputePipeline(inversionShader);

    butterflyShader.destroy();
    inversionShader.destroy();

    record(deviceBundle, N, stages);

    fftSignalSemaphore = new VkSemaphore(device);

    IntBuffer pWaitDstStageMask = memAllocInt(1);
    pWaitDstStageMask.put(0, VK_PIPELINE_STAGE_ALL_COMMANDS_BIT);
    fftSubmitInfo = new SubmitInfo();
    fftSubmitInfo.setCommandBuffers(fftCommandBuffer.getHandlePointer());
    fftSubmitInfo.setWaitSemaphores(hkt.getSignalSemaphore().getHandlePointer());
    fftSubmitInfo.setWaitDstStageMask(pWaitDstStageMask);
    fftSubmitInfo.setSignalSemaphores(fftSignalSemaphore.getHandlePointer());
  }

  public void record(VkDeviceBundle deviceBundle, int N, int stages) {

    fftCommandBuffer = new CommandBuffer(deviceBundle.getLogicalDevice().getHandle(),
        deviceBundle.getLogicalDevice().getComputeCommandPool(Thread.currentThread().getId()).getHandle(),
        VK_COMMAND_BUFFER_LEVEL_PRIMARY);

    fftCommandBuffer.beginRecord(VK_COMMAND_BUFFER_USAGE_RENDER_PASS_CONTINUE_BIT);

    fftCommandBuffer.bindComputePipelineCmd(butterflyPipeline.getHandle());

    // horizontal
    for (int i = 0; i < stages; i++) {

      // dy
      fftCommandBuffer.pushConstantsCmd(butterflyPipeline.getLayoutHandle(),
          VK_SHADER_STAGE_COMPUTE_BIT, horizontalPushConstants[i]);
      fftCommandBuffer.bindComputeDescriptorSetsCmd(butterflyPipeline.getLayoutHandle(),
          VkUtil.createLongArray(dyButterflyDescriptorSet));
      fftCommandBuffer.dispatchCmd(N / 16, N / 16, 1);

      // dx
      fftCommandBuffer.pushConstantsCmd(butterflyPipeline.getLayoutHandle(),
          VK_SHADER_STAGE_COMPUTE_BIT, horizontalPushConstants[i]);
      fftCommandBuffer.bindComputeDescriptorSetsCmd(butterflyPipeline.getLayoutHandle(),
          VkUtil.createLongArray(dxButterflyDescriptorSet));
      fftCommandBuffer.dispatchCmd(N / 16, N / 16, 1);

      // dz
      fftCommandBuffer.pushConstantsCmd(butterflyPipeline.getLayoutHandle(),
          VK_SHADER_STAGE_COMPUTE_BIT, horizontalPushConstants[i]);
      fftCommandBuffer.bindComputeDescriptorSetsCmd(butterflyPipeline.getLayoutHandle(),
          VkUtil.createLongArray(dzButterflyDescriptorSet));
      fftCommandBuffer.dispatchCmd(N / 16, N / 16, 1);

      fftCommandBuffer.pipelineMemoryBarrierCmd(
          VK_ACCESS_SHADER_WRITE_BIT, VK_ACCESS_SHADER_READ_BIT,
          VK_PIPELINE_STAGE_COMPUTE_SHADER_BIT,
          VK_PIPELINE_STAGE_COMPUTE_SHADER_BIT);
    }

    // vertical
    for (int i = 0; i < stages; i++) {

      // dy
      fftCommandBuffer.pushConstantsCmd(butterflyPipeline.getLayoutHandle(),
          VK_SHADER_STAGE_COMPUTE_BIT, verticalPushConstants[i]);
      fftCommandBuffer.bindComputeDescriptorSetsCmd(butterflyPipeline.getLayoutHandle(),
          VkUtil.createLongArray(dyButterflyDescriptorSet));
      fftCommandBuffer.dispatchCmd(N / 16, N / 16, 1);

      // dx
      fftCommandBuffer.pushConstantsCmd(butterflyPipeline.getLayoutHandle(),
          VK_SHADER_STAGE_COMPUTE_BIT, verticalPushConstants[i]);
      fftCommandBuffer.bindComputeDescriptorSetsCmd(butterflyPipeline.getLayoutHandle(),
          VkUtil.createLongArray(dxButterflyDescriptorSet));
      fftCommandBuffer.dispatchCmd(N / 16, N / 16, 1);

      // dz
      fftCommandBuffer.pushConstantsCmd(butterflyPipeline.getLayoutHandle(),
          VK_SHADER_STAGE_COMPUTE_BIT, verticalPushConstants[i]);
      fftCommandBuffer.bindComputeDescriptorSetsCmd(butterflyPipeline.getLayoutHandle(),
          VkUtil.createLongArray(dzButterflyDescriptorSet));
      fftCommandBuffer.dispatchCmd(N / 16, N / 16, 1);

      fftCommandBuffer.pipelineMemoryBarrierCmd(
          VK_ACCESS_SHADER_WRITE_BIT, VK_ACCESS_SHADER_READ_BIT,
          VK_PIPELINE_STAGE_COMPUTE_SHADER_BIT,
          VK_PIPELINE_STAGE_COMPUTE_SHADER_BIT);
    }

    // inversion
    fftCommandBuffer.bindComputePipelineCmd(inversionPipeline.getHandle());
    fftCommandBuffer.pushConstantsCmd(inversionPipeline.getLayoutHandle(),
        VK_SHADER_STAGE_COMPUTE_BIT, inversionPushConstants);
    // dy
    fftCommandBuffer.bindComputeDescriptorSetsCmd(inversionPipeline.getLayoutHandle(),
        VkUtil.createLongArray(dyInversionDescriptorSet));
    fftCommandBuffer.dispatchCmd(N / 16, N / 16, 1);

    // dx
    fftCommandBuffer.bindComputeDescriptorSetsCmd(inversionPipeline.getLayoutHandle(),
        VkUtil.createLongArray(dxInversionDescriptorSet));
    fftCommandBuffer.dispatchCmd(N / 16, N / 16, 1);

    // dz
    fftCommandBuffer.bindComputeDescriptorSetsCmd(inversionPipeline.getLayoutHandle(),
        VkUtil.createLongArray(dzInversionDescriptorSet));
    fftCommandBuffer.dispatchCmd(N / 16, N / 16, 1);

    fftCommandBuffer.finishRecord();
  }

  public void render() {

    hkt.render();
    fftSubmitInfo.submit(computeQueue);
  }

  private class ButterflyDescriptorSet extends DescriptorSet {

    public ButterflyDescriptorSet(VkDevice device, DescriptorPool descriptorPool,
        DescriptorSetLayout layout, VkImageView twiddleFactors,
        VkImageView coefficients, VkImageView pingpongImage) {

      super(device, descriptorPool.getHandle(), layout.getHandlePointer());

      updateDescriptorImageBuffer(twiddleFactors.getHandle(),
          VK_IMAGE_LAYOUT_GENERAL, -1,
          0, VK_DESCRIPTOR_TYPE_STORAGE_IMAGE);
      updateDescriptorImageBuffer(coefficients.getHandle(),
          VK_IMAGE_LAYOUT_GENERAL, -1,
          1, VK_DESCRIPTOR_TYPE_STORAGE_IMAGE);
      updateDescriptorImageBuffer(pingpongImage.getHandle(),
          VK_IMAGE_LAYOUT_GENERAL, -1,
          2, VK_DESCRIPTOR_TYPE_STORAGE_IMAGE);
    }
  }

  private class InversionDescriptorSet extends DescriptorSet {

    public InversionDescriptorSet(VkDevice device, DescriptorPool descriptorPool,
        DescriptorSetLayout layout, VkImageView spatialDomain,
        VkImageView coefficients, VkImageView pingpongImage) {

      super(device, descriptorPool.getHandle(), layout.getHandlePointer());
      updateDescriptorImageBuffer(spatialDomain.getHandle(),
          VK_IMAGE_LAYOUT_GENERAL, -1,
          0, VK_DESCRIPTOR_TYPE_STORAGE_IMAGE);
      updateDescriptorImageBuffer(coefficients.getHandle(),
          VK_IMAGE_LAYOUT_GENERAL, -1,
          1, VK_DESCRIPTOR_TYPE_STORAGE_IMAGE);
      updateDescriptorImageBuffer(pingpongImage.getHandle(),
          VK_IMAGE_LAYOUT_GENERAL, -1,
          2, VK_DESCRIPTOR_TYPE_STORAGE_IMAGE);
    }
  }

  public void destroy() {

    twiddleFactors.destroy();
    h0k.destroy();
    hkt.destroy();
    dxImageView.destroy();
    dyImageView.destroy();
    dzImageView.destroy();
    dxImage.destroy();
    dyImage.destroy();
    dzImage.destroy();
    descriptorLayout.destroy();
    butterflyPipeline.destroy();
    inversionPipeline.destroy();

    dyButterflyDescriptorSet.destroy();
    dyInversionDescriptorSet.destroy();
    dyPingpongImageView.destroy();
    dyPingpongImage.destroy();

    dxButterflyDescriptorSet.destroy();
    dxInversionDescriptorSet.destroy();
    dxPingpongImageView.destroy();
    dxPingpongImage.destroy();

    dzButterflyDescriptorSet.destroy();
    dzInversionDescriptorSet.destroy();
    dzPingpongImageView.destroy();
    dzPingpongImage.destroy();

    fftCommandBuffer.destroy();
    fftSignalSemaphore.destroy();
  }
}
