package org.oreon.core.vk.memory;

import static org.lwjgl.system.MemoryUtil.memAddress;
import static org.lwjgl.system.MemoryUtil.memAllocInt;
import static org.lwjgl.system.MemoryUtil.memAllocLong;
import static org.lwjgl.system.MemoryUtil.memAllocPointer;
import static org.lwjgl.system.MemoryUtil.memCopy;
import static org.lwjgl.system.MemoryUtil.memFree;
import static org.lwjgl.vulkan.VK10.VK_SHARING_MODE_EXCLUSIVE;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_BUFFER_CREATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_MEMORY_ALLOCATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_SUCCESS;
import static org.lwjgl.vulkan.VK10.vkAllocateMemory;
import static org.lwjgl.vulkan.VK10.vkBindBufferMemory;
import static org.lwjgl.vulkan.VK10.vkCreateBuffer;
import static org.lwjgl.vulkan.VK10.vkDestroyBuffer;
import static org.lwjgl.vulkan.VK10.vkFreeMemory;
import static org.lwjgl.vulkan.VK10.vkGetBufferMemoryRequirements;
import static org.lwjgl.vulkan.VK10.vkMapMemory;
import static org.lwjgl.vulkan.VK10.vkUnmapMemory;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;

import org.lwjgl.PointerBuffer;
import org.lwjgl.vulkan.VkBufferCreateInfo;
import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkMemoryAllocateInfo;
import org.lwjgl.vulkan.VkMemoryRequirements;
import org.lwjgl.vulkan.VkPhysicalDeviceMemoryProperties;
import org.oreon.core.vk.device.DeviceCapabilities;
import org.oreon.core.vk.util.VkUtil;

import lombok.Getter;

public class VkBuffer {

  @Getter
  private long handle;
  @Getter
  private long memory;

  private long allocationSize;

  private VkDevice device;

  public VkBuffer(VkDevice device, int size, int usage) {

    this.device = device;

    VkBufferCreateInfo bufInfo = VkBufferCreateInfo.calloc()
        .sType(VK_STRUCTURE_TYPE_BUFFER_CREATE_INFO)
        .pNext(0)
        .size(size)
        .usage(usage)
        .sharingMode(VK_SHARING_MODE_EXCLUSIVE)
        .flags(0);

    LongBuffer pBuffer = memAllocLong(1);
    int err = vkCreateBuffer(this.device, bufInfo, null, pBuffer);
    handle = pBuffer.get(0);

    memFree(pBuffer);
    bufInfo.free();

    if (err != VK_SUCCESS) {
      throw new AssertionError("Failed to create buffer: " + VkUtil.translateVulkanResult(err));
    }
  }

  public void allocate(VkPhysicalDeviceMemoryProperties memoryProperties,
      int memoryPropertyFlags) {

    VkMemoryRequirements memRequirements = VkMemoryRequirements.calloc();
    vkGetBufferMemoryRequirements(device, handle, memRequirements);
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
      throw new AssertionError("Failed to allocate buffer memory: " + VkUtil.translateVulkanResult(err));
    }
  }

  public void bindBufferMemory() {

    int err = vkBindBufferMemory(device, handle, memory, 0);
    if (err != VK_SUCCESS) {
      throw new AssertionError("Failed to bind memory to buffer: " + VkUtil.translateVulkanResult(err));
    }
  }

  public void mapMemory(ByteBuffer buffer) {

    PointerBuffer pData = memAllocPointer(1);
    int err = vkMapMemory(device, memory, 0, buffer.remaining(), 0, pData);

    long data = pData.get(0);
    memFree(pData);
    if (err != VK_SUCCESS) {
      throw new AssertionError("Failed to map buffer memory: " + VkUtil.translateVulkanResult(err));
    }

    memCopy(memAddress(buffer), data, buffer.remaining());
    memFree(buffer);
    vkUnmapMemory(device, memory);
  }

  public void destroy() {

    vkFreeMemory(device, memory, null);
    vkDestroyBuffer(device, handle, null);
  }

}
