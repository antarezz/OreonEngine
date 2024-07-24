package org.oreon.core.vk.framebuffer;

import static org.lwjgl.vulkan.VK10.VK_IMAGE_ASPECT_DEPTH_BIT;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_USAGE_DEPTH_STENCIL_ATTACHMENT_BIT;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_USAGE_SAMPLED_BIT;

import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkPhysicalDeviceMemoryProperties;
import org.oreon.core.vk.image.VkImageView;
import org.oreon.core.vk.wrapper.image.Image2DDeviceLocal;
import org.oreon.core.vk.wrapper.image.VkImageBundle;

public class FrameBufferDepthAttachment extends VkImageBundle {

  public FrameBufferDepthAttachment(VkDevice device, VkPhysicalDeviceMemoryProperties memoryProperties,
      int width, int height, int format, int samples) {

    image = new Image2DDeviceLocal(device, memoryProperties, width, height, format,
        VK_IMAGE_USAGE_DEPTH_STENCIL_ATTACHMENT_BIT | VK_IMAGE_USAGE_SAMPLED_BIT,
        samples);
    imageView = new VkImageView(device, image.getFormat(), image.getHandle(),
        VK_IMAGE_ASPECT_DEPTH_BIT);
  }
}
