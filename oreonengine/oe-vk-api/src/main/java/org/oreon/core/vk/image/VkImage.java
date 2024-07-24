package org.oreon.core.vk.image;

import static org.lwjgl.system.MemoryUtil.memAddress;
import static org.lwjgl.system.MemoryUtil.memAllocInt;
import static org.lwjgl.system.MemoryUtil.memAllocLong;
import static org.lwjgl.system.MemoryUtil.memAllocPointer;
import static org.lwjgl.system.MemoryUtil.memCopy;
import static org.lwjgl.system.MemoryUtil.memFree;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_LAYOUT_UNDEFINED;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_TILING_OPTIMAL;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_TYPE_2D;
import static org.lwjgl.vulkan.VK10.VK_SHARING_MODE_EXCLUSIVE;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_IMAGE_CREATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_MEMORY_ALLOCATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_SUCCESS;
import static org.lwjgl.vulkan.VK10.vkAllocateMemory;
import static org.lwjgl.vulkan.VK10.vkBindImageMemory;
import static org.lwjgl.vulkan.VK10.vkCreateImage;
import static org.lwjgl.vulkan.VK10.vkDestroyImage;
import static org.lwjgl.vulkan.VK10.vkFreeMemory;
import static org.lwjgl.vulkan.VK10.vkGetImageMemoryRequirements;
import static org.lwjgl.vulkan.VK10.vkMapMemory;
import static org.lwjgl.vulkan.VK10.vkUnmapMemory;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;

import org.lwjgl.PointerBuffer;
import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkExtent3D;
import org.lwjgl.vulkan.VkImageCreateInfo;
import org.lwjgl.vulkan.VkMemoryAllocateInfo;
import org.lwjgl.vulkan.VkMemoryRequirements;
import org.lwjgl.vulkan.VkPhysicalDeviceMemoryProperties;
import org.oreon.core.image.Image;
import org.oreon.core.vk.device.DeviceCapabilities;
import org.oreon.core.vk.util.VkUtil;

import lombok.Getter;

public class VkImage extends Image {

  @Getter
  private long handle;
  @Getter
  private long memory;
  @Getter
  private int format;
  private long allocationSize;
  private VkDevice device;

  public VkImage(VkDevice device, int width, int height, int depth,
      int format, int usage, int samples, int mipLevels) {

    this.device = device;
    this.format = format;

    VkExtent3D extent = VkExtent3D.calloc()
        .width(width)
        .height(height)
        .depth(depth);

    VkImageCreateInfo createInfo = VkImageCreateInfo.calloc()
        .sType(VK_STRUCTURE_TYPE_IMAGE_CREATE_INFO)
        .imageType(VK_IMAGE_TYPE_2D)
        .extent(extent)
        .mipLevels(mipLevels)
        .arrayLayers(1)
        .format(format)
        .tiling(VK_IMAGE_TILING_OPTIMAL)
        .initialLayout(VK_IMAGE_LAYOUT_UNDEFINED)
        .usage(usage)
        .sharingMode(VK_SHARING_MODE_EXCLUSIVE)
        .samples(VkUtil.getSampleCountBit(samples))
        .flags(0);

    LongBuffer pBuffer = memAllocLong(1);
    int err = vkCreateImage(device, createInfo, null, pBuffer);
    handle = pBuffer.get(0);

    memFree(pBuffer);
    createInfo.free();

    if (err != VK_SUCCESS) {
      throw new AssertionError("Failed to create image: " + VkUtil.translateVulkanResult(err));
    }
  }

  public void allocate(VkPhysicalDeviceMemoryProperties memoryProperties,
      int memoryPropertyFlags) {

    VkMemoryRequirements memRequirements = VkMemoryRequirements.calloc();
    vkGetImageMemoryRequirements(device, handle, memRequirements);
    IntBuffer memoryTypeIndex = memAllocInt(1);

    if (!DeviceCapabilities.getMemoryTypeIndex(memoryProperties,
        memRequirements.memoryTypeBits(),
        memoryPropertyFlags,
        memoryTypeIndex)) {
      throw new AssertionError("No memory Type found");
    }

    allocationSize = memRequirements.size();

    VkMemoryAllocateInfo memAlloc = VkMemoryAllocateInfo.calloc()
        .sType(VK_STRUCTURE_TYPE_MEMORY_ALLOCATE_INFO)
        .pNext(0)
        .allocationSize(allocationSize)
        .memoryTypeIndex(memoryTypeIndex.get(0));

    LongBuffer pMemory = memAllocLong(1);
    int err = vkAllocateMemory(device, memAlloc, null, pMemory);
    memory = pMemory.get(0);
    memFree(pMemory);
    memAlloc.free();
    if (err != VK_SUCCESS) {
      throw new AssertionError("Failed to allocate image memory: " + VkUtil.translateVulkanResult(err));
    }
  }

  public void bindImageMemory() {

    int err = vkBindImageMemory(device, handle, memory, 0);
    if (err != VK_SUCCESS) {
      throw new AssertionError("Failed to bind memory to image buffer: " + VkUtil.translateVulkanResult(err));
    }
  }

  public void mapMemory(ByteBuffer imageBuffer) {

    PointerBuffer pData = memAllocPointer(1);
    int err = vkMapMemory(device, memory, 0, imageBuffer.remaining(), 0, pData);

    long data = pData.get(0);
    memFree(pData);
    if (err != VK_SUCCESS) {
      throw new AssertionError("Failed to map image memory: " + VkUtil.translateVulkanResult(err));
    }

    memCopy(memAddress(imageBuffer), data, imageBuffer.remaining());
    memFree(imageBuffer);
    vkUnmapMemory(device, memory);
  }

  public void destroy() {

    vkFreeMemory(device, memory, null);
    vkDestroyImage(device, handle, null);
  }
}
