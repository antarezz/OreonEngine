package org.oreon.core.vk.wrapper.image;

import static org.lwjgl.vulkan.VK10.VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT;

import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkPhysicalDeviceMemoryProperties;
import org.oreon.core.image.ImageMetaData;
import org.oreon.core.vk.image.VkImage;

public class Image2DDeviceLocal extends VkImage {

  public Image2DDeviceLocal(VkDevice device, VkPhysicalDeviceMemoryProperties memoryProperties,
      int width, int height, int format, int usage, int samples, int mipLevels) {

    super(device, width, height, 1, format, usage, samples, mipLevels);
    allocate(memoryProperties, VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT);
    bindImageMemory();
  }

  public Image2DDeviceLocal(VkDevice device, VkPhysicalDeviceMemoryProperties memoryProperties,
      int width, int height, int format, int usage, int samples, int mipLevels,
      ImageMetaData metaData) {

    super(device, width, height, 1, format, usage, samples, mipLevels);
    this.metaData = metaData;
    allocate(memoryProperties, VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT);
    bindImageMemory();
  }

  public Image2DDeviceLocal(VkDevice device, VkPhysicalDeviceMemoryProperties memoryProperties,
      int width, int height, int format, int usage) {

    super(device, width, height, 1, format, usage, 1, 1);
    allocate(memoryProperties, VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT);
    bindImageMemory();
  }

  public Image2DDeviceLocal(VkDevice device, VkPhysicalDeviceMemoryProperties memoryProperties,
      int width, int height, int format, int usage, int samples) {

    super(device, width, height, 1, format, usage, samples, 1);
    allocate(memoryProperties, VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT);
    bindImageMemory();
  }

  public Image2DDeviceLocal(VkDevice device, VkPhysicalDeviceMemoryProperties memoryProperties,
      int width, int height, int format, int usage, ImageMetaData metaData) {

    super(device, width, height, 1, format, usage, 1, 1);
    this.metaData = metaData;
    allocate(memoryProperties, VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT);
    bindImageMemory();
  }

}
