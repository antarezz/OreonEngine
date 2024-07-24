package org.oreon.core.vk.image;

import static org.lwjgl.system.MemoryUtil.memAllocLong;
import static org.lwjgl.system.MemoryUtil.memFree;
import static org.lwjgl.vulkan.VK10.VK_COMPONENT_SWIZZLE_A;
import static org.lwjgl.vulkan.VK10.VK_COMPONENT_SWIZZLE_B;
import static org.lwjgl.vulkan.VK10.VK_COMPONENT_SWIZZLE_G;
import static org.lwjgl.vulkan.VK10.VK_COMPONENT_SWIZZLE_R;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_VIEW_TYPE_2D;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_IMAGE_VIEW_CREATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_SUCCESS;
import static org.lwjgl.vulkan.VK10.vkCreateImageView;
import static org.lwjgl.vulkan.VK10.vkDestroyImageView;

import java.nio.LongBuffer;

import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkImageViewCreateInfo;
import org.oreon.core.vk.util.VkUtil;

import lombok.Getter;

public class VkImageView {

  @Getter
  private long handle;

  private VkDevice device;

  public VkImageView(VkDevice device, int imageFormat, long image,
      int aspectMask) {

    this(device, imageFormat, image, aspectMask, 1);
  }

  public VkImageView(VkDevice device, int imageFormat, long image,
      int aspectMask, int mipLevels) {

    this.device = device;

    VkImageViewCreateInfo imageViewCreateInfo = VkImageViewCreateInfo.calloc()
        .sType(VK_STRUCTURE_TYPE_IMAGE_VIEW_CREATE_INFO)
        .pNext(0)
        .viewType(VK_IMAGE_VIEW_TYPE_2D)
        .format(imageFormat)
        .image(image);
    imageViewCreateInfo
        .components()
        .r(VK_COMPONENT_SWIZZLE_R)
        .g(VK_COMPONENT_SWIZZLE_G)
        .b(VK_COMPONENT_SWIZZLE_B)
        .a(VK_COMPONENT_SWIZZLE_A);
    imageViewCreateInfo
        .subresourceRange()
        .aspectMask(aspectMask)
        .baseMipLevel(0)
        .levelCount(mipLevels)
        .baseArrayLayer(0)
        .layerCount(1);

    LongBuffer pImageView = memAllocLong(1);
    int err = vkCreateImageView(device, imageViewCreateInfo, null, pImageView);
    if (err != VK_SUCCESS) {
      throw new AssertionError("Failed to create image view: " + VkUtil.translateVulkanResult(err));
    }

    handle = pImageView.get(0);

    memFree(pImageView);
    imageViewCreateInfo.free();
  }

  public void destroy() {

    vkDestroyImageView(device, handle, null);
  }

}
