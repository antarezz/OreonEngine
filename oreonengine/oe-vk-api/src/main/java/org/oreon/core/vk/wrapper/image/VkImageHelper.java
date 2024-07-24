package org.oreon.core.vk.wrapper.image;

import static org.lwjgl.vulkan.VK10.VK_ACCESS_TRANSFER_READ_BIT;
import static org.lwjgl.vulkan.VK10.VK_ACCESS_TRANSFER_WRITE_BIT;
import static org.lwjgl.vulkan.VK10.VK_COMMAND_BUFFER_LEVEL_PRIMARY;
import static org.lwjgl.vulkan.VK10.VK_COMMAND_BUFFER_USAGE_ONE_TIME_SUBMIT_BIT;
import static org.lwjgl.vulkan.VK10.VK_FILTER_LINEAR;
import static org.lwjgl.vulkan.VK10.VK_FORMAT_R8G8B8A8_UNORM;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_ASPECT_COLOR_BIT;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_LAYOUT_TRANSFER_SRC_OPTIMAL;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_LAYOUT_UNDEFINED;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_USAGE_TRANSFER_DST_BIT;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_USAGE_TRANSFER_SRC_BIT;
import static org.lwjgl.vulkan.VK10.VK_PIPELINE_STAGE_TOP_OF_PIPE_BIT;
import static org.lwjgl.vulkan.VK10.VK_PIPELINE_STAGE_TRANSFER_BIT;
import static org.lwjgl.vulkan.VK10.VK_QUEUE_FAMILY_IGNORED;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_IMAGE_MEMORY_BARRIER;
import static org.lwjgl.vulkan.VK10.vkCmdBlitImage;
import static org.lwjgl.vulkan.VK10.vkCmdPipelineBarrier;

import java.nio.ByteBuffer;

import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkImageBlit;
import org.lwjgl.vulkan.VkImageMemoryBarrier;
import org.lwjgl.vulkan.VkOffset3D;
import org.lwjgl.vulkan.VkPhysicalDeviceMemoryProperties;
import org.lwjgl.vulkan.VkQueue;
import org.oreon.core.image.ImageMetaData;
import org.oreon.core.util.Util;
import org.oreon.core.vk.command.CommandBuffer;
import org.oreon.core.vk.command.SubmitInfo;
import org.oreon.core.vk.image.VkImage;
import org.oreon.core.vk.image.VkImageLoader;
import org.oreon.core.vk.synchronization.Fence;
import org.oreon.core.vk.wrapper.buffer.StagingBuffer;
import org.oreon.core.vk.wrapper.command.ImageCopyCmdBuffer;
import org.oreon.core.vk.wrapper.command.ImageLayoutTransitionCmdBuffer;

public class VkImageHelper {

  public static VkImage loadImageFromFile(VkDevice device,
      VkPhysicalDeviceMemoryProperties memoryProperties,
      long commandPool, VkQueue queue, String file,
      int usage, int layout, int dstAccesMask, int dstStageMask,
      int dstQueueFamilyIndex) {

    return loadImage(device, memoryProperties, commandPool, queue,
        file, usage, layout, dstAccesMask, dstStageMask,
        dstQueueFamilyIndex, false);
  }

  public static VkImage loadImageFromFileMipmap(VkDevice device,
      VkPhysicalDeviceMemoryProperties memoryProperties,
      long commandPool, VkQueue queue, String file,
      int usage, int layout, int dstAccessMask, int dstStageMask,
      int dstQueueFamilyIndex) {

    return loadImage(device, memoryProperties, commandPool, queue,
        file, usage, layout, dstAccessMask, dstStageMask,
        dstQueueFamilyIndex, true);
  }

  private static VkImage loadImage(VkDevice device,
      VkPhysicalDeviceMemoryProperties memoryProperties, long commandPool, VkQueue queue,
      String file, int usage, int finalLayout, int dstAccessMask, int dstStageMask,
      int dstQueueFamilyIndex, boolean mipmap) {

    ImageMetaData metaData = VkImageLoader.getImageMetaData(file);
    ByteBuffer imageBuffer = VkImageLoader.decodeImage(file);

    StagingBuffer stagingBuffer = new StagingBuffer(device, memoryProperties, imageBuffer);

    int mipLevels = mipmap ? Util.getMipLevelCount(metaData) : 1;

    VkImage image = new Image2DDeviceLocal(device,
        memoryProperties, metaData.getWidth(), metaData.getHeight(),
        VK_FORMAT_R8G8B8A8_UNORM, usage | VK_IMAGE_USAGE_TRANSFER_DST_BIT |
        // if mipmap == true, usage flag TRANSFER_SRC_BIT is necessarry for mipmap generation
        (mipmap ? VK_IMAGE_USAGE_TRANSFER_SRC_BIT : 0),
        1, mipLevels, metaData);

    // transition layout barrier
    ImageLayoutTransitionCmdBuffer imageMemoryBarrierLayout0 = new ImageLayoutTransitionCmdBuffer(device, commandPool);
    imageMemoryBarrierLayout0.record(image.getHandle(),
        VK_COMMAND_BUFFER_USAGE_ONE_TIME_SUBMIT_BIT,
        VK_IMAGE_LAYOUT_UNDEFINED, VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL,
        0, VK_ACCESS_TRANSFER_WRITE_BIT, VK_PIPELINE_STAGE_TOP_OF_PIPE_BIT,
        VK_PIPELINE_STAGE_TRANSFER_BIT, mipLevels);
    imageMemoryBarrierLayout0.submit(queue);

    // copy buffer to image
    ImageCopyCmdBuffer imageCopyCmd = new ImageCopyCmdBuffer(device, commandPool);
    imageCopyCmd.record(stagingBuffer.getHandle(), image.getHandle(), metaData);
    imageCopyCmd.submit(queue);

    // transition layout barrier
    ImageLayoutTransitionCmdBuffer imageMemoryBarrierLayout1 = new ImageLayoutTransitionCmdBuffer(device, commandPool);
    imageMemoryBarrierLayout1.record(image.getHandle(),
        VK_COMMAND_BUFFER_USAGE_ONE_TIME_SUBMIT_BIT,
        VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL, finalLayout,
        VK_ACCESS_TRANSFER_WRITE_BIT, dstAccessMask,
        VK_PIPELINE_STAGE_TRANSFER_BIT, dstStageMask, mipLevels);
    Fence fence = new Fence(device);
    imageMemoryBarrierLayout1.submit(queue, fence);

    if (mipmap) {
      generateMipmap(device, commandPool, queue,
          image.getHandle(), metaData.getWidth(), metaData.getHeight(), mipLevels,
          finalLayout, finalLayout,
          dstAccessMask, dstAccessMask,
          dstStageMask, dstStageMask,
          dstQueueFamilyIndex);
    }

    imageMemoryBarrierLayout0.destroy();
    imageMemoryBarrierLayout1.destroy();
    imageCopyCmd.destroy();
    stagingBuffer.destroy();
    fence.destroy();

    return image;
  }

  public static void generateMipmap(VkDevice device, long commandPool, VkQueue queue,
      long image, int width, int height, int mipLevels,
      int initialLayout, int finalLayout,
      int initialSrcAccesMask, int finalDstAccesMask,
      int initialSrcStageMask, int finalDstStageMask,
      int dstQueueFamilyIndex) {

    CommandBuffer commandBuffer = new CommandBuffer(device,
        commandPool, VK_COMMAND_BUFFER_LEVEL_PRIMARY);

    commandBuffer.beginRecord(VK_COMMAND_BUFFER_USAGE_ONE_TIME_SUBMIT_BIT);

    VkImageMemoryBarrier.Buffer barrier = VkImageMemoryBarrier.calloc(1)
        .sType(VK_STRUCTURE_TYPE_IMAGE_MEMORY_BARRIER)
        .srcQueueFamilyIndex(VK_QUEUE_FAMILY_IGNORED)
        .dstQueueFamilyIndex(VK_QUEUE_FAMILY_IGNORED)
        .image(image)
        .oldLayout(initialLayout)
        .newLayout(VK_IMAGE_LAYOUT_TRANSFER_SRC_OPTIMAL)
        .srcAccessMask(initialSrcAccesMask)
        .dstAccessMask(VK_ACCESS_TRANSFER_READ_BIT);
    barrier.subresourceRange()
        .baseMipLevel(0)
        .aspectMask(VK_IMAGE_ASPECT_COLOR_BIT)
        .baseArrayLayer(0)
        .layerCount(1)
        .levelCount(1);

    commandBuffer.pipelineImageMemoryBarrierCmd(image,
        initialSrcStageMask,
        VK_PIPELINE_STAGE_TRANSFER_BIT,
        barrier);

    int mipWidth = width;
    int mipHeight = height;

    for (int i = 1; i < mipLevels; i++) {

      barrier.oldLayout(VK_IMAGE_LAYOUT_UNDEFINED)
          .newLayout(VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL)
          .srcAccessMask(0)
          .dstAccessMask(VK_ACCESS_TRANSFER_WRITE_BIT);
      barrier.subresourceRange()
          .baseMipLevel(i);

      vkCmdPipelineBarrier(commandBuffer.getHandle(),
          VK_PIPELINE_STAGE_TRANSFER_BIT,
          VK_PIPELINE_STAGE_TRANSFER_BIT,
          0, null, null, barrier);

      VkOffset3D src0_offset3D = VkOffset3D.calloc()
          .x(0).y(0).z(0);

      VkOffset3D src1_offset3D = VkOffset3D.calloc()
          .x(mipWidth).y(mipHeight).z(1);

      VkOffset3D dst0_offset3D = VkOffset3D.calloc()
          .x(0).y(0).z(0);

      VkOffset3D dst1_offset3D = VkOffset3D.calloc()
          .x(mipWidth / 2).y(mipHeight / 2).z(1);

      VkImageBlit.Buffer blit = VkImageBlit.calloc(1)
          .srcOffsets(0, src0_offset3D)
          .srcOffsets(1, src1_offset3D)
          .dstOffsets(0, dst0_offset3D)
          .dstOffsets(1, dst1_offset3D);
      blit.srcSubresource()
          .aspectMask(VK_IMAGE_ASPECT_COLOR_BIT)
          .mipLevel(i - 1)
          .baseArrayLayer(0)
          .layerCount(1);
      blit.dstSubresource()
          .aspectMask(VK_IMAGE_ASPECT_COLOR_BIT)
          .mipLevel(i)
          .baseArrayLayer(0)
          .layerCount(1);

      vkCmdBlitImage(commandBuffer.getHandle(),
          image, VK_IMAGE_LAYOUT_TRANSFER_SRC_OPTIMAL,
          image, VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL,
          blit, VK_FILTER_LINEAR);

      barrier.oldLayout(VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL)
          .newLayout(VK_IMAGE_LAYOUT_TRANSFER_SRC_OPTIMAL)
          .srcAccessMask(VK_ACCESS_TRANSFER_WRITE_BIT)
          .dstAccessMask(VK_ACCESS_TRANSFER_READ_BIT);

      vkCmdPipelineBarrier(commandBuffer.getHandle(),
          VK_PIPELINE_STAGE_TRANSFER_BIT,
          VK_PIPELINE_STAGE_TRANSFER_BIT,
          0, null, null, barrier);

      if (mipWidth > 1) {
        mipWidth /= 2;
      }
      if (mipHeight > 1) {
        mipHeight /= 2;
      }
    }

    barrier.subresourceRange()
        .baseMipLevel(0)
        .levelCount(mipLevels);
    barrier.oldLayout(VK_IMAGE_LAYOUT_TRANSFER_SRC_OPTIMAL)
        .newLayout(finalLayout)
        .srcAccessMask(VK_ACCESS_TRANSFER_WRITE_BIT)
        .dstAccessMask(finalDstAccesMask)
        .srcQueueFamilyIndex(VK_QUEUE_FAMILY_IGNORED)
        .dstQueueFamilyIndex(VK_QUEUE_FAMILY_IGNORED);

    vkCmdPipelineBarrier(commandBuffer.getHandle(),
        VK_PIPELINE_STAGE_TRANSFER_BIT, finalDstStageMask,
        0, null, null, barrier);

    commandBuffer.finishRecord();

    SubmitInfo submitInfo = new SubmitInfo();
    submitInfo.setCommandBuffers(commandBuffer.getHandlePointer());
    submitInfo.submit(queue);
  }

}
