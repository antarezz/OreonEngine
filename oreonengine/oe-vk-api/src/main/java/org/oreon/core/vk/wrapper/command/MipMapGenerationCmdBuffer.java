package org.oreon.core.vk.wrapper.command;

import static org.lwjgl.vulkan.VK10.VK_ACCESS_TRANSFER_READ_BIT;
import static org.lwjgl.vulkan.VK10.VK_ACCESS_TRANSFER_WRITE_BIT;
import static org.lwjgl.vulkan.VK10.VK_COMMAND_BUFFER_LEVEL_PRIMARY;
import static org.lwjgl.vulkan.VK10.VK_COMMAND_BUFFER_USAGE_RENDER_PASS_CONTINUE_BIT;
import static org.lwjgl.vulkan.VK10.VK_FILTER_LINEAR;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_ASPECT_COLOR_BIT;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_LAYOUT_TRANSFER_SRC_OPTIMAL;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_LAYOUT_UNDEFINED;
import static org.lwjgl.vulkan.VK10.VK_PIPELINE_STAGE_TRANSFER_BIT;
import static org.lwjgl.vulkan.VK10.VK_QUEUE_FAMILY_IGNORED;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_IMAGE_MEMORY_BARRIER;
import static org.lwjgl.vulkan.VK10.vkCmdBlitImage;
import static org.lwjgl.vulkan.VK10.vkCmdPipelineBarrier;

import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkImageBlit;
import org.lwjgl.vulkan.VkImageMemoryBarrier;
import org.lwjgl.vulkan.VkOffset3D;
import org.oreon.core.vk.command.CommandBuffer;

public class MipMapGenerationCmdBuffer extends CommandBuffer {

  public MipMapGenerationCmdBuffer(VkDevice device, long commandPool,
      long image, int width, int height, int mipLevels,
      int initialLayout, int initialSrcAccessMask, int initialSrcStageMask,
      int finalLayout, int finalDstAccessMask, int finalDstStageMask) {

    super(device, commandPool, VK_COMMAND_BUFFER_LEVEL_PRIMARY);

    beginRecord(VK_COMMAND_BUFFER_USAGE_RENDER_PASS_CONTINUE_BIT);

    VkImageMemoryBarrier.Buffer barrier = VkImageMemoryBarrier.calloc(1)
        .sType(VK_STRUCTURE_TYPE_IMAGE_MEMORY_BARRIER)
        .srcQueueFamilyIndex(VK_QUEUE_FAMILY_IGNORED)
        .dstQueueFamilyIndex(VK_QUEUE_FAMILY_IGNORED)
        .image(image)
        .oldLayout(initialLayout)
        .newLayout(VK_IMAGE_LAYOUT_TRANSFER_SRC_OPTIMAL)
        .srcAccessMask(initialSrcAccessMask)
        .dstAccessMask(VK_ACCESS_TRANSFER_READ_BIT);
    barrier.subresourceRange()
        .baseMipLevel(0)
        .aspectMask(VK_IMAGE_ASPECT_COLOR_BIT)
        .baseArrayLayer(0)
        .layerCount(1)
        .levelCount(1);

    pipelineImageMemoryBarrierCmd(image,
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

      vkCmdPipelineBarrier(getHandle(),
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

      vkCmdBlitImage(getHandle(),
          image, VK_IMAGE_LAYOUT_TRANSFER_SRC_OPTIMAL,
          image, VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL,
          blit, VK_FILTER_LINEAR);

      barrier.oldLayout(VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL)
          .newLayout(VK_IMAGE_LAYOUT_TRANSFER_SRC_OPTIMAL)
          .srcAccessMask(VK_ACCESS_TRANSFER_WRITE_BIT)
          .dstAccessMask(VK_ACCESS_TRANSFER_READ_BIT);

      vkCmdPipelineBarrier(getHandle(),
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
        .dstAccessMask(finalDstAccessMask)
        .srcQueueFamilyIndex(VK_QUEUE_FAMILY_IGNORED)
        .dstQueueFamilyIndex(VK_QUEUE_FAMILY_IGNORED);

    vkCmdPipelineBarrier(getHandle(),
        VK_PIPELINE_STAGE_TRANSFER_BIT, finalDstStageMask,
        0, null, null, barrier);

    finishRecord();
  }

}
